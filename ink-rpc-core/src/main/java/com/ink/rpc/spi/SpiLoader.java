package com.ink.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.ink.rpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {

    /**
     * 系统 spi 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义 spi 目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 加载类时扫描的路径
     */
    private static final String[] SCAN_DIR = new String[]{RPC_CUSTOM_SPI_DIR, RPC_SYSTEM_SPI_DIR};

    /**
     * 加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = List.of(Serializer.class);

    /**
     * 存储已加载的类，防止实例化时无法找到类
     */
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存，避免重复实例化
     */
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    public static void loadAll() {
        log.info("加载所有 SPI");
        for (Class<?> c : LOAD_CLASS_LIST) {
            load(c);
        }
    }

    /**
     * 获取某个接口的实现类的实例
     *
     * @param tClass 接口类
     * @param key    实现类名称
     * @param <T>    泛型参数
     * @return 指定实现类的实例
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        String className = tClass.getName();
        Map<String, Class<?>> classMap = loaderMap.get(className);
        if (classMap == null) {
            throw new RuntimeException(String.format("%s 类型未加载", className));
        }
        if (!classMap.containsKey(key)) {
            throw new RuntimeException(String.format("%s 不存在 %s 的类型", className, key));
        }
        //获取要加载的实现类
        Class<?> implClass = classMap.get(key);
        String implClassName = implClass.getName();
        //检查实力缓存
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                String errMessage = String.format("%s 实例化失败", implClassName);
                throw new RuntimeException(errMessage, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }

    /**
     * 加载某个类型（接口）
     *
     * @param loadClass 指定的类型
     */
    public static void load(Class<?> loadClass) {
        String loadClassName = loadClass.getName();
        log.info("加载 {} 类型的 SPI", loadClassName);
        //检查是否已加载过
        if (loaderMap.containsKey(loadClassName)) {
            log.info("{} 类型已加载过", loadClassName);
            loaderMap.get(loadClassName);
            return;
        }
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String dir : SCAN_DIR) {
            List<URL> resources = ResourceUtil.getResources(dir + loadClass.getName());
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if (split.length > 1) {
                            String key = split[0];
                            String className = split[1];
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("SPI 资源加载失败", e);
                }
            }
        }
        loaderMap.put(loadClassName, keyClassMap);
        //return keyClassMap;原定返回Map<String, Class<?>>类型
    }

}
