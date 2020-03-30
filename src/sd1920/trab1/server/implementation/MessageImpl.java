package sd1920.trab1.server.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import sd1920.trab1.api.Message;
import sd1920.trab1.api.User;
import sd1920.trab1.api.soap.MessageServiceSoap;
import sd1920.trab1.api.soap.MessagesException;
import sd1920.trab1.server.resources.MessageResource;
import sd1920.trab1.server.resources.UserResource;
import sd1920.trab1.server.utils.MessageUtills;

@WebService(serviceName = MessageServiceSoap.NAME, targetNamespace = MessageServiceSoap.NAMESPACE, endpointInterface = MessageServiceSoap.INTERFACE)
public class MessageImpl implements MessageServiceSoap {

	private final Map<Long, Message> allMessages = new HashMap<Long, Message>();
	protected static final Map<String, Set<Long>> userInboxs = new HashMap<String, Set<Long>>();
	private final Map<String, User> allusers = UserResource.getAllusers();

	private Random randomNumberGenerator;

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public MessageImpl() {
		this.randomNumberGenerator = new Random(System.currentTimeMillis());
	}

	@Override
	public long postMessage(String pwd, Message msg) throws MessagesException {

		String sender_name = msg.getSender();
		if (sender_name.contains("@")) // if is a domain, gets the name of sender
			sender_name = sender_name.substring(0, sender_name.indexOf("@"));

		User sender = allusers.get(sender_name);
		Log.info("Received request to register a new message (Sender: " + msg.getSender() + "; Subject: "
				+ msg.getSubject() + ")");

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
			// Add the message to the global list of messages
			allMessages.put(newID, msg);
		}

		Log.info("Created new message with id: " + newID);
		MessageUtills.printMessage(allMessages.get(newID));

		synchronized (this) {
			// Add the message (identifier) to the inbox of each recipient
			for (String recipient : msg.getDestination()) {
				if (recipient.contains("@"))
					recipient = recipient.substring(0, recipient.indexOf("@"));

				if (!userInboxs.containsKey(recipient)) {
					userInboxs.put(recipient, new HashSet<Long>());
				}
				userInboxs.get(recipient).add(newID);
			}
		}

		// Return the id of the registered message to the client (in the body of a HTTP
		// Response with 200)
		Log.info("Recorded message with identifier: " + newID);
		return newID;
	}

	@Override
	public Message getMessage(String user, String pwd, long mid) throws MessagesException {

		Log.info("Received request for message with id: " + mid + ".");
		Message m = null;

		synchronized (this) {
			m = allMessages.get(mid);
		}

		if (m == null) {
			Log.info("Requested message does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		Log.info("Returning requested message to user.");
		return m;
	}

	@Override
	public List<Long> getMessages(String user, String pwd) throws MessagesException {

		List<Long> messagesIds = new ArrayList<>();

		Log.info("Collecting all messages in server for user " + user);
		synchronized (this) {
			Set<Long> mids = userInboxs.getOrDefault(user, Collections.emptySet());
			for (Long l : mids) {
				Log.info("Adding message");
				messagesIds.add(l);
			}

		}
		Log.info("Returning the list of messages.");
		return messagesIds;
	}

	@Override
	public void removeFromUserInbox(String user, String pwd, long mid) throws MessagesException {

		Log.info("Removing message with id " + mid + " from the user " + user + " inbox");
		synchronized (this) {
			Log.info("Deleting message with id: " + mid);
			userInboxs.get(user).remove(mid);
		}

	}

	@Override
	public void deleteMessage(String user, String pwd, long mid) throws MessagesException {

		Log.info("Received request to delete message with id: " + mid + ".");
		Log.info("Deleting message with id: " + mid);
		synchronized (this) {
			for (Entry<String, Set<Long>> entry : userInboxs.entrySet()) {
				entry.getValue().remove(mid);
			}
			allMessages.remove(mid);
		}

	}

	protected static Map<String, Set<Long>> getUserInbox() {
		return userInboxs;
	}

}
