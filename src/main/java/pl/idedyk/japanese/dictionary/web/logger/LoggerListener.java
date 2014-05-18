package pl.idedyk.japanese.dictionary.web.logger;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryRadicalsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocomplete;

public class LoggerListener implements MessageListener {
	
	private static final Logger logger = Logger.getLogger(LoggerListener.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Override
	public void onMessage(Message message) {
		
		if (message instanceof ObjectMessage) {
			
			ObjectMessage objectMessage = (ObjectMessage)message;
			
			Serializable object = null;
			long jmsTimestamp = 0;
			
			try {
				object = objectMessage.getObject();
				
				jmsTimestamp = message.getJMSTimestamp();
				
			} catch (JMSException e) {
				logger.error("Bład pobierania obiektu z ObjectMessage: " + message, e);
				
				return;
			}
			
			GenericLogOperationEnum operation = mapClassToGenericLogOperationEnum(object.getClass());
			
			GenericLog genericLog = null;
			
			if (object instanceof LoggerModelCommon) {
				
				LoggerModelCommon loggerModelCommon = (LoggerModelCommon)object;
				
				// utworzenie wpisu do bazy danych
				genericLog = new GenericLog();
				
				genericLog.setTimestamp(new Timestamp(jmsTimestamp));
				genericLog.setSessionId(loggerModelCommon.getSessionId());
				genericLog.setRemoteIp(loggerModelCommon.getRemoteIp());
				genericLog.setRemoteHost(Utils.getHostname(loggerModelCommon.getRemoteIp()));
				genericLog.setOperation(operation);
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertGenericLog(genericLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
			}
			
			// obsluga specyficznych typow
			if (operation == GenericLogOperationEnum.WORD_DICTIONARY_START) {
				// noop
				
			} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_AUTOCOMPLETE) {
				
				WordDictionaryAutocompleteLoggerModel wordDictionaryAutocompleteLoggerModel = (WordDictionaryAutocompleteLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				WordDictionaryAutocomplete wordDictionaryAutocomplete = new WordDictionaryAutocomplete();
				
				wordDictionaryAutocomplete.setGenericLogId(genericLog.getId());
				wordDictionaryAutocomplete.setTerm(wordDictionaryAutocompleteLoggerModel.getTerm());
				wordDictionaryAutocomplete.setFoundElements(wordDictionaryAutocompleteLoggerModel.getFoundElemets());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertWordDictionaryAutocomplete(wordDictionaryAutocomplete);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}				
			}
			
			// i inne typy
			
			/*
			if (object instanceof WordDictionaryStartLoggerModel) {
				int fixme = 1; // obsluga
				
				logger.info(object);
			
			} else if (object instanceof WordDictionarySearchLoggerModel) {
				int fixme = 1; // obsluga
				
				logger.info(object);
								
			} else if (object instanceof WordDictionaryAutocompleteLoggerModel) {
				int fixme = 1; // obsluga
				
				logger.info(object);
				
				
			} else if (object instanceof WordDictionaryDetailsLoggerModel) {
				int fixme = 1; // obsluga
				
				logger.info(object);				
				
			} else {
				logger.error("Nieznany typ obiektu: " + object.getClass());
			}
			*/
			
		} else {
			logger.error("Odebrano nieznany typ komunikatu: " + message);
		}
	}
	
	private GenericLogOperationEnum mapClassToGenericLogOperationEnum(Class<?> clazz) {
		
		if (WordDictionaryStartLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_START;
			
		} else if (WordDictionaryAutocompleteLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_AUTOCOMPLETE;
			
		} else if (WordDictionarySearchLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_SEARCH;

		} else if (WordDictionaryDetailsLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_DETAILS;

		} else if (KanjiDictionaryStartLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_START;

		} else if (KanjiDictionaryAutocompleteLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_AUTOCOMPLETE;

		} else if (KanjiDictionarySearchLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH;

		} else if (KanjiDictionaryRadicalsLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_RADICALS;

		} else if (KanjiDictionaryDetectLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_DETECT;

		} else if (KanjiDictionaryDetailsLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_DETAILS;

		} else {
			throw new RuntimeException("Nieznany klasa: " + clazz);
		}
	}
}
