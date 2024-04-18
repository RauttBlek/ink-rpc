package com.ink.rpc.config;

import lombok.Data;

/**
 * RPC 注册中心配置类
 */
@Data
public class RegistryConfig {

    /**
     * 注册中心的类别
     */
    private String registry = "etcd";

    /**
     * 注册中心地址
     */
    private String registryAddress = "http://localhost:2380";

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（毫秒）
     */
    private Long timeout = 10000L;


}
