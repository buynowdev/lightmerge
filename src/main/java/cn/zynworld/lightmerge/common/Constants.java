package cn.zynworld.lightmerge.common;

public interface Constants {
    String PROJECT_WORKSPACE = System.getProperties().getProperty("user.home") + "/.lightmerge/";
    String PROJECT_CONFIG_FILE = System.getProperties().getProperty("user.home") + "/.lightmerge/config.json";
    String BRANCH_PRE = "refs/heads/";
}
