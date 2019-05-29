package com.aaron.es;

import com.aaron.es.model.EsQueryResult;
import com.aaron.es.model.EsQueryVo;
import com.aaron.es.util.JsonUtils;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Aaron
 * @version 1.0
 * @description Elasticsearch查询，需要进行es查询时注入该类即可
 * @date 2019/4/2
 */
@Component
public class ElasticsearchTemplate {
    Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private TransportClient client;

    /**
     * 查询
     * @param queryVo
     * @return
     * @throws Exception
     */
    public EsQueryResult query(EsQueryVo queryVo) throws Exception {
        String[] terms = getTerms(queryVo.getKeyword());
        if(terms==null){
            return null;
        }
        HighlightBuilder hlb = new HighlightBuilder();
        hlb.preTags("<font style=\"color:red\">");
        hlb.postTags("</font>");
        // must
        BoolQueryBuilder bqbMust = QueryBuilders.boolQuery();
        for(String term:terms){

            bqbMust.must(QueryBuilders.matchPhraseQuery("FULL_TEXT", term));
//                bqbMust.must(QueryBuilders.wildcardQuery("FULL_TEXT", "*"+term+"*"));
//              bqbMust.must(QueryBuilders.multiMatchQuery(term));
        }
        SortBuilder sortBuilder = null;
        if(!StringUtils.isEmpty(queryVo.getSortField())){
            sortBuilder = SortBuilders.fieldSort(queryVo.getSortField());
            sortBuilder.order(queryVo.isDesc() ? SortOrder.DESC : SortOrder.ASC);
        }
        String [] index = splitIndices(queryVo.getIndices().trim());
        SearchResponse searchResponse = search(bqbMust, hlb,sortBuilder, queryVo.getPageNum(), queryVo.getPageSize(), index);
        StringBuilder sb = new StringBuilder();
        sb.append(queryVo.toString()).append(",共找到[").append(searchResponse.getHits().getTotalHits()).append("]条。");
        logger.info(sb);
        return translate2QueryResult(searchResponse,false);
    }

    /**
     * 查询（不分页）
     * @param queryBuilder
     * @param highlightBuilder
     * @param index 索引
     * @return SearchResponse
     */
    public SearchResponse search(QueryBuilder queryBuilder, HighlightBuilder highlightBuilder,SortBuilder sortBuilder,String ... index){
        return search(queryBuilder,highlightBuilder,sortBuilder,0,0,index);
    }

    /**
     * 通用查询
     * @param queryBuilder
     * @param highlightBuilder
     * @param index 索引
     * @param pageNum 页码
     * @param pageSize 每页显示数
     * @return SearchResponse
     */
    public SearchResponse search(QueryBuilder queryBuilder, HighlightBuilder highlightBuilder, SortBuilder sortBuilder, int pageNum, int pageSize, String ... index){
        if(null == queryBuilder){
            return null;
        }
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
        //排序
        if(null != sortBuilder){
            searchRequestBuilder.addSort(sortBuilder);
        }

        //如果分页，设置分页参数
        if(pageSize > 0){
            searchRequestBuilder.setFrom(getFromValue(pageNum, pageSize))
                    .setSize(pageSize);
        }
        //如果高亮
        if(null != highlightBuilder){
            searchRequestBuilder.highlighter(highlightBuilder);
        }
        SearchResponse searchResponse = searchRequestBuilder
                .setQuery(queryBuilder)
                .get();
        return searchResponse;
    }

    //根据词条生成BoolQueryBuilder
    protected BoolQueryBuilder getBoolQueryBuilderForTerms(String[] terms){
        if(terms == null || terms.length == 0){
            return null;
        }
        BoolQueryBuilder bqbMust = QueryBuilders.boolQuery();
        for(String term:terms){
            bqbMust.must(QueryBuilders.matchPhraseQuery("FULL_TEXT", term));
//          bqbMust.must(QueryBuilders.multiMatchQuery(term));

        }
        return bqbMust;
    }
    private SearchRequestBuilder getSearchRequestBuilderForTerms(TransportClient client, String indice, String[] terms) throws Exception {
        // must
        BoolQueryBuilder bqbMust = getBoolQueryBuilderForTerms(terms);
        SearchRequestBuilder sr = client
                .prepareSearch(indice)
                .setSource(new SearchSourceBuilder()
                        .size(0)
                        .query(QueryBuilders
                                .boolQuery()
                                .must(bqbMust)));
        return sr;
    }

