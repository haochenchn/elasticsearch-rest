package com.aaron.es.test.controller;

import com.aaron.es.ElasticsearchIndex;
import com.aaron.es.ElasticsearchRestTemplate;
import com.aaron.es.test.Leader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/test")
@Controller
public class TestController {
    @Autowired
    private ElasticsearchIndex index;
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @RequestMapping("/index")
    @ResponseBody
    public Object testIndex(){
        try {
            index.createIndex(Leader.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "hello";
    }

    @RequestMapping("/save")
    @ResponseBody
    public Object save(){
        Leader leader = new Leader("武汉","湖北省","ORG","21","");
        try {
            boolean save = restTemplate.save(leader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "hao";
    }

}
