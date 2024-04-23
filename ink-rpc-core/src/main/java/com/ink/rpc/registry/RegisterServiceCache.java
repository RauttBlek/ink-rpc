package com.ink.rpc.registry;

import com.ink.rpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册服务本地缓存
 */
public class RegisterServiceCache {
    /**
     * 缓存列表，类型与服务发现方法返回类型一致
     */
    List<ServiceMetaInfo> serviceCache;

    /**
     * 写入缓存
     *
     * @param serviceCache 服务元信息列表
     */
    void writeCache(List<ServiceMetaInfo> serviceCache) {
        this.serviceCache = serviceCache;
    }

    /**
     * 读取缓存
     *
     * @return 服务元信息缓存列表
     */
    List<ServiceMetaInfo> readCache() {
        return serviceCache;
    }

    /**
     * 清空缓存
     */
    void clearCache() {
        serviceCache = null;
    }

}
