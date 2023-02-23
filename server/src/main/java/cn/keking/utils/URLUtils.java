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

}
