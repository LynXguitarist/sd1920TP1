package sd1920.trab1.server.resources;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import sd1920.trab1.api.User;
import sd1920.trab1.api.rest.UserService;

public class UserResource implements UserService {

	private final List<User> allusers = new LinkedList<>();

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public UserResource() {
	}

	@Override
	public String postUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUser(String name, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User deleteUser(String user, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}

}
