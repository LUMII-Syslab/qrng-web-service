// SPDX-License-Identifier: MIT
package lv.lumii.qrng;

import jakarta.servlet.*;
import org.eclipse.jetty.websocket.server.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Copyright (c) Institute of Mathematics and Computer Science, University of Latvia
 * License: MIT
 * Contributors:
 * Sergejs Kozlovics
 * <p>
 * Implementing the QRNG web service that listens the insecure HTTP_PORT on "localhost".
 * This QRNG web service is intended to work as a backend for HAProxy
 * compiled with quantum-resistant algorithms from openquantumsafe.org.
 * <p>
 * HAProxy will provide the HTTPS web socket end point identified by
 * a quantum-safe server certificate identified by our self-signed quantum-safe CA key.
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    /***
     * The insecure (non-HTTPS) port where the QRNG web service listens on "localhost".
     * The QRNG web service will be accessed by HAProxy located on the same machine.
     */
    private static int HTTP_PORT = 4444;


    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                HTTP_PORT = Integer.parseInt(args[0]);
            }

            Server webServer = new Server();
            ///// HTTP CONFIG /////

            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setOutputBufferSize(32768);
            http_config.setRequestHeaderSize(8192);
            http_config.setResponseHeaderSize(8192);
            http_config.setSendServerVersion(true);
            http_config.setSendDateHeader(false);

            ServerConnector http = new ServerConnector(webServer,
                    new HttpConnectionFactory(http_config));
            http.setHost("localhost");
            // ^^^ bind to localhost, since we won't encrypt the connection between
            //     QRNG web service and HAProxy located at the same machine
            http.setPort(HTTP_PORT);
            http.setIdleTimeout(30000);

            webServer.addConnector(http);

            ///// WEB-SOCKET HANDLER /////
            ContextHandlerCollection handlerColl = new ContextHandlerCollection();
            webServer.setHandler(handlerColl);

            //ServletContextHandler wsContextHandler = new ServletContextHandler(handlerColl, "/ws", true, true); // First Server!!!
            ServletContextHandler wsContextHandler = new ServletContextHandler(handlerColl, "", true, true); // First Server!!!

            ///// BIG BUFFER AND USERS /////
            BigBuffer bigBuffer = new BigBuffer();
            UsersQueue<QrngWebSocket> usersQueue = new UsersQueue<>(1000);

            ///// Jetty 11.x Web Socket servlet /////
            Servlet websocketServlet = new JettyWebSocketServlet() {
                @Override
                protected void configure(JettyWebSocketServletFactory factory) {
                    factory.addMapping("/", (req, res) -> new QrngWebSocket(bigBuffer, usersQueue));
                    //factory.register(BridgeSocket.class);
                }
            };
            wsContextHandler.addServlet(new ServletHolder(websocketServlet), "/*");
            JettyWebSocketServletContainerInitializer.configure(wsContextHandler, null);

            webServer.start();
            wsContextHandler.start();
            handlerColl.mapContexts();
            logger.info("QRNG web service started");

            new Thread(() -> {
                // consumer thread
                for (; ; ) {
                    byte[] block = null;
                    try {
                        block = bigBuffer.consume(); // can throw BufferUnderflowException
                        QrngWebSocket socket = usersQueue.takeUser(); // blocking
                        socket.getRemote().sendBytes(ByteBuffer.wrap(block));
                    } catch (BufferUnderflowException bufferEmpty) {
                        try {
                            // wait some time in hope that bigBuffer gets replenished
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // we don't care of interrupts; we will wait again, if needed
                        }
                    } catch (Exception ex) {
                        logger.error("Exception in the consumer thread: " + ex.getMessage());
                    }
                }
            }).start();

            try {
                webServer.join();
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
            logger.info("QRNG web service stopped.");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
