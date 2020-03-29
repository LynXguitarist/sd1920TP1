package sd1920.trab1.clients.rest.users;

import java.util.Scanner;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import sd1920.trab1.api.User;
import sd1920.trab1.api.rest.UserService;
import sd1920.trab1.discovery.Discovery;

public class PostUserRest {

	public static final int MAX_RETRIES = 3;
	public static final long RETRY_PERIOD = 1000;
	public static final int CONNECTION_TIMEOUT = 1000;
	public static final int REPLY_TIMEOUT = 600;

	private static final String serviceName = "UserService";

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		System.out.println("Provide the server url:");
		String serverUrl = Discovery.knownUrisOf(serviceName)[0].getPath();
		System.out.println("server = " + serverUrl);

		System.out.println("Provide username:");// username
		String username = sc.nextLine();
		System.out.println("Username = " + username);

		System.out.println("Provide user password:");// password
		String pwd = sc.nextLine();

		System.out.println("Provide user domain:");// domain
		String domain = sc.nextLine();

		System.out.println("Provide user displayName:");
		String displayName = sc.nextLine();

		sc.close();

		User user = new User(username, pwd, domain);
		user.setDisplayName(displayName);

		System.out.println("Sending request to server.");

		ClientConfig config = new ClientConfig();
		// How much time until timeout on opening TCP connection to the server
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
		// How much time to wait for the reply of the server after sending the request
		config.property(ClientProperties.READ_TIMEOUT, REPLY_TIMEOUT);
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(serverUrl).path(UserService.PATH);

		short retries = 0;
		boolean success = false;

		while (!success && retries < MAX_RETRIES) {
			try {

				Response r = target.request().accept(MediaType.APPLICATION_JSON)
						.post(Entity.entity(user, MediaType.APPLICATION_JSON));

				if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity())
					System.out.println("Success, user posted with domain: " + r.readEntity(String.class));
				else
					System.out.println("Error, HTTP error status: " + r.getStatus());

				success = true;
			} catch (ProcessingException pe) {// error in communication with server
				System.out.println("Timeout occured.");
				pe.printStackTrace();
				retries++;
				try {
					Thread.sleep(RETRY_PERIOD);// wait until attempting again
				} catch (InterruptedException e) {
					// Nothing be done here, if this happens we will just retry sooner
				}
				System.out.println("Retrying to execute request.");
			}
		}

	}

}
