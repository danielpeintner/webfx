package dev.webfx.kit.mapper.peers.javafxgraphics.gwt.shared;

import dev.webfx.kit.mapper.peers.javafxgraphics.emul_coupling.base.ScenePeerBase;
import dev.webfx.kit.mapper.peers.javafxgraphics.emul_coupling.base.StagePeerBase;
import dev.webfx.kit.mapper.peers.javafxgraphics.gwt.html.HtmlScenePeer;
import dev.webfx.kit.mapper.peers.javafxgraphics.gwt.util.HtmlUtil;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;


/**
 * @author Bruno Salmon
 */
public final class GwtPrimaryStagePeer extends StagePeerBase {

    public GwtPrimaryStagePeer(Stage stage) {
        super(stage);
        // Disabling browser horizontal and vertical scroll bars
        HtmlUtil.setStyleAttribute(document.documentElement, "overflow", "hidden");
        // Removing the default margin around the body so it fills the whole browser tab
        HtmlUtil.setStyleAttribute(document.body, "margin", "0");
        // Disabling default text selection (as in JavaFX) to avoid nasty selection graphical elements (buttons etc...)
        HtmlUtil.setStyleAttribute(document.body, "user-select", "none");
        // Considering the current window size
        changedWindowSize();
/* Finally commented and replaced by window resize event to avoid lasting activity on animation frame (Lighthouse downgrade)
        // Checking the window size on each pulse (window resize event is not enough because it doesn't detect vertical scroll bar apparition)
        //UiScheduler.schedulePeriodicInAnimationFrame(this::changedWindowSize, AnimationFramePass.PROPERTY_CHANGE_PASS);
*/
        window.addEventListener("resize", e -> changedWindowSize());
    }

    @Override
    protected ScenePeerBase getScenePeer() {
        Scene scene = getWindow().getScene();
        return scene == null ? null : (ScenePeerBase) scene.impl_getPeer();
    }

    @Override
    protected double getPeerWindowWidth() {
        return window.innerWidth;
    }

    @Override
    protected double getPeerWindowHeight() {
        return window.innerHeight;
    }

    @Override
    public void setTitle(String title) {
        document.title = title;
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void onSceneRootChanged() {
        setWindowContent(((HtmlScenePeer) getWindow().getScene().impl_getPeer()).getSceneNode());
    }

    private void setWindowContent(elemental2.dom.Node content) {
        HtmlUtil.setBodyContent(content);
    }
}
