// SPDX-License-Identifier: MIT
package lv.lumii.qrng;

import java.io.*;
import java.nio.file.Path;

import jakarta.servlet.*;
import org.eclipse.jetty.websocket.server.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * a quantum-safe server certificate signed by our self-signed quantum-safe CA key.
 */
public class Main {

    private static Logger logger;

    private static Path myDir = new RootDirectory().path();

    static {
        /*
        do not use log4j2 in native executables/libraries!!!
        slf4j with simple logger is ok;

        gradle dependencies:
            implementation 'org.slf4j:slf4j-api:2.+'
            implementation 'org.slf4j:slf4j-simple:2.+'
         */

        System.setProperty("org.slf4j.simpleLogger.logFile", "C:\\Users\\SysLab\\source\\qrng-web-service\\qqq.log");
        logger = LoggerFactory.getLogger(Main.class);
        logger.error("myDir="+myDir);
    }

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

            final Server webServer = new Server();
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
            WaitingUsers<QrngWebSocketClient> waitingUsers = new WaitingUsers<>(1000);

            ///// Jetty 11.x Web Socket servlet /////
            Servlet websocketServlet = new JettyWebSocketServlet() {
                @Override
                protected void configure(JettyWebSocketServletFactory factory) {
                    factory.addMapping("/", (req, res) -> new QrngWebSocketClient(bigBuffer, waitingUsers));
                    // ^^^ Variant with URL path:
                    //     factory.addMapping("/path", (req, res) -> new QrngWebSocket(bigBuffer, waitingUsers));
                }
            };
            wsContextHandler.addServlet(new ServletHolder(websocketServlet), "/*");
            JettyWebSocketServletContainerInitializer.configure(wsContextHandler, null);

            webServer.start();
            wsContextHandler.start();
            handlerColl.mapContexts();
            logger.info("QRNG web service started");
            new FileWatchman(myDir, "NotifyFile", (fileName) -> {
                System.out.println("CALLBACK "+fileName);
                System.exit(0);
            }, true).start();

            final ConsumingThread consumingThread = new ConsumingThread(bigBuffer, waitingUsers);
            final QuantisThreadPool pool = new QuantisThreadPool(bigBuffer,
                    () -> {
                        logger.error("All replenishing threads stopped. Exiting...");
                        System.exit(1);
                    }
            );


            pool.startAll();
            logger.info("Replenishing thread(s) started");
            consumingThread.start();
            logger.info("Consuming thread started");

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    consumingThread.interrupt();
                    logger.info("Consuming thread asked to interrupt");
                    pool.stopAll();
                    logger.info("Replenishing thread(s) asked to interrupt");
                    try {
                        webServer.stop();
                        logger.info("Web server asked to stop");
                    } catch (Exception e) {
                        logger.error("Could not stop the web server", e);
                    }
                }
            });

            try {
                webServer.join();
            } catch (InterruptedException ex) {
                logger.error("Web server join failed", ex);
            }
            logger.info("QRNG web service stopped.");
        } catch (Exception e) {
            logger.error("QRNG service exception " + e.getClass().getSimpleName(), e);
            e.printStackTrace();
        }
    }
}
