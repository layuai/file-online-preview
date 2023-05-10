package cn.keking.service;

import cn.keking.exception.CallBackException;
import cn.keking.model.FileAttribute;
import cn.keking.model.FileType;
import cn.keking.service.cache.CacheService;
import cn.keking.utils.HttpUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.ExtendedModelMap;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by kl on 2018/1/19.
 * Content :消费队列中的转换文件
 */
@Service
public class FileConvertQueueTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FilePreviewFactory previewFactory;
    private final CacheService cacheService;
    private final FileHandlerService fileHandlerService;

    public FileConvertQueueTask(FilePreviewFactory previewFactory, CacheService cacheService, FileHandlerService fileHandlerService) {
        this.previewFactory = previewFactory;
        this.cacheService = cacheService;
        this.fileHandlerService = fileHandlerService;
    }

    @PostConstruct
    public void startTask() {
        new Thread(new ConvertTask(previewFactory, cacheService, fileHandlerService))
                .start();
        logger.info("队列处理文件转换任务启动完成 ");
    }

    static class ConvertTask implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(ConvertTask.class);
        private final FilePreviewFactory previewFactory;
        private final CacheService cacheService;
        private final FileHandlerService fileHandlerService;

        public ConvertTask(FilePreviewFactory previewFactory,
                           CacheService cacheService,
                           FileHandlerService fileHandlerService) {
            this.previewFactory = previewFactory;
            this.cacheService = cacheService;
            this.fileHandlerService = fileHandlerService;
        }

        @Override
        public void run() {
            while (true) {
                String url = null;
                try {
                    url = cacheService.takeQueueTask();
                    if (url != null) {
                        String callbackUrl = null;
                        if (url.contains("callbackUrl")) {
                            callbackUrl = url.split("callbackUrl")[1];
                            url = url.split("callbackUrl")[0];
                        }
                        FileAttribute fileAttribute = fileHandlerService.getFileAttribute(url, null);
                        FileType fileType = fileAttribute.getType();
                        logger.info("正在处理预览转换任务，url：{}，预览类型：{}", url, fileType);
                        if (isNeedConvert(fileType)) {
                            FilePreview filePreview = previewFactory.get(fileAttribute);
                            filePreview.filePreviewHandle(url, new ExtendedModelMap(), fileAttribute);
                        } else {
                            logger.info("预览类型无需处理，url：{}，预览类型：{}", url, fileType);
                        }
                        String finalCallbackUrl = callbackUrl;
                        new Thread(() -> callback(finalCallbackUrl)).start();
                    }
                } catch (Exception e) {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception ex) {
                        Thread.currentThread().interrupt();
                        ex.printStackTrace();
                    }
                    logger.info("处理预览转换任务异常，url：{}", url, e);
                }
            }
        }

        private void callback(String callbackUrl) {
            if (null != callbackUrl && !callbackUrl.equals("")) {
                //http回调通知
                try {
                    JSONObject jsonObject = HttpUtils.sendGet(callbackUrl);
                    if (null != jsonObject && jsonObject.getInteger("code") == 200) {
                        logger.info("成功回调通知地址：" + callbackUrl);
                    } else {
                        logger.error("失败回调通知地址：" + callbackUrl);
                    }
                } catch (CallBackException e) {
                    try {
                        Thread.sleep(1000 * 10);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    JSONObject jsonObject1 = HttpUtils.sendGet(callbackUrl);
                    if (null != jsonObject1 && jsonObject1.getInteger("code") == 200) {
                        logger.info("成功回调通知地址：" + callbackUrl);
                    } else {
                        logger.error("失败回调通知地址：" + callbackUrl);
                    }
                }
            }
        }

        public boolean isNeedConvert(FileType fileType) {
            return fileType.equals(FileType.COMPRESS) || fileType.equals(FileType.PDF) || fileType.equals(FileType.OFFICE) || fileType.equals(FileType.CAD);
        }
    }

}
