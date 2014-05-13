package pl.idedyk.japanese.dictionary.web.logger;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;

public class LoggerSender {

	private JmsTemplate jmsTemplate;
	
	private Destination destination;
	
	public void sendWordDictionarySearchLog(WordDictionarySearchLoggerModel wordDictionarySearchLoggerModel) {
		sendSerializable(wordDictionarySearchLoggerModel);
	}

	public void sendWordDictionaryAutocompleteLog(WordDictionaryAutocompleLoggerModel wordDictionaryAutocompleteLoggerModel) {
		sendSerializable(wordDictionaryAutocompleteLoggerModel);
	}
	
	public void sendWordDictionaryDetailsLog(WordDictionaryDetailsLoggerModel wordDictionaryDetailsLoggerModel) {
		sendSerializable(wordDictionaryDetailsLoggerModel);
	}

	private void sendSerializable(final Serializable serializable) { 
		
		jmsTemplate.send(destination, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				
				ObjectMessage message = session.createObjectMessage();
				
				message.setObject(serializable);
				                
                return message;
			}
		});		
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}
}
