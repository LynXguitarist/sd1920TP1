package sd1920.trab1.clients.rest.users;

import java.util.Scanner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;

import sd1920.trab1.api.User;
import sd1920.trab1.api.rest.UserService;

public class PostUserRest {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		// You should replace this by the discovery class developed last week
		System.out.println("Provide the server url:");
		String serverUrl = sc.nextLine();

		System.out.println("Provide username:");// username
		String username = sc.nextLine();

		System.out.println("Provide user password:");// password
		String pwd = sc.nextLine();

		System.out.println("Provide user domain:");// domain
		String domain = sc.nextLine();

		sc.close();

		User user = new User(username, pwd, domain);

		System.out.println("Sending request to server.");

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(serverUrl).path(UserService.PATH);

		Response r = target.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity())
			System.out.println("Success, user posted with domain: " + r.readEntity(String.class));
		else
			System.out.println("Error, HTTP error status: " + r.getStatus());

	}

}
