package cn.keking.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLUtils {

    private static final Logger logger = LoggerFactory.getLogger(URLUtils.class);

    /**
     * 判断 URL 是否编码
     */
    public static boolean isEncoded(String url, String enc) {
        String decodedUrl = null;
        try {
            decodedUrl = URLDecoder.decode(url, enc);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return !decodedUrl.equals(url);
    }

    public static String encode(String s, String enc) {
        try {
            if (!isEncoded(s, enc)) {
                s = URLEncoder.encode(s, enc);
            }
        } catch (Exception e) {
            logger.error("URLUtils中encode异常", e);
        }
        return s;
    }

//    public static void main(String[] args) {
//        String ss = "https://devminio.haoweiyun.com.cn/authorized-access/cwc/saas/test/merge/15891c25-9cb7-4e9f-97ec-37074f7e5a61_bz11.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&amp;X-Amz-Credential=hI6SjDDA8OFQmbK7%2F20230224%2Fus-east-1%2Fs3%2Faws4_request&amp;X-Amz-Date=20230224T060031Z&amp;X-Amz-Expires=300&amp;X-Amz-SignedHeaders=host&amp;X-Amz-Signature=930b2ddef4cb535b90be36ffad8248688e14d340053941f4bac6498c798bde61";
//        System.out.println(decode(ss, "utf-8"));
//    }

    public static String decode(String s, String enc) {
        try {
            String decodedStr = null;
            try {
                decodedStr = URLDecoder.decode(s, enc);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return isEncoded(decodedStr, enc) ? decode(decodedStr, enc) : decodedStr;
        } catch (Exception e) {
            logger.error("URLUtils中encode异常", e);
        }
        return s;
    }

}
