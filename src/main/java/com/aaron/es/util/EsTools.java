package com.aaron.es.util;


import com.aaron.es.annotation.ESID;
import com.aaron.es.annotation.ESMapping;
import com.aaron.es.annotation.ESMetaData;
import com.aaron.es.model.MappingData;
import com.aaron.es.model.MetaData;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @description: ES工具类
 * @author: Aaron
 * @date 2019/4/2
 **/
public class EsTools {
    /**
     * 获取索引元数据：indexname、indextype
     * @param clazz
     * @return
     */
    public static MetaData getIndexType(Class<?> clazz){
        String indexname;
        String indextype;
        if(clazz.getAnnotation(ESMetaData.class) != null){
            indexname = clazz.getAnnotation(ESMetaData.class).indexName();
            indextype = clazz.getAnnotation(ESMetaData.class).indexType();
            MetaData metaData = new MetaData(indexname,indextype);
            metaData.setPrintLog(clazz.getAnnotation(ESMetaData.class).printLog());
            if(arrayISNULL(clazz.getAnnotation(ESMetaData.class).searchIndexNames())) {
                metaData.setSearchIndexNames(new String[]{indexname});
            }else{
                metaData.setSearchIndexNames((clazz.getAnnotation(ESMetaData.class).searchIndexNames()));
            }
            return metaData;
        }
        return null;
    }

    /**
     * 获取索引元数据：主分片、备份分片数的配置
     * @param clazz
     * @return
     */
    public static MetaData getShardsConfig(Class<?> clazz){
        int number_of_shards;
        int number_of_replicas;
        if(clazz.getAnnotation(ESMetaData.class) != null){
            number_of_shards = clazz.getAnnotation(ESMetaData.class).number_of_shards();
            number_of_replicas = clazz.getAnnotation(ESMetaData.class).number_of_replicas();
            MetaData metaData = new MetaData(number_of_shards,number_of_replicas);
            metaData.setPrintLog(clazz.getAnnotation(ESMetaData.class).printLog());
            return metaData;
        }
        return null;
    }

    /**
     * 获取索引元数据：indexname、indextype、主分片、备份分片数的配置
     * @param clazz
     * @return
     */
    public static MetaData getMetaData(Class<?> clazz){
        String indexname = "";
        String indextype = "";
        int number_of_shards = 0;
        int number_of_replicas = 0;
        if(clazz.getAnnotation(ESMetaData.class) != null){
            indexname = clazz.getAnnotation(ESMetaData.class).indexName();
            indextype = clazz.getAnnotation(ESMetaData.class).indexType();
            number_of_shards = clazz.getAnnotation(ESMetaData.class).number_of_shards();
            number_of_replicas = clazz.getAnnotation(ESMetaData.class).number_of_replicas();
            MetaData metaData = new MetaData(indexname,indextype,number_of_shards,number_of_replicas);
            metaData.setPrintLog(clazz.getAnnotation(ESMetaData.class).printLog());
            if(arrayISNULL(clazz.getAnnotation(ESMetaData.class).searchIndexNames())) {
                metaData.setSearchIndexNames(new String[]{indexname});
            }else{
                metaData.setSearchIndexNames((clazz.getAnnotation(ESMetaData.class).searchIndexNames()));
            }
            return metaData;
        }
        return null;
    }

