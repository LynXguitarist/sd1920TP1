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
import sd1920.trab1.clients.utils.UserUtills;

public class UpdateUserRest {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		// You should replace this by the discovery class developed last week
		System.out.println("Provide the server url:");
		String serverUrl = sc.nextLine();

		System.out.println("Provide username:");
		String username = sc.nextLine();

		String pwd = sc.nextLine();
		if (pwd.equalsIgnoreCase(""))
			pwd = null;

		sc.nextLine();// username

		System.out.println("New pwd:");
		String new_pwd = sc.nextLine();

		System.out.println("New domain:");
		String new_domain = sc.nextLine();

		System.out.println();

		sc.close();

		if (new_pwd == null)
			new_pwd = pwd;
		if (new_domain == null)
			new_domain = serverUrl;// ver se esta bem

		User user = new User(username, new_pwd, new_domain);

		System.out.println("Sending request to server.");

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(serverUrl).path(UserService.PATH);

		if (pwd != null)
			target = target.queryParam("pwd", pwd);

		Response r = target.path(username).request().accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (r.getStatus() == Status.OK.getStatusCode() && r.hasEntity()) {
			System.out.println("Success, user updated:");
			User user_content = r.readEntity(User.class);
			UserUtills.printUser(user_content);

		} else
			System.out.println("Error, HTTP error status: " + r.getStatus());

	}

}
