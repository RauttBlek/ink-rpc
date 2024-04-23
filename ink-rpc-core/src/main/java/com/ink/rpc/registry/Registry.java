package com.ink.rpc.registry;

import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心
 */
public interface Registry {

    /**
     * 初始化注册器
     * @param registryConfig 注册器配置
     */
    void init(RegistryConfig registryConfig);

    /**
     * 服务注册
     * @param serviceMetaInfo 服务元信息
     * @throws Exception 异常
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 服务注销
     * @param serviceMetaInfo 服务元信息
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     * 服务发现（消费端，获取服务的所有节点）
     * @param serviceKey 服务键名
     * @return 所有服务的元信息
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 服务销毁
     */
    void destroy();

    /**
     * 心跳检测（用于服务端续约）
     */
    void heartBeat();

    /**
     * 监听节点（消费端）
     * @param serviceNodeKey 节点键名
     */
    void watch(String serviceNodeKey);
}
