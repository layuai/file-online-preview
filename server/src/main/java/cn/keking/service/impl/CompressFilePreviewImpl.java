package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FilePreview;
import cn.keking.service.ZtreeNodeVo;
import cn.keking.utils.DownloadUtils;
import cn.keking.service.FileHandlerService;
import cn.keking.service.CompressFileReader;
import cn.keking.utils.KkFileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.EncryptedDocumentException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by kl on 2018/1/17.
 * Content :处理压缩包文件
 */
@Service
public class CompressFilePreviewImpl implements FilePreview {

    private static FileHandlerService fileHandlerService = null;
    private final CompressFileReader compressFileReader;
    private final OtherFilePreviewImpl otherFilePreview;
    private static final String Rar_PASSWORD_MSG = "password";
    private static final String fileDir = ConfigConstants.getFileDir();
    public CompressFilePreviewImpl(FileHandlerService fileHandlerService, CompressFileReader compressFileReader, OtherFilePreviewImpl otherFilePreview) {
        CompressFilePreviewImpl.fileHandlerService = fileHandlerService;
        this.compressFileReader = compressFileReader;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        String fileName=fileAttribute.getName();
        String filePassword = fileAttribute.getFilePassword();
        boolean forceUpdatedCache=fileAttribute.forceUpdatedCache();
        String fileTree = null;
        // 判断文件名是否存在(redis缓存读取)
        if (forceUpdatedCache || !StringUtils.hasText(fileHandlerService.getConvertedFile(fileName))  || !ConfigConstants.isCacheEnabled()) {
            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
            if (response.isFailure()) {
                return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
            }
            String filePath = response.getContent();
            try {
                fileTree = compressFileReader.unRar(filePath, filePassword,fileName);
            } catch (Exception e) {
                Throwable[] throwableArray = ExceptionUtils.getThrowables(e);
                for (Throwable throwable : throwableArray) {
                    if (throwable instanceof IOException || throwable instanceof EncryptedDocumentException) {
                        if (e.getMessage().toLowerCase().contains(Rar_PASSWORD_MSG)) {
                            model.addAttribute("needFilePassword", true);
                            return EXEL_FILE_PREVIEW_PAGE;
                        }
                    }
                }
            }
            if (!ObjectUtils.isEmpty(fileTree)) {
                KkFileUtils.deleteDirectory(fileDir+fileName + "_/__MACOSX");  //清理macOSX系统产生的垃圾文件
                //是否保留压缩包源文件
                if (ConfigConstants.getDeleteSourceFile()) {
                    if (!url.contains("?fileKey=")) { //不删除解压后 二级压缩包源文件 方便使用更新缓存命令
                        KkFileUtils.deleteFileByPath(filePath);
                    }
                }
                if (ConfigConstants.isCacheEnabled()) {
                    putTree(fileTree);  // 加入缓存
                    fileHandlerService.addConvertedFile(fileName, fileName);
                }
            }else {
                fileHandlerService.addConvertedFile(fileName, "error");  //转换错误加入缓存
                return otherFilePreview.notSupportedFile(model, fileAttribute, "压缩文件密码错误! 压缩文件损坏!  压缩文件类型不受支持!");
            }
        }
        model.addAttribute("fileName", fileName);
        model.addAttribute("fileTree", fileName+"_");
        return COMPRESS_FILE_PREVIEW_PAGE;
    }
    /**
     * 压缩包目录树加入缓存方法
     */
    public static List<ZtreeNodeVo> putTree(String rootPath) {
        List<ZtreeNodeVo> nodes = new ArrayList<>();
        File file = new File(fileDir+rootPath);
        ZtreeNodeVo node = traverse(file);
        nodes.add(node);
        fileHandlerService.putCompressCache(rootPath, nodes); // 添加到缓存
        return  nodes;
    }
    /**
     * 读取压缩包缓存方法
     */

    public static List<ZtreeNodeVo> getTree(String rootPath) {
        if (ConfigConstants.isCacheEnabled()) {  // 是否开启缓存
            List<ZtreeNodeVo> zipImgUrls = fileHandlerService.getCompressCache(rootPath); //查询是否存在缓存
            if (!ObjectUtils.isEmpty(zipImgUrls)) {
                return  fileHandlerService.getCompressCache(rootPath); //读取缓存数据
            }
        }
        List<ZtreeNodeVo> nodes = new ArrayList<>();
        File file = new File(fileDir+rootPath);
        if(!file.exists() || file.length() == 0) {
            return null;
        }
        ZtreeNodeVo node = traverse(file);
        nodes.add(node);
        return  nodes;
    }
    private static ZtreeNodeVo traverse(File file) {
        ZtreeNodeVo pathNodeVo = new ZtreeNodeVo();
        pathNodeVo.setId(file.getAbsolutePath().replace(fileDir, "").replace("\\", "/"));
        pathNodeVo.setName(file.getName());
        pathNodeVo.setPid(file.getParent().replace(fileDir, "").replace("\\", "/"));
        if (file.isDirectory()) {
            List<ZtreeNodeVo> subNodeVos = new ArrayList<>();
            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                return pathNodeVo;
            }
            for (File subFile : subFiles) {
                ZtreeNodeVo subNodeVo = traverse(subFile);
                subNodeVos.add(subNodeVo);
            }
            pathNodeVo.setChildren(subNodeVos);
        }
        return pathNodeVo;
    }
}
