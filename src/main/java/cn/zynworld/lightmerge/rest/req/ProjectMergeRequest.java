package cn.zynworld.lightmerge.rest.req;

import lombok.Data;

import java.util.List;

/**
 * @author zhaoyuening
 * @date 19-8-3 下午3:58
 */
@Data
public class ProjectMergeRequest {
    private String projectName;
    private List<String> branchNames;
    private String swimlaneName;
}
