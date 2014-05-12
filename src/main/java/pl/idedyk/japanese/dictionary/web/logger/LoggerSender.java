package pl.idedyk.japanese.dictionary.web.logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class LoggerSender {

	private JmsTemplate jmsTemplate;
	
	private Destination destination;
	
	public void sendTestMessage() {
	
		int fixme = 1;
		
		System.out.println("!!!!!!!");
		
		jmsTemplate.send(destination, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				
				TextMessage message = null;
				
                try {
                    message = session.createTextMessage();
                    message.setStringProperty("text", "Hello World");
                    
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                
                return message;
			}
		});
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("!!!!!!! - 22222");
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
