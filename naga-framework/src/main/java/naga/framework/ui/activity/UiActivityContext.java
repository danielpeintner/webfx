package naga.framework.ui.activity;

import javafx.beans.property.Property;
import naga.platform.activity.ActivityContext;
import naga.platform.client.url.history.History;
import naga.platform.json.spi.JsonObject;
import naga.toolkit.properties.markers.HasNodeProperty;
import naga.toolkit.spi.nodes.GuiNode;

/**
 * @author Bruno Salmon
 */
public interface UiActivityContext<C extends UiActivityContext<C>> extends ActivityContext<C>, HasNodeProperty {

    History getHistory();

    JsonObject getParams();

    Property<GuiNode> nodeProperty();

    Property<GuiNode> mountNodeProperty();

    static ActivityContext create(ActivityContext parent) {
        return new UiActivityContextImpl(parent, UiActivityContext::create);
    }

}
