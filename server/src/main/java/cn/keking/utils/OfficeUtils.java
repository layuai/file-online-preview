package cn.keking.utils;

import org.apache.tika.Tika;
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Office工具类
 *
 * @author ylyue
 * @since 2022/7/5
 */
public class OfficeUtils {

    /**
     * office docx pptx xlsx 设置密码保护被检测到的类型
     */
    public static final String MIME_X_TIKA_OOXML_PROTECTED = "application/x-tika-ooxml-protected";
    /**
     * wps docx pptx xlsx 设置密码保护被检测到的类型
     */
    public static final String MIME_X_TIKA_MSOFFICE = "application/x-tika-msoffice";

    /**
     * office/wps doc
     */
    public static final String MIME_MSWORD = "application/msword";
    /**
     * office/wps ppt
     */
    public static final String MIME_MSPOWERPOINT = "application/vnd.ms-powerpoint";
    /**
     * office/wps xls
     */
    public static final String MIME_MSEXCEL = "application/vnd.ms-excel";
    /**
     * office/wps docx
     */
    public static final String MIME_OFFICE_DOCUMENT_WORD = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    /**
     * office/wps pptx
     */
    public static final String MIME_OFFICE_DOCUMENT_POWERPOINT = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    /**
     * office/wps xlsx
     */
    public static final String MIME_OFFICE_DOCUMENT_SHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * doc ppt xls
     */
    public static final List<String> MS_OLD_MIMETYPES = Arrays.asList(MIME_MSWORD, MIME_MSEXCEL, MIME_MSPOWERPOINT);

    /**
     * 判断office（word,excel,ppt）文件是否受密码保护
     *
     * @param path office文件路径
     * @return 是否受密码保护
     */
    public static boolean isPwdProtected(String path) {
        try (TikaInputStream tikaInputStream = TikaInputStream.get(new FileInputStream(path))) {
            String detect = new Tika().detect(tikaInputStream);
            if (MIME_X_TIKA_OOXML_PROTECTED.equals(detect) || MIME_X_TIKA_MSOFFICE.equals(detect)) {
                return true;
            } else if (MS_OLD_MIMETYPES.contains(detect)) {
                Metadata metadata = new Metadata();
                ContentHandler handler = new DefaultHandler();
                ParseContext context = new ParseContext();
                try {
                    new AutoDetectParser().parse(tikaInputStream, handler, metadata, context);
                    // wps docx 加密
//                    if (metadata.names().length <= 2 && handler.toString().length() == 0) {
//                        if (MIME_X_TIKA_MSOFFICE.equals(metadata.get(Metadata.CONTENT_TYPE))) {
//                            return true;
//                        }
//                    }
                } catch (TikaException e) {
                    // doc 加密保护
                    if (e instanceof EncryptedDocumentException) {
                        return true;
                    }

                    // office docx 加密保护
                    if (e.getCause() instanceof org.apache.poi.EncryptedDocumentException) {
                        return true;
                    }

                    return true;
                }

                return false;
            }
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }

        return false;
    }

}
