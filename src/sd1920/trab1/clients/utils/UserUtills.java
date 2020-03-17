package sd1920.trab1.clients.utils;

import sd1920.trab1.api.User;

public class UserUtills {

	public static void printUser(User user) {
		System.out.println("Username: " + user.getName());
		System.out.println("Password: " + user.getPwd());
		System.out.println("Displayname: " + user.getDisplayName());
		System.out.println("Domain: " + user.getDomain());
	}

}
