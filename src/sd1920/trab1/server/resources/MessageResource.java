package sd1920.trab1.server.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import sd1920.trab1.api.Message;
import sd1920.trab1.api.rest.MessageService;
import sd1920.trab1.clients.utils.MessageUtills;

@Singleton
public class MessageResource implements MessageService{

	@Override
	public long postMessage(String pwd, Message msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Message getMessage(String user, long mid, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Message> getMessages(String user, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeFromUserInbox(String user, long mid, String pwd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMessage(String user, long mid, String pwd) {
		// TODO Auto-generated method stub
		
	}

	
}
