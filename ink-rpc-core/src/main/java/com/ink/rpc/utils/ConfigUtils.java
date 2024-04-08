package com.ink.rpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * 配置工具类
 */
public class ConfigUtils {


    public static <T> T loadConfig(Class<T> tClass, String prefix){
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 读取配置文件，返回一个配置类
     * @param tClass 配置类的 Class 对象
     * @param prefix 配置项统一前缀
     * @param environment 配置文件作用环境，用于增加后缀
     * @return 配置类
     * @param <T> 泛型
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment){
        StringBuilder stringBuilder = new StringBuilder("application");
        //检查环境，以判断是否添加后缀
        if(StrUtil.isNotBlank(environment)){
            stringBuilder.append(environment);
        }
        //拼接配置文件名
        stringBuilder.append(".properties");
        //构建读取配置文件的 props 类
        Props props = new Props(stringBuilder.toString());
        return props.toBean(tClass, prefix);
    }

}
