package pl.idedyk.japanese.dictionary.web.logger;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;

public class LoggerSender {

	private JmsTemplate jmsTemplate;
	
	private Destination destination;
	
	public void sendLog(LoggerModelCommon log) {
		sendSerializable(log);
	}

	private void sendSerializable(final Serializable serializable) { 
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				jmsTemplate.send(destination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {
						
						ObjectMessage message = session.createObjectMessage();
						
						message.setObject(serializable);
						                
		                return message;
					}
				});				
			}
		}).start();
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
