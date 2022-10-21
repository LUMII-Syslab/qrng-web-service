package lv.lumii.qrng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.websocket.api.*;
import com.google.gson.*;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;


public class QrngWebSocketClient extends WebSocketAdapter {
    // javadoc for WebSocketAdapter:
    // https://www.eclipse.org/jetty/javadoc/jetty-11/org/eclipse/jetty/websocket/api/WebSocketAdapter.html

    private static Logger logger = LoggerFactory.getLogger(QrngWebSocketClient.class);

    private DesiredSize desiredSize;


    private class MyWriteCallback implements WriteCallback {

    }

    private WriteCallback writeCallback = new MyWriteCallback();

    private BigBuffer bigBuffer;
    private WaitingUsers<QrngWebSocketClient> waitingUsers;

    public QrngWebSocketClient(BigBuffer bigBuffer, WaitingUsers waitingUsers) {
        this.desiredSize = new DesiredSize(0);
        this.bigBuffer = bigBuffer;
        this.waitingUsers = waitingUsers;
    }

    @Override
    public String toString() {
        return "wsclient" + this.hashCode();
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        logger.info("Socket connected: " + this + " @ " + Instant.now().toString());
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        // "this" is our "user"
        waitingUsers.kick(this);
        logger.error("Socket disconnected: " + this + " @ " + Instant.now().toString());
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);

        // "this" is our "user"
        waitingUsers.kick(this);
        logger.error("Socket error: "
                + this + " @ " + Instant.now().toString() + " cause=" + cause.getMessage());
    }


    @Override
    public void onWebSocketText(final String message) {
        System.out.println("on text: " + message);
        JsonObject retVal = new JsonObject();
        retVal.addProperty("jsonrpc", "2.0");

        String version;
        JsonObject o;
        long id;
        String methodName;
        try {
            o = new Gson().fromJson(message, JsonObject.class);
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32700);
            error.addProperty("message", "Parse error");

            retVal.add("error", error);
            retVal.add("id", null);
            try {
                getRemote().sendString(retVal.toString());
            } catch (IOException ex) {
                logger.error("Could not send jsonrpc response to " + getRemote().getRemoteAddress().toString());
            }
            return;
        }

        try {
            id = o.get("id").getAsLong();
            methodName = o.get("method").getAsString();
            assert "consume".equals(methodName);
            version = o.get("jsonrpc").getAsString();
            assert "2.0".equals(version);
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32600);
            error.addProperty("message", "Invalid Request");

            retVal.add("error", error);
            retVal.add("id", null);
            try {
                getRemote().sendString(retVal.toString());
            } catch (IOException ex) {
                logger.error("Could not send jsonrpc response to " + getRemote().getRemoteAddress().toString());
            }
            return;
        }

        try {
            waitingUsers.enqueue(this);
        } catch (BufferOverflowException | KeyAlreadyExistsException e) {
            retVal.addProperty("id", id);
            retVal.addProperty("result", false);
            // ^^ too many users are waiting or the given user is already in the queue
            try {
                getRemote().sendString(retVal.toString());
            } catch (IOException ex) {
                logger.error("Could not send jsonrpc response to " + getRemote().getRemoteAddress().toString());
            }
            return;
        }
    }

    private int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip(); //need flip
        return buffer.getInt();
    }

    /**
     * Receives an integer (=#of desired random bytes) from the client
     * and updates the previously desired #of random bytes.
     * The new value will be considered the next time, when sending bytes to the client.
     * @param payload
     * @param offset
     * @param len
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // alternative to json-rpc: an integer n (little-endian) has to be passed;
        // n is the actual number of random bytes the client still wants from us
        int n = bytesToInt(Arrays.copyOfRange(payload, offset, offset+len));
        synchronized (desiredSize) {
            desiredSize.update(n);
            logger.debug("Socket "+this+" was requested to send "+n+" random bytes");

            if (!desiredSize.fulfilled())
                waitingUsers.enqueue(this);
        }
    }

    private int step=0;
    /**
     * Sends the given block of random bytes to the client.
     * If the client's desiredSize has not been fulfilled, enqueue the client once again.
     * @param bytes
     */
    public void sendRandomBytes(byte[] bytes) {
        synchronized (desiredSize) {
            if (desiredSize.size() < bytes.length) // fewer bytes desired than the #bytes we are offering
                bytes = Arrays.copyOfRange(bytes, 0, desiredSize.size());
            if (bytes.length>0) {
                this.getRemote().sendBytes(ByteBuffer.wrap(bytes), this.writeCallback); // async
                logger.debug("Socket "+this+" has just sent "+bytes.length+" random bytes");
                desiredSize.fulfill(bytes.length);

                if (!desiredSize.fulfilled()) {
                    try {
                        waitingUsers.enqueue(this);
                    }
                    catch(KeyAlreadyExistsException e) {
                        // it is ok, if the key already exists
                    }

                }
            }
        }
    }

}