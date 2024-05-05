package com.ink.rpc.loadbalancer;

import com.ink.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

public interface LoadBalancer {

    /**
     * 负载均衡下的服务选择
     * @param requestParams 请求参数
     * @param serviceMetaInfoList 查询到的可用服务列表
     * @return 选中的服务
     */
    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);


}
