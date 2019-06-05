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

import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.AndroidMessageListWrapper;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.MessageEntry;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.WebMessageListWrapper;

@Service
public class MessageService {

	private static final Logger logger = Logger.getLogger(MessageService.class);

	@Autowired
	private ConfigService configService;

	private JAXBContext jaxbContext;
	
	//
	
	private File messageFile = null; 
	private Long messageFileLastModified = null;
	
	private Message message = null;
	
	@PostConstruct
	public void init() throws JAXBException {

		logger.info("Inicjowanie MessageService");
		
		jaxbContext = JAXBContext.newInstance(Message.class);
		
		messageFile = new File(configService.getCatalinaConfDir(), "message.xml");
		
		checkAndReloadMessageFile();
	}
	
	public Message.MessageEntry getMessageForAndroid(String userAgent) {
		
		checkAndReloadMessageFile();
		
		if (userAgent == null) {
			return null;
		}
		
		if (message == null) {
			return null;
		}
		
		// proba znalezienia odpowiedzi
		AndroidMessageListWrapper androidMessageListWrapper = message.getAndroidMessageListWrapper();
		
		if (androidMessageListWrapper == null) {
			return null;
		}
		
		List<MessageEntry> androidMessageList = androidMessageListWrapper.getAndroidMessageList();
		
		MessageEntry defaultMessage = null;
		
		for (MessageEntry message : androidMessageList) {
			
			String messageUserAgentCondition = message.getUserAgentCondition();
			String messageTimestamp = message.getTimestamp();
			
			if (messageUserAgentCondition == null || messageTimestamp == null) {
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

	public Message.MessageEntry getMessageForWeb() {
		
		checkAndReloadMessageFile();
				
		if (message == null) {
			return null;
		}
		
		// proba znalezienia odpowiedzi
		WebMessageListWrapper webMessageListWrapper = message.getWebMessageListWrapper();
		
		if (webMessageListWrapper == null) {
			return null;
		}
		
		List<MessageEntry> webMessageList = webMessageListWrapper.getWebMessageList();
				
		for (MessageEntry message : webMessageList) {
			
			String messageUserAgentCondition = message.getUserAgentCondition();
			String messageTimestamp = message.getTimestamp();
			
			if (messageUserAgentCondition == null || messageTimestamp == null) {
				continue;
			}
			
			if (messageUserAgentCondition.equals("default") == true) { // szukamy tylko default
				return message;
			}			
		}
		
		// nic nie znalezlismy
		return null;
	}
	
	private void checkAndReloadMessageFile() {
		
		// nie ma pliku lub nie mozna go przeczytac
		if (messageFile.exists() == false || messageFile.canRead() == false) {
			
			messageFileLastModified = null;
			message = null;
			
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
			
			message = (Message)unmarshaller.unmarshal(messageFile);
			
			messageFileLastModified = messageFile.lastModified();
			
		} catch (JAXBException e) {
			
			logger.error("Błąd podczas wczytywania pliku: " + messageFile, e);
			
			messageFileLastModified = null;
			message = null;
			
			return;			
		}
	}

	//

	@XmlRootElement(name = "message")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Message {
		
		@XmlElement(name = "androidMessageList")
		private AndroidMessageListWrapper androidMessageListWrapper;
		
		@XmlElement(name = "webMessageList")
		private WebMessageListWrapper webMessageListWrapper;
		
		//
		
		public AndroidMessageListWrapper getAndroidMessageListWrapper() {
			return androidMessageListWrapper;
		}

		public WebMessageListWrapper getWebMessageListWrapper() {
			return webMessageListWrapper;
		}

		public void setAndroidMessageListWrapper(AndroidMessageListWrapper androidMessageListWrapper) {
			this.androidMessageListWrapper = androidMessageListWrapper;
		}

		public void setWebMessageListWrapper(WebMessageListWrapper webMessageListWrapper) {
			this.webMessageListWrapper = webMessageListWrapper;
		}
		
		//

		@XmlAccessorType(XmlAccessType.FIELD)
		public static class AndroidMessageListWrapper {
			
			@XmlElement(name = "androidMessage")
			private List<MessageEntry> androidMessageList;

			public List<MessageEntry> getAndroidMessageList() {
				
				if (androidMessageList == null) {
					androidMessageList = new ArrayList<>();
				}
				
				return androidMessageList;
			}

			public void setAndroidMessageList(List<MessageEntry> androidMessageList) {
				this.androidMessageList = androidMessageList;
			}
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		public static class WebMessageListWrapper {
			
			@XmlElement(name = "webMessage")
			private List<MessageEntry> webMessageList;

			public List<MessageEntry> getWebMessageList() {
				
				if (webMessageList == null) {
					webMessageList = new ArrayList<>();
				}
				
				return webMessageList;
			}

			public void setWebMessageList(List<MessageEntry> webMessageList) {
				this.webMessageList = webMessageList;
			}
		}
		
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class MessageEntry {

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
