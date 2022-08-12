package javafx.scene.control;

import dev.webfx.kit.mapper.peers.javafxgraphics.markers.HasMaxProperty;
import dev.webfx.kit.mapper.peers.javafxgraphics.markers.HasMinProperty;
import dev.webfx.kit.mapper.peers.javafxgraphics.markers.HasValueProperty;
import dev.webfx.kit.registry.javafxcontrols.JavaFxControlsRegistry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;

/**
 * @author Bruno Salmon
 */
public class Slider extends Control implements
        HasMinProperty,
        HasValueProperty,
        HasMaxProperty {

    public Slider() {
    }

    /**
     * Constructs a Slider control with the specified slider min, max and current value values.
     * @param min Slider minimum value
     * @param max Slider maximum value
     * @param value Slider current value
     */
    public Slider(double min, double max, double value) {
        setMax(max);
        setMin(min);
        setValue(value);
        //adjustValues();
        //initialize();
    }
    private final DoubleProperty maxProperty = new SimpleDoubleProperty(100d);
    @Override
    public DoubleProperty maxProperty() {
        return maxProperty;
    }

    private final DoubleProperty minProperty = new SimpleDoubleProperty(0d);
    @Override
    public DoubleProperty minProperty() {
        return minProperty;
    }

    private final DoubleProperty valueProperty = new SimpleDoubleProperty(0d);
    @Override
    public DoubleProperty valueProperty() {
        return valueProperty;
    }

    private final ObjectProperty<Orientation> orientationProperty = new SimpleObjectProperty<>(Orientation.HORIZONTAL);
    public ObjectProperty<Orientation> orientationProperty() {
        return orientationProperty;
    }

    public void setOrientation(Orientation orientation) { orientationProperty().setValue(orientation); }
    public Orientation getOrientation() { return orientationProperty().get(); }

    static {
        JavaFxControlsRegistry.registerSlider();
    }
}
