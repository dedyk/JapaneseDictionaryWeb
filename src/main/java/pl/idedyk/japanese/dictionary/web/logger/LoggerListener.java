package pl.idedyk.japanese.dictionary.web.logger;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;

public class LoggerListener implements MessageListener {
	
	private static final Logger logger = Logger.getLogger(LoggerListener.class);

	@Override
	public void onMessage(Message message) {
				
		if (message instanceof ObjectMessage) {
			
			ObjectMessage objectMessage = (ObjectMessage)message;
			
			Serializable object = null;
			
			try {
				object = objectMessage.getObject();
			} catch (JMSException e) {
				logger.error("Bład pobierania obiektu z ObjectMessage: " + message, e);
				
				return;
			}
			
			if (object instanceof WordDictionarySearchLoggerModel) {
				int fixme = 1; // obsluga
				
				logger.info(object);
				
				
			} else if (object instanceof WordDictionaryAutocompleLoggerModel) {
				int fixme = 1; // obsluga
				
				logger.info(object);
				
				
			} else if (object instanceof WordDictionaryDetailsLoggerModel) {
				int fixme = 1; // obsluga
				
				logger.info(object);				
				
			} else {
				logger.error("Nieznany typ obiektu: " + object.getClass());
			}
			
		} else {
			logger.error("Odebrano nieznany typ komunikatu: " + message);
		}
	}
}
