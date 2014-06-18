package pl.idedyk.japanese.dictionary.web.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.queue.QueueService;

public class LoggerSender {

	private static final Logger logger = Logger.getLogger(LoggerSender.class);
	
	private QueueService queueService;
	
	public void sendLog(LoggerModelCommon loggerModelCommon) {
		
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
		
		/*
		try {
			loggerListener.onMessage(log);
			
		} catch (Exception e) {
			// noop
		}
		*/
				
		ByteArrayOutputStream bos = null;
		ObjectOutput objectOutput = null;
		
		try {
			// serializacja obiektu
			bos = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bos);
			
			objectOutput.writeObject(loggerModelCommon);
			
			objectOutput.close();
			bos.close();
			
			byte[] loggerModelCommonByteArray = bos.toByteArray();
			
			// wstawienie do kolejki
			queueService.sendToQueue("log", loggerModelCommonByteArray);
			
		} catch (Exception e) {
			
			logger.error("Blad zapisu log'a do kolejki", e);
			
		} finally {
			
			if (objectOutput != null) {
				try {
					objectOutput.close();
				} catch (IOException e) {
				}
			}
			
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
		}		
	}

	public QueueService getQueueService() {
		return queueService;
	}

	public void setQueueService(QueueService queueService) {
		this.queueService = queueService;
	}
}
