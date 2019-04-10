package com.aaron.es.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 高亮对象封装
 * @author: Aaron
 * @date 2019/4/2
 **/
public class HighLight {
    //高亮默认红色字体
    private String preTag = "<font style=\"color:red\">";
    private String postTag = "</font>";
    private List<String> highLightList = null;

    public HighLight(){
        highLightList = new ArrayList<>();
    }

    public HighLight field(String fieldValue){
        highLightList.add(fieldValue);
        return this;
    }

    public List<String> getHighLightList(){
        return highLightList;
    }

    public String getPreTag() {
        return preTag;
    }

    public void setPreTag(String preTag) {
        this.preTag = preTag;
    }

    public String getPostTag() {
        return postTag;
    }

    public void setPostTag(String postTag) {
        this.postTag = postTag;
    }
}