    public EsQueryResult queryByQueryString(EsQueryVo queryVo) throws Exception {
        String[] terms = getTerms(queryVo.getKeyword());
        if(terms==null){
            return null;
        }
        // must
//        BoolQueryBuilder bqbMust = getBoolQueryBuilderForTerms(terms);
        String queryString = queryStringbyMultiTerms(terms);
        SearchResponse searchResponse=client
                .prepareSearch(splitIndices(queryVo.getIndices()))
                .setSource(new SearchSourceBuilder().query(QueryBuilders.
                        queryStringQuery(queryString))).setFrom(getFromValue(queryVo.getPageNum(),queryVo.getPageSize())).setSize(queryVo.getPageSize()).get();
        StringBuilder sb = new StringBuilder();
        sb.append(",共找到[").append(searchResponse.getHits().getTotalHits()).append("]条。");
        logger.info(sb);
        return translate2QueryResult(searchResponse,false);
    }

    //queryString中多个token查询所使用的字符串形式 eg:"+中国 +武汉市 +洪山区"
    //用于模糊查询
    public String queryStringbyMultiTerms(String[] terms){

        StringBuffer sbf=new StringBuffer();

        for(int i=0;i<terms.length;i++){
            if(i!=terms.length-1){
                sbf.append("+"+terms[i]+" ");
            }else{
                sbf.append("+"+terms[i]+" ");
            }
        }
        String queryString=sbf.toString();

        return queryString;
    }


    private MultiSearchRequestBuilder getQueryStringReqBulder(TransportClient client, EsQueryVo queryVo) throws Exception{
        String[] terms = getTerms(queryVo.getKeyword());
        if(terms==null){
            return null;
        }
        String queryString=queryStringbyMultiTerms(terms);
        queryVo.setIndices("");
        String[] indices =splitIndices(queryVo.getIndices());
        MultiSearchRequestBuilder msrb=client.prepareMultiSearch();

        for(String indice:indices){
            SearchRequestBuilder srb=client
                    .prepareSearch(indice)
                    .setSource(new SearchSourceBuilder().size(0).query(QueryBuilders.
                            queryStringQuery(queryString)));
            msrb.add(srb);
        }
        return msrb;
    }

    /**
     * 获取文本的term搜索数组
     */
    protected String[] getTerms(String searchText)throws Exception{ if(StringUtils.isEmpty(searchText)){
            return null;
        }
        //将多个分隔符空格替换为单个空格
        Pattern p=Pattern.compile("\\s+");
        Matcher m=p.matcher(searchText);
        searchText=m.replaceAll(" ");
        String[] terms=searchText.split(" ");
        return terms;
    }
    protected String[] splitIndices(String indicesStr) throws Exception{
        if(StringUtils.isEmpty(indicesStr)){
            return null;
        }
        String[] indices= indicesStr.split(",");
        return indices;
    }
    /**
     * 获取当前的from值
     */
    protected int getFromValue(int pageNum, int pageSize){
        int from = ((pageNum)-1)*pageSize;
        return from < 0 ? 0 : from;
    }

    /**
     * 将es返回的SearchResponse转成平台通用的EsQueryResult对象
     * @param searchResponse
     * @param justId
     * @return
     */
    public EsQueryResult translate2QueryResult(SearchResponse searchResponse, boolean justId){
        long timeTook = searchResponse.getTook().getMillis();
        SearchHits searchHits = searchResponse.getHits();
        long allHitCnt = searchHits.getTotalHits();
        SearchHit[] searchHitIns = searchHits.getHits();

        List<Pair<String, String>> records = new ArrayList<>();
        if (searchHitIns != null && searchHitIns.length > 0) {
            for (SearchHit hit : searchHitIns) {
                String id = hit.getId();
                String value = null;
                if (!justId) {
                    Map<String, Object> tmp = processNumber(hit.getSourceAsMap());
                    value = JsonUtils.obj2String(tmp);
                }
                records.add(new Pair<>(id, value));
            }
        }
        return EsQueryResult.createSuccessResult(timeTook, allHitCnt, records);
    }

    /**
     * 解决number经过gson处理后变成科学计数法的临时方案,暂时用不上
     * @param src
     * @return
     */
    private static Map<String, Object> processNumber(Map<String, Object> src) {
        if (src == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>(src.size());
        for (Map.Entry<String, Object> entry : src.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer || value instanceof Long) {
                String valueStr = String.valueOf(value);
                result.put(key, valueStr);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
}







