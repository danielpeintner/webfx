package naga.toolkit.fxdata.spi.viewer.base;

import naga.toolkit.fxdata.chart.AreaChart;

/**
 * @author Bruno Salmon
 */
public class AreaChartViewerBase
        <C, N extends AreaChart, NB extends AreaChartViewerBase<C, N, NB, NM>, NM extends AreaChartViewerMixin<C, N, NB, NM>>

        extends ChartViewerBase<C, N, NB, NM> {
}