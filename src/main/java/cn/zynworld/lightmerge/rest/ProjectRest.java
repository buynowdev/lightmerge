package cn.zynworld.lightmerge.rest;

import cn.zynworld.lightmerge.common.Constants;
import cn.zynworld.lightmerge.common.Result;
import cn.zynworld.lightmerge.domain.GitBranch;
import cn.zynworld.lightmerge.domain.GitProject;
import cn.zynworld.lightmerge.domain.Swimlane;
import cn.zynworld.lightmerge.domain.repository.GitProjectRepository;
import cn.zynworld.lightmerge.rest.req.ProjectCheckConflictsRequest;
import cn.zynworld.lightmerge.rest.req.ProjectInitRequest;
import cn.zynworld.lightmerge.rest.req.ProjectMergeRequest;
import cn.zynworld.lightmerge.rest.req.ProjectReloadRequest;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaoyuening
 * @date 19-8-3 下午3:55
 */
@RequestMapping("api")
@RestController
public class ProjectRest {
    @Autowired
    private GitProjectRepository projectRepository;

    @GetMapping(path = "test")
    public List<String> test() {
        return Collections.singletonList("hello");
    }

    @GetMapping(path = "projects")
    @ResponseBody
    public List<GitProject> getProjects() {
        return projectRepository.getProjects();
    }

    @GetMapping(path = "project")
    @ResponseBody
    public GitProject getProjects(String projectName) {
        return projectRepository.getProjectByName(projectName);
    }

    @PostMapping(path = "checkConflicts")
    public Result checkConflicts(@RequestBody ProjectCheckConflictsRequest request) {
        GitProject project = projectRepository.getProjectByName(request.getProjectName());
        boolean lockResult = project.lockProject();
        if (!lockResult) {
            return Result.fail().setMsg("请勿同时合并项目!");
        }

        try (Git git = project.localGit()){
            // 新分支名称
            String tmpBranchName = "TMP-" + UUID.randomUUID().toString();
            // 拉取 第一个分支
            String firstBranchName = request.getBaseBranch();
            Result fetch = project.fetch(firstBranchName, tmpBranchName);
            // 对当前分支 reset
            project.resetHard(null);
            // 切换分支
            project.checkout(tmpBranchName);
            if (!fetch.isSuccess()) {
                return fetch;
            }


            Map<String, GitProject.MergeStatus> mergeResult = new HashMap<>();
            for (String branch : request.getBranches()) {
                GitProject.MergeStatus status = project.mergeRemoteBranch(branch, true);
                mergeResult.put(branch, status);
            }

            // 对当前分支 reset
            project.checkout(firstBranchName);
            project.delete(tmpBranchName);

            project.resetHard(null);
            return Result.success().setData(mergeResult);
        } finally {
            project.unlockProject();
        }
    }

    @PostMapping(path = "merges")
    public Result merge(@RequestBody ProjectMergeRequest mergeRequest) {
        GitProject project = projectRepository.getProjectByName(mergeRequest.getProjectName());
        boolean lockResult = project.lockProject();
        if (!lockResult) {
            return Result.fail().setMsg("请勿同时合并项目!");
        }
        try (Git git = project.localGit()){
            if (CollectionUtils.isEmpty(mergeRequest.getBranchNames())) {
                project.unlockProject();
                return Result.fail().setMsg("合并分支为空");
            }


            // 新分支名称
            String tmpBranchName = "TMP-" + UUID.randomUUID().toString();
            // 拉取 第一个分支
            String firstBranchName = mergeRequest.getBranchNames().get(Constants.FIRST);
            project.fetch(firstBranchName, tmpBranchName);
            // 对当前分支 reset
            project.resetHard(null);
            // 切换分支
            project.checkout(tmpBranchName);
            // 合并多分支
            Result result = project.mergeRemoteBeanchs(mergeRequest.getBranchNames().subList(1, mergeRequest.getBranchNames().size()));
            if (!result.isSuccess()) {
                return result;
            }

            // 删除远程分支
            project.removeRemoteBeanch(project.getSwimlane(mergeRequest.getSwimlaneName()).getRemotePushBranchName());
            // 推送到远端
            project.push(tmpBranchName, project.getSwimlane(mergeRequest.getSwimlaneName()).getRemotePushBranchName());
            // 删除本地分支
            project.checkout(firstBranchName);
            project.delete(tmpBranchName);

            // 合并成功修改泳道被选分支
            List<GitBranch> branchs = mergeRequest.getBranchNames().stream()
                    .map(branchName -> new GitBranch().setBranchName(branchName))
                    .collect(Collectors.toList());
            project.getSwimlane(mergeRequest.getSwimlaneName()).setSelectedBranches(branchs);

            return Result.success().setData(mergeRequest.getBranchNames());
        } finally {
            project.unlockProject();
        }
    }

    @GetMapping(path = "branchs")
    public List<GitBranch> getBranchs(@RequestParam String projectName) {
        return projectRepository.getProjectByName(projectName).listRemoteBranch();
    }

    @PostMapping(path = "initProject")
    public Result initProject(@RequestBody ProjectInitRequest initRequest) {
        boolean result = projectRepository.getProjectByName(initRequest.getProjectName()).init();
        return Result.build(result);
    }

    /**
     * 对项目泳道进行重新合并部署
     * 合并分支为当前已选分支
     */
    @PostMapping(path = "reload")
    public Result reload(@RequestBody ProjectReloadRequest reloadRequest) {
        // 获取当前已选分支
        GitProject project = projectRepository.getProjectByName(reloadRequest.getProjectName());
        Swimlane swimlane = project.getSwimlane(reloadRequest.getSwimlaneName());
        List<GitBranch> selectedBranches = swimlane.getSelectedBranches();

        ProjectMergeRequest projectMergeRequest = new ProjectMergeRequest();
        projectMergeRequest.setProjectName(reloadRequest.getProjectName());
        projectMergeRequest.setSwimlaneName(reloadRequest.getSwimlaneName());
        projectMergeRequest.setBranchNames(selectedBranches.stream().map(GitBranch::getBranchName).collect(Collectors.toList()));

        return merge(projectMergeRequest);
    }

}
