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
	private static Map<String, Set<Long>> userInbox = MessageResource.getUserInbox();

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public static final String serviceName = "MessageService";

	public UserResource() {
	}

	@Override
	public String postUser(User user) {

		Log.info("Received request to register the user " + user.getName());
		String serverUrl = Discovery.knownUrisOf(serviceName)[0].toString().trim();

		if (!user.getDomain().equals(serverUrl)) {
			Log.info("User domain is different then the server domain.");
			Log.info("ServerUrl Requested = "+user.getDomain());
			Log.info("Serverurl received = "+serverUrl);
			throw new WebApplicationException(Status.FORBIDDEN);
		} else if (user.getName() == null || user.getDomain() == null || user.getPwd() == null) {
			Log.info("Pwd or domain or username is null.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		Log.info("Creating user.");
		synchronized (this) {
			allusers.put(user.getName(), user);
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
			user = allusers.get(name);
			user_pwd = allusers.get(name).getPwd();
		}

		if (user == null || user_pwd != pwd) {// sees if the user exists or if the pwd is correct
			Log.info("User doesn't exist.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		Log.info("Returning user with name : " + name);
		return allusers.get(name);
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		Log.info("Received request to update user: " + name);

		if (allusers.get(name) == null) {// sees if the user exists
			Log.info("User doesn't exist.");
			throw new WebApplicationException(Status.CONFLICT);
		} else if (allusers.get(name).getPwd() != pwd) {// sees if the pwd is correct
			Log.info("Wrong password.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		String new_pwd = user.getPwd();
		String new_displayName = user.getDisplayName();
		String domain = user.getDomain();

		synchronized (this) {
			if (new_pwd == null)
				new_pwd = allusers.get(name).getPwd();
			if (new_displayName == null)
				new_displayName = allusers.get(name).getDisplayName();
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
	public User deleteUser(String user, String pwd) {
		Log.info("Received request to delete user " + user);
		User user_deleted = null;
		String user_pwd = null;

		synchronized (this) {
			user_deleted = allusers.get(user);
			user_pwd = allusers.get(user).getPwd();
		}

		if (user_deleted == null || user_pwd != pwd) {// sees if the user exists or if the pwd is correct
			Log.info("User dones't exist.");
			throw new WebApplicationException(Status.CONFLICT);
		}

		Log.info("Deleting user " + user);

		synchronized (this) {
			allusers.remove(user);
			userInbox.remove(user);
		}

		Log.info("Returning deleted user " + user);
		return user_deleted;
	}

	public static Map<String, User> getAllusers() {
		return allusers;
	}

}
