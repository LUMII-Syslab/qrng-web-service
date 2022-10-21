package lv.lumii.qrng;

import com.idquantique.quantis.Quantis;
import com.idquantique.quantis.QuantisException;
import org.cactoos.scalar.Sticky;
import org.cactoos.scalar.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class QuantisThreadPool {

    private static final Logger logger = LoggerFactory.getLogger(QuantisThreadPool.class);

    private final BigBuffer bigBuffer;
    private final Unchecked<List<Thread>> threads;
    private final Runnable onAllStopped;


    private long totalSpeed = 0;
    private long totalStarted = 0;

    /*package-visibility*/
    synchronized void addSpeed(long delta) {
        totalSpeed += delta;
        logger.info("Current speed is " + totalSpeed + " bytes/sec");
    }

    public QuantisThreadPool(BigBuffer bigBuffer, Runnable onAllStopped) {
        this.bigBuffer = bigBuffer;
        this.onAllStopped = onAllStopped;
        this.threads = new Unchecked<>(new Sticky<>(this::createSuspendedThreads)); // true OOP style!
    }

    private List<Thread> createSuspendedThreads() throws QuantisException {
        int countPci = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI);
        logger.debug("Found " + countPci + " Quantis PCI devices.");
        int countUsb = Quantis.Count(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB);
        logger.debug("Found " + countUsb + " Quantis USB devices.");

        List<Thread> threads = new LinkedList<>();

        Runnable onOneStopped = new Runnable() {
            @Override
            public void run() {
                    totalStarted--;
                    if (totalStarted==0 && onAllStopped!=null)
                        onAllStopped.run();
            }
        };

        for (int i = 0; i < countPci; i++) {
            try {
                Thread t = new QuantisThread(Quantis.QuantisDeviceType.QUANTIS_DEVICE_PCI, i, bigBuffer, this, onOneStopped);
                threads.add(t);
            }
            catch (QuantisException e) {
                String msg = "Quantis device PCI#"+i+" is not working. Do not using it.";
                logger.error(msg);
            }

    }

        for (int i = 0; i < countUsb; i++) {
            try {
                Thread t = new QuantisThread(Quantis.QuantisDeviceType.QUANTIS_DEVICE_USB, i, bigBuffer, this, onOneStopped);
                threads.add(t);
            }
            catch (QuantisException e) {
                String msg = "Quantis device USB#"+i+" is not working. Do not using it.";
                logger.error(msg);
            }
        }

        if ((countPci == 0) && (countUsb == 0))
            throw new QuantisException("No Quantis device installed or none is usable.");

        return threads;
    }

    public void startAll() throws Exception {
        try {
            for (Thread t : this.threads.value()) {
                t.start();
                totalStarted++;
            }
        } catch (Exception e) {
            stopAll();
            throw new Exception("No threads running, since an exception occurred.", e);
        }
    }

    public void stopAll() {
        for (Thread t : this.threads.value()) {
            if (t.isAlive()) {
                logger.debug("Interrupting " + t + " on stopAll()");
                t.interrupt();
            }
        }
        totalStarted = 0;
    }

    public long getMaxReplenishingSpeed() {
        return totalSpeed;
    }

    /**
     * @param args the command line arguments
     * @throws Exception any exception that forces the main app to terminate
     */
    public static void main(String[] args) throws Exception { // test
        System.out.println("Java is running on " + System.getProperty("os.name") + "/" + System.getProperty("os.arch") + "\n");

        System.out
                .println("Searching Quantis library in following path:\n" + System.getProperty("java.library.path") + "\n");

        System.out.println("Using Quantis Library v" + Quantis.GetLibVersion() + "\n");

        BigBuffer buf = new BigBuffer(10);
        QuantisThreadPool pool = new QuantisThreadPool(buf, ()->System.out.println("Zero speed reached"));
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
