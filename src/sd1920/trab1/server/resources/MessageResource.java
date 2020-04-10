package sd1920.trab1.server.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;

import sd1920.trab1.api.Message;
import sd1920.trab1.api.User;
import sd1920.trab1.api.rest.MessageService;
import sd1920.trab1.discovery.Discovery;
import sd1920.trab1.server.utils.MessageUtills;

@Singleton
public class MessageResource implements MessageService {

	private final Map<Long, Message> allMessages = new HashMap<Long, Message>();
	protected static final Map<String, Set<Long>> userInboxs = new HashMap<String, Set<Long>>();
	// Map consumed from UserResource
	private final Map<String, User> allusers = UserResource.getAllusers();

	private Random randomNumberGenerator;

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public MessageResource() {
		this.randomNumberGenerator = new Random(System.currentTimeMillis());
	}

	@Override
	public long postMessage(String pwd, Message msg) {

		String sender_name = msg.getSender();
		if (sender_name.contains("@")) // if is a domain, gets the name of sender
			sender_name = sender_name.substring(0, sender_name.indexOf("@"));

		User sender = allusers.get(sender_name);
		Log.info("Received request to register a new message (Sender: " + msg.getSender() + "; Subject: "
				+ msg.getSubject() + ")");

		if (sender == null || !pwd.equals(sender.getPwd())) {
			Log.info("Message was rejected due to sender not existing or wrong password");
			throw new WebApplicationException(Status.FORBIDDEN);
		} else if (msg.getSender() == null || msg.getDestination() == null || msg.getDestination().size() == 0) {
			Log.info("Message was rejected due to lack of recepients.");
			throw new WebApplicationException(Status.CONFLICT); // Check if message is valid, if not return HTTP //
																// CONFLICT (409)
		}

		long newID = 0;
		synchronized (this) {
			// Generate a new id for the message, that is not in use yet
			newID = Math.abs(randomNumberGenerator.nextLong());
			while (allMessages.containsKey(newID)) {
				newID = Math.abs(randomNumberGenerator.nextLong());
			}
			String email = sender.getName() + "@" + sender.getDomain();
			String new_sender = sender.getDisplayName() + " <" + email + ">";
			msg.setSender(new_sender);
			msg.setId(newID);
			// Add the message to the global list of messages
			allMessages.put(newID, msg);
		}

		Log.info("Created new message with id: " + newID);
		MessageUtills.printMessage(allMessages.get(newID));

		synchronized (this) {
			// Add the message (identifier) to the inbox of each recipient
			for (String recipient : msg.getDestination()) {

				String domain = "";
				if (recipient.contains("@")) {
					domain = recipient.split("@")[1];
					recipient = recipient.substring(0, recipient.indexOf("@"));
				}

				if (!sender.getDomain().equals(domain)) {
					Log.info("MR: Domain is..."+ domain);
					sendMessage(domain, newID, recipient, msg);// calls the server from the recipient domain
				} else {
					if (!userInboxs.containsKey(recipient))
						userInboxs.put(recipient, new HashSet<Long>());

					userInboxs.get(recipient).add(newID);
				}
			}
		}

		// Return the id of the registered message to the client (in the body of a HTTP
		// Response with 200)
		Log.info("Recorded message with identifier: " + newID);
		return newID;
	}

