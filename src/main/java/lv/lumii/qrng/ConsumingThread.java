package lv.lumii.qrng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ConsumingThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(ConsumingThread.class);

    private BigBuffer bigBuffer;
    private WaitingUsers<QrngWebSocketClient> waitingUsers;

    public ConsumingThread(BigBuffer bigBuffer, WaitingUsers<QrngWebSocketClient> waitingUsers) {
        this.bigBuffer = bigBuffer;
        this.waitingUsers = waitingUsers;
        this.setName("ConsumingThread");
    }

    @Override
    public void run() {
        for (; ; ) {
            byte[] block = null;
            try {
                logger.debug("getting (consuming) a block...");
                block = bigBuffer.consume(); // can throw BufferUnderflowException
                logger.debug("getting (taking) a user wanting random bytes...");
                QrngWebSocketClient client = waitingUsers.takeUser(); // blocking
                logger.debug("sending the block to the user...");
                client.sendRandomBytes(block); // can re-enqueue the client, if not all desired size has been fulfilled
            } catch (BufferUnderflowException bufferEmpty) {
                logger.debug("ConsumerThread buffer is empty! Waiting 1000 ms for the buffer to be filled.");
                try {
                    // wait some time in hope that bigBuffer gets replenished
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return; // e.g., SIGTERM received
                }
            } catch (Exception ex) {
                logger.error("Exception in ConsumingThread", ex);
            }
        }
    }

}
