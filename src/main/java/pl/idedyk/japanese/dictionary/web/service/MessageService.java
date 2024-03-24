package pl.idedyk.japanese.dictionary.web.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.AndroidAutocompleteMessageEntry;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.AndroidAutocompleteMessageListWrapper;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.AndroidMessageListWrapper;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.MessageEntry;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.WebMessageListWrapper;

@Service
public class MessageService {

	private static final Logger logger = LogManager.getLogger(MessageService.class);

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
	
	public synchronized Message.MessageEntry getMessageForAndroid(String userAgent) {
		
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
	
	public synchronized Message.AndroidAutocompleteMessageEntry getMessageForAndroidAutocomplete(String userAgent, String autocompleteType) {
		
		checkAndReloadMessageFile();
		
		if (userAgent == null) {
			return null;
		}
		
		if (message == null) {
			return null;
		}
		
		// proba znalezienia odpowiedzi
		AndroidAutocompleteMessageListWrapper androidAutocompleteMessageListWrapper = message.getAndroidAutocompleteMessageListWrapper();
		
		if (androidAutocompleteMessageListWrapper == null) {
			return null;
		}
		
		List<AndroidAutocompleteMessageEntry> androidAutocompleteMessageList = androidAutocompleteMessageListWrapper.getAndroidAutocompleteMessageList();
		
		AndroidAutocompleteMessageEntry defaultMessage = null;
		
		for (AndroidAutocompleteMessageEntry message : androidAutocompleteMessageList) {
			
			String messageUserAgentCondition = message.getUserAgentCondition();
			String messageTimestamp = message.getTimestamp();
			String messageAutocompleteType = message.getAutocompleteType();
			
			if (messageUserAgentCondition == null || messageTimestamp == null || messageAutocompleteType == null) {
				continue;
			}
			
			if (autocompleteType.equals(messageAutocompleteType) == true && messageUserAgentCondition.equals("default") == true) {
				defaultMessage = message;
				
				continue;
			}
			
			// mamy pasujaca odpowiedz
			
			try {
				if (autocompleteType.equals(messageAutocompleteType) == true && userAgent.matches(messageUserAgentCondition) == true) {
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

	public synchronized Message.MessageEntry getMessageForWeb() {
		
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

		@XmlElement(name = "androidAutocompleteList")
		private AndroidAutocompleteMessageListWrapper androidAutocompleteMessageListWrapper;
		
		@XmlElement(name = "webMessageList")
		private WebMessageListWrapper webMessageListWrapper;
		
		//
		
		public AndroidMessageListWrapper getAndroidMessageListWrapper() {
			return androidMessageListWrapper;
		}

		public AndroidAutocompleteMessageListWrapper getAndroidAutocompleteMessageListWrapper() {
			return androidAutocompleteMessageListWrapper;
		}

		public WebMessageListWrapper getWebMessageListWrapper() {
			return webMessageListWrapper;
		}

		public void setAndroidMessageListWrapper(AndroidMessageListWrapper androidMessageListWrapper) {
			this.androidMessageListWrapper = androidMessageListWrapper;
		}

		public void setAndroidAutocompleteMessageListWrapper(AndroidAutocompleteMessageListWrapper androidAutocompleteMessageListWrapper) {
			this.androidAutocompleteMessageListWrapper = androidAutocompleteMessageListWrapper;
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
		public static class AndroidAutocompleteMessageListWrapper {
			
			@XmlElement(name = "androidAutocompleteMessage")
			private List<AndroidAutocompleteMessageEntry> androidAutocompleteMessageList;

			public List<AndroidAutocompleteMessageEntry> getAndroidAutocompleteMessageList() {
				
				if (androidAutocompleteMessageList == null) {
					androidAutocompleteMessageList = new ArrayList<>();
				}
				
				return androidAutocompleteMessageList;
			}

			public void setAndroidAutocompleteMessageList(List<AndroidAutocompleteMessageEntry> androidAutocompleteMessageList) {
				this.androidAutocompleteMessageList = androidAutocompleteMessageList;
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
		
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class AndroidAutocompleteMessageEntry extends MessageEntry {
			
			@XmlElement(name = "autocompleteType")
			private String autocompleteType;

			public String getAutocompleteType() {
				return autocompleteType;
			}

			public void setAutocompleteType(String autocompleteType) {
				this.autocompleteType = autocompleteType;
			}			
		}
	}
}
