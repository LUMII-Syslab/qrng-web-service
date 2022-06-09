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

    public QuantisThread(Quantis.QuantisDeviceType deviceType, int deviceIndex, BigBuffer bigBuffer, QuantisThreadPool threadPool) {
        this.deviceType = deviceType;
        this.deviceIndex = deviceIndex;
        this.bigBuffer = bigBuffer;
        this.threadPool = threadPool;
    }

    @Override
    public String toString() {
        return "QuantisThread for " + deviceType.name() + "#" + deviceIndex;
    }

    @Override
    public void run() {
        Quantis quantis = new Quantis(deviceType, deviceIndex);
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

                byte[] bytes = quantis.Read(currentDeviceSpeed);
                int nBlocks = bytes.length / 1024;

                logger.debug("Replenishing " + nBlocks + " blocks...");
                for (int i = 0; i < nBlocks; i++) {
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

        } catch (QuantisException e) {
            logger.error("QuantisException in QuantisThread", e);
        } catch (InterruptedException e) {
            logger.info(this + " has been interrupted with InterruptedException");
        } catch (Exception e) {
            logger.error("Error in QuantisThread", e);
        } finally {
            threadPool.addSpeed(-currentDeviceSpeed);
        }

    }

}
