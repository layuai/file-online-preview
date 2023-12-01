package cn.keking.utils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * css长度 转换单位工具
 *
 * <br>
 * 绝对长度单位
 * 当输出介质的物理性质已知时，如用于打印布局，绝对长度单位代表一个物理度量单位。这是通过：将一个单元锚定到一个物理单元，并将其定义为相对于它的另一个，来实现的。对于低分辨率的设备（如屏幕），高分辨率设备（如打印机），该锚定是不同的。
 * 对于低 dpi 设备，单位 px 表示物理参考像素;其他单位是相对于它定义的。因此，1 in 定义为 96px，等于 72pt。此定义的后果是，在此类设备上，以英寸（in）、厘米（cm）或 毫米（mm）表示的尺寸不需要与同名的物理单位的大小相匹配。
 * 对于高 dpi 设备，英寸（in）、厘米（cm）和毫米（mm）与物理设备相同。因此，px 单位是相对于它们定义的（1/96 of 1 inch）。
 * 备注： 很多用户选择增加用户代理 user agent 的字体大小以提高文本可读性。因为绝对长度是固定值，无法根据用户的设置进行缩放，因此会降低页面友好性，因此在设置字体大小 font-size 相关的长度值时，最好优先选择使用相对长度单位，比如 em 或 rem。
 * 译者注： 一些浏览器还支持设置最低字体大小。常见的最低字体大小限制为 9px 到 12px，
 * px
 * 一像素（pixel）。对于普通的屏幕，通常是一个设备像素（点）。 对于打印机和高分辨率屏幕，一个 CSS 像素往往占多个设备像素。一般来说，每英寸的像素的数量保持在 96 左右， 1px = 1in 的 96 分之一。
 * cm
 * 一厘米。 1cm = 96px / 2.54。
 * mm
 * 一毫米。 1mm = 1/10 * 1cm。
 * Q 实验性
 * 四分之一毫米。1Q = 1/40 * 1cm。
 * in
 * 一英寸。1in = 2.54cm = 96px。
 * pc
 * 一十二点活字（pica），六分之一英寸。 1pc = 12pt = 1/6 * 1in。
 * pt
 * 一磅（point），72 分之一英寸。1pt = 1/12 * 1pc = 1/72 * 1in。
 * <a href="https://developer.mozilla.org/zh-CN/docs/Web/CSS/length">_length_ - CSS：层叠样式表 _ MDN</a>
 *
 * @author lih
 * @since 2023/06/06
 */
public final class CssDataTypeUtil {
    /**
     * 长度单位
     * 基准长度单位 cm
     * 1.0cm = 10.0mm = 37.795277px = 40.0Q = 0.39370078in = 2.3622048pc = 28.346458pt
     */
    public enum CssDataType {
        CM("cm", 1f), MM("mm", CM.value / 10), PX("px", CM.value / (96f / 2.54f)), Q("Q", CM.value / 40), IN("in", CM.value * 2.54f), PC("pc", IN.value / 6f), PT("pt", PC.value / 12f);
        private final String name;
        private final Float value;

        CssDataType(String initName, Float initValue) {
            name = initName;
            value = initValue;
        }

        private static final Map<String, CssDataType> mappings;

        static {
            mappings = Collections.unmodifiableMap(Arrays.stream(values()).collect(Collectors.toMap(CssDataType::getName, Function.identity())));
        }

        public static CssDataType resolve(String name) {
            return mappings.get(name);
        }

        public static boolean containsKey(String value) {
            return mappings.containsKey(value);
        }

        public String getName() {
            return name;
        }

        public Float getValue() {
            return value;
        }
    }

    private float px, cm, mm, Q, in, pc, pt;

    private CssDataTypeUtil(Float length, String dataType) {
        this(length, getCssDataType(dataType));
    }

    private CssDataTypeUtil(Float length, CssDataType cssDataType) {
        cm = length * cssDataType.value;
        px = cm / CssDataType.PX.value;
        Q = cm / CssDataType.Q.value;
        mm = cm / CssDataType.MM.value;
        in = cm / CssDataType.IN.value;
        pc = cm / CssDataType.PC.value;
        pt = cm / CssDataType.PT.value;
    }

    public static CssDataTypeUtil of(Float length, String dataType) {
        return create(length, dataType);
    }

