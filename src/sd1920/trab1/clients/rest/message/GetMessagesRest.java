package sd1920.trab1.clients.rest.message;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import sd1920.trab1.api.Message;
import sd1920.trab1.api.rest.MessageService;
import sd1920.trab1.clients.utils.MessageUtills;

public class GetMessagesRest {

	public final static int MAX_RETRIES = 3;
	public final static long RETRY_PERIOD = 1000;
	public final static int CONNECTION_TIMEOUT = 1000;
	public final static int REPLY_TIMEOUT = 600;

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		//You should replace this by the discovery class developed last week
		System.out.println("Provide the server url:");
		String serverUrl = sc.nextLine();
		//
		
		
		System.out.println("Provide the user:");
		String user = sc.nextLine();
		
		System.out.println("Provide the password:");
		String pwd = sc.nextLine();
		if (pwd.equalsIgnoreCase(""))
			pwd = null;
		
		sc.close();
		
System.out.println("Sending request to server.");
		
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		
		WebTarget target = client.target( serverUrl ).path( MessageService.PATH );
		
		if(user != null)
			target = target.queryParam("user", user);
		
		if (pwd != null)
			target = target.queryParam("pwd", pwd);
		
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
				List<Message> messages = r.readEntity(new GenericType<List<Message>>() {});
				System.out.println("Success: (" + messages.size() + " messages)");
				for(Message m: messages) {
					MessageUtills.printMessage(m);
					System.out.println("-----------------------------------\n");
				}
				
		} else
			System.out.println("Error, HTTP error status: " + r.getStatus() );

	}
	
}
