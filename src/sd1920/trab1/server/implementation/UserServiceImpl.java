package sd1920.trab1.server.implementation;

import sd1920.trab1.api.User;
import sd1920.trab1.api.soap.MessagesException;
import sd1920.trab1.api.soap.UserServiceSoap;

public class UserServiceImpl implements UserServiceSoap{

	@Override
	public String postUser(User user) throws MessagesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUser(String name, String pwd) throws MessagesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User updateUser(String name, String pwd, User user) throws MessagesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User deleteUser(String name, String pwd) throws MessagesException {
		// TODO Auto-generated method stub
		return null;
	}

}
