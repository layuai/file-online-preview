package cn.keking.service;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.FileType;
import cn.keking.service.cache.CacheService;
import cn.keking.utils.KkFileUtils;
import cn.keking.utils.WebUtils;
import com.aspose.cad.Color;
import com.aspose.cad.fileformats.cad.CadDrawTypeMode;
import com.aspose.cad.imageoptions.CadRasterizationOptions;
import com.aspose.cad.imageoptions.PdfOptions;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yudian-it
 * @date 2017/11/13
 */
@Component
public class FileHandlerService {

    private final Logger logger = LoggerFactory.getLogger(FileHandlerService.class);

    private static final String DEFAULT_CONVERTER_CHARSET = System.getProperty("sun.jnu.encoding");
    private final String fileDir = ConfigConstants.getFileDir();
    private final CacheService cacheService;

    @Value("${server.tomcat.uri-encoding:UTF-8}")
    private String uriEncoding;

    public FileHandlerService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * @return 已转换过的文件集合(缓存)
     */
    public Map<String, String> listConvertedFiles() {
        if (ConfigConstants.isCacheEnabled()) {
            return cacheService.getPDFCache();
        }
        return new HashMap<>(16);
    }

    /**
     * @return 已转换过的文件，根据文件名获取
     */
    public String getConvertedFile(String key) {
        if (ConfigConstants.isCacheEnabled()) {
            return cacheService.getPDFCache(key);
        }
        return null;
    }

    /**
     * @param key pdf本地路径
     * @return 已将pdf转换成图片的图片本地相对路径
     */
    public Integer getConvertedPdfImage(String key) {
        if (ConfigConstants.isCacheEnabled()) {
            return cacheService.getPdfImageCache(key);
        }
        return null;
    }


    /**
     * 从路径中获取文件负
     *
     * @param path 类似这种：C:\Users\yudian-it\Downloads
     * @return 文件名
     */
    public String getFileNameFromPath(String path) {
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    /**
     * 获取相对路径
     *
     * @param absolutePath 绝对路径
     * @return 相对路径
     */
    public String getRelativePath(String absolutePath) {
        return absolutePath.substring(fileDir.length());
    }

    /**
     * 添加转换后PDF缓存
     *
     * @param url pdfurl
     * @param value    缓存相对路径
     */
    public void addConvertedFile(String url, String value) {
        if (ConfigConstants.isCacheEnabled()) {
            cacheService.putPDFCache(url, value);
        }
    }

    /**
     * 添加转换后图片组缓存
     *
     * @param pdfFilePath pdf文件绝对路径
     * @param num         图片张数
     */
    public void addConvertedPdfImage(String pdfFileUrl, int num) {
        if (ConfigConstants.isCacheEnabled()) {
            cacheService.putPdfImageCache(pdfFileUrl, num);
        }
    }

    /**
     * 获取redis中压缩包内图片文件
     *
     * @param fileKey fileKey
     * @return 图片文件访问url列表
     */
    public List<String> getImgCache(String fileKey) {
        if (ConfigConstants.isCacheEnabled()) {
            return cacheService.getImgCache(fileKey);
        }
        return null;
    }

    /**
     * 设置redis中压缩包内图片文件
     *
     * @param fileKey fileKey
     * @param imgs    图片文件访问url列表
     */
    public void putImgCache(String fileKey, List<String> imgs) {
        if (ConfigConstants.isCacheEnabled()) {
            cacheService.putImgCache(fileKey, imgs);
        }
    }

    /**
     * 对转换后的文件进行操作(改变编码方式)
     *
     * @param outFilePath 文件绝对路径
     */
    public void doActionConvertedFile(String outFilePath) {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(outFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, DEFAULT_CONVERTER_CHARSET))) {
            String line;
            while (null != (line = reader.readLine())) {
                if (line.contains("charset=gb2312")) {
                    line = line.replace("charset=gb2312", "charset=utf-8");
                }
                sb.append(line);
            }
            // 添加sheet控制头
            sb.append("<script src=\"js/jquery-3.0.0.min.js\" type=\"text/javascript\"></script>");
            sb.append("<script src=\"js/excel.header.js\" type=\"text/javascript\"></script>");
            sb.append("<link rel=\"stylesheet\" href=\"bootstrap/css/bootstrap.min.css\">");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 重新写入文件
        try (FileOutputStream fos = new FileOutputStream(outFilePath);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  pdf文件转换成jpg图片集
     * @param pdfFilePath pdf文件路径
     * @param pdfName pdf文件名称
     * @param baseUrl 基础访问地址
     * @return 图片访问集合
     */
    public List<String> pdf2jpg(String pdfFilePath, String pdfName, String baseUrl, FileAttribute fileAttribute) {
        List<String> imageUrls = new ArrayList<>();
        Integer imageCount = null;
        if (ConfigConstants.isCacheEnabled()) {
            imageCount = this.getConvertedPdfImage(fileAttribute.getUrl());
        }
        String jpgForlderName = "jpg" + pdfName.replace(".", "_");
        String imageFileSuffix = ".jpg";
        Path fileDirPath = Paths.get(pdfFilePath).getParent();
        Path rootRirPath = Paths.get(fileDir);
        Path pdfFolderPath = rootRirPath.relativize(fileDirPath);
        String pdfFolder = pdfFolderPath.toString();
        String urlPrefix;
        try {
            urlPrefix = baseUrl + URLEncoder.encode(pdfFolderPath.toString(), uriEncoding).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException", e);
            urlPrefix = baseUrl + pdfFolder;
        }
        if (imageCount != null && imageCount > 0) {
            for (int i = 0; i < imageCount; i++) {
                // 加一个pdfname的hash 解决压缩文件同名问题
                imageUrls.add(urlPrefix + "/" + jpgForlderName + "/" + i + imageFileSuffix);
            }
            return imageUrls;
        }
        try {
            File pdfFile = new File(pdfFilePath);
            PDDocument doc = PDDocument.load(pdfFile);
            int pageCount = doc.getNumberOfPages();
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            String folder = fileDirPath.toString();

            File path = new File(folder + File.separator + jpgForlderName);
            if (!path.exists() && !path.mkdirs()) {
                logger.error("创建转换文件【{}】目录失败，请检查目录权限！", folder);
            }
            String imageFilePath;
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                imageFilePath = path.getAbsolutePath() + File.separator + pageIndex + imageFileSuffix;
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 105, ImageType.RGB);
                ImageIOUtil.writeImage(image, imageFilePath, 105);
                imageUrls.add(urlPrefix + "/" + jpgForlderName + "/" + pageIndex + imageFileSuffix);
            }
            doc.close();
            this.addConvertedPdfImage(fileAttribute.getUrl(), pageCount);
        } catch (IOException e) {
            logger.error("Convert pdf to jpg exception, pdfFilePath：{}", pdfFilePath, e);
        }
        return imageUrls;
    }