    private static CssDataTypeUtil create(Float length, String dataType) {
        return new CssDataTypeUtil(length, dataType);
    }

    /**
     * 字符串转换
     *
     * @param str
     * @return
     */
    public static CssDataTypeUtil parse(String str) {
        Objects.requireNonNull(str, "str");
        Pattern pattern = Pattern.compile("(\\d+|\\.\\d+)([a-zA-Z]+)");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            String extractedNumber = matcher.group(1);
            String extractedUnit = matcher.group(2);
            return of(Float.valueOf(extractedNumber), extractedUnit);
        } else {
            return null;
        }
    }

    /**
     * 格式化字符串
     *
     * @param dataType 长度单位
     * @return
     */
    public String format(String dataType) {
        return format(getCssDataType(dataType));
    }

    private static CssDataType getCssDataType(String dataType) {
        CssDataType cssDataType = CssDataType.resolve(dataType);
        if (Objects.isNull(cssDataType)) {
            throw new IllegalArgumentException("No enum constant " + CssDataType.class.getName() + "." + dataType);
        }
        return cssDataType;
    }

    /**
     * 格式化字符串
     *
     * @param cssDataType 长度单位枚举
     * @return
     */
    public String format(CssDataType cssDataType) {
        return get(cssDataType) + cssDataType.getName();
    }

    /**
     * 增减px
     *
     * @param pxToAdd
     * @return
     */
    public CssDataTypeUtil plusPx(float pxToAdd) {
        if (pxToAdd == 0) {
            return this;
        }
        return new CssDataTypeUtil(px + pxToAdd, CssDataType.PX);
    }

    /**
     * 指定px
     *
     * @param pxToDesign
     * @return
     */
    public CssDataTypeUtil withPx(float pxToDesign) {
        if (px == pxToDesign) {
            return this;
        }
        return new CssDataTypeUtil(pxToDesign, CssDataType.PX);
    }

    /**
     * 增减cm
     *
     * @param cmToAdd
     * @return
     */
    public CssDataTypeUtil plusCm(float cmToAdd) {
        if (cmToAdd == 0) {
            return this;
        }
        return new CssDataTypeUtil(cm + cmToAdd, CssDataType.CM);
    }

    /**
     * 指定cm
     *
     * @param cmToDesign
     * @return
     */
    public CssDataTypeUtil withCm(float cmToDesign) {
        if (cm == cmToDesign) {
            return this;
        }
        return new CssDataTypeUtil(cmToDesign, CssDataType.CM);
    }


    /**
     * 增减mm
     *
     * @param mmToAdd
     * @return
     */
    public CssDataTypeUtil plusMm(float mmToAdd) {
        if (mmToAdd == 0) {
            return this;
        }
        return new CssDataTypeUtil(mm + mmToAdd, CssDataType.MM);
    }

    /**
     * 指定mm
     *
     * @param mmToDesign
     * @return
     */
    public CssDataTypeUtil withMm(float mmToDesign) {
        if (mm == mmToDesign) {
            return this;
        }
        return new CssDataTypeUtil(mmToDesign, CssDataType.MM);
    }

    /**
     * 增减Q
     *
     * @param QToAdd
     * @return
     */
    public CssDataTypeUtil plusQ(float QToAdd) {
        if (QToAdd == 0) {
            return this;
        }
        return new CssDataTypeUtil(Q + QToAdd, CssDataType.Q);
    }

    /**
     * 指定Q
     *
     * @param QToDesign
     * @return
     */
    public CssDataTypeUtil withQ(float QToDesign) {
        if (Q == QToDesign) {
            return this;
        }
        return new CssDataTypeUtil(QToDesign, CssDataType.Q);
    }

    /**
     * 增减in
     *
     * @param inToAdd
     * @return
     */
    public CssDataTypeUtil plusIn(float inToAdd) {
        if (inToAdd == 0) {
            return this;
        }
        return new CssDataTypeUtil(in + inToAdd, CssDataType.IN);
    }

    /**
     * 指定in
     *
     * @param inToDesign
     * @return
     */
    public CssDataTypeUtil withIn(float inToDesign) {
        if (in == inToDesign) {
            return this;
        }
        return new CssDataTypeUtil(inToDesign, CssDataType.IN);
    }

    /**
     * 增减pc
     *
     * @param pcToAdd
     * @return
     */
    public CssDataTypeUtil plusPc(float pcToAdd) {
        if (pcToAdd == 0) {
            return this;
        }
        return new CssDataTypeUtil(pc + pcToAdd, CssDataType.PC);
    }

    /**
     * 指定pc
     *
     * @param pcToDesign
     * @return
     */
    public CssDataTypeUtil withPC(float pcToDesign) {
        if (pc == pcToDesign) {
            return this;
        }
        return new CssDataTypeUtil(pcToDesign, CssDataType.PC);
    }

    /**
     * 增减pt
     *
     * @param ptToAdd
     * @return
     */
    public CssDataTypeUtil plusPt(float ptToAdd) {
        if (ptToAdd == 0) {
            return this;
        }
        return new CssDataTypeUtil(pt + ptToAdd, CssDataType.PT);
    }

    /**
     * 指定pt
     *
     * @param ptToDesign
     * @return
     */
    public CssDataTypeUtil withPt(float ptToDesign) {
        if (pt == ptToDesign) {
            return this;
        }
        return new CssDataTypeUtil(ptToDesign, CssDataType.PT);
    }

    /**
     * 比较 当前长度 在 设定长度 之前  返回的类型是Boolean类型
     *
     * @return
     */
    public boolean isBefore(CssDataTypeUtil cssDataTypeUtil) {
        return px < cssDataTypeUtil.px;
    }

    /**
     * 比较 当前长度 在 设定长度 之后 返回的类型是Boolean类型
     *
     * @return
     */
    public boolean isAfter(CssDataTypeUtil cssDataTypeUtil) {
        return px > cssDataTypeUtil.px;
    }

    /**
     * 两个相差值
     *
     * @return
     */
    public static CssDataTypeUtil between(CssDataTypeUtil startCssDataTypeUtil, CssDataTypeUtil endCssDataTypeUtil) {
        return startCssDataTypeUtil.until(endCssDataTypeUtil);
    }


    /**
     * 比较 如今的时间 和 设定的时候  相等  返回类型是Boolean类型
     *
     * @return
     */
    public boolean equals(CssDataTypeUtil cssDataTypeUtil) {
        return px == cssDataTypeUtil.px;
    }

    /**
     * 获取相差对象
     *
     * @param endCssDataTypeUtil
     * @return
     */
    public CssDataTypeUtil until(CssDataTypeUtil endCssDataTypeUtil) {
        return new CssDataTypeUtil(endCssDataTypeUtil.px - px, CssDataType.PX);
    }

    /**
     * 单位相差
     *
     * @param endCssDataTypeUtil
     * @return
     */
    public float until(CssDataTypeUtil endCssDataTypeUtil, String cssDataType) {
        return until(endCssDataTypeUtil, getCssDataType(cssDataType));
    }

    /**
     * 单位相差
     *
     * @param endCssDataTypeUtil
     * @param cssDataType
     * @return
     */
    public float until(CssDataTypeUtil endCssDataTypeUtil, CssDataType cssDataType) {
        return endCssDataTypeUtil.get(cssDataType) - get(cssDataType);
    }

    /**
     * 根据单位获取值
     *
     * @param cssDataType
     * @return
     */
    public float get(String cssDataType) {
        return get(getCssDataType(cssDataType));
    }

    /**
     * 根据单位获取值
     *
     * @param cssDataType
     * @return
     */
    public float get(CssDataType cssDataType) {
        switch (cssDataType) {
            case PX:
                return px;
            case CM:
                return cm;
            case MM:
                return mm;
            case Q:
                return Q;
            case IN:
                return in;
            case PC:
                return pc;
            case PT:
                return pt;
            default:
                return 0;
        }
    }

    public Float getPx() {
        return px;
    }

    public Float getCm() {
        return cm;
    }

    public Float getMm() {
        return mm;
    }

    public Float getQ() {
        return Q;
    }

    public Float getIn() {
        return in;
    }

    public Float getPc() {
        return pc;
    }

    public Float getPt() {
        return pt;
    }

    @Override
    public String toString() {
        return "CssDataTypeUtil{" +
                "px=" + px +
                ", cm=" + cm +
                ", mm=" + mm +
                ", Q=" + Q +
                ", in=" + in +
                ", pc=" + pc +
                ", pt=" + pt +
                '}';
    }

    public static void main(String[] args) {
        System.out.println(parse("32px").getPt());
    }
}
