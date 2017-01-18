package naga.fxdata.cell.collator;

import emul.javafx.geometry.Pos;
import emul.javafx.scene.layout.FlowPane;
import emul.javafx.scene.layout.HBox;
import emul.javafx.scene.layout.VBox;
import naga.commons.util.Arrays;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public class NodeCollatorRegistry {

    private static Map<String, NodeCollator> collators = new HashMap<>();

    private static NodeCollator hBoxCollator = nodes -> {
        HBox hBox = new HBox((double) 5, nodes);
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    };
    private static NodeCollator vBoxCollator = (children) -> new VBox(children);
    private static NodeCollator flowPaneCollator = (children) -> new FlowPane(children);
    private static NodeCollator firstCollator = nodes -> Arrays.getValue(nodes, 0);

    static {
        registerCollator("hBox", hBoxCollator);
        registerCollator("vBox", vBoxCollator);
        registerCollator("flowPane", flowPaneCollator);
        registerCollator("first", firstCollator);
    }

    private static void registerCollator(String name, NodeCollator collator) {
        collators.put(name.toLowerCase(), collator);
    }

    public static NodeCollator getCollator(String name) {
        return name == null ? null : collators.get(name.toLowerCase());
    }

    public static NodeCollator hBoxCollator() {
        return hBoxCollator;
    }

    public static NodeCollator vBoxCollator() {
        return vBoxCollator;
    }

    public static NodeCollator flowPaneCollator() {
        return flowPaneCollator;
    }

    public static NodeCollator firstCollator() {
        return firstCollator;
    }
}
