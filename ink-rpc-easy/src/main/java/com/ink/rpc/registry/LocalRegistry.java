package com.ink.rpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册器
 * 直接获取注册的实现类
 */
public class LocalRegistry {

    private static final Map<String, Class<?>> map= new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param impClass 服务实现类
     */
    public static void register(String serviceName, Class<?> impClass){
        map.put(serviceName, impClass);
    }

    /**
     * 获取已注册的服务
     * @param serviceName 服务名称
     * @return 服务实现类
     */
    public static Class<?> get(String serviceName){
        return map.get(serviceName);
    }

    /**
     * 移除已注册的服务
     * @param serviceName 服务名称
     */
    public static void remove(String serviceName){
        map.remove(serviceName);
    }
}
