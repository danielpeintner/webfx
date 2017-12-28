package naga.fx.spi.gwt.html.peer;

import elemental2.dom.HTMLElement;
import emul.javafx.scene.Node;
import emul.javafx.scene.control.Button;
import emul.javafx.scene.control.ButtonBase;
import naga.fx.spi.gwt.util.HtmlUtil;
import naga.fx.spi.peer.base.ButtonPeerBase;
import naga.fx.spi.peer.base.ButtonPeerMixin;

/**
 * @author Bruno Salmon
 */
public class HtmlButtonPeer
        <N extends Button, NB extends ButtonPeerBase<N, NB, NM>, NM extends ButtonPeerMixin<N, NB, NM>>

        extends HtmlButtonBasePeer<N, NB, NM>
        implements ButtonPeerMixin<N, NB, NM> {

    public HtmlButtonPeer() {
        this((NB) new ButtonPeerBase(), HtmlUtil.createButtonElement());
    }

    public HtmlButtonPeer(NB base, HTMLElement element) {
        super(base, element);
        prepareDomForAdditionalSkinChildren();
    }

    @Override
    public void updateGraphic(Node graphic) {
        super.updateGraphic(graphic);
        // Restoring pointer events (were disabled by prepareDomForAdditionalSkinChildren()) in case the graphic is clickable (ex: radio button)
        if (graphic instanceof ButtonBase)
            HtmlUtil.setStyleAttribute(getChildrenContainer(), "pointer-events", "auto");

    }
}
