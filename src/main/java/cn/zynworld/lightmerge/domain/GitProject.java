package cn.zynworld.lightmerge.domain;

import cn.zynworld.lightmerge.common.Constants;
import cn.zynworld.lightmerge.common.Result;
import cn.zynworld.lightmerge.config.LightMergeConfig;
import cn.zynworld.lightmerge.config.SafeConfig;
import cn.zynworld.lightmerge.helper.GitHelper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.FileUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ToString
public class GitProject implements Serializable {
    private static final long serialVersionUID = 420677041482106689L;

    /**
     * 过滤分支前缀
     */
    private List<String> filterBranchNames = new ArrayList<>();

    private String projectName;
    private String remoteAddress;
    private SafeConfig safeConfig;
    // 项目锁
    private Lock projectLock = new ReentrantLock();
    private Map<String, Swimlane> swimlaneMap = new HashMap<>();

    public GitProject(SafeConfig safeConfig) {
        this.safeConfig = safeConfig;
    }

    public GitProject() {}

    public void resetHard() {
        try (Git localGit = localGit()) {
            localGit.add().addFilepattern("*").call();
            localGit.reset().setMode(ResetCommand.ResetType.HARD).call();

        } catch (GitAPIException e) {
            log.error("resetHard fail. GitProject:{} ", this, e);
        }
    }

    public void checkout(String branchName) {
        try (Git localGit = localGit()) {
            localGit.checkout()
                    .setName(Constants.BRANCH_PRE + branchName)
                    .call();
        } catch (GitAPIException e) {
            log.error("resetHard fail. GitProject:{} ", this, e);
        }
    }

    /**
     * 初始化项目
     */
    public boolean init() {
        Git git = null;

        try {
            final File PROJECT_PATH = new File(Constants.PROJECT_WORKSPACE + projectName);

            // 删除原有目录
            FileUtils.delete(PROJECT_PATH,FileUtils.SKIP_MISSING | FileUtils.RECURSIVE);
            FileUtils.mkdir(new File(Constants.PROJECT_WORKSPACE), true);

            // 克隆项目到本地
            git = Git.cloneRepository()
                    .setURI(remoteAddress)
                    .setDirectory(PROJECT_PATH)
                    .setTransportConfigCallback(GitHelper.createTransportConfigCallback(safeConfig))
                    .call();

            return Objects.nonNull(localGit());
        } catch (Exception e) {
            log.info("init project fail. GitProject:{}", this, e);
            return false;
        } finally {
            GitHelper.closeGit(git);
        }
    }


