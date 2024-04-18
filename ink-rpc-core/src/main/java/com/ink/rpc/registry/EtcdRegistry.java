package com.ink.rpc.registry;

import cn.hutool.json.JSONUtil;
import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry{

    private Client client;

    private KV kvClient;

    /**
     * 默认根键名
     */
    private static final String DEFAULT_ROOT_PATH = "/rpc/";

    /**
     * 初始化 etcd 的 client 与 kvClient
     * @param registryConfig 注册器配置
     */
    @Override
    public void init(RegistryConfig registryConfig) {
         client = Client.builder().endpoints(registryConfig.getRegistryAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
         kvClient = client.getKVClient();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        //创建 lease 客户端
        Lease leaseClient = client.getLeaseClient();
        //创建一个三十秒的租约
        long leaseId = leaseClient.grant(30).get().getID();

        //设置存储的键值对
        String registerKey = DEFAULT_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence byteKey = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence byteValue = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        //关联键值对和租约，设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        //存入键值对
        kvClient.put(byteKey, byteValue, putOption).get();

    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        //删除键值对即可
        kvClient.delete(ByteSequence.from(serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        //使用服务名前缀搜索
        String searchPrefix = DEFAULT_ROOT_PATH + serviceKey + "/";

        try{
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get()
                    .getKvs();
            return keyValues.stream()
                    .map(keyValue -> {
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
        }catch (Exception e){
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void destroy() {
        //关闭注册中心，释放资源
        if(kvClient != null){
            kvClient.close();
        }
        if(client != null){
            client.close();
        }

    }
}
