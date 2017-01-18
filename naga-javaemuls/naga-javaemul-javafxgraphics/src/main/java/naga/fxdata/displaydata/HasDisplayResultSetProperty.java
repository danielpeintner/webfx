package naga.fxdata.displaydata;

import emul.javafx.beans.property.Property;

/**
 * @author Bruno Salmon
 */

public interface HasDisplayResultSetProperty {

    Property<DisplayResultSet> displayResultSetProperty();
    default void setDisplayResultSet(DisplayResultSet selected) { displayResultSetProperty().setValue(selected); }
    default DisplayResultSet getDisplayResultSet() { return displayResultSetProperty().getValue(); }

}
