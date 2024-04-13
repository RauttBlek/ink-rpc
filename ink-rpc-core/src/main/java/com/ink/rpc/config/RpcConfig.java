package com.ink.rpc.config;

import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "ink-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机
     */
    private String serverHost = "localhost";

    /**
     * 端口号
     */
    private Integer post = 8080;

    /**
     * 是否开启 MOCK
     */
    private boolean mock = false;

}
