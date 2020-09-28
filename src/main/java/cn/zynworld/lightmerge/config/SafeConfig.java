package cn.zynworld.lightmerge.config;

import lombok.Data;

/**
 * @author zhaoyuening
 */
@Data
public class SafeConfig {
    // 默认私钥位置
    private String privateKeyPosition = "~/.ssh/id_rsa";
}
