package sd1920.trab1.server.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import sd1920.trab1.api.User;
import sd1920.trab1.api.rest.UserService;
import sd1920.trab1.clients.utils.UserUtills;

public class UserResource implements UserService {

	private static final Map<String, User> allusers = new HashMap<>();

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public UserResource() {
	}

	@Override
	public String postUser(User user) {// displayName ver bem
		Log.info("Received request to register the user " + user.getName());
		String serverUrl = null;// mudar isto pelo serverUrl

		if (user.getDomain() != serverUrl) {
			Log.info("User domain is different then the server domain.");
			throw new WebApplicationException(Status.FORBIDDEN);
		} else if (user.getName() == null || user.getDomain() == null || user.getPwd() == null) {
			Log.info("Pwd or domain or username is null.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		Log.info("Creating user.");
		synchronized (this) {
			getAllusers().put(user.getName(), user);
		}
		Log.info("Created new user with domain: " + user.getDomain());
		UserUtills.printUser(user);
		return user.getDomain();
	}

	@Override
	public User getUser(String name, String pwd) {
		Log.info("Received request to return user with username: " + name);
		User user = null;
		String user_pwd = null;

		synchronized (this) {
			user = getAllusers().get(name);
			user_pwd = getAllusers().get(name).getPwd();
		}

		if (user == null || user_pwd != pwd) {// sees if the user exists or if the pwd is correct
			Log.info("User doesn't exist.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		Log.info("Returning user with name : " + name);
		return getAllusers().get(name);
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		Log.info("Received request to update user: " + name);
		User old_user = null;
		String old_pwd = null;
		String old_displayName = null;
		String old_domain = null;

		synchronized (this) {
			old_user = getAllusers().get(name);
			old_pwd = getAllusers().get(name).getPwd();
			old_displayName = getAllusers().get(name).getDisplayName();
			old_domain = getAllusers().get(name).getDomain();
		}

		if (old_user == null || old_pwd != pwd) {// sees if the user exists or if the pwd is correct
			Log.info("User doesn't exist.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		String new_pwd = user.getPwd();
		String new_displayName = user.getDisplayName();
		String new_domain = user.getDomain();

		if (new_pwd == null)
			new_pwd = old_pwd;
		if (new_displayName == null)
			new_displayName = old_displayName;
		if (new_domain == null)
			new_domain = old_domain;

		Log.info("Updating user " + name);

		synchronized (this) {
			getAllusers().put(name, new User(name, new_pwd, new_domain));
			getAllusers().get(name).setDisplayName(new_displayName);
		}

		Log.info("Returning user " + name);
		return getAllusers().get(name);
	}

	@Override
	public User deleteUser(String user, String pwd) {
		Log.info("Received request to delete user " + user);
		User user_deleted = null;
		String user_pwd = null;

		synchronized (this) {
			user_deleted = getAllusers().get(user);
			user_pwd = getAllusers().get(user).getPwd();
		}

		if (user_deleted == null || user_pwd != pwd) {// sees if the user exists or if the pwd is correct
			Log.info("User dones't exist.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		Log.info("Deleting user " + user);

		synchronized (this) {
			getAllusers().remove(user);
		}

		Log.info("Returning deleted user " + user);
		return user_deleted;
	}

	public static Map<String, User> getAllusers() {
		return allusers;
	}

}
