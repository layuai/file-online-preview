package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.KkFileUtils;
import cn.keking.utils.WebUtils;
import cn.keking.web.filter.BaseUrlFilter;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import static cn.keking.service.impl.OfficeFilePreviewImpl.getPreviewType;

/**
 * @author chenjh
 * @since 2019/11/21 14:28
 */
@Service
public class CadFilePreviewImpl implements FilePreview {

    private static final String OFFICE_PREVIEW_TYPE_IMAGE = "image";
    private static final String OFFICE_PREVIEW_TYPE_ALL_IMAGES = "allImages";

    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;

    public CadFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        // 预览Type，参数传了就取参数的，没传取系统默认
        String officePreviewType = fileAttribute.getOfficePreviewType() == null ? ConfigConstants.getOfficePreviewType() : fileAttribute.getOfficePreviewType();
        String baseUrl = BaseUrlFilter.getBaseUrl();
        boolean forceUpdatedCache = fileAttribute.forceUpdatedCache();
        String fileName = fileAttribute.getName();
        String cadPreviewType = ConfigConstants.getCadPreviewType();
        String cacheName = fileAttribute.getCacheName();
        String outFilePath = fileAttribute.getOutFilePath();
        // 判断之前是否已转换过，如果转换过，直接返回，否则执行转换
        if (forceUpdatedCache || !fileHandlerService.listConvertedFiles().containsKey(cacheName) || !ConfigConstants.isCacheEnabled()) {
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            }
            String filePath = response.getContent();
            if (StringUtils.hasText(outFilePath)) {
                try {
                    FileHandlerService.putConvertingMap(cacheName, cacheName);  //添加转换符号
                    fileHandlerService.cadToPdf(filePath, outFilePath, cadPreviewType, fileAttribute);
                } catch (Exception e) {
                    FileHandlerService.removeConvertingMap(cacheName, cacheName);  // 删除转换标记
                    if (e.getMessage().contains("overtime")) {
                        System.out.println("CAD转换超时:"+cacheName);
                        fileHandlerService.addConvertedFile(cacheName, "timeout");  //转换超时错误加入缓存
                        return otherFilePreview.notSupportedFile(model, fileAttribute, "CAD转换超时异常，请联系管理员");
                    }
                    e.printStackTrace();
                    fileHandlerService.addConvertedFile(cacheName, "error");  //失败加入缓存
                    return otherFilePreview.notSupportedFile(model, fileAttribute, "CAD转换异常，请联系管理员");
                }
                FileHandlerService.removeConvertingMap(fileName, fileName);  //转换成功删除缓存转换符号
                //是否保留CAD源文件
                if (!fileAttribute.isCompressFile() && ConfigConstants.getDeleteSourceFile()) {
                    KkFileUtils.deleteFileByPath(filePath);
                }
                if (ConfigConstants.isCacheEnabled()) {
                    // 加入缓存
                    fileHandlerService.addConvertedFile(cacheName, fileHandlerService.getRelativePath(outFilePath));
                }
            }
        }
        cacheName=  WebUtils.encodeFileName(cacheName);
        if ("tif".equalsIgnoreCase(cadPreviewType)) {
            model.addAttribute("currentUrl", cacheName);
            return TIFF_FILE_PREVIEW_PAGE;
        } else if ("svg".equalsIgnoreCase(cadPreviewType)) {
            model.addAttribute("currentUrl", cacheName);
            return SVG_FILE_PREVIEW_PAGE;
        }
        if (baseUrl != null && (OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType) || OFFICE_PREVIEW_TYPE_ALL_IMAGES.equals(officePreviewType))) {
            return getPreviewType(model, fileAttribute, officePreviewType, cacheName, outFilePath, fileHandlerService, OFFICE_PREVIEW_TYPE_IMAGE, otherFilePreview);
        }
        model.addAttribute("pdfUrl", cacheName);
        return PDF_FILE_PREVIEW_PAGE;
    }
}
