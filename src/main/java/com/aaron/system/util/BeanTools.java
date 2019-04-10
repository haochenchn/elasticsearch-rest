package com.aaron.system.util;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aaron
 * @version 1.0
 * @description 类对象操作工具类
 * @date 2019/3/31
 */
public class BeanTools {
    /**
     * map转对象
     * @param map
     * @param beanClass
     * @return
     */
    public static Object map2Object(Map map, Class<?> beanClass){
        if(null == map){
            return null;
        }
        try {
            Object obj = beanClass.newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields){
                if(null == map.get(field.getName()) || StringUtils.isEmpty(map.get(field.getName()))){
                    continue;
                }
                int mod = field.getModifiers();
                if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){
                    continue;
                }
                field.setAccessible(true);
                field.set(obj, map.get(field.getName()));
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对象转map
     * @param obj
     * @return
     */
    public static Map<String,Object> object2Map(Object obj){
        if(null == obj) return null;
        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field field : fields){
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
