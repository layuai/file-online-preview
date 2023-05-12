package cn.keking.web.controller;

import java.io.File;
import java.util.Arrays;

import cn.keking.model.ReturnResponse;
import cn.keking.utils.KkFileUtils;
import cn.keking.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.keking.config.ConfigConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FolderController {

    @Value("${sc.password}")
    private String KEY;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final String folderPath = ConfigConstants.getFileDir();
    public static final String BASE64_DECODE_ERROR_MSG = "Base64解码失败，请检查你的 %s 是否采用 Base64 + urlEncode 双重编码了！";

    /**
     * 删除文件夹及其内容
     *
     * @param fileName 文件名
     * @param key      验证码
     * @return 是否删除成功
     */
    @GetMapping("/deleteFolder")
    public ReturnResponse<Object> deleteFiles(@RequestParam("fileName") String fileName, @RequestParam("key") String key) {
        if (!key.equals(KEY)) {
            return ReturnResponse.failure("无权操作！");
        }
        if (fileName == null || fileName.length() == 0) {
            return ReturnResponse.failure("文件名为空，删除失败！");
        }
        try {
            fileName = WebUtils.decodeUrl(fileName);
        } catch (Exception ex) {
            String errorMsg = String.format(BASE64_DECODE_ERROR_MSG, "url");
            return ReturnResponse.failure(errorMsg + "删除失败！");
        }
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        if (KkFileUtils.isIllegalFileName(fileName)) {
            return ReturnResponse.failure("非法文件名，删除失败！");
        }
        // 文件名过滤器，排除掉不合法的文件名
        String regex = "^.*\\.(dll|sh|exe|msi|sql|php|java|py)$";
        if (fileName.matches(regex)) {
            return ReturnResponse.failure("不允许删除非法的文件后缀名！");
        }
        File folder = new File(folderPath);
        if (!folder.exists()) {
            return ReturnResponse.failure(folder.getAbsolutePath() + " 路径不存在！");
        }
        if (!folder.isDirectory()) {
            return ReturnResponse.failure(folder.getAbsolutePath() + " 不是文件夹！");
        }
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return ReturnResponse.failure(folder.getAbsolutePath() + " 目录下没有任何文件或文件夹！");
        }
        if (fileName == null || fileName.trim().equals("")) {
            return ReturnResponse.failure(" 文件名不能为空！");
        }
        logger.info("打印目录: {}", Arrays.toString(files));
        // 截取文件名,不要后缀
        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."));
        int[] result = new int[]{0, 0, 0}; // 删除的文件数、文件夹数、操作用时
        long start = System.currentTimeMillis();
        for (File file : files) {
            logger.info("打印: {}", "进来删除for循环了");
            if (!file.exists()) {
                logger.info("删除文件：{}", file.getAbsolutePath() + " 文件或文件夹不存在！");
                continue;
            }
            if (file.isDirectory()) {
                logger.info("打印: {}", "进来删除单个文件");
                // 获得文件夹名称
                String folderName = file.getName();
                if (folderName.indexOf(".") > 0) { // 如果文件夹名称包含扩展名，则截取前面的字符串作为文件夹名称
                    folderName = folderName.substring(0, folderName.lastIndexOf("."));
                }
                // 如果是同名文件夹，则删除文件夹及其内容
                if (folderName.equals(fileNameWithoutExt)) {
                    boolean deleteFolderResult = deleteFolder(file);
                    if (deleteFolderResult) {
                        logger.info("删除文件：{}", file.getAbsolutePath() + " 文件夹删除成功！");
                        result[1]++;
                    } else {
                        logger.info("删除文件：{}", file.getAbsolutePath() + " 文件夹删除失败！");
                    }
                    result[2]++;
                }
            } else {
                logger.info("打印: {}", "进来删除文件夹");
                // 获取文件名称
                String fullName = file.getName();
                if (fullName.indexOf(".") > 0) { // 如果文件名称包含扩展名，则截取前面的字符串作为文件名称
                    fullName = fullName.substring(0, fullName.lastIndexOf("."));
                }
                // 如果是同名文件，则删除文件
                if (fullName.equals(fileNameWithoutExt)) {
                    boolean deleteFileResult = file.delete();
                    if (deleteFileResult) {
                        logger.info("删除文件：{}", file.getAbsolutePath() + " 文件删除成功！");
                        result[0]++;
                    } else {
                        logger.info("删除文件：{}", file.getAbsolutePath() + " 文件删除失败！");
                    }
                    result[2]++;
                }
            }
        }
        long end = System.currentTimeMillis();
        return ReturnResponse.success("总共删除了 " + result[0] + " 个文件和 " + result[1] + " 个文件夹，用时 " + (end - start) + "ms。");
    }

    /**
     * 删除文件夹及其内容
     *
     * @param folder 文件夹路径
     * @return 是否删除成功
     */
    private static boolean deleteFolder(File folder) {
        logger.info("打印: {}", "进来删除文件夹了啊");
        if (!folder.exists()) {
            logger.info("删除文件：{}", folder.getAbsolutePath() + " 文件夹不存在！");
            return false;
        }
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file); // 递归删除子文件夹
                } else {
                    boolean deleteFileResult = file.delete(); // 删除子文件
                    if (deleteFileResult) {
                        logger.info("删除文件：{}", file.getAbsolutePath() + " 文件删除成功！");
                    } else {
                        logger.info("删除文件：{}", file.getAbsolutePath() + " 文件删除失败！");
                        return false;
                    }
                }
            }
        }
        boolean deleteFolderResult = folder.delete(); // 删除文件夹本身
        if (deleteFolderResult) {
            logger.info("删除文件：{}", folder.getAbsolutePath() + " 文件夹删除成功！");
            return true;
        } else {
            logger.info("删除文件：{}", folder.getAbsolutePath() + " 文件夹删除失败！");
            return false;
        }
    }
}