    /**
     * cad文件转pdf
     * @param inputFilePath cad文件路径
     * @param outputFilePath pdf输出文件路径
     * @return 转换是否成功
     */
    public boolean cadToPdf(String inputFilePath, String outputFilePath)  {
        com.aspose.cad.Image cadImage = com.aspose.cad.Image.load(inputFilePath);
        CadRasterizationOptions cadRasterizationOptions = new CadRasterizationOptions();
        cadRasterizationOptions.setLayouts(new String[]{"Model"});
        cadRasterizationOptions.setNoScaling(true);
        cadRasterizationOptions.setBackgroundColor(Color.getWhite());
        cadRasterizationOptions.setPageWidth(cadImage.getWidth());
        cadRasterizationOptions.setPageHeight(cadImage.getHeight());
        cadRasterizationOptions.setPdfProductLocation("center");
        cadRasterizationOptions.setAutomaticLayoutsScaling(true);
        cadRasterizationOptions.setDrawType(CadDrawTypeMode.UseObjectColor);
        PdfOptions pdfOptions = new PdfOptions();
        pdfOptions.setVectorRasterizationOptions(cadRasterizationOptions);
        File outputFile = new File(outputFilePath);
        OutputStream stream;
        try {
            stream = new FileOutputStream(outputFile);
            cadImage.save(stream, pdfOptions);
            cadImage.close();
            return true;
        } catch (FileNotFoundException e) {
            logger.error("PDFFileNotFoundException，inputFilePath：{}", inputFilePath, e);
            return false;
        }
    }

    /**
     * 获取文件属性
     *
     * @param url url
     * @return 文件属性
     */
    public FileAttribute getFileAttribute(String url, HttpServletRequest req) {
        FileAttribute attribute = new FileAttribute();
        String suffix;
        FileType type;
        String fileName;
        String fullFileName = WebUtils.getUrlParameterReg(url, "fullfilename");
        if (StringUtils.hasText(fullFileName)) {
            fileName = fullFileName;
            type = FileType.typeFromFileName(fullFileName);
            suffix = KkFileUtils.suffixFromFileName(fullFileName);
        } else {
            fileName = WebUtils.getFileNameFromURL(url);
            type = FileType.typeFromUrl(url);
            suffix = WebUtils.suffixFromUrl(url);
        }
        if (url.contains("?fileKey=")) {
            attribute.setSkipDownLoad(true);
        }
        attribute.setType(type);
        attribute.setName(fileName);
        attribute.setSuffix(suffix);
        url = WebUtils.encodeUrlFileName(url);
        attribute.setUrl(url);
        if (req != null) {
            String officePreviewType = req.getParameter("officePreviewType");
            String fileKey = WebUtils.getUrlParameterReg(url,"fileKey");
            if (StringUtils.hasText(officePreviewType)) {
                attribute.setOfficePreviewType(officePreviewType);
            }
            if (StringUtils.hasText(fileKey)) {
                attribute.setFileKey(fileKey);
            }

            String tifPreviewType = req.getParameter("tifPreviewType");
            if (StringUtils.hasText(tifPreviewType)) {
                attribute.setTifPreviewType(tifPreviewType);
            }

            String filePassword = req.getParameter("filePassword");
            if (StringUtils.hasText(filePassword)) {
                attribute.setFilePassword(filePassword);
            }

            String userToken = req.getParameter("userToken");
            if (StringUtils.hasText(userToken)) {
                attribute.setUserToken(userToken);
            }
        }

        return attribute;
    }

    /**
     * @return 已转换过的视频文件集合(缓存)
     */
    public Map<String, String> listConvertedMedias() {
        if (ConfigConstants.isCacheEnabled()) {
            return cacheService.getMediaConvertCache();
        }
        return new HashMap<>(16);
    }

    /**
     * 添加转换后的视频文件缓存
     * @param fileName
     * @param value
     */
    public void addConvertedMedias(String fileName, String value) {
        if (ConfigConstants.isCacheEnabled()) {
            cacheService.putMediaConvertCache(fileName, value);
        }
    }

    /**
     * @return 已转换视频文件缓存，根据文件名获取
     */
    public String getConvertedMedias(String key) {
        if (ConfigConstants.isCacheEnabled()) {
            return cacheService.getMediaConvertCache(key);
        }
        return null;
    }
}
