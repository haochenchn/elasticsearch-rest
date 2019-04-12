package com.aaron.es.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

/**
 * @author Aaron
 * @description Jackson工具
 * @date 2019/4/12
 */
public class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        //忽略多余字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对象转字符串
     */
    public static <T> String obj2String(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 字符串转对象
     */
    public static <T> T string2Obj(String str, Class<T> clazz){
        if (str == null || str.trim() == "" || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * map转对象
     */
    public static <T> T map2Obj(Map<String,?> map, Class<T> clazz){
        if(map == null || clazz == null){
            return null;
        }
        return objectMapper.convertValue(map, clazz);
    }
    /**
     * 对象转map
     */
    public static <T> Map obj2Map(T obj){
        if(obj == null){
            return null;
        }
        return objectMapper.convertValue(obj, Map.class);
    }
}
