package sd1920.trab1.clients.rest.message;

import java.util.Scanner;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import sd1920.trab1.api.rest.MessageService;

public class DeleteMessageRest {
	public final static int MAX_RETRIES = 3;
	public final static long RETRY_PERIOD = 1000;
	public final static int CONNECTION_TIMEOUT = 1000;
	public final static int REPLY_TIMEOUT = 600;

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		// You should replace this by the discovery class developed last week
		System.out.println("Provide the server url:");
		String serverUrl = sc.nextLine();

		System.out.println("Provide the user:");
		String user = sc.nextLine();

		System.out.println("Provide message identifier:");
		String mid = "" + Long.parseLong(sc.nextLine());

		System.out.println("Provide the password:");
		String pwd = sc.nextLine();
		if (pwd.equalsIgnoreCase(""))
			pwd = null;

		sc.close();

		System.out.println("Sending request to server.");

		ClientConfig config = new ClientConfig();
		// How much time until timeout on opening the TCP connection to the server
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
		// How much time to wait for the reply of the server after sending the request
		config.property(ClientProperties.READ_TIMEOUT, REPLY_TIMEOUT);
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(serverUrl).path(MessageService.PATH + "/msg");

		if (pwd != null)
			target = target.queryParam("pwd", pwd);

		short retries = 0;
		boolean success = false;

		while (!success && retries < MAX_RETRIES) {
			try {
				Response r = target.path(user).path(mid).request().accept(MediaType.APPLICATION_JSON).delete();
				if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
					System.out.println("Success:");
					String content = new String(r.readEntity(byte[].class));
					System.out.println(content);
				} else
					System.out.println("Error, HTTP error status: " + r.getStatus());

				success = true;
			} catch (ProcessingException pe) { // Error in communication with server
				System.out.println("Timeout occurred.");
				pe.printStackTrace(); // Could be removed
				retries++;
				try {
					Thread.sleep(RETRY_PERIOD); // wait until attempting again.
				} catch (InterruptedException e) {
					// Nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
	}

}
