package naga.fx.spi.gwt.html.peer;

import elemental2.dom.*;
import emul.javafx.scene.Node;
import emul.javafx.scene.Parent;
import emul.javafx.scene.layout.HBox;
import emul.javafx.scene.layout.Region;
import naga.util.Strings;
import naga.util.tuples.Unit;
import naga.fx.spi.gwt.util.DomType;
import naga.fx.spi.gwt.util.HtmlPaints;
import naga.fx.spi.gwt.util.HtmlUtil;
import naga.fxdata.cell.renderer.ImageTextRenderer;
import naga.fxdata.control.DataGrid;
import naga.fxdata.displaydata.*;
import naga.fxdata.spi.peer.base.DataGridPeerBase;
import naga.fxdata.spi.peer.base.DataGridPeerMixin;

import static naga.fx.spi.gwt.util.HtmlUtil.*;

/**
 * @author Bruno Salmon
 */
public class HtmlDataGridPeer
        <N extends DataGrid, NB extends DataGridPeerBase<HTMLTableCellElement, N, NB, NM>, NM extends DataGridPeerMixin<HTMLTableCellElement, N, NB, NM>>

        extends HtmlRegionPeer<N, NB, NM>
        implements DataGridPeerMixin<HTMLTableCellElement, N, NB, NM>, HtmlLayoutMeasurable {


    private final HTMLTableElement table = HtmlUtil.createTableElement();
    private final HTMLTableSectionElement tHead = (HTMLTableSectionElement) table.createTHead();
    private HTMLTableRowElement tHeadRow = (HTMLTableRowElement) tHead.insertRow(0);
    private final HTMLTableSectionElement tBody = createElement("tbody");
    private double scrollTop;

    public HtmlDataGridPeer() {
        this((NB) new DataGridPeerBase(), HtmlUtil.createDivElement());
    }

    public HtmlDataGridPeer(NB base, HTMLElement element) {
        super(base, element);
        table.appendChild(tBody);
        setChild(element, table);
        setStyleAttribute(table, "width", "100%");
        // Capturing scroll position (in scrollTop field)
        element.onscroll = p0 -> {
            scrollTop = element.scrollTop;
            return null;
        };
        // Restoring scroll position when visiting back the page
        HtmlUtil.onNodeInsertedIntoDocument(element, () -> element.scrollTop = scrollTop);
    }

    @Override
    public void updateHeaderVisible(boolean headerVisible) {
        if (headerVisible)
            table.insertBefore(tHead, tBody);
        else
            table.removeChild(tHead);
    }

    @Override
    public void updateFullHeight(boolean fullHeight) {
        setElementStyleAttribute("overflow-y", fullHeight ? "hidden" : "auto");
        if (fullHeight)
            getElement().scrollTop = scrollTop = 0;
    }

    @Override
    public double prefHeight(double width) {
        setElementStyleAttribute("overflow-y", "visible");
        double height = measureHeight(width);
        setElementStyleAttribute("overflow-y", getNode().isFullHeight() ? "hidden" : "auto");
        return height;
    }

    @Override
    public void updateSelectionMode(SelectionMode mode) {
    }

    @Override
    public void updateDisplaySelection(DisplaySelection selection) {
        Unit<Integer> lastUnselectedRowIndex = new Unit<>(0);
        DataGrid node = getNode();
        DisplaySelection displaySelection = node.getDisplaySelection();
        if (displaySelection != null)
            displaySelection.forEachRow(rowIndex -> {
                applyVisualSelectionRange(lastUnselectedRowIndex.get(), rowIndex - 1, false);
                applyVisualSelectionRange(rowIndex, rowIndex, true);
                lastUnselectedRowIndex.set(rowIndex + 1);
            });
        DisplayResultSet rs = node.getDisplayResultSet();
        if (rs != null)
            applyVisualSelectionRange(lastUnselectedRowIndex.get(), rs.getRowCount(), false);
    }

    private void applyVisualSelectionRange(int firstRow, int lastRow, boolean selected) {
        HTMLCollection<HTMLTableRowElement> rows = tBody.rows;
        lastRow = Math.min(lastRow, rows.getLength() - 1);
        for (int trIndex = firstRow; trIndex <= lastRow; trIndex++)
            setPseudoClass(rows.item(trIndex), "selected", selected);
    }

    @Override
    public void updateResultSet(DisplayResultSet rs) {
        DataGrid node = getNode();
        node.setDisplaySelection(null);
        NB base = getNodePeerBase();
        HtmlUtil.removeChildren(tHeadRow);
        HtmlUtil.removeChildren(tBody);
        base.fillGrid(rs);
        if (rs != null) {
            int rowCount = rs.getRowCount();
            int columnCount = rs.getColumnCount();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                HTMLTableRowElement tBodyRow = (HTMLTableRowElement) tBody.insertRow(-1);
                int finalRowIndex = rowIndex;
                tBodyRow.onclick = a -> {
                    DisplaySelection displaySelection = node.getDisplaySelection();
                    if (node.getSelectionMode() != SelectionMode.DISABLED && (displaySelection == null || displaySelection.getSelectedRow() != finalRowIndex))
                        displaySelection = DisplaySelection.createSingleRowSelection(finalRowIndex);
                    else
                        displaySelection = null;
                    node.setDisplaySelection(displaySelection);
                    return null;
                };
                String rowStyle = base.getRowStyle(rowIndex);
                if (rowStyle != null)
                    tBodyRow.className = rowStyle;
                tBodyRow.style.background = HtmlPaints.toCssPaint(base.getRowBackground(rowIndex), DomType.HTML);
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    if (base.isDataColumn(columnIndex))
                        base.fillCell((HTMLTableCellElement) tBodyRow.insertCell(-1), rowIndex, columnIndex);
                }
            }
        }
        clearCache();
    }

    @Override
    public void setUpGridColumn(int gridColumnIndex, int rsColumnIndex, DisplayColumn displayColumn) {
        Label label = displayColumn.getLabel();
        getNodePeerBase().fillCell((HTMLTableCellElement) tHeadRow.insertCell(gridColumnIndex), new Object[]{label.getIconPath(), label.getText()}, displayColumn, ImageTextRenderer.SINGLETON);
    }

    @Override
    public void setCellContent(HTMLTableCellElement cell, Node content, DisplayColumn displayColumn) {
        DisplayStyle displayStyle = displayColumn.getStyle();
        String textAlign = displayStyle.getTextAlign();
        Double prefWidth = displayStyle.getPrefWidth();
        CSSStyleDeclaration cssStyle = cell.style;
        if (prefWidth != null) {
            String prefWidthPx = toPx(prefWidth);
            cssStyle.width = CSSProperties.WidthUnionType.of(prefWidthPx); // Enough for Chrome
            cssStyle.maxWidth = CSSProperties.MaxWidthUnionType.of(prefWidthPx); // Required for FireFox
            cssStyle.tableLayout = "fixed";
            if (textAlign == null)
                textAlign = "center";
        }
        if (textAlign != null)
            cssStyle.textAlign = textAlign;
        Element contentElement = toContainerElement(content, getNode().getScene());
        if (contentElement != null) {
            setStyleAttribute(contentElement, "position", "relative");
            //setStyleAttribute(contentElement, "width", null);
            //setStyleAttribute(contentElement, "height", null);
            if (content instanceof HBox) { // temporary code for HBox, especially for table headers
                double spacing = content instanceof HBox ? ((HBox) content).getSpacing() : 0;
                for (int i = 0, n = (int) contentElement.childElementCount; i < n; i++) {
                    elemental2.dom.Node childNode = contentElement.childNodes.item(i);
                    if (childNode instanceof HTMLImageElement && Strings.isEmpty(((HTMLImageElement) childNode).src)) {
                        contentElement.removeChild(childNode);
                        i--; n--;
                    } else {
                        setStyleAttribute(childNode, "position", "relative");
                        if (spacing > 0 && i < n - 1)
                            setStyleAttribute(childNode, "margin-right", toPx(spacing));
                    }
                }
            } else if (content instanceof Parent) {
                if (content instanceof Region) {
                    Region region = (Region) content;
                    if (contentElement.parentNode == null)
                        contentElement.ownerDocument.documentElement.appendChild(contentElement);
                    double width = region.prefWidth(-1);
                    double height = region.prefHeight(-1);
                    region.resize(width, height);
                }
                ((Parent) content).layout();
            }
            cell.appendChild(contentElement);
        }
    }

/*
    private final HtmlLayoutCache cache = new HtmlLayoutCache();
    @Override
    public HtmlLayoutCache getCache() {
        return cache;
    }
*/
}
