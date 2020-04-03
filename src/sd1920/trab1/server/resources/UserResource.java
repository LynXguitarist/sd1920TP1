package sd1920.trab1.server.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import sd1920.trab1.api.User;
import sd1920.trab1.api.rest.UserService;
import sd1920.trab1.discovery.Discovery;
import sd1920.trab1.server.utils.UserUtills;

@Singleton
public class UserResource implements UserService {

	private static final Map<String, User> allusers = new HashMap<>();
	// Map consumed from MessageResource
	private static Map<String, Set<Long>> userInbox = MessageResource.getUserInbox();

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public UserResource() {
	}

	@Override
	public String postUser(User user) {

		Log.info("Received request to register the user " + user.getName());
		String name = "";
		String domain = "";
		String pwd = "";
		boolean hasUser = false;

		synchronized (this) {
			hasUser = allusers.containsKey(user.getName());
		}

		if (user != null) {
			name = user.getName();
			domain = user.getDomain();
			pwd = user.getPwd();
		}

		if (IsNullOrEmpty(name) || IsNullOrEmpty(pwd) || IsNullOrEmpty(domain) || hasUser) {
			Log.info("Pwd or domain or username is null.");
			throw new WebApplicationException(Status.CONFLICT);
		} else if (Discovery.knownUrisOf(domain).length == 0) {
			Log.info("User domain is different then the server domain.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

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
	public User getUser(String name, String pwd) {

		Log.info("Received request to return user with username: " + name);
		User user = null;
		String user_pwd = "";

		synchronized (this) {
			user = allusers.get(name);
			if (user != null)
				user_pwd = user.getPwd();
		}

		if (user == null || !user_pwd.equals(pwd)) {// sees if the user exists or if the pwd is correct
			Log.info("User doesn't exist.");
			Log.info("user password = " + user_pwd + " pwd = " + pwd);
			Log.info("User = " + user);
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Log.info("Returning user with name : " + name);
		return user;
	}

	@Override
	public User updateUser(String name, String pwd, User user) {

		Log.info("Received request to update user: " + name);

		User old_user = null;
		String old_pwd = "";
		synchronized (this) {
			old_user = allusers.get(name);
		}

		if (user != null)
			old_pwd = user.getPwd();

		if (old_user == null) {// sees if the user exists
			Log.info("User doesn't exist.");
			throw new WebApplicationException(Status.FORBIDDEN);
		} else if (!pwd.equals(old_pwd)) {// sees if the pwd is correct
			Log.info("Wrong password.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		String new_pwd = user.getPwd();
		String new_displayName = user.getDisplayName();
		String domain = old_user.getDomain();

		if (IsNullOrEmpty(new_pwd))
			new_pwd = old_pwd;
		if (IsNullOrEmpty(new_displayName))
			new_displayName = old_user.getDisplayName();

		synchronized (this) {
			Log.info("Updating user " + name);
			allusers.put(name, new User(name, new_pwd, domain));
			allusers.get(name).setDisplayName(new_displayName);
		}

		Log.info("Returning user " + name);
		return allusers.get(name);
	}

	@Override
	public User deleteUser(String user, String pwd) {
		Log.info("Received request to delete user " + user);
		String user_pwd = "";
		User old_user = null;

		synchronized (this) {
			old_user = allusers.get(user);
			if (old_user != null)
				user_pwd = old_user.getPwd();
		}

		if (old_user == null || !user_pwd.equals(pwd)) {// sees if the user exists or if the pwd is correct
			Log.info("User dones't exist.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		Log.info("Deleting user " + user);

		synchronized (this) {
			allusers.remove(user);
			userInbox.remove(user);
		}

		Log.info("Returning deleted user " + user);
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
