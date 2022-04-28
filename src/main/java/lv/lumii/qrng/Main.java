package lv.lumii.qrng;
import jakarta.servlet.*;
import org.eclipse.jetty.websocket.server.*;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static int PORT = 4444;


    public static void main(String[] args) {
        try {
            Server webServer = new Server();
            new Server();

            ///// HTTP /////

            HttpConfiguration http_config = new HttpConfiguration();
            //http_config.setSecureScheme("https");
            //http_config.setSecurePort(cfg.secure_port);
            http_config.setOutputBufferSize(32768);
            http_config.setRequestHeaderSize(8192);
            http_config.setResponseHeaderSize(8192);
            http_config.setSendServerVersion(true);
            http_config.setSendDateHeader(false);

            ServerConnector http = new ServerConnector(webServer,
                    new HttpConnectionFactory(http_config));
            http.setPort(PORT);
            http.setIdleTimeout(30000);

            webServer.addConnector(http);
            logger.info("PORT is " + PORT);


            ContextHandlerCollection handlerColl = new ContextHandlerCollection();
            webServer.setHandler(handlerColl);

            ServletContextHandler wsContextHandler = new ServletContextHandler(handlerColl, "/ws", true, true); // First Server!!!


            // Jetty 9.x
            //ServletHolder holderEvents = new ServletHolder("ws-events", BridgeSocket.Servlet.class);
            //wsContextHandler.addServlet(holderEvents, "/*");  // the path will be ws://domain.org/ws/ or wss://domain.org/ws/  - the trailing slash is mandatory!

            // Jetty 11.x
            Servlet websocketServlet = new JettyWebSocketServlet() {
                @Override
                protected void configure(JettyWebSocketServletFactory factory) {
                    factory.addMapping("/", (req, res) -> new QrngWebSocket());
                    //factory.register(BridgeSocket.class);
                }
            };
            wsContextHandler.addServlet(new ServletHolder(websocketServlet), "/*");
            JettyWebSocketServletContainerInitializer.configure(wsContextHandler, null);

            webServer.start();
            wsContextHandler.start();
            handlerColl.mapContexts();

            try {
                webServer.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("Server stopped.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
