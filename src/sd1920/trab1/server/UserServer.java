package sd1920.trab1.server;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd1920.trab1.server.resources.UserResource;

public class UserServer {

	private static Logger Log = Logger.getLogger(MessageServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8080;
	public static final String SERVICE = "MessageService";
	
	public static void main(String[] args) throws UnknownHostException {
		String ip = InetAddress.getLocalHost().getHostAddress();
			
		ResourceConfig config = new ResourceConfig();
		config.register(UserResource.class);
		
		//If rest and soap
		String serverURI = String.format("http://%s:%s/rest", ip, PORT);
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);
	
		Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
		
	}


}
