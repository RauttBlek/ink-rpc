package com.ink.rpc.loadbalancer;

import com.ink.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡
 */
public class RoundRobinLoadBalancer implements LoadBalancer{

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        //判空
        if(serviceMetaInfoList.isEmpty()){
            return null;
        }
        int size = serviceMetaInfoList.size();
        //服务唯一，直接返回
        if(size == 1){
            return serviceMetaInfoList.get(0);
        }
        //使用取模进行轮询
        int index = atomicInteger.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
