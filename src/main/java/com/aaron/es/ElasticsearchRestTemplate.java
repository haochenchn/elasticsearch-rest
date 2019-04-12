package com.aaron.es;

import com.aaron.es.annotation.ESMapping;
import com.aaron.es.enums.DataType;
import com.aaron.es.model.*;
import com.aaron.es.util.EsConstant;
import com.aaron.es.util.EsTools;
import com.aaron.es.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @description: Elasticsearch基础功能组件实现类 rest
 * @author: Aaron
 * @date 2019/4/2
 **/
@Component
public class ElasticsearchRestTemplate<T, M> {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    RestHighLevelClient restClient;

    /**
     * 新增document
     * @param t
     * @return
     * @throws Exception
     */
    public boolean save(T t) throws Exception {
        MetaData metaData = EsTools.getIndexType(t.getClass());
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        String id = EsTools.getESId(t);
        IndexRequest indexRequest;
        if (StringUtils.isEmpty(id)) {
            indexRequest = new IndexRequest(indexname, indextype);
        } else {
            indexRequest = new IndexRequest(indexname, indextype, id);
        }
        String source = JsonUtils.obj2String(t);
        indexRequest.source(source, XContentType.JSON);
        IndexResponse indexResponse;
        indexResponse = restClient.index(indexRequest);
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("INDEX CREATE SUCCESS");
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("INDEX UPDATE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    /**
     * 批量插入document
     * @param list
     * @return
     * @throws Exception
     */
    public BulkResponse save(List<T> list) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        T t = list.get(0);
        MetaData metaData = EsTools.getIndexType(t.getClass());
        BulkRequest rrr = new BulkRequest();
        for (int i = 0; i < list.size(); i++) {
            T tt = list.get(i);
            String id = EsTools.getESId(tt);
            //            rrr.add(new IndexRequest(indexname, indextype, id)
            //                    .source(XContentType.JSON, JsonUtils.obj2String(tt)));
            if(StringUtils.isEmpty(id)){
                rrr.add(new IndexRequest(metaData.getIndexname(), metaData.getIndextype())
                        .source(JsonUtils.obj2Map(tt)));
            }else {
                rrr.add(new IndexRequest(metaData.getIndexname(), metaData.getIndextype(), id)
                        .source(JsonUtils.obj2Map(tt)));
            }
        }
        BulkResponse bulkResponse = restClient.bulk(rrr);
        return bulkResponse;
    }

    /**
     * 更新document
     * @param t
     * @return
     * @throws Exception
     */
    public boolean update(T t) throws Exception {
        MetaData metaData = EsTools.getIndexType(t.getClass());
        String id = EsTools.getESId(t);
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        UpdateRequest updateRequest = new UpdateRequest(metaData.getIndexname(), metaData.getIndextype(), id);
        updateRequest.doc(EsTools.getFieldValue(t));
        UpdateResponse updateResponse;
        updateResponse = restClient.update(updateRequest);
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            logger.info("INDEX CREATE SUCCESS");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            logger.info("INDEX UPDATE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    /**
     * 删除document
     * @param t
     * @return
     * @throws Exception
     */
    public boolean delete(T t) throws Exception {
        MetaData metaData = EsTools.getIndexType(t.getClass());
        String id = EsTools.getESId(t);
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        DeleteRequest deleteRequest = new DeleteRequest(metaData.getIndexname(),metaData.getIndextype(), id);
        DeleteResponse deleteResponse;
        deleteResponse = restClient.delete(deleteRequest);
        if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
            logger.info("INDEX DELETE SUCCESS");
        } else {
            return false;
        }
        return true;
    }

    /**
     * 【最原始】查询
     * @param searchRequest
     * @return
     * @throws IOException
     */
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        SearchResponse searchResponse = restClient.search(searchRequest);
        return searchResponse;
    }

    /**
     * 非分页查询
     * 目前暂时传入类类型
     * @param queryBuilder
     * @param highlightBuilder
     * @param clazz
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder,HighlightBuilder highlightBuilder, Class<T> clazz) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        return search(queryBuilder,highlightBuilder,clazz,indexname);
    }

    /**
     * 非分页查询（跨索引）
     * @param queryBuilder
     * @param highlightBuilder
     * @param clazz
     * @param indexs
     * @return
     * @throws Exception
     */
    public List<T> search(QueryBuilder queryBuilder,HighlightBuilder highlightBuilder, Class<T> clazz, String... indexs) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        String indextype = metaData.getIndextype();
        List<T> list = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(EsConstant.DEFAULT_PAGE_SIZE);
        boolean highLightFlag = false;
        if(null != highlightBuilder){
            highLightFlag = true;
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        searchRequest.source(searchSourceBuilder);
        if(metaData.isPrintLog()){
            logger.info(searchSourceBuilder.toString());
        }

        SearchResponse searchResponse = restClient.search(searchRequest);
        result(searchResponse,clazz,list,highLightFlag);
        return list;
    }

    /**
     * 处理搜索结果
     * @param response
     * @param list
     * @param highLightFlag
     */
    public void result(SearchResponse response,Class<T> clazz, List list, boolean highLightFlag){
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            T t = JsonUtils.string2Obj(hit.getSourceAsString(),clazz);
            //替换高亮字段
            if (highLightFlag) {
                Map<String, HighlightField> hmap = hit.getHighlightFields();
                hmap.forEach((k, v) ->
                    {
                        try {
                            Object obj = JsonUtils.map2Obj(hmap,clazz);
                            BeanUtils.copyProperties(obj, t, EsTools.getNoValuePropertyNames(obj));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                );
            }
            list.add(t);
        }
    }

    public T getById(M id, Class<T> clazz) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        GetRequest getRequest = new GetRequest(metaData.getIndexname(), metaData.getIndextype(), id.toString());
        GetResponse getResponse = restClient.get(getRequest);
        if (getResponse.isExists()) {
            return JsonUtils.string2Obj(getResponse.getSourceAsString(), clazz);
        }
        return null;
    }

    public List<T> mgetById(M[] ids, Class<T> clazz) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        MultiGetRequest request = new MultiGetRequest();
        for (int i = 0; i < ids.length; i++) {
            request.add(new MultiGetRequest.Item(indexname, indextype, ids[i].toString()));
        }
        MultiGetResponse response = restClient.multiGet(request);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < response.getResponses().length; i++) {
            MultiGetItemResponse item = response.getResponses()[i];
            GetResponse getResponse = item.getResponse();
            if (getResponse.isExists()) {
                list.add(JsonUtils.string2Obj(getResponse.getSourceAsString(), clazz));
            }
        }
        return list;
    }

    public boolean exists(M id, Class<T> clazz) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        String indexname = metaData.getIndexname();
        String indextype = metaData.getIndextype();
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID cannot be empty");
        }
        GetRequest getRequest = new GetRequest(indexname, indextype, id.toString());
        GetResponse getResponse = restClient.get(getRequest);
        if (getResponse.isExists()) {
            return true;
        }
        return false;
    }

    private static final String keyword = ".keyword";

    /**
     * 组织字段是否带有.keyword
     *
     * @param field
     * @param name
     * @return
     */
    private String genKeyword(Field field, String name) {
        ESMapping esMapping = field.getAnnotation(ESMapping.class);
        //带着.keyword直接忽略
        if (name == null || name.indexOf(keyword) > -1) {
            return name;
        }
        //只要keyword是true就要拼接
        //没配注解，但是类型是字符串，默认keyword是true
        if (esMapping == null) {
            if (field.getType() == String.class) {
                return name + keyword;
            }
        }
        //配了注解，但是类型是字符串，默认keyword是true
        else {
            if (esMapping.datatype() == DataType.text_type && esMapping.keyword() == true) {
                return name + keyword;
            }
        }
        return name;
    }

    /**
     * 分页查询
     * @param queryBuilder
     * @param pageSortHighLight
     * @param clazz
     * @return
     * @throws Exception
     */
   public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        String[] indexname = metaData.getSearchIndexNames();
        PageList<T> pageList = new PageList<>();
        if(pageSortHighLight == null){
            throw new NullPointerException("PageSortHighLight不能为空!");
        }
        search(queryBuilder,pageSortHighLight,clazz,indexname);
        return pageList;
    }

    /**
     * 分页查询(跨索引)
     * @param queryBuilder
     * @param pageSortHighLight
     * @param clazz
     * @param indexs
     * @return
     * @throws Exception
     */
    public PageList<T> search(QueryBuilder queryBuilder, PageSortHighLight pageSortHighLight, Class<T> clazz, String... indexs) throws Exception {
        MetaData metaData = EsTools.getIndexType(clazz);
        PageList<T> pageList = new PageList<>();
        List<T> list = new ArrayList<>();
        if(pageSortHighLight == null){
            throw new NullPointerException("PageSortHighLight不能为空!");
        }
        SearchRequest searchRequest = new SearchRequest(indexs);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        //分页
        searchSourceBuilder.from((pageSortHighLight.getCurrentPage() - 1) * pageSortHighLight.getPageSize());
        searchSourceBuilder.size(pageSortHighLight.getPageSize());
        //排序
        Sort sort = pageSortHighLight.getSort();
        List<Sort.Order> orders = sort.listOrders();
        orders.forEach(order ->
                searchSourceBuilder.sort(new FieldSortBuilder(order.getProperty()).order(order.getDirection()))
        );
        //高亮
        HighLight highLight = pageSortHighLight.getHighLight();
        boolean highLightFlag = false;
        if (highLight.getHighLightList() != null && highLight.getHighLightList().size() != 0) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            if (!StringUtils.isEmpty(highLight.getPreTag()) && !StringUtils.isEmpty(highLight.getPostTag())) {
                highlightBuilder.preTags(highLight.getPreTag());
                highlightBuilder.postTags(highLight.getPostTag());
            }
            for (int i = 0; i < highLight.getHighLightList().size(); i++) {
                highLightFlag = true;
                HighlightBuilder.Field highlightField = new HighlightBuilder.Field(highLight.getHighLightList().get(i));
                highlightBuilder.field(highlightField);
            }
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        searchRequest.source(searchSourceBuilder);
        if(metaData.isPrintLog()){
            logger.info(searchSourceBuilder.toString());
        }
        SearchResponse searchResponse = restClient.search(searchRequest);
        result(searchResponse,clazz,list,highLightFlag);

        pageList.setList(list);
        pageList.setTotalElements(searchResponse.getHits().totalHits);
        pageList.setTotalPages(getTotalPages(searchResponse.getHits().totalHits, pageSortHighLight.getPageSize()));
        return pageList;
    }

    private int getTotalPages(long totalHits, int pageSize) {
        return pageSize == 0 ? 1 : (int) Math.ceil((double) totalHits / (double) pageSize);
    }


}
