package com.ink.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry{

    private Client client;

    private KV kvClient;

    /**
     * 本地已注册的节点，用于服务端节点续约
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 服务发现缓存，用于消费端查询服务
     */
    private final RegisterServiceCache serviceCache = new RegisterServiceCache();
    /**
     * 受监听的 Key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

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
         heartBeat();
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
        //添加已注册节点到集合中
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = DEFAULT_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        //删除 ETCD 中键值对
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));
        //删除注册节点集合中对应键名
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        //缓存查询
        List<ServiceMetaInfo> serviceCacheList = serviceCache.readCache();
        if (serviceCacheList != null) {
            //缓存存在时直接返回
            return serviceCacheList;
        }
        //使用服务名前缀搜索
        String searchPrefix = DEFAULT_ROOT_PATH + serviceKey + "/";

        try{
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get()
                    .getKvs();
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
            serviceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        }catch (Exception e){
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void destroy() {
        //删除 ETCD 中节点
        for(String key:localRegisterNodeKeySet){
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        //关闭注册中心，释放资源
        if(kvClient != null){
            kvClient.close();
        }
        if(client != null){
            client.close();
        }

    }

    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", (Task) () -> {
            for(String key:localRegisterNodeKeySet){
                try{
                    List<KeyValue> keyValues =  kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                            .get()
                            .getKvs();
                    if(CollUtil.isEmpty(keyValues)){
                        //列表为空，则节点已过期，不续签，必须重启该节点
                        continue;
                    }
                    //未过期则重新注册，重置过期时间
                    KeyValue keyValue = keyValues.get(0);
                    //将拿到的 ByteSequence 转为 JSON 字符串
                    String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                    //将 JSON 字符串转为指定的对象
                    ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                    //重新注册
                    register(serviceMetaInfo);
                }catch (Exception e){
                    throw new RuntimeException("节点续签失败", e);
                }
            }
        });
        //设置秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        boolean add = watchingKeySet.add(serviceNodeKey);
        if(add){
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), watchResponse -> {
                for(WatchEvent event:watchResponse.getEvents()){
                    switch (event.getEventType()){
                        case DELETE:
                            //存在 key 被删除时清空服务缓存
                            serviceCache.clearCache();
                            break;
                        //服务注册时会更新缓存，因此监听到 PUT 操作时无需行动
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

}
