package com.ink.rpc.loadbalancer;

import com.ink.rpc.spi.SpiLoader;

public class LoadBalancerFactory {

    static {
        //加载对应类
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    /**
     * 获取对应负载均衡器实例
     * @param key 名称
     * @return 对应实例
     */
    public static LoadBalancer getInstance(String key){
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }

}