    /**
     * 获取本地 git 仓库
     */
    public Git localGit() {
        try {
            File gitFiles = new File(Constants.PROJECT_WORKSPACE + projectName + "/.git");
            return Git.open(gitFiles);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 将本地分支推送到远程
     */
    public Result push(String localBranchName, String remoteBranchName) {
        try (Git localGit = localGit()){
            PushResult pushResult = localGit.push()
                    .setTransportConfigCallback(GitHelper.createTransportConfigCallback(safeConfig.getPrivateKeyPosition()))
                    .setRefSpecs(new RefSpec(Constants.BRANCH_PRE + localBranchName + ":" + Constants.BRANCH_PRE + remoteBranchName))
                    .setForce(true)
                    .call().iterator().next();
            return Result.success();
        } catch (Exception e) {
            log.error("fail. GitProject:{} ", this, e);
            return Result.fail().setMsg(e.getMessage());
        }
    }

    /**
     * 删除远程分支
     * @param remoteBranchName
     * @return
     */
    public Result removeRemoteBeanch(String remoteBranchName) {
        try (Git localGit = localGit()) {
            localGit.push()
                    .setTransportConfigCallback(GitHelper.createTransportConfigCallback(safeConfig.getPrivateKeyPosition()))
                    .setRefSpecs(new RefSpec(":" + Constants.BRANCH_PRE + remoteBranchName))
                    .setForce(true)
                    .call().iterator().next();
            return Result.success();
        } catch (Exception e) {
            log.error("fail.GitProject:{}", this, e);
            return Result.fail().setMsg(e.getMessage());
        }
    }

    /**
     * 从远程分支拉取
     */
    public Result mergeRemoteBranch(String branchName) {
        try (Git localGit = localGit()){
            PullResult mergeResult = localGit.pull()
                    .setTransportConfigCallback(GitHelper.createTransportConfigCallback(safeConfig))
                    .setRemoteBranchName(branchName)
                    .call();
            if (!mergeResult.getMergeResult().getMergeStatus().isSuccessful()) {
                log.warn("merge fail!. branch:{} status:{}",branchName,mergeResult.getMergeResult().getMergeStatus());
                // 将本地仓库复原
                resetHard();
                return Result.fail().setMsg("branchName:" +branchName + " " + mergeResult.getMergeResult().getMergeStatus().toString());
            }
            return Result.success();
        } catch (Exception e) {
            log.error("fail. GitProject:{}", this, e);
            return Result.fail().setMsg(e.getMessage());
        }
    }

    /**
     * 同时对多个分支进行合并
     */
    public Result mergeRemoteBeanchs(List<String> branchNames) {
        Set<String> waitMergeBranchSet = new HashSet<>(branchNames);
        Integer successCounter = 0;
        do {
            successCounter = 0;
            Iterator<String> iterator = waitMergeBranchSet.iterator();
            while (iterator.hasNext()) {
                String branchName = iterator.next();
                Result mergeBranchResult = mergeRemoteBranch(branchName);
                if (mergeBranchResult.isSuccess()) {
                    successCounter ++;
                    iterator.remove();
                }
            }

        } while (successCounter > 0 && !waitMergeBranchSet.isEmpty());

        if (!waitMergeBranchSet.isEmpty()) {
            return Result.fail().setMsg("merge fail!.branchs: " + waitMergeBranchSet.toString());
        }

        return Result.success();
    }

    public List<GitBranch> listRemoteBranch() {
        try {
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setTransportConfigCallback(GitHelper.createTransportConfigCallback(safeConfig))
                    .setRemote(this.remoteAddress)
                    .setHeads(true)
                    .call();
            return refs.stream()
                    .map(parseBranchName())
                    .filter(this::filterByBranchName)
                    .collect(Collectors.toList());
        } catch (GitAPIException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean filterByBranchName(GitBranch gitBranch) {
        if (CollectionUtils.isEmpty(filterBranchNames)) {
            return true;
        }

        for (String filterBranchName : filterBranchNames) {
            String branchName = gitBranch.getBranchName();
            if (branchName.startsWith(filterBranchName)) {
                return false;
            }
        }

        return true;
    }

    private Function<Ref, GitBranch> parseBranchName() {
        return ref -> {
            GitBranch branch = new GitBranch();
            branch.setBranchName(ref.getName().substring(11));
            return branch;
        };
    }

    public Result fetch(String remoteBranchName, String localBranchName) {
        try (Git localGit = localGit()){
            FetchResult call = localGit.fetch()
                    .setTransportConfigCallback(GitHelper.createTransportConfigCallback(safeConfig))
                    .setCheckFetchedObjects(true)
                    .setRemote(this.remoteAddress)
                    .setRefSpecs(new RefSpec("+"+Constants.BRANCH_PRE + remoteBranchName + ":" + Constants.BRANCH_PRE + localBranchName))
                    .call();
            return Result.success();
        } catch (Exception e) {
            log.error("fail. GitProject:{}", this, e);
            return Result.fail().setMsg(e.getMessage());
        }
    }

    /**
     * 为项目添加泳道
     */
    public void addSwimlane(Swimlane swimlane) {
        this.swimlaneMap.put(swimlane.getSwimlaneName(), swimlane);
    }

    /**
     * 锁项目
     */
    public boolean lockProject() {
        return projectLock.tryLock();
    }

    /**
     * 解锁项目
     */
    public void unlockProject() {
        projectLock.unlock();
    }

    /**
     * 从项目获取泳道
     */
    public Swimlane getSwimlane(String swimlaneName) {
        return this.swimlaneMap.get(swimlaneName);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Map<String, Swimlane> getSwimlaneMap() {
        return swimlaneMap;
    }

    public void setSafeConfig(SafeConfig safeConfig) {
        this.safeConfig = safeConfig;
    }

    public List<String> getFilterBranchNames() {
        return filterBranchNames;
    }

    public void setFilterBranchNames(List<String> filterBranchNames) {
        this.filterBranchNames = filterBranchNames;
    }
}
