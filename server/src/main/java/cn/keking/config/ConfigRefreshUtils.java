package cn.keking.config;

import org.artofsolving.jodconverter.office.OfficeUtils;

import java.util.Properties;

/**
 * 配置文件刷新工具类
 *
 * @author chenjh,zhongjiahua
 * @date 2022-11-29
 */
public class ConfigRefreshUtils {

    public static void parseProperties(Properties properties) {
        String media;
        boolean fileUploadDisable;
        String ftpUsername;
        String pdfPresentationModeDisable;
        String pdfOpenFileDisable;
        String ftpPassword;
        String ftpControlEncoding;
        String tifPreviewType;
        boolean cacheEnabled;
        String pdfPrintDisable;
        String[] textArray;
        String baseUrl;
        String pdfBookmarkDisable;
        String pdfDownloadDisable;
        String officePreviewSwitchDisabled;
        String text;
        String[] mediaArray;
        String officePreviewType;
        String trustHost;
        OfficeUtils.restorePropertiesFromEnvFormat(properties);
        cacheEnabled = Boolean.parseBoolean(properties.getProperty("cache.enabled", ConfigConstants.DEFAULT_CACHE_ENABLED));
        text = properties.getProperty("simText", ConfigConstants.DEFAULT_TXT_TYPE);
        media = properties.getProperty("media", ConfigConstants.DEFAULT_MEDIA_TYPE);
        officePreviewType = properties.getProperty("office.preview.type", ConfigConstants.DEFAULT_OFFICE_PREVIEW_TYPE);
        officePreviewSwitchDisabled = properties.getProperty("office.preview.switch.disabled", ConfigConstants.DEFAULT_OFFICE_PREVIEW_SWITCH_DISABLED);
        ftpUsername = properties.getProperty("ftp.username", ConfigConstants.DEFAULT_FTP_USERNAME);
        ftpPassword = properties.getProperty("ftp.password", ConfigConstants.DEFAULT_FTP_PASSWORD);
        ftpControlEncoding = properties.getProperty("ftp.control.encoding", ConfigConstants.DEFAULT_FTP_CONTROL_ENCODING);
        textArray = text.split(",");
        mediaArray = media.split(",");
        baseUrl = properties.getProperty("base.url", ConfigConstants.DEFAULT_BASE_URL);
        trustHost = properties.getProperty("trust.host", ConfigConstants.DEFAULT_TRUST_HOST);
        pdfPresentationModeDisable = properties.getProperty("pdf.presentationMode.disable", ConfigConstants.DEFAULT_PDF_PRESENTATION_MODE_DISABLE);
        pdfOpenFileDisable = properties.getProperty("pdf.openFile.disable", ConfigConstants.DEFAULT_PDF_OPEN_FILE_DISABLE);
        pdfPrintDisable = properties.getProperty("pdf.print.disable", ConfigConstants.DEFAULT_PDF_PRINT_DISABLE);
        pdfDownloadDisable = properties.getProperty("pdf.download.disable", ConfigConstants.DEFAULT_PDF_DOWNLOAD_DISABLE);
        pdfBookmarkDisable = properties.getProperty("pdf.bookmark.disable", ConfigConstants.DEFAULT_PDF_BOOKMARK_DISABLE);
        fileUploadDisable = Boolean.parseBoolean(properties.getProperty("file.upload.disable", ConfigConstants.DEFAULT_FILE_UPLOAD_DISABLE));
        tifPreviewType = properties.getProperty("tif.preview.type", ConfigConstants.DEFAULT_TIF_PREVIEW_TYPE);

        ConfigConstants.setCacheEnabledValueValue(cacheEnabled);
        ConfigConstants.setSimTextValue(textArray);
        ConfigConstants.setMediaValue(mediaArray);
        ConfigConstants.setOfficePreviewTypeValue(officePreviewType);
        ConfigConstants.setFtpUsernameValue(ftpUsername);
        ConfigConstants.setFtpPasswordValue(ftpPassword);
        ConfigConstants.setFtpControlEncodingValue(ftpControlEncoding);
        ConfigConstants.setBaseUrlValue(baseUrl);
        ConfigConstants.setTrustHostValue(trustHost);
        ConfigConstants.setOfficePreviewSwitchDisabledValue(officePreviewSwitchDisabled);
        ConfigConstants.setPdfPresentationModeDisableValue(pdfPresentationModeDisable);
        ConfigConstants.setPdfOpenFileDisableValue(pdfOpenFileDisable);
        ConfigConstants.setPdfPrintDisableValue(pdfPrintDisable);
        ConfigConstants.setPdfDownloadDisableValue(pdfDownloadDisable);
        ConfigConstants.setPdfBookmarkDisableValue(pdfBookmarkDisable);
        ConfigConstants.setFileUploadDisableValue(fileUploadDisable);
        ConfigConstants.setTifPreviewTypeValue(tifPreviewType);
        setWatermarkConfig(properties);
    }

    private static void setWatermarkConfig(Properties properties) {
        String watermarkTxt = properties.getProperty("watermark.txt", WatermarkConfigConstants.DEFAULT_WATERMARK_TXT);
        String watermarkXSpace = properties.getProperty("watermark.x.space", WatermarkConfigConstants.DEFAULT_WATERMARK_X_SPACE);
        String watermarkYSpace = properties.getProperty("watermark.y.space", WatermarkConfigConstants.DEFAULT_WATERMARK_Y_SPACE);
        String watermarkFont = properties.getProperty("watermark.font", WatermarkConfigConstants.DEFAULT_WATERMARK_FONT);
        String watermarkFontsize = properties.getProperty("watermark.fontsize", WatermarkConfigConstants.DEFAULT_WATERMARK_FONTSIZE);
        String watermarkColor = properties.getProperty("watermark.color", WatermarkConfigConstants.DEFAULT_WATERMARK_COLOR);
        String watermarkAlpha = properties.getProperty("watermark.alpha", WatermarkConfigConstants.DEFAULT_WATERMARK_ALPHA);
        String watermarkWidth = properties.getProperty("watermark.width", WatermarkConfigConstants.DEFAULT_WATERMARK_WIDTH);
        String watermarkHeight = properties.getProperty("watermark.height", WatermarkConfigConstants.DEFAULT_WATERMARK_HEIGHT);
        String watermarkAngle = properties.getProperty("watermark.angle", WatermarkConfigConstants.DEFAULT_WATERMARK_ANGLE);
        WatermarkConfigConstants.setWatermarkTxtValue(watermarkTxt);
        WatermarkConfigConstants.setWatermarkXSpaceValue(watermarkXSpace);
        WatermarkConfigConstants.setWatermarkYSpaceValue(watermarkYSpace);
        WatermarkConfigConstants.setWatermarkFontValue(watermarkFont);
        WatermarkConfigConstants.setWatermarkFontsizeValue(watermarkFontsize);
        WatermarkConfigConstants.setWatermarkColorValue(watermarkColor);
        WatermarkConfigConstants.setWatermarkAlphaValue(watermarkAlpha);
        WatermarkConfigConstants.setWatermarkWidthValue(watermarkWidth);
        WatermarkConfigConstants.setWatermarkHeightValue(watermarkHeight);
        WatermarkConfigConstants.setWatermarkAngleValue(watermarkAngle);

    }

}
