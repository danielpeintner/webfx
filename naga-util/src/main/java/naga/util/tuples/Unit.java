package naga.util.tuples;

/**
 * @author Bruno Salmon
 */
public class Unit<T> {

    private T o1;

    public Unit() {
    }

    public Unit(T o1) {
        this.o1 = o1;
    }

    public T get() {
        return o1;
    }

    public void set(T object) {
        this.o1 = object;
    }
}