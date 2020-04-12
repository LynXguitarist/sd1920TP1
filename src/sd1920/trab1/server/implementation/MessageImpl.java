package sd1920.trab1.server.implementation;

import java.net.URL;
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
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import sd1920.trab1.api.Message;
import sd1920.trab1.api.User;
import sd1920.trab1.api.soap.MessageServiceSoap;
import sd1920.trab1.api.soap.MessagesException;
import sd1920.trab1.discovery.Discovery;
import sd1920.trab1.server.resources.MessageResource;
import sd1920.trab1.server.utils.MessageUtills;

@WebService(serviceName = MessageServiceSoap.NAME, targetNamespace = MessageServiceSoap.NAMESPACE, endpointInterface = MessageServiceSoap.INTERFACE)
public class MessageImpl implements MessageServiceSoap {

	private final Map<Long, Message> allMessages = new HashMap<Long, Message>();
	protected static final Map<String, Set<Long>> userInboxs = new HashMap<String, Set<Long>>();
	private final Map<String, User> allusers = UserImpl.getAllusers();

	private Random randomNumberGenerator;

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	private static final String MESSAGES_WSDL = "/messages/?wsdl";

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

		if (sender == null || !pwd.equals(sender.getPwd()) || msg.getSender() == null || msg.getDestination() == null
				|| msg.getDestination().size() == 0)
			throw new MessagesException();

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
				String[] name_domain = recipient.split("@");
				String name = name_domain[0];
				String domain = name_domain[1];

				Log.info("MI: User: " + name + " in domain: " + domain);
				
