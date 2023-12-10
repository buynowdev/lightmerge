package cn.zynworld.lightmerge.domain.repository;

import cn.zynworld.lightmerge.domain.GitProject;
import org.eclipse.jgit.api.Git;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author zhaoyuening
 * @date 19-8-3 下午3:59
 */
public class GitProjectRepository {

    private Map<String, GitProject> projectMap = new HashMap<>();

    public List<GitProject> getProjects() {
        return new ArrayList<>(projectMap.values());
    }

    public GitProject getProjectByName(String projectName) {
        return projectMap.get(projectName);
    }

    public void addProject(GitProject project) {
        projectMap.put(project.getProjectName(), project);
    }

    public void setProjects(Map<String, GitProject> projectMap) {
        this.projectMap = projectMap;
    }

    @PostConstruct
    public void init() {
        // 检测所有项目是否初始化完成
        for (GitProject project : getProjects()) {
            Git localGit = project.localGit();
            if (Objects.isNull(localGit)) {
                // 项目初始化
                project.init();
            }
        }
    }
}
