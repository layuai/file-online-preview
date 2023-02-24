package cn.keking.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MD5Utils.class);

    private static final String MD5_PWD = "a!sd54&1#2h@";
    private static final String FINAL_MD5 = "MD5";

    public static String md5(String info) {
        if (!StringUtils.hasText(info)) {
            return null;
        }
        info = MD5_PWD.concat(info);
        try {
            MessageDigest md5 = MessageDigest.getInstance(FINAL_MD5);
            md5.update(info.getBytes(StandardCharsets.UTF_8));
            byte[] md5Array = md5.digest();
            return bytesToHex(md5Array);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("MD5加密异常", e);
            return null;
        }
    }

    private static String bytesToHex(byte[] md5Array) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte aMd5Array : md5Array) {
            int temp = 0xff & aMd5Array;
            String hexString = Integer.toHexString(temp);
            if (hexString.length() == 1) {
                strBuilder.append("0").append(hexString);
            } else {
                strBuilder.append(hexString);
            }
        }
        return strBuilder.toString();
    }

}
