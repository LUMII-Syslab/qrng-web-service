package lv.lumii.qrng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.websocket.api.*;

public class QrngWebSocket extends WebSocketAdapter {
    // javadoc for WebSocketAdapter:
    // https://www.eclipse.org/jetty/javadoc/jetty-11/org/eclipse/jetty/websocket/api/WebSocketAdapter.html

    private static Logger logger = LoggerFactory.getLogger(QrngWebSocket.class);

    private class MyWriteCallback implements WriteCallback {

    }

    private WriteCallback writeCallback = new MyWriteCallback();


    public QrngWebSocket() {
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);

        logger.info("Socket Connected: session=" + sess.hashCode() + " object=" + this.hashCode() + " remote="
                + sess.getRemote().hashCode());
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
    }


    @Override
    public void onWebSocketText(final String message) {
        logger.info("Received: "+message);
        getRemote().sendString("Bob", writeCallback);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        QrngWebSocket myThis = this;
        // myThis.getRemote().sendString(text, writeCallback);
    }

}