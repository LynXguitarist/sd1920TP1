package sd1920.trab1.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpServer;

import sd1920.trab1.discovery.Discovery;
import sd1920.trab1.server.implementation.MessageImpl;
import sd1920.trab1.server.implementation.UserImpl;

@SuppressWarnings("restriction")
public class MessageSoapServer {

	private static Logger Log = Logger.getLogger(MessageSoapServer.class.getName());

	private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "MessageService";
	public static final String SOAP_MESSAGES_PATH = "/soap/messages";
	public static final String SOAP_USERS_PATH = "/soap/users";

	public static void main(String[] args) throws Exception {
		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format("http://%s:%s/soap", ip, PORT);

		// Create an HTTP server, accepting requests at PORT (from all local interfaces)
		HttpServer server = HttpServer.create(new InetSocketAddress(ip, PORT), 0);

		// Provide an executor to create threads as needed...
		server.setExecutor(Executors.newCachedThreadPool());

		// Create a SOAP Endpoint for Messages and other for Users
		Endpoint soapMessagesEndpoint = Endpoint.create(new MessageImpl());
		Endpoint soapUsersEndpoint = Endpoint.create(new UserImpl());

		// Publish Users and Messages SOAP webservices, under the
		// "http://<ip>:<port>/soap"
		soapMessagesEndpoint.publish(server.createContext(SOAP_MESSAGES_PATH));
		soapUsersEndpoint.publish(server.createContext(SOAP_USERS_PATH));

		server.start();// starts the server(regists the services)

		Log.info(String.format("\n%s Server ready @ %s\n", InetAddress.getLocalHost().getCanonicalHostName(), serverURI));

		Discovery discovery = new Discovery(DISCOVERY_ADDR, InetAddress.getLocalHost().getCanonicalHostName(), serverURI);
		discovery.start();
	}

}
