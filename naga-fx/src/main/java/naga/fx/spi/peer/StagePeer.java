package naga.fx.spi.peer;

import javafx.stage.Stage;

/**
 * @author Bruno Salmon
 */
public interface StagePeer extends WindowPeer {

    default Stage getStage() {
        return (Stage) getWindow();
    }

}
