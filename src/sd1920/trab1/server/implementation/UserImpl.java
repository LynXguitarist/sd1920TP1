package sd1920.trab1.server.implementation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.jws.WebService;

import sd1920.trab1.api.User;
import sd1920.trab1.api.soap.MessagesException;
import sd1920.trab1.api.soap.UserServiceSoap;
import sd1920.trab1.server.resources.MessageResource;
import sd1920.trab1.server.utils.UserUtills;

@WebService(serviceName = UserServiceSoap.NAME, targetNamespace = UserServiceSoap.NAMESPACE, endpointInterface = UserServiceSoap.INTERFACE)
public class UserImpl implements UserServiceSoap {

	private static final Map<String, User> allusers = new HashMap<>();
	private static Map<String, Set<Long>> userInbox = MessageResource.getUserInbox();

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public UserImpl() {
	}

	@Override
	public String postUser(User user) throws MessagesException {

		Log.info("Received request to register the user " + user.getName());
		Log.info("Creating user.");
		synchronized (this) {
			allusers.put(user.getName(), user);
		}

		Log.info("Created new user with domain: " + user.getDomain());
		UserUtills.printUser(user);

		String name_domain = user.getName() + "@" + user.getDomain();
		return name_domain;
	}

	@Override
	public User getUser(String name, String pwd) throws MessagesException {

		Log.info("Received request to return user with username: " + name);
		User user = null;

		synchronized (this) {
			user = allusers.get(name);
		}

		Log.info("Returning user with name : " + name);
		return user;
	}

	@Override
	public User updateUser(String name, String pwd, User user) throws MessagesException {

		Log.info("Received request to update user: " + name);

		User old_user = null;
		synchronized (this) {
			old_user = allusers.get(name);
		}

		String new_pwd = user.getPwd();
		String new_displayName = user.getDisplayName();
		String domain = old_user.getDomain();

		synchronized (this) {
			new_pwd = old_user.getPwd();
			new_displayName = old_user.getDisplayName();
		}

		Log.info("Updating user " + name);

		synchronized (this) {
			allusers.put(name, new User(name, new_pwd, domain));
			allusers.get(name).setDisplayName(new_displayName);
		}

		Log.info("Returning user " + name);
		return allusers.get(name);
	}

	@Override
	public User deleteUser(String name, String pwd) throws MessagesException {

		Log.info("Received request to delete user " + name);
		User old_user = null;

		synchronized (this) {
			old_user = allusers.get(name);

			Log.info("Deleting user " + name);
			allusers.remove(name);
			userInbox.remove(name);
		}

		Log.info("Returning deleted user " + name);
		return old_user;
	}

}
