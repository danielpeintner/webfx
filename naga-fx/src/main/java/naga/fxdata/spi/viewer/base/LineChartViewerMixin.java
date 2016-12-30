package naga.fxdata.spi.viewer.base;

import naga.fxdata.chart.LineChart;

/**
 * @author Bruno Salmon
 */
public interface LineChartViewerMixin
        <C, N extends LineChart, NB extends LineChartViewerBase<C, N, NB, NM>, NM extends LineChartViewerMixin<C, N, NB, NM>>

        extends ChartViewerMixin<C, N, NB, NM> {
}