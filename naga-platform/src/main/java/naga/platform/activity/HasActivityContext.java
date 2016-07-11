package naga.platform.activity;

/**
 * @author Bruno Salmon
 */
public interface HasActivityContext<C extends ActivityContext<C>> {

    C getActivityContext();

}
