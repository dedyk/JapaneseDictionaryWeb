package pl.idedyk.japanese.dictionary.web.logger;

import javax.jms.Message;
import javax.jms.MessageListener;

public class LoggerListener implements MessageListener {

	@Override
	public void onMessage(Message message) {
		
		int fixme = 1;
		
		System.out.println("*****: " + message);
	}

}
