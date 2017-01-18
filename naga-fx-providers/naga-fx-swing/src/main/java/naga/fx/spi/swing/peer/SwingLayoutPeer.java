package naga.fx.spi.swing.peer;

import naga.fx.spi.peer.base.RegionPeerBase;
import emul.com.sun.javafx.geom.Point2D;
import emul.javafx.scene.layout.Region;
import naga.fx.spi.peer.base.RegionPeerMixin;

/**
 * @author Bruno Salmon
 */
public class SwingLayoutPeer
        <N extends Region, NB extends RegionPeerBase<N, NB, NM>, NM extends RegionPeerMixin<N, NB, NM>>
        extends SwingRegionPeer<N, NB, NM> {

    public SwingLayoutPeer() {
        super((NB) new RegionPeerBase<N, NB, NM>());
    }

    @Override
    public boolean containsPoint(Point2D point) {
        return false;
    }

}