				if (!sender.getDomain().equals(domain))
					sendMessage(domain, newID, name, msg);
				else {
					if (!userInboxs.containsKey(name))
						userInboxs.put(name, new HashSet<Long>());

					userInboxs.get(name).add(newID);
				}
			}
		}

		// Return the id of the registered message to the client (in the body of a HTTP
		// Response with 200)
		Log.info("Recorded message with identifier: " + newID);
		return newID;
	}

	@Override
	public Message getMessage(String user, String pwd, long mid) throws MessagesException {

		User receiver = allusers.get(user);
		String user_pwd = "";
		if (receiver != null)
			user_pwd = receiver.getPwd();

		if (pwd == null)
			pwd = "";

		if (receiver == null || !pwd.equals(user_pwd))
			throw new MessagesException();

		Log.info("Received request for message with id: " + mid + "in inbox " + user);
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

		if (m == null)
			throw new MessagesException();

		Log.info("Returning requested message to user.");
		return m;
	}

	@Override
	public List<Long> getMessages(String user, String pwd) throws MessagesException {

		User receiver = allusers.get(user);
		String user_pwd = "";
		if (receiver != null)
			user_pwd = receiver.getPwd();

		if (pwd == null)
			pwd = "";

		List<Long> messagesIds = new ArrayList<Long>();

		if (receiver == null || !pwd.equals(user_pwd))
			throw new MessagesException();

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
	public void removeFromUserInbox(String user, String pwd, long mid) throws MessagesException {

		User receiver = allusers.get(user);

		if (pwd == null)
			pwd = "";

		if (receiver == null || !pwd.equals(receiver.getPwd()))
			throw new MessagesException();

		Log.info("Removing message with id " + mid + " from the user " + user + " inbox");

		boolean found = false;
		synchronized (this) {
			found = userInboxs.get(user).contains(mid);
		}

		if (!found)
			throw new MessagesException();

		synchronized (this) {
			Log.info("Deleting message with id: " + mid + " from the " + user + " inbox.");
			userInboxs.get(user).remove(mid);
		}

	}

	@Override
	public void deleteMessage(String user, String pwd, long mid) throws MessagesException {

		User sender = allusers.get(user);

		if (pwd == null)
			pwd = "";

		if (sender == null || !pwd.equals(sender.getPwd()))
			throw new MessagesException();

		Log.info("Received request to delete message with id: " + mid + ".");
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

				Log.info("Deleting message with id: " + mid + " in domain: " + sender.getDomain());
				// map that holds all users from the current domain and the domains
				Map<String, String> domains = new HashMap<>(m.getDestination().size());

				for (String recipient : m.getDestination()) {
					String[] name_domain = recipient.split("@");
					String name = name_domain[0];
					String domain = name_domain[1];
					Log.info("MR: Name before if: " + name + " in domain: " + domain);

					Log.info("Same domain: " + domain.equals(sender.getDomain()));
					// if domain doesnt exist or is the current domain
					if (!domains.containsValue(domain) || domain.equals(sender.getDomain())) {
						Log.info("MR: Adding to Map: " + name + " - " + domain);
						domains.put(name, domain);
					}
				}

				for (Entry<String, String> entry : domains.entrySet()) {
					String name = entry.getKey();
					String domain = entry.getValue();

					if (domain.equals(sender.getDomain())) {
						Log.info("MR: Deleting from this domain, from userInbox: " + name + " message: " + mid);
						userInboxs.get(name).remove(mid);
					} else {
						sendDelete(domain, mid);
					}
				}
				allMessages.remove(mid);
			}
		}
	}

	@Override
	public void addMessageToInbox(long newID, String name, Message msg) {
		try {
			Log.info("Received message with ID " + newID + " from another domain.");
			Log.info("Adding msg to " + name + " inbox.");
			if (!userInboxs.containsKey(name))
				userInboxs.put(name, new HashSet<Long>());
			
			Log.info("MI: Passou do if");
			
			userInboxs.get(name).add(newID);
			allMessages.put(newID, msg);
		} catch (Exception e) {
			Log.info("MI: Rebentou np addmessageToInbox exception");
			e.printStackTrace();
		}

	}

	@Override
	public void deleteMessageInOtherServers(long mid) {
		try {
			Log.info("Received request from another domain.");
			Log.info("Deleting message with id: " + mid);
			for (Entry<String, Set<Long>> entry : userInboxs.entrySet())
				entry.getValue().remove(mid);

			allMessages.remove(mid);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// --------------------------------Private_Methods------------------------------------//

	protected static Map<String, Set<Long>> getUserInbox() {
		return userInboxs;
	}

	public static void sendMessage(String domain, long newID, String name, Message msg) {
		MessageServiceSoap messages = null;
		try {
			String serverUrl = Discovery.getUri(domain);
			// if service is different
			if (serverUrl.contains("/rest")) {
				Log.info("MI: Connecting to rest server: " + serverUrl);
				MessageResource.sendMessage(domain, newID, name, msg);
			}

			QName QNAME = new QName(MessageServiceSoap.NAMESPACE, MessageServiceSoap.NAME);
			Service service = Service.create(new URL(serverUrl + MESSAGES_WSDL), QNAME);

			messages = service.getPort(sd1920.trab1.api.soap.MessageServiceSoap.class);
			messages.addMessageToInbox(newID, name, msg);

		} catch (WebServiceException wse) {
			Log.info("MI: Rebentou no webexception wse");
			System.err.println("Could not contact server: " + wse.getMessage());
			System.exit(1);// Terminates client
		} catch (Exception e) {
			Log.info("MI: Rebentou na Exception e");
			e.printStackTrace();
		}
		// Set Timeouts
		/*
		 * ((BindingProvider)
		 * messages).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT,
		 * CONNECTION_TIMEOUT); ((BindingProvider)
		 * messages).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT,
		 * REPLY_TIMEOUT);
		 */
	}

	public static void sendDelete(String domain, long mid) {
		MessageServiceSoap messages = null;
		try {
			String serverUrl = Discovery.getUri(domain);
			Log.info("MI: Trying to connect to rest server: " + serverUrl);
			// if service is different
			if (serverUrl.contains("/rest")) {
				Log.info("MI: Connecting to rest server: " + serverUrl);
				MessageResource.sendDelete(domain, mid);
			}

			QName QNAME = new QName(MessageServiceSoap.NAMESPACE, MessageServiceSoap.NAME);
			Service service = Service.create(new URL(serverUrl + MESSAGES_WSDL), QNAME);

			messages = service.getPort(sd1920.trab1.api.soap.MessageServiceSoap.class);
			messages.deleteMessageInOtherServers(mid);

		} catch (WebServiceException wse) {
			System.err.println("Could not contact server: " + wse.getMessage());
			System.exit(1);// Terminates client
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
