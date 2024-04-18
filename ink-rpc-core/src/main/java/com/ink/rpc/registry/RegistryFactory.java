package com.ink.rpc.registry;

import com.ink.rpc.spi.SpiLoader;

public class RegistryFactory {

    static {
        //加载注册器类
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认注册器
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取具体注册器实例
     *
     * @param key 具体注册器名称
     * @return 注册器实例
     */
    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }

}
