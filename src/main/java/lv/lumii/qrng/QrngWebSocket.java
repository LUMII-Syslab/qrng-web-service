package lv.lumii.qrng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.websocket.api.*;
import com.google.gson.*;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.nio.BufferOverflowException;

public class QrngWebSocket extends WebSocketAdapter {
    // javadoc for WebSocketAdapter:
    // https://www.eclipse.org/jetty/javadoc/jetty-11/org/eclipse/jetty/websocket/api/WebSocketAdapter.html

    private static Logger logger = LoggerFactory.getLogger(QrngWebSocket.class);

    private class MyWriteCallback implements WriteCallback {

    }

    private WriteCallback writeCallback = new MyWriteCallback();

    private BigBuffer bigBuffer;
    private UsersQueue<QrngWebSocket> usersQueue;

    public QrngWebSocket(BigBuffer bigBuffer, UsersQueue usersQueue) {
        this.bigBuffer = bigBuffer;
        this.usersQueue = usersQueue;
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        logger.info("Socket connected: ws=" + this.hashCode());
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        // "this" is our "user"
        usersQueue.kick(this);
        logger.error("Socket disconnected: ws=" + this.hashCode());
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);

        // "this" is our "user"
        usersQueue.kick(this);
        logger.error("Socket disconnected because of an error: ws=" + this.hashCode()+" cause="+cause.getMessage());
    }


    @Override
    public void onWebSocketText(final String message) {
        JsonObject retVal = new JsonObject();
        retVal.addProperty("jsonrpc", "2.0");

        String version;
        JsonObject o;
        long id;
        String methodName;
        try {
            o = new Gson().fromJson(message, JsonObject.class);
        }
        catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32700);
            error.addProperty("message", "Parse error");

            retVal.add("error", error);
            retVal.add("id", null);
            try {
                getRemote().sendString(retVal.toString());
            } catch (IOException ex) {
                logger.error("Could not send jsonrpc response to "+getRemote().getRemoteAddress().toString());
            }
            return;
        }

        try {
            id = o.get("id").getAsLong();
            methodName = o.get("method").getAsString();
            assert "consume".equals(methodName);
            version = o.get("version").getAsString();
            assert "2.0".equals(version);
        }
        catch(Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", -32600);
            error.addProperty("message", "Invalid Request");

            retVal.add("error", error);
            retVal.add("id", null);
            try {
                getRemote().sendString(retVal.toString());
            } catch (IOException ex) {
                logger.error("Could not send jsonrpc response to "+getRemote().getRemoteAddress().toString());
            }
            return;
        }

        try {
            usersQueue.enqueue(this);
        }
        catch(BufferOverflowException | KeyAlreadyExistsException e) {
            retVal.addProperty("id", id);
            retVal.addProperty("result", false);
            // ^^ too many users are waiting or the given user is already in the queue
            try {
                getRemote().sendString(retVal.toString());
            } catch (IOException ex) {
                logger.error("Could not send jsonrpc response to "+getRemote().getRemoteAddress().toString());
            }
            return;
        }
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // incoming binary streams are not used
    }

}