package naga.fx.spi.gwt.util;

import elemental2.Element;
import naga.commons.util.collection.Collections;
import naga.fx.scene.paint.Color;
import naga.fx.scene.paint.CycleMethod;
import naga.fx.scene.paint.LinearGradient;
import naga.fx.scene.paint.Stop;
import naga.fx.scene.shape.StrokeLineCap;
import naga.fx.scene.shape.StrokeLineJoin;
import naga.fx.scene.shape.StrokeType;

import static elemental2.Global.document;

/**
 * @author Bruno Salmon
 */
public class SvgUtil {

    private final static String svgNS = "http://www.w3.org/2000/svg";

    public static Element createSvgElement(String tag) {
        return document.createElementNS(svgNS, tag);
    }

    public static /*SVGElement Elemental2 compilation error */ Element createSvgElement() {
        return /*SVGElement*/ createSvgElement("svg");
    }

    public static Element createSvgDefs() {
        return createSvgElement("defs");
    }

    public static Element createSvgRectangle() {
        return createSvgElement("rect");
    }

    public static Element createSvgCircle() {
        return createSvgElement("circle");
    }

    public static Element createSvgText() {
        return createSvgElement("text");
    }

    public static Element createSvgGroup() {
        return createSvgElement("g");
    }

    public static String getDefUrl(Element defElement) {
        return defElement == null ? null : "url(#" + defElement.getAttribute("id") + ")";
    }

    private static int clipSeq;
    public static Element createClipPath() {
        return HtmlUtil.setAttribute(createSvgElement("clipPath"), "id", "CLIP" + ++clipSeq);
    }

    private static int lgSeq;
    public static Element createLinearGradient() {
        return HtmlUtil.setAttribute(createSvgElement("linearGradient"), "id", "LG" + ++lgSeq);
    }

    private static int filterSeq;
    public static Element createFilter() {
        return HtmlUtil.setAttribute(createSvgElement("filter"), "id", "F" + ++filterSeq);
    }

    public static Element updateLinearGradient(LinearGradient lg, Element svgLg) {
        if (svgLg == null)
            svgLg = createLinearGradient();
        svgLg.setAttribute("x1", lg.getStartX());
        svgLg.setAttribute("y1", lg.getStartY());
        svgLg.setAttribute("x2", lg.getEndX());
        svgLg.setAttribute("y2", lg.getEndY());
        CycleMethod cycleMethod = lg.getCycleMethod();
        svgLg.setAttribute("spreadMethod", cycleMethod == CycleMethod.REPEAT ? "repeat" : cycleMethod == CycleMethod.REFLECT ? "reflect" : "pad");
        svgLg.setAttribute("gradientUnits", lg.isProportional() ? "objectBoundingBox" : "userSpaceOnUse");
        return HtmlUtil.setChildren(svgLg, Collections.convert(lg.getStops(), SvgUtil::toSvgStop));
    }

    private static Element toSvgStop(Stop stop) {
        Element svgStop = createSvgElement("stop");
        svgStop.setAttribute("offset", (stop.getOffset() * 100) + "%");
        Color stopColor = stop.getColor();
        svgStop.setAttribute("stop-color", HtmlPaints.toCssOpaqueColor(stopColor));
        if (!stopColor.isOpaque())
            svgStop.setAttribute("stop-opacity", stopColor.getOpacity());
        return svgStop;
    }

    public static String toSvgStrokeLineCap(StrokeLineCap strokeLineCap) {
        if (strokeLineCap != null)
            switch (strokeLineCap) {
                case BUTT: return "butt";
                case ROUND: return "round";
                case SQUARE: return "square";
            }
        return null;
    }

    public static String toSvgStrokeLineJoin(StrokeLineJoin strokeLineJoin) {
        if (strokeLineJoin != null)
            switch (strokeLineJoin) {
                case BEVEL: return "bevel";
                case MITER: return "miter";
                case ROUND: return "round";
            }
        return null;
    }

    public static String toSvgStrokeAlignment(StrokeType strokeType) {
        if (strokeType != null)
            switch (strokeType) {
                case CENTERED: return "center";
                case INSIDE: return "inner";
                case OUTSIDE: return "outer";
            }
        return null;
    }
}