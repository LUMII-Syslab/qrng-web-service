package lv.lumii.qrng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.*;

public class BigBuffer {

    private static Logger logger = LoggerFactory.getLogger(BigBuffer.class);

    /**
     * queue - the queue of 1KiB blocks (not-thread-safe, we will synchronize manually).
     */
    private Queue<byte[]> blocks;
    private static final int DEFAULT_MAX_BLOCKS = 100*1024;
    private final int maxBlocks;
    // there will be at most maxBlocks 1 KiB blocks;
    // thus, the default max RAM usage by the buffer is 100 MiB

    public BigBuffer() {
        this(DEFAULT_MAX_BLOCKS);
    }

    public BigBuffer(int maxBlocks) {
        if (maxBlocks >= 1)
            this.maxBlocks = maxBlocks;
        else
            this.maxBlocks = DEFAULT_MAX_BLOCKS;

        blocks = new LinkedList<>();
    }

    public synchronized void replenishWith(byte[] bytes) throws BufferOverflowException {
        if (bytes.length != 1024)
            throw new IllegalArgumentException("Each block must be 1024 bytes long (got "+bytes.length+" bytes)");
        if (blocks.size() >= maxBlocks)
            throw new BufferOverflowException();
        blocks.add(bytes);

    }

    public synchronized byte[] consume() throws BufferUnderflowException {
        if (blocks.size() == 0)
            throw new BufferUnderflowException();
        return blocks.remove();
    }

    public int capacityInBlocks() {
        return maxBlocks;
    }


}
