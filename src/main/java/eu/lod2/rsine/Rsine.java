package eu.lod2.rsine;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.security.InvalidParameterException;

@Component
public class Rsine {

    public final static String propertiesFileName = "application.properties";
    private final static Logger logger = LoggerFactory.getLogger(Rsine.class);

    public static CmdParams cmdParams;

    public static void main(String[] args) {
        try {
            cmdParams = new CmdParams(args);
            if (!cmdParams.help) startServer();
        }
        catch (InvalidParameterException e) {
            logger.error("Insufficient parameters for starting the service");
        }
        catch (Exception e) {
            logger.error("Error starting rsine service", e);
        }
    }

    public static Server initAndStart(int port,
                                      String managedStoreSparqlEndpoint,
                                      String authoritativeUri,
                                      String feedbackFileName)
        throws Exception
    {
        cmdParams = new CmdParams();
        cmdParams.port = port;
        cmdParams.managedStoreSparqlEndpoint = managedStoreSparqlEndpoint;
        cmdParams.authoritativeUri = authoritativeUri;
        cmdParams.feedbackFileName = feedbackFileName;

        return startServer();
    }

    private static Server startServer() throws Exception {
        Server server = new Server(cmdParams.port);

        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setConfigLocation("classpath:application-context.xml");

        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");

        contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), "/*");
        contextHandler.addEventListener(new ContextLoaderListener(context));
        server.setHandler(contextHandler);

        server.start();
        return server;
    }

}
