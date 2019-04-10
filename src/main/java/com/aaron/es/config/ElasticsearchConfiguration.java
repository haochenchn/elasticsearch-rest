package com.aaron.es.config;

import com.aaron.es.util.EsConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.InetAddress;

/**
 * @author Aaron
 * @version 1.0
 * @description es配置类
 * @date 2019/4/2
 */
@Configuration
@ComponentScan("com.whhx.oneKeySearch")
public class ElasticsearchConfiguration {
    private Logger logger = com.whhx.system.utils.log.Logger.getLogger(this.getClass());

    //tcp通信
    @Value("${es.tcp.hosts}")
    private String tcpHosts;
    //http通信
    @Value("${es.http.hosts}")
    private String httpHosts;
    //集群名称
    @Value("${es.cluster.name:hxctproduct}")
    private String clusterName;
    //是否开启集群嗅探功能
    @Value("${es.sniff:false}")
    private Boolean clientsniff;


    @Bean(destroyMethod = "close")
    @Scope("singleton")
    public TransportClient client(){
        TransportClient client;
        try {
            if(StringUtils.isEmpty(tcpHosts)){
                tcpHosts = EsConstant.DEFAULT_ES_HOST;
            }
            String[] hostArr=splitIpList(tcpHosts);
            //使用ElasticSearch需要的配置参数
            Settings settings = Settings.builder()
                    .put("cluster.name", clusterName)
                    .put("client.transport.sniff", clientsniff)
                    .build();
            client = new PreBuiltTransportClient(settings);
            for(String host:hostArr){
                client.addTransportAddress(
                        new TransportAddress(InetAddress.getByName(host.split(":")[0]), Integer.parseInt(host.split(":")[1]))
                );
            }
            logger.info("************* Elasticsearch [TransportClient] 初始化成功 *************");
            return client;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Elasticsearch [TransportClient] 初始化失败！"+e.getMessage());
            return null;
        }

    }

    @Bean(destroyMethod="close")//这个close是调用RestHighLevelClient中的close
    @Scope("singleton")
    public RestHighLevelClient restClient() {
        RestHighLevelClient restClient;
        try {
            if(org.springframework.util.StringUtils.isEmpty(httpHosts)){
                httpHosts = EsConstant.DEFAULT_ES_HOST;
            }
            String[] hosts = splitIpList(httpHosts);
            HttpHost[] httpHosts = new HttpHost[hosts.length];
            for (int i = 0; i < httpHosts.length; i++) {
                String h = hosts[i];
                httpHosts[i] = new HttpHost(h.split(":")[0], Integer.parseInt(h.split(":")[1]), "http");
            }
            restClient = new RestHighLevelClient(
                    RestClient.builder(httpHosts));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Elasticsearch [RestHighLevelClient] 初始化失败！"+e.getMessage());
            return null;
        }
        logger.info("************* Elasticsearch [RestHighLevelClient] 初始化成功 *************");
        return restClient;
    }


    /**
     * 将IP列表切割
     * @param IpList
     * @return
     * @throws Exception
     */
    public String[] splitIpList(String IpList) throws Exception{

        if(IpList==null||"".equals(IpList)){
            throw new Exception("节点地址列表不能为空,请在配置文件中配置地址列表。例如 IpList:156.18.65.49:9820;156.18.65.49:9820");
        }
        if(!IpList.contains(";")){
            return new String[]{IpList};
        }
        return IpList.split(";");
    }

}
