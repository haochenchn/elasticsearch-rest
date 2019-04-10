package com.aaron.system.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @title HttpUtils.java  
 * @description 远程访问HTTP服务工具类
 * @company 武汉虹旭信息技术有限责任公司
 * @author mjw    
 */
public class HttpUtils {    
    /**
     * @title doPost  
     * @description post请求访问连接
     * <p>访问方法说明</p>
     * <ul>
     * <li>line51.post请求url，与get请求不同的是不需要带参数</li>
     * <li>line52.打开连接</li>
     * <li>line53.默认是get方式，这里显式修改为post方式</li>
     * <li>line54.设置是否向connection输出，因为post请求参数要放在http正文内，设置为true</li>
     * <li>line55.从连接中读数据，默认为true</li>
     * <li>line56.post请求不能使用缓存</li>
     * <li>line57.正文是urlencoded编码过的form参数（如果value有特殊符号或者中文，需要转换一下</li>
     * <li>line58.开启连接，所有设置需要在连接之前完成</li>
     * <li>line59.写参数</li>
     * <li>line71.读返回值</li>
     * </ul>
     * @param @param URL
     * @param @param parameters
     * @param @return 
     * @return String 
     * @throws
     */
    @SuppressWarnings("unchecked")
    public static String doPost(String URL, String params){
        BufferedReader br = null;
        DataOutputStream dos = null;
        String result = "";
        try {
            URL url = new URL(URL);
            HttpURLConnection httpConnect = (HttpURLConnection)url.openConnection();
            httpConnect.setRequestMethod("POST");
            httpConnect.setDoOutput(true);
            httpConnect.setDoInput(true);
            httpConnect.setUseCaches(false);
            httpConnect.setRequestProperty("ContentType", "application/x-www-form-urlencoded");
            //httpConnect.setRequestProperty("ContentType", "utf-8");
            httpConnect.connect();
            dos = new DataOutputStream(httpConnect.getOutputStream());
            //dos.writeBytes("jsonStr=" + params);
            dos.write(("json=" + URLEncoder.encode(params, "utf-8")).getBytes());
            dos.flush();
            br = new BufferedReader(new InputStreamReader(httpConnect.getInputStream(), "UTF-8"));
            String str = null;
            while((str = br.readLine()) != null){
               result += str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if (br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dos != null){
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}