	@Override
	public Message getMessage(String user, long mid, String pwd) {

		User receiver = allusers.get(user);
		String user_pwd = "";
		if (receiver != null)
			user_pwd = receiver.getPwd();

		if (pwd == null)
			pwd = "";

		if (receiver == null || !pwd.equals(user_pwd)) {
			Log.info("Message was rejected due to sender not existing or wrong password");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Log.info("Received request for message with id: " + mid + "in inbox " + user);
		Log.info("Username = " + user + " - " + receiver.getName());
		Message m = null;

		synchronized (this) {
			Set<Long> mids = userInboxs.getOrDefault(user, Collections.emptySet());
			for (Long l : mids) {
				Log.info("Getting message with id " + l + ".");
				if (l == mid) {
					m = allMessages.get(l);
					break;
				}
			}
		}

		if (m == null) {
			Log.info("Requested message does not exist." + mid);
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		Log.info("Returning requested message to user.");
		return m;
	}

	@Override
	public List<Long> getMessages(String user, String pwd) {

		User receiver = allusers.get(user);
		String user_pwd = "";
		if (receiver != null)
			user_pwd = receiver.getPwd();

		if (pwd == null)
			pwd = "";

		List<Long> messagesIds = new ArrayList<Long>();

		if (receiver == null || !pwd.equals(user_pwd)) {
			Log.info("Message was rejected due to sender not existing or wrong password");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Log.info("Collecting all messages in server for user " + user);
		synchronized (this) {
			Set<Long> mids = userInboxs.getOrDefault(user, Collections.emptySet());
			for (Long l : mids) {
				Log.info("Adding message with id: " + l + ".");
				messagesIds.add(l);

			}
		}
		Log.info("Returning the list of messages.");
		return messagesIds;
	}

	@Override
	public void removeFromUserInbox(String user, long mid, String pwd) {

		User receiver = allusers.get(user);

		if (pwd == null)
			pwd = "";

		if (receiver == null || !pwd.equals(receiver.getPwd())) {
			Log.info("Sender does not exist or wrong password");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Log.info("Removing message with id " + mid + " from the user " + user + " inbox");

		boolean found = false;
		synchronized (this) {
			found = userInboxs.get(user).contains(mid);
		}

		if (!found) {
			Log.info("Message with id: " + mid + " doen't exist in " + user + " inbox");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		synchronized (this) {
			Log.info("Deleting message with id: " + mid + " from the " + user + " inbox.");
			userInboxs.get(user).remove(mid);
		}

	}

	@Override
	public void deleteMessage(String user, long mid, String pwd) {

		User sender = allusers.get(user);

		if (pwd == null)
			pwd = "";

		if (sender == null || !pwd.equals(sender.getPwd())) {
			Log.info("Sender does not exist or wrong password");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Log.info("Received request to delete message with id: " + mid + ".");
		Log.info("User requesting = " + user);

		Message m = null;
		String m_sender = "";
		synchronized (this) {
			m = allMessages.get(mid); // checks if message exists
			if (m != null)
				m_sender = m.getSender();
		}

		if (m != null) {
			// checks if the user is the sender of this message
			if (m_sender.contains(user)) {
				Log.info("Deleting message with id: " + mid);
				for (Entry<String, Set<Long>> entry : userInboxs.entrySet()) {
					entry.getValue().remove(mid);
				}
				allMessages.remove(mid);
			}

		}
	}

	@Override
	public void addMessageToInbox(long newID, String name, Message msg) {
		try {
			Log.info("Received message with ID " + newID + " from another domain.");
			Log.info("Adding msg to " + name + "inbox.");
			if (!userInboxs.containsKey(name))
				userInboxs.put(name, new HashSet<Long>());

			userInboxs.get(name).add(newID);
			allMessages.put(newID, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// --------------------------------Private_Methods------------------------------------//

	protected static Map<String, Set<Long>> getUserInbox() {
		return userInboxs;
	}

	private void sendMessage(String domain, long newID, String name, Message msg) {
		try {
			String mid = String.valueOf(newID);

			ClientConfig config = new ClientConfig();
			Client client = ClientBuilder.newClient(config);
			String serverUrl = Discovery.getUrl(domain);

			Log.info("MR: Conecting to... " + serverUrl);

			WebTarget target = client.target(serverUrl).path(MessageService.PATH);
			target.path("/otherdomain").path(mid).path(name).request().accept(MediaType.APPLICATION_JSON)
					.post(Entity.entity(msg, MediaType.APPLICATION_JSON));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
