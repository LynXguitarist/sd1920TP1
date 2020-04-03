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
	protected static Map<String, Set<Long>> userInbox = MessageImpl.getUserInbox();

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public UserImpl() {
	}

	/*
	 * fiz assim porque o if fora do sync tava a dar me erros que nao tava a
	 * perceber muda se achares que e melhor
	 */
	@Override
	public String postUser(User user) throws MessagesException {

		Log.info("Received request to register the user " + user.getName());
		Log.info("Creating user.");
		synchronized (this) {
			if (allusers.containsKey(user.getName()) || IsNullOrEmpty(user.getName()) || IsNullOrEmpty(user.getPwd())
					|| IsNullOrEmpty(user.getDomain()) || user.getDomain().contains(" ")) {

				throw new MessagesException();

			} else {

				allusers.put(user.getName(), user);
			}

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
		// Exeptions
		if (user == null || IsNullOrEmpty(pwd) || !pwd.equals(user.getPwd()))
			throw new MessagesException();

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
			if (IsNullOrEmpty(new_pwd))
				new_pwd = old_user.getPwd();
			if (IsNullOrEmpty(new_displayName))
				new_displayName = old_user.getDisplayName();
		
		}

		Log.info("Updating user " + name);

		synchronized (this) {
			
			allusers.put(name, new User(name, new_pwd, domain));
			allusers.get(name).setDisplayName(new_displayName);
		
			
		}

		if(!old_user.getPwd().contentEquals(pwd)) {
			throw new MessagesException();
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

	protected synchronized static Map<String, User> getAllusers() {
		return allusers;
	}

	private boolean IsNullOrEmpty(String string) {
		if (string == null || string.isEmpty())
			return true;

		return false;
	}

}
