package com.ink.rpc;

import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.config.RpcConfig;
import com.ink.rpc.constant.RpcConstant;
import com.ink.rpc.registry.Registry;
import com.ink.rpc.registry.RegistryFactory;
import com.ink.rpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc 框架应用，存放全局变量
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    /**
     * 初始化
     */
    private static void init(){
        RpcConfig newConfig;
        try {
            newConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        }catch (Exception e){
            //加载失败时使用默认值
            newConfig = new RpcConfig();
        }
        init(newConfig);
    }

    /**
     * 支持传入自定义配置的初始化
     * @param config 自定义配置
     */
    private static void init(RpcConfig config){
        rpcConfig = config;
        log.info("rpc init, config = {}", config.toString());
        //初始化注册中心
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init completely");
    }

    /**
     * 获取一个 RpcConfig
     * @return RpcConfig
     */
    public static RpcConfig getRpcConfig(){
        //双检锁实现单例
        if(rpcConfig == null){
            synchronized(RpcApplication.class){
                if(rpcConfig == null){
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
