package lv.lumii.qrng;

import com.idquantique.quantis.Quantis;
import com.idquantique.quantis.QuantisException;
import org.cactoos.Scalar;
import org.cactoos.scalar.Sticky;
import org.cactoos.scalar.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class QuantisThreadPool {

    private static Logger logger = LoggerFactory.getLogger(QuantisThreadPool.class);

    private BigBuffer bigBuffer;
    private Unchecked<List<Thread>> threads;


    private long totalSpeed = 0;

    /*package-visibility*/
    synchronized void addSpeed(long delta) {
        totalSpeed += delta;
        logger.debug("Current speed is " + totalSpeed + " bytes/sec");
    }

    public QuantisThreadPool(BigBuffer bigBuffer) {
        this.bigBuffer = bigBuffer;
        this.threads = new Unchecked<>(new Sticky<>(() -> createSuspendedThreads()));
    }

    private List<Thread> createSuspendedThreads() throws QuantisException {
        int countPci = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI);
        logger.debug("Found " + countPci + " Quantis PCI devices.");
        int countUsb = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB);
        logger.debug("Found " + countUsb + " Quantis USB devices.");

        List<Thread> threads = new LinkedList<>();

        for (int i = 0; i < countPci; i++) {
            Thread t = new QuantisThread(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI, i, bigBuffer, this);
            threads.add(t);
        }

        for (int i = 0; i < countUsb; i++) {
            Thread t = new QuantisThread(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB, i, bigBuffer, this);
            threads.add(t);
        }

        if ((countPci == 0) && (countUsb == 0))
            throw new QuantisException("No Quantis device installed.");

        return threads;
    }

    public void startAll() throws Exception {
        try {
            for (Thread t : this.threads.value()) {
                t.start();
            }
        } catch (Exception e) {
            stopAll();
            throw new Exception("No threads running, since an exception occurred.", e);
        }
    }

    public void stopAll() throws Exception {
        for (Thread t : this.threads.value()) {
            if (t.isAlive()) {
                logger.debug("Interrupting " + t + " on stopAll()");
                t.interrupt();
            }
        }
    }

    public long getMaxReplenishingSpeed() {
        return totalSpeed;
    }

    /**
     * @param args the command line arguments
     * @throws QuantisException
     */
    public static void main(String[] args) throws Exception { // test
        System.out.println("Java is running on " + System.getProperty("os.name") + "/" + System.getProperty("os.arch") + "\n");

        System.out
                .println("Searching Quantis library in following path:\n" + System.getProperty("java.library.path") + "\n");

        System.out.println("Using Quantis Library v" + Quantis.GetLibVersion() + "\n");

        BigBuffer buf = new BigBuffer(10);
        QuantisThreadPool pool = new QuantisThreadPool(buf);
        System.out.println("Total replenishing speed (initial): " + pool.getMaxReplenishingSpeed());
        pool.startAll();

        for (int m = 1; m <= 10; m++) {
            Thread.sleep(1000);
            System.out.println("Total replenishing speed: " + pool.getMaxReplenishingSpeed());
        }

        pool.stopAll();

        for (int m = 1; m <= 10; m++) {
            Thread.sleep(1000);
            System.out.println("Total replenishing speed (after stopAll): " + pool.getMaxReplenishingSpeed());
        }


    }

}
