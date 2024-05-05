package com.ink.rpc.config;

import com.ink.rpc.loadbalancer.LoadBalancerKeys;
import com.ink.rpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置类
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
    private Integer serverPort = 8080;

    /**
     * 是否开启 MOCK
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 注册器配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
