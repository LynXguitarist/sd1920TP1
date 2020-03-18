package sd1920.trab1.server.resources;

import java.util.List;
import javax.inject.Singleton;
import sd1920.trab1.api.Message;
import sd1920.trab1.api.rest.MessageService;

@Singleton
public class MessageResource implements MessageService {

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
