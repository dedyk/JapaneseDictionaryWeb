package pl.idedyk.japanese.dictionary.web.logger;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.model.DailyReportLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.InfoLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryRadicalsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.logger.model.RobotsGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SitemapGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.StartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SuggestionSendLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SuggestionStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.mail.MailSender;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GeneralExceptionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetectLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryRadicalsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.SuggestionSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchLog;

public class LoggerListener implements MessageListener {
	
	private static final Logger logger = Logger.getLogger(LoggerListener.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Autowired
	private MailSender mailSender;
		
	@Override
	public void onMessage(Message message) {
		
		logger.info("Przetwarzam zadanie z kolejki");
				
		if (message instanceof ObjectMessage) {
			
			ObjectMessage objectMessage = (ObjectMessage)message;
			
			Serializable object = null;
			
			try {
				object = objectMessage.getObject();
								
			} catch (JMSException e) {
				logger.error("Bład pobierania obiektu z ObjectMessage: " + message, e);
				
				throw new RuntimeException(e);
			}
			
			GenericLogOperationEnum operation = mapClassToGenericLogOperationEnum(object.getClass());
			
			GenericLog genericLog = null;
			
			if (object instanceof LoggerModelCommon) {
				
				LoggerModelCommon loggerModelCommon = (LoggerModelCommon)object;
				
				// utworzenie wpisu do bazy danych
				genericLog = new GenericLog();
				
				genericLog.setTimestamp(new Timestamp(loggerModelCommon.getDate().getTime()));
				genericLog.setSessionId(loggerModelCommon.getSessionId());
				genericLog.setRemoteIp(loggerModelCommon.getRemoteIp());
				genericLog.setUserAgent(loggerModelCommon.getUserAgent());
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
				
			} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_RADICALS) {
				
				KanjiDictionaryRadicalsLoggerModel kanjiDictionaryRadicalsLoggerModel = (KanjiDictionaryRadicalsLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				KanjiDictionaryRadicalsLog kanjiDictionaryRadicalsLog = new KanjiDictionaryRadicalsLog();
				
				kanjiDictionaryRadicalsLog.setGenericLogId(genericLog.getId());
				
				String[] radicals = kanjiDictionaryRadicalsLoggerModel.getRadicals();
				
				if (radicals != null && radicals.length > 0) {
					
					StringBuffer radicalsJoined = new StringBuffer();
					
					for (String currentRadical : radicals) {
						radicalsJoined.append(currentRadical);
					}
					
					kanjiDictionaryRadicalsLog.setRadicals(radicalsJoined.toString());					
				}
				
				kanjiDictionaryRadicalsLog.setFoundElements(kanjiDictionaryRadicalsLoggerModel.getFoundElemets());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertKanjiDictionaryRadicalsLog(kanjiDictionaryRadicalsLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_DETECT) {
				
				KanjiDictionaryDetectLoggerModel kanjiDictionaryDetectLoggerModel = (KanjiDictionaryDetectLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				KanjiDictionaryDetectLog kanjiDictionaryDetectLog = new KanjiDictionaryDetectLog();
				
				kanjiDictionaryDetectLog.setGenericLogId(genericLog.getId());
				
				kanjiDictionaryDetectLog.setStrokes(kanjiDictionaryDetectLoggerModel.getStrokes());
				
				List<KanjiRecognizerResultItem> detectKanjiResultList = kanjiDictionaryDetectLoggerModel.getDetectKanjiResult();
				
				StringBuffer detectKanjiResultSb = new StringBuffer();
				
				for (int idx = 0; idx < 5 && idx < detectKanjiResultList.size(); ++idx) {
					
					KanjiRecognizerResultItem currentKanjiRecognizerResultItem = detectKanjiResultList.get(idx);
					
					detectKanjiResultSb.append(currentKanjiRecognizerResultItem.getKanji() + " " + currentKanjiRecognizerResultItem.getScore());
					
					if (idx != 5 - 1) {
						detectKanjiResultSb.append("\n");
					}
				}
				
				kanjiDictionaryDetectLog.setDetectKanjiResult(detectKanjiResultSb.toString());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertKanjiDictionaryDetectLog(kanjiDictionaryDetectLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_DETAILS) {
				
				KanjiDictionaryDetailsLoggerModel kanjiDictionaryDetailsLoggerModel = (KanjiDictionaryDetailsLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				KanjiDictionaryDetailsLog kanjiDictionaryDetailsLog = new KanjiDictionaryDetailsLog();
				
				kanjiDictionaryDetailsLog.setGenericLogId(genericLog.getId());
				
				kanjiDictionaryDetailsLog.setKanjiEntryId(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getId());

				kanjiDictionaryDetailsLog.setKanjiEntryKanji(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getKanji());
				
				kanjiDictionaryDetailsLog.setKanjiEntryTranslateList(
						pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getPolishTranslates()));
				
				kanjiDictionaryDetailsLog.setKanjiEntryInfo(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getInfo());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertKanjiDictionaryDetailsLog(kanjiDictionaryDetailsLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.SUGGESTION_SEND) {
				
				SuggestionSendLoggerModel suggestionSendLoggerModel = (SuggestionSendLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				SuggestionSendLog suggestionSendLog = new SuggestionSendLog();
				
				suggestionSendLog.setGenericLogId(genericLog.getId());

				suggestionSendLog.setTitle(suggestionSendLoggerModel.getTitle());
				suggestionSendLog.setSender(suggestionSendLoggerModel.getSender());
				suggestionSendLog.setBody(suggestionSendLoggerModel.getBody());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertSuggestionSendLoggerModel(suggestionSendLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
				// wysylanie mail'a
				try {
					mailSender.sendSuggestion(genericLog, suggestionSendLog);
				} catch (Exception e) {
					logger.error("Błąd wysyłki wiadomości", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.DAILY_REPORT) {
				
				DailyReportLoggerModel dailyReportLoggerModel = (DailyReportLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				DailyReportSendLog dailyReportSendLog = new DailyReportSendLog();
				
				dailyReportSendLog.setGenericLogId(genericLog.getId());

				dailyReportSendLog.setTitle(dailyReportLoggerModel.getTitle());
				dailyReportSendLog.setReport(dailyReportLoggerModel.getReport());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertDailyReportSendLog(dailyReportSendLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}

				// wysylanie mail'a
				try {
					mailSender.sendDailyReport(dailyReportSendLog);
				} catch (Exception e) {
					logger.error("Błąd wysyłki wiadomości", e);
					
					throw new RuntimeException(e);
				}
				
			} else if (operation == GenericLogOperationEnum.GENERAL_EXCEPTION) {
				
				GeneralExceptionLoggerModel generalExceptionLoggerModel = (GeneralExceptionLoggerModel)object;
				
				// utworzenie wpisu do bazy danych
				GeneralExceptionLog generalExceptionLog = new GeneralExceptionLog();
				
				generalExceptionLog.setGenericLogId(genericLog.getId());
				
				generalExceptionLog.setRequestUri(generalExceptionLoggerModel.getRequestURI());
				generalExceptionLog.setStatusCode(generalExceptionLoggerModel.getStatusCode());				
				
				Throwable throwable = generalExceptionLoggerModel.getThrowable();
								
				StringBuffer exceptionSb = new StringBuffer();
				
				exceptionSb.append(throwable.getClass() + ":").append(ExceptionUtils.getMessage(throwable)).append("\n\n").append(ExceptionUtils.getStackTrace(throwable));
				
				generalExceptionLog.setException(exceptionSb.toString());
				
				// wstawienie wpisu do bazy danych
				try {
					mySQLConnector.insertGeneralExceptionLog(generalExceptionLog);
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
				// wysylanie mail'a
				try {
					mailSender.sendGeneralExceptionLog(genericLog, generalExceptionLog);
				} catch (Exception e) {
					logger.error("Błąd wysyłki wiadomości", e);
					
					throw new RuntimeException(e);
				}
			}
			
		} else {
			logger.error("Odebrano nieznany typ komunikatu: " + message);
		}
	}
	
	private GenericLogOperationEnum mapClassToGenericLogOperationEnum(Class<?> clazz) {
		
		if (StartLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.START;
			
		} else if (RobotsGenerateLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.ROBOTS_GENERATE;
			
		} else if (SitemapGenerateLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.SITEMAP_GENERATE;
			
		} else if (WordDictionaryStartLoggerModel.class.isAssignableFrom(clazz) == true) {
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

		} else if (SuggestionStartLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.SUGGESTION_START;
			
		} else if (SuggestionSendLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.SUGGESTION_SEND;
		
		} else if (DailyReportLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.DAILY_REPORT;
		
		} else if (InfoLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.INFO;
					
		} else if (GeneralExceptionLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.GENERAL_EXCEPTION;
			
		} else {
			throw new RuntimeException("Nieznany klasa: " + clazz);
		}
	}
}