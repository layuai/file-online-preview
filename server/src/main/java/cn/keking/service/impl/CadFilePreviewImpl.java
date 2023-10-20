package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.KkFileUtils;
import cn.keking.web.filter.BaseUrlFilter;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static cn.keking.service.impl.OfficeFilePreviewImpl.getPreviewType;

/**
 * @author chenjh
 * @since 2019/11/21 14:28
 */
@Service
public class CadFilePreviewImpl implements FilePreview {

    private static final String OFFICE_PREVIEW_TYPE_IMAGE = "image";
    private static final String OFFICE_PREVIEW_TYPE_ALL_IMAGES = "allImages";
    private static final String FILE_DIR = ConfigConstants.getFileDir();

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
        boolean forceUpdatedCache=fileAttribute.forceUpdatedCache();
        String fileName = fileAttribute.getName();
        String suffix = fileAttribute.getSuffix();
        String cadPreviewType = ConfigConstants.getCadPreviewType();
        String pdfName = fileName.substring(0, fileName.lastIndexOf(".")) + suffix +"." + cadPreviewType ; //生成文件添加类型后缀 防止同名文件
        String outFilePath = FILE_DIR + pdfName;
        String ConvertRecords = FileHandlerService.queryRecords(forceUpdatedCache,pdfName,fileHandlerService);
        if (Objects.equals(ConvertRecords, "error"))
        { return otherFilePreview.notSupportedFile(model, fileAttribute, "文件["+fileName+"]转换失败，请联系系统管理员");
        }else if (Objects.equals(ConvertRecords, "convert"))
        { return otherFilePreview.notSupportedFile(model, fileAttribute, "文件["+fileName+"]正在转换中,请稍后刷新访问");
        }
        // 判断之前是否已转换过，如果转换过，直接返回，否则执行转换
        if (forceUpdatedCache || !fileHandlerService.listConvertedFiles().containsKey(pdfName) || !ConfigConstants.isCacheEnabled()) {
            String filePath;
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, null);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            }
            filePath = response.getContent();
            String imageUrls = null;
            if (StringUtils.hasText(outFilePath)) {
                try {
                    FileHandlerService.ConvertingMap.put(pdfName, pdfName);  //添加转换符号
                    imageUrls =  fileHandlerService.cadToPdf(filePath, outFilePath,cadPreviewType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (imageUrls == null ) {
                    return otherFilePreview.notSupportedFile(model, fileAttribute, "CAD转换异常，请联系管理员");
                }
                FileHandlerService.ConvertingMap.remove(pdfName, pdfName);  //转换成功删除缓存转换符号
                //是否保留CAD源文件
                if( ConfigConstants.getDeleteSourceFile()) {
                    KkFileUtils.deleteFileByPath(filePath);
                }
                if (ConfigConstants.isCacheEnabled()) {
                    // 加入缓存
                    fileHandlerService.addConvertedFile(pdfName, fileHandlerService.getRelativePath(outFilePath));
                }
            }
        }
        if("tif".equalsIgnoreCase(cadPreviewType)){
            model.addAttribute("currentUrl", pdfName);
            return TIFF_FILE_PREVIEW_PAGE;
        }else if("svg".equalsIgnoreCase(cadPreviewType)){
            model.addAttribute("currentUrl", pdfName);
            return SVG_FILE_PREVIEW_PAGE;
        }
        if (baseUrl != null && (OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType) || OFFICE_PREVIEW_TYPE_ALL_IMAGES.equals(officePreviewType))) {
            return getPreviewType(model, fileAttribute, officePreviewType, baseUrl, pdfName, outFilePath, fileHandlerService, OFFICE_PREVIEW_TYPE_IMAGE,otherFilePreview);
        }
        model.addAttribute("pdfUrl", pdfName);
        return PDF_FILE_PREVIEW_PAGE;
    }
}
