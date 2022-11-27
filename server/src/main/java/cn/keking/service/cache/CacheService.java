package cn.keking.service.cache;

import java.util.List;
import java.util.Map;

/**
 * @author: chenjh
 * @since: 2019/4/2 16:45
 */
public interface CacheService {

    String FILE_PREVIEW_PDF_KEY = "converted-preview-pdf-file";
    String FILE_PREVIEW_IMGS_KEY = "converted-preview-imgs-file";//压缩包内图片文件集合
    String FILE_PREVIEW_PDF_IMGS_KEY = "converted-preview-pdfimgs-file";
    String FILE_PREVIEW_MEDIA_CONVERT_KEY = "converted-preview-media-file";

    /**
     * 文件路径映射
     */
    String FILE_PATH_KEY = "file-path-map";
    String TASK_QUEUE_NAME = "convert-task";

    Integer DEFAULT_PDF_CAPACITY = 500000;
    Integer DEFAULT_IMG_CAPACITY = 500000;
    Integer DEFAULT_PDFIMG_CAPACITY = 500000;
    Integer DEFAULT_MEDIACONVERT_CAPACITY = 500000;

    Integer DEFAULT_FILE_PATH_CAPACITY = 500000;


    void initPDFCachePool(Integer capacity);
    void initIMGCachePool(Integer capacity);
    void initPdfImagesCachePool(Integer capacity);
    void initMediaConvertCachePool(Integer capacity);

    void initFilePathCachePool(Integer capacity);
    void putPDFCache(String key, String value);
    void putImgCache(String key, List<String> value);
    Map<String, String> getPDFCache();
    String getPDFCache(String key);
    Map<String, List<String>> getImgCache();
    List<String> getImgCache(String key);
    Integer getPdfImageCache(String key);
    void putPdfImageCache(String pdfFilePath, int num);
    Map<String, String> getMediaConvertCache();
    void putMediaConvertCache(String key, String value);
    String getMediaConvertCache(String key);

    /**
     * url对本地磁盘的cache
     * @return
     */
    Map<String, String> putFilePathCache();

    /**
     * 缓存文件key对本地磁盘 url的cache
     * @param key
     * @param value
     * @return
     */
    void putFilePathCache(String key, String value);

    /**
     * 获取本地磁盘的cache
     * @param key
     * @return
     */
    String getFilePathCache(String key);

    void cleanCache();
    void addQueueTask(String url);
    String takeQueueTask() throws InterruptedException;
}
