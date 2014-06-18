package pl.idedyk.japanese.dictionary.web.logger;

import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;

public class LoggerSender {
	
	private LoggerListener loggerListener;
	
	/*
	private JmsTemplate jmsTemplate;
	
	private Destination destination;
	*/
	
	public void sendLog(LoggerModelCommon log) {
		
		/*
		jmsTemplate.send(destination, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
								
				ObjectMessage message = session.createObjectMessage();
				
				message.setObject(serializable);
								
                return message;
			}
		});
		*/
		
		int fixme = 1;
		
		try {
			loggerListener.onMessage(log);
			
		} catch (Exception e) {
			// noop
		}
	}

	public LoggerListener getLoggerListener() {
		return loggerListener;
	}

	public void setLoggerListener(LoggerListener loggerListener) {
		this.loggerListener = loggerListener;
	}

	/*
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
	*/
}
