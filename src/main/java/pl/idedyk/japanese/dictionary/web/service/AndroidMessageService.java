package pl.idedyk.japanese.dictionary.web.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.service.AndroidMessageService.AndroidMessage.Message;

@Service
public class AndroidMessageService {

	private static final Logger logger = Logger.getLogger(AndroidMessageService.class);

	@Autowired
	private ConfigService configService;

	private JAXBContext jaxbContext;
	
	//
	
	private File messageFile = null; 
	private Long messageFileLastModified = null;
	
	private AndroidMessage androidMessage = null;
	
	@PostConstruct
	public void init() throws JAXBException {

		logger.info("Inicjowanie AndroidMessageService");
		
		jaxbContext = JAXBContext.newInstance(AndroidMessage.class);
		
		messageFile = new File(configService.getCatalinaConfDir(), "android_message.xml");
		
		checkAndReloadMessageFile();
	}
	
	public AndroidMessage.Message getMessage(String userAgent) {
		
		checkAndReloadMessageFile();
		
		if (userAgent == null) {
			return null;
		}
		
		if (androidMessage == null) {
			return null;
		}
		
		// proba znalezienia odpowiedzi
		List<Message> messageList = androidMessage.getMessageList();
		
		Message defaultMessage = null;
		
		for (Message message : messageList) {
			
			String messageUserAgentCondition = message.getUserAgentCondition();
			String messageTimestamp = message.getTimestamp();
			String messageMessage = message.getMessage();
			
			if (messageUserAgentCondition == null || messageTimestamp == null || messageMessage == null) {
				continue;
			}
			
			if (messageUserAgentCondition.equals("default") == true) {
				defaultMessage = message;
				
				continue;
			}
			
			// mamy pasujaca odpowiedz
			
			try {
				if (userAgent.matches(messageUserAgentCondition) == true) {
					return message;
				}
				
			} catch (PatternSyntaxException e) {
				logger.error("Niepoprawne wyrażenie regularne dla: " + messageUserAgentCondition);
				
				continue;
			}
			
		}
		
		// nic nie dopasowalismy, zwrocenie domyslnej odpowiedzi (jesli jakas dopasowala sie)
		return defaultMessage;
	}
	
	private void checkAndReloadMessageFile() {
		
		// nie ma pliku lub nie mozna go przeczytac
		if (messageFile.exists() == false || messageFile.canRead() == false) {
			
			messageFileLastModified = null;
			androidMessage = null;
			
			return;
		}
		
		// plik nie zmienil sie
		if (messageFileLastModified != null && messageFileLastModified.longValue() == messageFile.lastModified()) {
			return;
		}
		
		// probujemy wczytac plik
		logger.info("Wczytywanie pliku: " + messageFile);
		
		try {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			
			androidMessage = (AndroidMessage)unmarshaller.unmarshal(messageFile);
			
			messageFileLastModified = messageFile.lastModified();
			
		} catch (JAXBException e) {
			
			logger.error("Błąd podczas wczytywania pliku: " + messageFile, e);
			
			messageFileLastModified = null;
			androidMessage = null;
			
			return;			
		}
	}

	//

	@XmlRootElement(name = "AndroidMessage")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class AndroidMessage {

		private List<Message> messageList;

		public List<Message> getMessageList() {
			
			if (messageList == null) {
				messageList = new ArrayList<>();
			}
			
			return messageList;
		}

		public void setMessageList(List<Message> messageList) {
			this.messageList = messageList;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		public static class Message {

			@XmlElement(name = "userAgentCondition")
			private String userAgentCondition;

			@XmlElement(name = "timestamp")
			private String timestamp;

			@XmlElement(name = "message")
			private String message;

			public String getUserAgentCondition() {
				return userAgentCondition;
			}

			public String getTimestamp() {
				return timestamp;
			}

			public String getMessage() {
				return message;
			}

			public void setUserAgentCondition(String userAgentCondition) {
				this.userAgentCondition = userAgentCondition;
			}

			public void setTimestamp(String timestamp) {
				this.timestamp = timestamp;
			}

			public void setMessage(String message) {
				this.message = message;
			}			
		}
	}

}
