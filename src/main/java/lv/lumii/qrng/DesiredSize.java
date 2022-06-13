package lv.lumii.qrng;

/**
 * A class representing a non-negative integer that corresponds to some size (e.g., buffer size in bytes),
 * which can be updated at runtime.
 */
public class DesiredSize {

    private int size;

    public DesiredSize() {
        this(0);
    }

    public DesiredSize(int size) {
        this.size =0;
    }

    public boolean fulfilled() {
        return (size == 0);
    }

    public int size() {
        return this.size;
    }

    public void fulfill(int fulfilledSize) {
        if (fulfilledSize > size)
            fulfilledSize = size; // cannot fulfill more than desired
        size -= fulfilledSize;
    }

    public void update(int newSize) {
        if (newSize < 0)
            newSize = 0;
        size = newSize;
    }
}
