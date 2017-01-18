package naga.fx.spi.peer.base;

import javafx.scene.control.TextInputControl;
import javafx.scene.text.Font;

/**
 * @author Bruno Salmon
 */
public interface TextInputControlPeerMixin
        <N extends TextInputControl, NB extends TextInputControlPeerBase<N, NB, NM>, NM extends TextInputControlPeerMixin<N, NB, NM>>

        extends ControlPeerMixin<N, NB, NM> {

    void updateFont(Font font);

    void updateText(String text);

    void updatePrompt(String prompt);
}
