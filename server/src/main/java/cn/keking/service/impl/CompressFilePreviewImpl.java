package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.service.FileHandlerService;
import cn.keking.service.CompressFileReader;
import cn.keking.utils.KkFileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.EncryptedDocumentException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by kl on 2018/1/17.
 * Content :处理压缩包文件
 */
@Service
public class CompressFilePreviewImpl implements FilePreview {

    private final FileHandlerService fileHandlerService;
    private final CompressFileReader compressFileReader;
    private final OtherFilePreviewImpl otherFilePreview;
    private static final String Rar_PASSWORD_MSG = "password";
    private static final String FILE_DIR = ConfigConstants.getFileDir();
    public CompressFilePreviewImpl(FileHandlerService fileHandlerService, CompressFileReader compressFileReader, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.compressFileReader = compressFileReader;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        String fileName=fileAttribute.getName();
        String filePassword = fileAttribute.getFilePassword();
        boolean forceUpdatedCache=fileAttribute.forceUpdatedCache();
        String fileTree = null;
        if(forceUpdatedCache){ //如果使用了更新缓存命令 清空执行标记
            FileHandlerService.AI_CONVERT_MAP.remove(fileName, 1); //删除转换符号
        }else if(Objects.equals(fileHandlerService.listConvertedFiles().get(fileName), "error")) //判断是否转换失败
        {
            return otherFilePreview.notSupportedFile(model, fileAttribute, "文件["+fileName+"]解压失败，请联系系统管理员");
        }
        if(FileHandlerService.AI_CONVERT_MAP.get(fileName) != null)  //判断是否正在转换中
        {
            return otherFilePreview.notSupportedFile(model, fileAttribute, "文件["+fileName+"]正在转换中,请稍后刷新访问");
        }
        // 判断文件名是否存在(redis缓存读取)
        if (forceUpdatedCache || !StringUtils.hasText(fileHandlerService.getConvertedFile(fileName))  || !ConfigConstants.isCacheEnabled()) {
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            }
            String filePath = response.getContent();
            try {
                FileHandlerService.AI_CONVERT_MAP.put(fileName, 1); //加入转换符号
                fileTree = compressFileReader.unRar(filePath, filePassword,fileName);
            } catch (Exception e) {
                Throwable[] throwableArray = ExceptionUtils.getThrowables(e);
                for (Throwable throwable : throwableArray) {
                    if (throwable instanceof IOException || throwable instanceof EncryptedDocumentException) {
                        if (e.getMessage().toLowerCase().contains(Rar_PASSWORD_MSG)) {
                            FileHandlerService.AI_CONVERT_MAP.remove(fileName, 1);  //删除转换符号
                            model.addAttribute("needFilePassword", true);
                            return EXEL_FILE_PREVIEW_PAGE;
                        }
                    }
                }
            }
            FileHandlerService.AI_CONVERT_MAP.remove(fileName, 1);  //删除转换符号
            if (!ObjectUtils.isEmpty(fileTree)) {
                //是否保留压缩包源文件
                if (ConfigConstants.getDeleteSourceFile()) {
                    KkFileUtils.deleteFileByPath(filePath);
                }
                KkFileUtils.deleteDirectory(FILE_DIR+fileName + "_/__MACOSX");   //删除macOSX系统产生的垃圾文件
                if (ConfigConstants.isCacheEnabled()) {
                    // 加入缓存
                    fileHandlerService.addConvertedFile(fileName, fileTree);
                }
            }else {
                fileHandlerService.addConvertedFile(fileName, "error");  //转换错误加入缓存
                return otherFilePreview.notSupportedFile(model, fileAttribute, "压缩文件密码错误! 压缩文件损坏!  压缩文件类型不受支持!");
            }
        } else {
            fileTree = fileHandlerService.getConvertedFile(fileName);
        }
            model.addAttribute("fileName", fileName);
            model.addAttribute("fileTree", fileTree);
            return COMPRESS_FILE_PREVIEW_PAGE;
    }
}
