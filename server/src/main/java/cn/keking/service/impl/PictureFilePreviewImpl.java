package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.KkFileUtils;
import cn.keking.utils.MD5Utils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kl on 2018/1/17.
 * Content :图片文件处理
 */
@Service
public class PictureFilePreviewImpl implements FilePreview {

    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;

    public PictureFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        url = KkFileUtils.htmlEscape(url);
        List<Map<String, String>> imgUrls = new ArrayList<>();
//        List<String> imgUrls = new ArrayList<>();
        final String curId = MD5Utils.md5(url);
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", url);
        urlMap.put("id", curId);
        imgUrls.add(urlMap);
//        imgUrls.add(url);
        String fileKey = fileAttribute.getFileKey();
        List<String> zipImgUrls = fileHandlerService.getImgCache(fileKey);
        if (!CollectionUtils.isEmpty(zipImgUrls)) {
            for (String zipImgUrl : zipImgUrls) {
                Map<String, String> tUrlMap = new HashMap<>();
                tUrlMap.put("url", zipImgUrl);
                tUrlMap.put("id", MD5Utils.md5(zipImgUrl));
                imgUrls.add(tUrlMap);
            }
//            imgUrls.addAll(zipImgUrls);
        }
        // 不是http开头，浏览器不能直接访问，需下载到本地
        if (url != null && !url.toLowerCase().startsWith("http")) {
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, null);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            } else {
                String file = fileHandlerService.getRelativePath(response.getContent());
                imgUrls.clear();
                final String id = MD5Utils.md5(file);
                Map<String, String> tUrlMap = new HashMap<>();
                tUrlMap.put("url", file);
                tUrlMap.put("id", id);
                imgUrls.add(tUrlMap);
//                imgUrls.add(file);
                model.addAttribute("imgUrls", imgUrls);
                model.addAttribute("currentUrl", id);
            }
        } else {
            model.addAttribute("imgUrls", imgUrls);
            model.addAttribute("currentUrl", curId);
        }
        return PICTURE_FILE_PREVIEW_PAGE;
    }
}
