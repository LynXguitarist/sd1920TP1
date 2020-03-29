package sd1920.trab1.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd1920.trab1.discovery.Discovery;
import sd1920.trab1.server.resources.MessageResource;

public class MessageRestServer {

	private static Logger Log = Logger.getLogger(MessageRestServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "MessageService";

	public static void main(String[] args) throws UnknownHostException {
		String ip = InetAddress.getLocalHost().getHostAddress();

		ResourceConfig config = new ResourceConfig();
		config.register(MessageResource.class);

		// if para ver se e REST ou SOAP???

		String serverURI = String.format("http://%s:%s/rest", ip, PORT);
		JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

		Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));
		
		InetSocketAddress addr = new InetSocketAddress(PORT);
		Discovery discovery = new Discovery(addr,SERVICE, serverURI);
		discovery.start();

	}

}