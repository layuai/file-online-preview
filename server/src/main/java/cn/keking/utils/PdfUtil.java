package cn.keking.utils;

import cn.keking.model.FileAttribute;
import com.itextpdf.awt.AsianFontMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * pdf添加水印工具
 * <p>
 * * @author lih
 * * @since 2023/06/06
 */
public class PdfUtil {

    /**
     * 添加水印并替换原先的文件
     *
     * @param outputFilePath 文件
     * @param fileAttribute  文件属性
     */
    public static void addWaterMark(String outputFilePath, FileAttribute fileAttribute) {
        // 如果水印内容不为空，则添加水印并替换原先文件
        String waterFilePath = outputFilePath.replaceAll(".pdf", "(水印).pdf");
        waterMark(outputFilePath, waterFilePath, fileAttribute);
        File outputFile = new File(outputFilePath);
        outputFile.delete();
        System.out.println("删除原Pdf文件");
        File waterFile = new File(waterFilePath);
        waterFile.renameTo(outputFile);
        System.out.println("重命名新(水印)Pdf文件为原Pdf文件");
    }

    /**
     * 根据输入文件添加水印后输出文件
     *
     * @param inputFile     输入文件
     * @param outputFile    输出文件
     * @param fileAttribute 文件属性
     */
    public static void waterMark(String inputFile, String outputFile, FileAttribute fileAttribute) {
        // 水印内容
        String watermarkTxt = fileAttribute.getWatermarkTxt();
        System.out.println("水印内容：" + watermarkTxt);
        // 水印x轴间隔
        Integer watermarkXSpace = fileAttribute.getWatermarkXSpace();
        // 水印y轴间隔
        Integer watermarkYSpace = fileAttribute.getWatermarkYSpace();

        // 水印字体
        String watermarkFont = fileAttribute.getWatermarkFont();
        // 水印字体大小
        String watermarkFontsize = fileAttribute.getWatermarkFontsize();

        Float fontSize = CssDataTypeUtil.parse(watermarkFontsize).getPt();
        // 水印字体颜色
        String watermarkColor = fileAttribute.getWatermarkColor();

        // 水印透明度
        Float watermarkAlpha = fileAttribute.getWatermarkAlpha();
        // 水印宽度
        Float watermarkWidth = fileAttribute.getWatermarkWidth();
        // 水印高度
        Float watermarkHeight = fileAttribute.getWatermarkHeight();
        // 水印倾斜度数
        Float watermarkAngle = fileAttribute.getWatermarkAngle();

        //水印起始位置x轴坐标
        float watermarkX = 20;
        //水印起始位置Y轴坐标
        float watermarkY = 20;
        //水印行数
        float watermarkRows = 0;
        //水印列数
        float watermarkCols = 0;

        PdfReader reader = null;
        PdfStamper stamper = null;
        try {

            reader = new PdfReader(inputFile);
            stamper = new PdfStamper(reader, Files.newOutputStream(Paths.get(outputFile)));
            BaseFont base = BaseFont.createFont(AsianFontMapper.ChineseSimplifiedFont, AsianFontMapper.ChineseSimplifiedEncoding_H, BaseFont.EMBEDDED);
            Rectangle pageRect = null;
            PdfGState gs = new PdfGState();
            //这里是透明度设置
            gs.setFillOpacity(watermarkAlpha);
            //这里是条纹不透明度
            gs.setStrokeOpacity(0.2f);
            int total = reader.getNumberOfPages() + 1;
            System.out.println("Pdf页数：" + reader.getNumberOfPages());
            JLabel label = new JLabel();
            label.setText(watermarkTxt);
            PdfContentByte under;
            //循环PDF，每页添加水印
            for (int pageSize = 1; pageSize < total; pageSize++) {
                pageRect = reader.getPageSizeWithRotation(pageSize);
                float pageWidth = pageRect.getWidth();
                float pageHeight = pageRect.getHeight();

                //水印列数
                watermarkCols = (pageWidth - watermarkX) / (watermarkWidth + watermarkXSpace);

                //水印行数
                watermarkRows = (pageHeight - watermarkY) / (watermarkHeight + watermarkYSpace);
                float pageOffsetTop = 0;
                float pageOffsetLeft = 0;
                float allWatermarkWidth = pageOffsetLeft + watermarkX + watermarkWidth * watermarkCols + watermarkXSpace * (watermarkCols - 1);
                float allWatermarkHeight = pageOffsetTop + watermarkY + watermarkHeight * watermarkRows + watermarkYSpace * (watermarkRows - 1);

                under = stamper.getOverContent(pageSize);  //在内容上方添加水印
                //under = stamper.getUnderContent(i);  //在内容下方添加水印
                under.saveState();
                under.setGState(gs);
                under.beginText();
                under.setColorFill(BaseColor.BLACK);  //添加文字颜色  不能动态改变 放弃使用
                under.setFontAndSize(base, fontSize); //这里是水印字体大小
                float x;
                float y;
                for (int i = 0; i < watermarkRows; i++) {
                    y = watermarkY + (pageHeight - allWatermarkHeight) / 2 + (watermarkYSpace + watermarkHeight) * i;
                    for (int j = 0; j < watermarkCols; j++) {
                        x = watermarkX + (pageWidth - allWatermarkWidth) / 2 + (watermarkWidth + watermarkXSpace) * j;
                        // rotation:倾斜角度
                        under.showTextAligned(Element.ALIGN_LEFT, watermarkTxt, x, y, watermarkAngle);
                    }
                }
                //添加水印文字
                under.endText();
            }
            System.out.println("添加水印成功！");
        } catch (IOException e) {
            System.out.println("添加水印失败！错误信息为: " + e);
            e.printStackTrace();
        } catch (DocumentException e) {
            System.out.println("添加水印失败！错误信息为: " + e);
            e.printStackTrace();
        } finally {
            //关闭流
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

}
