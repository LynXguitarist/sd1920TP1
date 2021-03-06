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
import sd1920.trab1.server.resources.UserResource;

public class MessageRestServer {

	private static Logger Log = Logger.getLogger(MessageRestServer.class.getName());

	private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static final int PORT = 8080;

	public static void main(String[] args) throws UnknownHostException {
		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format("http://%s:%s/rest", ip, PORT);
		String serviceName = InetAddress.getLocalHost().getCanonicalHostName();

		ResourceConfig config = new ResourceConfig();
		config.register(MessageResource.class);
		config.register(UserResource.class);

		JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

		Log.info(String.format("%s Server ready @ %s\n", serviceName, serverURI));
		
		Discovery discovery = new Discovery(DISCOVERY_ADDR, serviceName, serverURI);
		discovery.start();

	}

}
