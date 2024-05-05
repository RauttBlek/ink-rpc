package com.ink.rpc.loadbalancer;

import com.ink.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer implements LoadBalancer {

    private final TreeMap<Integer, ServiceMetaInfo> map = new TreeMap<>();

    private final Integer VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        //构造一致 Hash 环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                int hash = hash(serviceMetaInfo.getServiceAddress() + "#" + i);
                map.put(hash, serviceMetaInfo);
            }
        }
        //获取请求 Hash 值
        int hash = hash(requestParams);
        //选取最接近的且 Hash 值大于等于请求 Hash 值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = map.ceilingEntry(hash);
        if (entry == null) {
            entry = map.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * hash 算法
     *
     * @param o 需要计算 hash 值的对象
     * @return hash 值
     */
    private int hash(Object o) {
        return o.hashCode();
    }

}
