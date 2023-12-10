package cn.zynworld.lightmerge.rest.req;

import lombok.Data;

import java.util.List;

@Data
public class ProjectCheckConflictsRequest {
    private String baseBranch;
    private String projectName;
    private List<String> branches;
}
