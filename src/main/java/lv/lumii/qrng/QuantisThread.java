package lv.lumii.qrng;

import com.idquantique.quantis.Quantis;
import com.idquantique.quantis.QuantisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferOverflowException;
import java.util.Arrays;

public class QuantisThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(QuantisThread.class);

    private Quantis.QuantisDeviceType deviceType;
    private int deviceIndex;
    private BigBuffer bigBuffer;
    private QuantisThreadPool threadPool;

    private Quantis quantis;
    private Runnable onDone;

    public QuantisThread(Quantis.QuantisDeviceType deviceType, int deviceIndex, BigBuffer bigBuffer, QuantisThreadPool threadPool, Runnable onDone) throws QuantisException {
        this.deviceType = deviceType;
        this.deviceIndex = deviceIndex;
        this.bigBuffer = bigBuffer;
        this.threadPool = threadPool;
        this.onDone = onDone;
        this.setName("QuantisThread-" + deviceType.name() + "#" + deviceIndex);
        quantis = new Quantis(deviceType, deviceIndex);
        quantis.Read(1000); // test; can throw a QunatisException
    }

    @Override
    public String toString() {
        return "QuantisThread for " + deviceType.name() + "#" + deviceIndex;
    }

    @Override
    public void run() {
        logger.info(this + " started...");

        int currentDeviceSpeed = 0;
        try {
            currentDeviceSpeed = quantis.GetModulesDataRate() / 1024 * 1024;
            // ^^^ making divisible by 1024, since our blocks are 1024 bytes each
            threadPool.addSpeed(currentDeviceSpeed);

            // Replenishing the buffer until we are interrupted:
            for (; ; ) {
                if (this.isInterrupted()) {
                    logger.debug(this + " has been interrupted");
                    break;
                }
                logger.debug("Reading " + (currentDeviceSpeed) + " bytes...");

                byte[] bytes;
                // at most 10 tries to read bytes from the quantis device
                for (int tries = 10; ; tries--) {
                    try {
                        bytes = quantis.Read(currentDeviceSpeed);
                        break; // stop the cycle
                    } catch (QuantisException qex) {
                        logger.error("QuantisException in QuantisThread", qex);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new Exception("Recovering from QuantisException interrupted");
                        }
                        if (tries > 0) {
                            logger.error("Recovering after QuantisException (" + tries + " tries remaining)...");
                        } else {
                            throw new Exception("Too many QuantisException-s. We won't try to recover any more.");
                        }
                    }
                }

                int nBlocks = bytes.length / 1024;

                logger.debug("Replenishing with " + nBlocks + " blocks:");
                for (int i = 0; i < nBlocks; i++) {
                    logger.debug("  Replenishing block " + (i + 1) + " of " + nBlocks
                            + " into a buffer with " + bigBuffer.capacityInBlocks() + "-block capacity and "
                            + bigBuffer.usedCapacityInBlocks() + " already used blocks...");
                    boolean isReplenished = false;
                    while (!isReplenished) {
                        try {
                            bigBuffer.replenishWith(Arrays.copyOfRange(bytes, i * 1024, (i + 1) * 1024));
                            isReplenished = true;
                        } catch (BufferOverflowException ex) {
                            int ms = (int) (Math.random() * 1000); // 0-1000 ms
                            logger.debug("Buffer is full, waiting for " + ms + " ms");
                            Thread.sleep(ms);
                        }
                    }
                }
            }

        } catch (InterruptedException e) {
            logger.info(this + " has been interrupted with InterruptedException");
        } catch (Exception e) {
            logger.error("Exception in " + this, e);
        } finally {
            threadPool.addSpeed(-currentDeviceSpeed);
            if (onDone != null)
                onDone.run();
        }

    }

}
