package com.aaron.es;


import com.aaron.es.model.MappingData;
import com.aaron.es.model.MetaData;
import com.aaron.es.util.EsTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @description: 索引结构基础方法实现类
 * @author: Aaron
 * @date 2019/4/2
 **/
@Component
public class ElasticsearchIndex<T> {
    Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    RestHighLevelClient client;

    /**
     * 创建索引
     * @param clazz
     * @throws Exception
     */
    public void createIndex(Class<T> clazz) throws Exception{
        MetaData metaData = EsTools.getMetaData(clazz);
        CreateIndexRequest request = new CreateIndexRequest(metaData.getIndexname());

        StringBuffer source = new StringBuffer();
        source.append("  {\n" +
                "    \""+metaData.getIndextype()+"\": {\n" +
                "      \"properties\": {\n");
        MappingData[] mappingDataList = EsTools.getMappingData(clazz);

        boolean isAutocomplete = false;
        for (int i = 0; i < mappingDataList.length; i++) {
            MappingData mappingData = mappingDataList[i];
            if(mappingData == null || mappingData.getField_name() == null){
                continue;
            }
            source.append(" \""+mappingData.getField_name()+"\": {\n");
            source.append(" \"type\": \""+mappingData.getDatatype()+"\"\n");
            if(!StringUtils.isEmpty(mappingData.getCopy_to())){
                source.append(" ,\"copy_to\": \""+mappingData.getCopy_to()+"\"\n");
            }
            if(!mappingData.isAllow_search()){
                source.append(" ,\"index\": false\n");
            }
            if(mappingData.isAutocomplete() && (mappingData.getDatatype().equals("text") || mappingData.getDatatype().equals("keyword"))){
                source.append(" ,\"analyzer\": \"autocomplete\"\n");
                source.append(" ,\"search_analyzer\": \"standard\"\n");
                isAutocomplete = true;
            }else if(mappingData.getDatatype().equals("text")){
                source.append(" ,\"analyzer\": \"" + mappingData.getAnalyzer() + "\"\n");
                source.append(" ,\"search_analyzer\": \"" + mappingData.getSearch_analyzer() + "\"\n");
            }
//            if(mappingData.isKeyword() && !mappingData.getDatatype().equals("keyword")){
//                source.append(" \n");
//                source.append(" ,\"fields\": {\n");
//                source.append(" \"keyword\": {\n");
//                source.append(" \"type\": \"keyword\",\n");
//                source.append(" \"ignore_above\": "+mappingData.getIgnore_above());
//                source.append(" }\n");
//                source.append(" }\n");
//            }else if(mappingData.isSuggest()){
//                source.append(" \n");
//                source.append(" ,\"fields\": {\n");
//                source.append(" \"suggest\": {\n");
//                source.append(" \"type\": \"completion\",\n");
//                source.append(" \"analyzer\": \""+mappingData.getAnalyzer()+"\",\n");
//                source.append(" }\n");
//                source.append(" }\n");
//            }
            if(mappingData.isKeyword() && !mappingData.getDatatype().equals("keyword") && mappingData.isSuggest()){
                source.append(" \n");
                source.append(" ,\"fields\": {\n");

                source.append(" \"keyword\": {\n");
                source.append(" \"type\": \"keyword\",\n");
                source.append(" \"ignore_above\": "+mappingData.getIgnore_above());
                source.append(" },\n");

                source.append(" \"suggest\": {\n");
                source.append(" \"type\": \"completion\",\n");
                source.append(" \"analyzer\": \""+mappingData.getAnalyzer()+"\"\n");
                source.append(" }\n");

                source.append(" }\n");
            }else if(mappingData.isKeyword() && !mappingData.getDatatype().equals("keyword") && !mappingData.isSuggest()){
                source.append(" \n");
                source.append(" ,\"fields\": {\n");
                source.append(" \"keyword\": {\n");
                source.append(" \"type\": \"keyword\",\n");
                source.append(" \"ignore_above\": "+mappingData.getIgnore_above());
                source.append(" }\n");
                source.append(" }\n");
            }else if(!mappingData.isKeyword() && mappingData.isSuggest()){
                source.append(" \n");
                source.append(" ,\"fields\": {\n");
                source.append(" \"suggest\": {\n");
                source.append(" \"type\": \"completion\",\n");
                source.append(" \"analyzer\": \""+mappingData.getAnalyzer()+"\"\n");
                source.append(" }\n");
                source.append(" }\n");
            }
            if(i == mappingDataList.length - 1){
                source.append(" }\n");
            }else{
                source.append(" },\n");
            }
        }
        source.append(" }\n");
        source.append(" }\n");
        source.append(" }\n");

        if(isAutocomplete){
            request.settings(Settings.builder()
                    .put("index.number_of_shards", metaData.getNumber_of_shards())
                    .put("index.number_of_replicas", metaData.getNumber_of_replicas())
                    .put("analysis.filter.autocomplete_filter.type","edge_ngram")
                    .put("analysis.filter.autocomplete_filter.min_gram",1)
                    .put("analysis.filter.autocomplete_filter.max_gram",20)
                    .put("analysis.analyzer.autocomplete.type","custom")
                    .put("analysis.analyzer.autocomplete.tokenizer","standard")
                    .putList("analysis.analyzer.autocomplete.filter",new String[]{"lowercase","autocomplete_filter"})
            );
        }else{
            request.settings(Settings.builder()
                    .put("index.number_of_shards", metaData.getNumber_of_shards())
                    .put("index.number_of_replicas", metaData.getNumber_of_replicas())
            );
        }

        request.mapping(metaData.getIndextype(),//类型定义
                source.toString(),//类型映射，需要的是一个JSON字符串
                XContentType.JSON);
        StringBuilder sb = new StringBuilder("索引[").append(metaData.getIndexname()).append("]");
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request);
            //返回的CreateIndexResponse允许检索有关执行的操作的信息，如下所示：
            boolean acknowledged = createIndexResponse.isAcknowledged();//指示是否所有节点都已确认请求
            if(acknowledged){
                logger.info(sb.append("创建成功！"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(sb.append("创建失败！"));
        }
    }

    /**
     * 删除索引
     * @param clazz
     * @throws Exception
     */
    public void dropIndex(Class<T> clazz) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        String indexname = metaData.getIndexname();
        DeleteIndexRequest request = new DeleteIndexRequest(indexname);
        client.indices().delete(request);
    }

    /**
     * 索引是否存在
     * @param clazz
     * @return
     * @throws Exception
     */
    public boolean exists(Class<T> clazz) throws Exception{
        MetaData metaData = EsTools.getIndexType(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        GetIndexRequest request = new GetIndexRequest();
        request.indices(indexname);
        request.types(indextype);
        boolean exists = client.indices().exists(request);
        return exists;
    }
}
