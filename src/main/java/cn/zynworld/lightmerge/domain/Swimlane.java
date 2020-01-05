package cn.zynworld.lightmerge.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaoyuening
 * @date 19-8-3 下午4:09
 */
@Data
public class Swimlane implements Serializable {
    private static final long serialVersionUID = -582035273247138527L;

    private String swimlaneName;
    private String remotePushBranchName;
    private List<GitBranch> selectedBranches = new ArrayList<>();
}