    /**
     * 获取配置于Field上的mapping信息，如果未配置注解，则给出默认信息
     * @param field
     * @return
     */
    public static MappingData getMappingData(Field field){
        if(field == null){
            return null;
        }
        field.setAccessible(true);
        MappingData mappingData = new MappingData();
        mappingData.setField_name(field.getName());
        if(field.getAnnotation(ESMapping.class) != null){
            ESMapping esMapping = field.getAnnotation(ESMapping.class);
            mappingData.setDatatype(esMapping.datatype().toString().replaceAll("_type",""));
//            mappingData.setAnalyzedtype(esMapping.analyzedtype().toString());
            mappingData.setAnalyzer(esMapping.analyzer().toString());
            mappingData.setAutocomplete(esMapping.autocomplete());
            mappingData.setIgnore_above(esMapping.ignore_above());
            mappingData.setSearch_analyzer(esMapping.search_analyzer().toString());
            mappingData.setKeyword(esMapping.keyword());
            mappingData.setSuggest(esMapping.suggest());
            mappingData.setAllow_search(esMapping.allow_search());
            mappingData.setCopy_to(esMapping.copy_to());
        }else{
            mappingData.setDatatype("text");
//            mappingData.setAnalyzedtype("analyzed");
            mappingData.setAnalyzer("standard");
            mappingData.setAutocomplete(false);
            mappingData.setIgnore_above(256);
            mappingData.setSearch_analyzer("standard");
            mappingData.setKeyword(true);
            mappingData.setSuggest(false);
            mappingData.setAllow_search(true);
            mappingData.setCopy_to("");
        }
        return mappingData;
    }

    /**
     * 批量获取配置于Field上的mapping信息，如果未配置注解，则给出默认信息
     * @param clazz
     * @return
     */
    public static MappingData[] getMappingData(Class<?> clazz){
        Field[] fields = clazz.getDeclaredFields();
        MappingData[] mappingDataList = new MappingData[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getName().equals("serialVersionUID")){
                continue;
            }
            mappingDataList[i] = getMappingData(fields[i]);
        }
        return mappingDataList;
    }

    /**
     * 根据对象中的注解获取ID的字段值
     * @param obj
     * @return
     */
    public static String getESId(Object obj) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field f : fields){
            f.setAccessible(true);
            ESID esid = f.getAnnotation(ESID.class);
            if(esid != null){
                Object value = f.get(obj);
                return value.toString();
            }
        }
        return null;
    }

    /**
     * 获取o中所有的字段有值的map组合
     * @return
     */
    public static Map getFieldValue(Object o) throws IllegalAccessException {
        Map retMap = new HashMap();
        Field[] fs = o.getClass().getDeclaredFields();
        for(int i = 0;i < fs.length;i++){
            Field f = fs[i];
            f.setAccessible(true);
            if(f.get(o) != null){
                retMap.put(f.getName(),f.get(o) );
            }
        }
        return retMap;
    }

    public static String arraytostring(String[] strs){
        if(StringUtils.isEmpty(strs)){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        Arrays.asList(strs).stream().forEach(str -> sb.append(str).append(" "));
        return sb.toString();
    }

    public static boolean arrayISNULL(Object[] objs){
        if(objs == null || objs.length == 0){
            return true;
        }
        boolean flag = false;
        for (int i = 0; i < objs.length; i++) {
            if(!StringUtils.isEmpty(objs[i])){
                flag = true;
            }
        }
        if(flag){
            return false;
        }else{
            return true;
        }
    }

    public static String[] getNoValuePropertyNames (Object source) {
        Assert.notNull(source, "传递的参数对象不能为空");
        final BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();

        Set<String> noValuePropertySet = new HashSet<>();
        Arrays.stream(pds).forEach(pd -> {
            Object propertyValue = beanWrapper.getPropertyValue(pd.getName());
            if (StringUtils.isEmpty(propertyValue)) {
                noValuePropertySet.add(pd.getName());
            } else {
                if (Iterable.class.isAssignableFrom(propertyValue.getClass())) {
                    Iterable iterable = (Iterable) propertyValue;
                    Iterator iterator = iterable.iterator();
                    if (!iterator.hasNext()) noValuePropertySet.add(pd.getName());
                }
                if (Map.class.isAssignableFrom(propertyValue.getClass())) {
                    Map map = (Map) propertyValue;
                    if (map.isEmpty()) noValuePropertySet.add(pd.getName());
                }
            }
        });
        String[] result = new String[noValuePropertySet.size()];
        return noValuePropertySet.toArray(result);
    }

}
