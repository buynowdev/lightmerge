package cn.zynworld.lightmerge.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

public class GitBranch implements Serializable {
    private static final long serialVersionUID = -3023736795344167333L;

    private String branchName;

    public String getBranchName() {
        return branchName;
    }

    public GitBranch setBranchName(String branchName) {
        this.branchName = branchName;
        return this;
    }
}
