package webfx.fxkit.mapper.spi.impl.peer.markers;

import javafx.beans.property.Property;

/**
 * @author Bruno Salmon
 */
public interface HasPivotXProperty {

    Property<Double> pivotXProperty();
    default void setPivotX(Double pivotX) { pivotXProperty().setValue(pivotX); }
    default Double getPivotX() { return pivotXProperty().getValue(); }

}