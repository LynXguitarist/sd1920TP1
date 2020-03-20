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
import sd1920.trab1.api.User;
import sd1920.trab1.api.rest.MessageService;

@Singleton
public class MessageResource implements MessageService {

	private final Map<Long, Message> allMessages = new HashMap<Long, Message>();
	private final Map<String, Set<Long>> userInbox = new HashMap<String, Set<Long>>();

	private static Logger Log = Logger.getLogger(MessageResource.class.getName());

	public MessageResource() {

	}

	@Override
	public long postMessage(String pwd, Message msg) {
		
		//verificar a pwd neste if?
		if (msg.getSender() == null || msg.getDestination() == null || msg.getDestination().size() == 0) {
			Log.info("Message was rejected due to lack of recepients.");
			throw new WebApplicationException(Status.CONFLICT);
		}
	
		
		return 0;
	}

	@Override
	public Message getMessage(String user, long mid, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> getMessages(String user, String pwd) {
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
