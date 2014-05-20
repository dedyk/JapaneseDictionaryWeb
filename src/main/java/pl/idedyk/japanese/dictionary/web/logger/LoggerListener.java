package pl.idedyk.japanese.dictionary.web.logger;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
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
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchLog;

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
				WordDictionaryAutocompleteLog wordDictionaryAutocompleteLog = new WordDictionaryAutocompleteLog();
				
				wordDictionaryAutocompleteLog.setGenericLogId(genericLog.getId());
				wordDictionaryAutocompleteLog.setTerm(wordDictionaryAutocompleteLoggerModel.getTerm());
				wordDictionaryAutocompleteLog.setFoundElements(wordDictionaryAutocompleteLoggerModel.getFoundElemets());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertWordDictionaryAutocompleteLog(wordDictionaryAutocompleteLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_SEARCH) {
				
				WordDictionarySearchLoggerModel wordDictionarySearchLoggerModel = (WordDictionarySearchLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				WordDictionarySearchLog wordDictionarySearchLog = new WordDictionarySearchLog();
				
				wordDictionarySearchLog.setGenericLogId(genericLog.getId());
				
				wordDictionarySearchLog.setFindWordRequestWord(wordDictionarySearchLoggerModel.getFindWordRequest().word);
				
				wordDictionarySearchLog.setFindWordRequestKanji(wordDictionarySearchLoggerModel.getFindWordRequest().searchKanji);
				wordDictionarySearchLog.setFindWordRequestKana(wordDictionarySearchLoggerModel.getFindWordRequest().searchKana);
				wordDictionarySearchLog.setFindWordRequestRomaji(wordDictionarySearchLoggerModel.getFindWordRequest().searchRomaji);
				wordDictionarySearchLog.setFindWordRequestTranslate(wordDictionarySearchLoggerModel.getFindWordRequest().searchTranslate);
				wordDictionarySearchLog.setFindWordRequestInfo(wordDictionarySearchLoggerModel.getFindWordRequest().searchInfo);
				
				wordDictionarySearchLog.setFindWordRequestWordPlace(wordDictionarySearchLoggerModel.getFindWordRequest().wordPlaceSearch.toString());
				
				List<DictionaryEntryType> dictionaryEntryTypeList = wordDictionarySearchLoggerModel.getFindWordRequest().dictionaryEntryTypeList;
				
				StringBuffer dictionaryEntryTypeListSb = new StringBuffer();
				
				for (int dictionaryEntryTypeListIdx = 0; dictionaryEntryTypeList != null && dictionaryEntryTypeListIdx < dictionaryEntryTypeList.size(); ++dictionaryEntryTypeListIdx) {
					dictionaryEntryTypeListSb.append(dictionaryEntryTypeList.get(dictionaryEntryTypeListIdx).toString());
					
					if (dictionaryEntryTypeListIdx != dictionaryEntryTypeList.size() - 1) {
						dictionaryEntryTypeListSb.append("\n");
					}
				}
				
				wordDictionarySearchLog.setFindWordRequestDictionaryEntryTypeList(dictionaryEntryTypeListSb.toString());
				
				wordDictionarySearchLog.setFindWordResultResultSize(wordDictionarySearchLoggerModel.getFindWordResult().result.size());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertWordDictionarySearchLog(wordDictionarySearchLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_DETAILS) {
				
				WordDictionaryDetailsLoggerModel wordDictionaryDetailsLoggerModel = (WordDictionaryDetailsLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				WordDictionaryDetailsLog wordDictionaryDetailsLog = new WordDictionaryDetailsLog();
				
				wordDictionaryDetailsLog.setGenericLogId(genericLog.getId());

				wordDictionaryDetailsLog.setDictionaryEntryId(wordDictionaryDetailsLoggerModel.getDictionaryEntry().getId());
				
				wordDictionaryDetailsLog.setDictionaryEntryKanji(wordDictionaryDetailsLoggerModel.getDictionaryEntry().getKanji());
				
				wordDictionaryDetailsLog.setDictionaryEntryKanaList(
						pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(
								wordDictionaryDetailsLoggerModel.getDictionaryEntry().getKanaList()));

				wordDictionaryDetailsLog.setDictionaryEntryRomajiList(
						pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(
								wordDictionaryDetailsLoggerModel.getDictionaryEntry().getRomajiList()));

				wordDictionaryDetailsLog.setDictionaryEntryTranslateList(
						pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(
								wordDictionaryDetailsLoggerModel.getDictionaryEntry().getTranslates()));

				wordDictionaryDetailsLog.setDictionaryEntryInfo(
						wordDictionaryDetailsLoggerModel.getDictionaryEntry().getInfo());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertWordDictionaryDetailsLog(wordDictionaryDetailsLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_AUTOCOMPLETE) {
					
				KanjiDictionaryAutocompleteLoggerModel kanjiDictionaryAutocompleteLoggerModel = (KanjiDictionaryAutocompleteLoggerModel)object;

				// utworzenie wpisu do bazy danych
				KanjiDictionaryAutocompleteLog kanjiDictionaryAutocompleteLog = new KanjiDictionaryAutocompleteLog();

				kanjiDictionaryAutocompleteLog.setGenericLogId(genericLog.getId());
				kanjiDictionaryAutocompleteLog.setTerm(kanjiDictionaryAutocompleteLoggerModel.getTerm());
				kanjiDictionaryAutocompleteLog.setFoundElements(kanjiDictionaryAutocompleteLoggerModel.getFoundElemets());

				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertKanjiDictionaryAutocompleteLog(kanjiDictionaryAutocompleteLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);

					throw new RuntimeException(e);
				}

			} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH) {
				
				KanjiDictionarySearchLoggerModel kanjiDictionarySearchLoggerModel = (KanjiDictionarySearchLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				KanjiDictionarySearchLog kanjiDictionarySearchLog = new KanjiDictionarySearchLog();
				
				kanjiDictionarySearchLog.setGenericLogId(genericLog.getId());
				
				kanjiDictionarySearchLog.setFindKanjiRequestWord(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().word);
				
				kanjiDictionarySearchLog.setFindKanjiRequestWordPlace(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().wordPlaceSearch.toString());
				
				kanjiDictionarySearchLog.setFindKanjiRequestStrokeCountFrom(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().strokeCountFrom);
				kanjiDictionarySearchLog.setFindKanjiRequestStrokeCountTo(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().strokeCountTo);
				
				kanjiDictionarySearchLog.setFindKanjiResultResultSize(kanjiDictionarySearchLoggerModel.getFindKanjiResult().result.size());
								
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertKanjiDictionarySearchLog(kanjiDictionarySearchLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}				
			}
			
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
