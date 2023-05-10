package cn.keking.utils;

import cn.keking.exception.CallBackException;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @ClassName: HttpUtils
 * @Description:
 * @Author: wangf
 * @Date: 2019/11/12 0012 14:38
 * @Version: 1.0
 **/

public class HttpUtils {

    /**
     * 发送HttpGet请求
     *
     * @param url
     * @return     */
    public static JSONObject sendGet(String url) {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        URIBuilder uriBuilder = null;
        CloseableHttpResponse response = null;
        String data = "";
        try {
            uriBuilder = new URIBuilder(url);
            response = client.execute(new HttpGet(uriBuilder.build()));
            data = EntityUtils.toString(response.getEntity());
        }catch (Exception e){
            throw new CallBackException();
        }finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return JSONObject.parseObject(data);
    }
}
