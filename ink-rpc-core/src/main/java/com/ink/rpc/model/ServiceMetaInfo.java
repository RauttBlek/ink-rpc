package com.ink.rpc.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class ServiceMetaInfo {

    private String serviceName;

    private String version = "1.0";

    private String serviceHost;

    private Integer servicePort;

    /**
     * （待实现）服务分组
     */
    private String serviceGroup = "default";

    /**
     * 获取服务键名（服务名：版本号）
     * @return 服务键名信息
     */
    public String getServiceKey(){
        //服务分组待扩展
        return String.format("%s:%s", serviceName, version);
    }

    /**
     * 获取服务注册的节点名
     * @return 服务键名与其注册的地址
     */
    public String getServiceNodeKey(){
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }


    public String getServiceAddress(){
        if(!StrUtil.contains(serviceHost, "http")){
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }

}
