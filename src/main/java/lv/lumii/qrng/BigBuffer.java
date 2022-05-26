package lv.lumii.qrng;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Queue;

public class BigBuffer {

    /**
     * queue - the queue of 1KiB blocks (not-thread-safe, we will synchronize manually).
     */
    private Queue<byte[]> blocks;
    private static final int DEFAULT_MAX_BLOCKS = 100*1024;
    private int maxBlocks;
    // there will be at most maxBlocks 1KiB blocks;
    // thus, the default max RAM usage by the buffer is 100 MiB

    public BigBuffer() {
        this(DEFAULT_MAX_BLOCKS);
    }

    public BigBuffer(int maxBlocks) {
        if (maxBlocks >= 1)
            this.maxBlocks = maxBlocks;
        // else keep the default value
    }

    public synchronized void replenish(byte[] randomBlock1KiB) throws BufferOverflowException {
        if (blocks.size() >= maxBlocks)
            throw new BufferOverflowException();

        blocks.add(randomBlock1KiB);
    }

    public synchronized byte[] consume() throws BufferUnderflowException {
        if (blocks.size() > 0)
            throw new BufferUnderflowException();
        return blocks.remove();
    }


}
