package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.service.FileHandlerService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.EncryptedDocumentException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;

/**
 * Created by kl on 2018/1/17.
 * Content :处理pdf文件
 */
@Service
public class PdfFilePreviewImpl implements FilePreview {

    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;
    private static final String FILE_DIR = ConfigConstants.getFileDir();
    private static final String PDF_PASSWORD_MSG = "password";

    public PdfFilePreviewImpl(FileHandlerService fileHandlerService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        String fileName = fileAttribute.getName();
        String officePreviewType = fileAttribute.getOfficePreviewType();
        boolean forceUpdatedCache=fileAttribute.forceUpdatedCache();
        String pdfName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + "pdf";
        String outFilePath = FILE_DIR + pdfName;
        String ConvertRecords = FileHandlerService.queryRecords(forceUpdatedCache,pdfName,fileHandlerService);
        if (Objects.equals(ConvertRecords, "error"))
        { return otherFilePreview.notSupportedFile(model, fileAttribute, "文件["+fileName+"]转换失败，请联系系统管理员");
        }else if (Objects.equals(ConvertRecords, "convert"))
        { return otherFilePreview.notSupportedFile(model, fileAttribute, "文件["+fileName+"]正在转换中,请稍后刷新访问");
        }
        if (OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType) || OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_ALL_IMAGES.equals(officePreviewType)) {
            //当文件不存在时，就去下载
            if (forceUpdatedCache || !fileHandlerService.listConvertedFiles().containsKey(pdfName) || !ConfigConstants.isCacheEnabled()) {
                ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
                if (response.isFailure()) {
                    return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
                }
                outFilePath = response.getContent();
                if (ConfigConstants.isCacheEnabled()) {
                    // 加入缓存
                    fileHandlerService.addConvertedFile(pdfName, fileHandlerService.getRelativePath(outFilePath));
                }
            }
            List<String> imageUrls = null;
            try {
                FileHandlerService.ConvertingMap.put(pdfName, pdfName);  //添加转换符号
                imageUrls = fileHandlerService.pdf2jpg(outFilePath, pdfName, fileAttribute);
            } catch (Exception e) {
                Throwable[] throwableArray = ExceptionUtils.getThrowables(e);
                for (Throwable throwable : throwableArray) {
                    if (throwable instanceof IOException || throwable instanceof EncryptedDocumentException) {
                        if (e.getMessage().toLowerCase().contains(PDF_PASSWORD_MSG)) {
                            FileHandlerService.ConvertingMap.remove(pdfName, pdfName);  //加密文件删除缓存转换符号
                            model.addAttribute("needFilePassword", true);
                            return EXEL_FILE_PREVIEW_PAGE;
                        }
                    }
                }
            }
            if (imageUrls == null || imageUrls.size() < 1) {
                fileHandlerService.addConvertedFile(pdfName, "error");  //失败加入缓存
                return otherFilePreview.notSupportedFile(model, fileAttribute, "pdf转图片异常，请联系管理员");
            }
            FileHandlerService.ConvertingMap.remove(pdfName, pdfName);  //转换成功删除缓存转换符号
            model.addAttribute("imgUrls", imageUrls);
            model.addAttribute("currentUrl", imageUrls.get(0));
            if (OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType)) {
                return OFFICE_PICTURE_FILE_PREVIEW_PAGE;
            } else {
                return PICTURE_FILE_PREVIEW_PAGE;
            }
        } else {
            // 不是http开头，浏览器不能直接访问，需下载到本地
            if (url != null && !url.toLowerCase().startsWith("http")) {
                if (!fileHandlerService.listConvertedFiles().containsKey(pdfName) || !ConfigConstants.isCacheEnabled()) {
                    ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, pdfName);
                    if (response.isFailure()) {
                        return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
                    }
                    model.addAttribute("pdfUrl", fileHandlerService.getRelativePath(response.getContent()));
                    if (ConfigConstants.isCacheEnabled()) {
                        // 加入缓存
                        fileHandlerService.addConvertedFile(pdfName, fileHandlerService.getRelativePath(outFilePath));
                    }
                } else {
                    pdfName =   URLEncoder.encode(pdfName).replaceAll("\\+", "%20");
                    model.addAttribute("pdfUrl", pdfName);
                }
            } else {
                model.addAttribute("pdfUrl", url);
            }
        }
        return PDF_FILE_PREVIEW_PAGE;
    }
}
