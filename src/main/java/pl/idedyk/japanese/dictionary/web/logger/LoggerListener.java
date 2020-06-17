package pl.idedyk.japanese.dictionary.web.logger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidGetMessageLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel.Result;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel.Type;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidGetSpellCheckerSuggestionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidQueueEventLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidSendMissingWordLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.BingSiteAuthGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.ClientBlockLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.DailyReportLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.FaviconIconSendLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.InfoLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryAllKanjisLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryGetKanjiEntryListLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryRadicalsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchStrokeCountLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.logger.model.MethodNotAllowedExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RobotsGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.ServiceUnavailableExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SitemapGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.StartAppLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.StartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SuggestionSendLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SuggestionStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetDictionaryEntriesNameSizeLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetDictionaryEntriesSizeLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetDictionaryEntryGroupTypesLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetGroupDictionaryEntriesLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetTatoebaSentenceGroupLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetTransitiveIntransitivePairsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetWordPowerListLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryPdfDictionaryLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.mail.MailSender;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.AdminRequestLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidGetSpellCheckerSuggestionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidQueueEventLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidSendMissingWordLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GeneralExceptionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetectLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryRadicalsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.SuggestionSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchMissingWordQueue;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryUniqueSearch;

public class LoggerListener {
	
	private static final Logger logger = Logger.getLogger(LoggerListener.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Autowired
	private MailSender mailSender;
		
	@SuppressWarnings("unchecked")
	public void onMessage(LoggerModelCommon loggerModelCommon) {
							
		GenericLogOperationEnum operation = mapClassToGenericLogOperationEnum(loggerModelCommon.getClass());
		
		GenericLog genericLog = null;
		
		// ogolna obsluga			
		logger.info("Przetwarzam zadanie " + operation + " z kolejki od: " + loggerModelCommon.getRemoteIp() + " / " + Utils.getHostname(loggerModelCommon.getRemoteIp()));
		
		// utworzenie wpisu do bazy danych
		genericLog = new GenericLog();
		
		genericLog.setTimestamp(new Timestamp(loggerModelCommon.getDate().getTime()));
		genericLog.setSessionId(loggerModelCommon.getSessionId());
		genericLog.setRemoteIp(loggerModelCommon.getRemoteIp());
		genericLog.setUserAgent(loggerModelCommon.getUserAgent());
		genericLog.setRequestURL(loggerModelCommon.getRequestURL());
		genericLog.setRefererURL(loggerModelCommon.getRefererURL());
		genericLog.setRemoteHost(Utils.getHostname(loggerModelCommon.getRemoteIp()));
		genericLog.setOperation(operation);
		
		// wstawienie wpisu do bazy danych
		try {
			mySQLConnector.insertGenericLog(genericLog);
		} catch (SQLException e) {
			logger.error("Błąd podczas zapisu do bazy danych", e);
			
			throw new RuntimeException(e);
		}				
		
		// obsluga specyficznych typow
		if (operation == GenericLogOperationEnum.START_APP) {
			
			// wysylanie info
			try {
				mailSender.sendStartAppInfo(genericLog);
			} catch (Exception e) {
				logger.error("Błąd wysyłki wiadomości", e);
				
				throw new RuntimeException(e);
			}
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_START) {
			// noop
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_AUTOCOMPLETE) {
			
			WordDictionaryAutocompleteLoggerModel wordDictionaryAutocompleteLoggerModel = (WordDictionaryAutocompleteLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final WordDictionaryAutocompleteLog wordDictionaryAutocompleteLog = new WordDictionaryAutocompleteLog();
			
			wordDictionaryAutocompleteLog.setGenericLogId(genericLog.getId());
			wordDictionaryAutocompleteLog.setTerm(wordDictionaryAutocompleteLoggerModel.getTerm());
			wordDictionaryAutocompleteLog.setFoundElements(wordDictionaryAutocompleteLoggerModel.getFoundElemets());
			
			// wstawienie wpisu do bazy danych
			try {				
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertWordDictionaryAutocompleteLog(wordDictionaryAutocompleteLog);						
					}
					
					@Override
					public void changeDate() {						
						wordDictionaryAutocompleteLog.setTerm(Utils.stringToBase64String(wordDictionaryAutocompleteLog.getTerm()));
					}
				});

			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_SEARCH) {
			
			WordDictionarySearchLoggerModel wordDictionarySearchLoggerModel = (WordDictionarySearchLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final WordDictionarySearchLog wordDictionarySearchLog = new WordDictionarySearchLog();
			
			wordDictionarySearchLog.setGenericLogId(genericLog.getId());
			
			wordDictionarySearchLog.setFindWordRequestWord(wordDictionarySearchLoggerModel.getFindWordRequest().word);
			
			wordDictionarySearchLog.setFindWordRequestKanji(wordDictionarySearchLoggerModel.getFindWordRequest().searchKanji);
			wordDictionarySearchLog.setFindWordRequestKana(wordDictionarySearchLoggerModel.getFindWordRequest().searchKana);
			wordDictionarySearchLog.setFindWordRequestRomaji(wordDictionarySearchLoggerModel.getFindWordRequest().searchRomaji);
			wordDictionarySearchLog.setFindWordRequestTranslate(wordDictionarySearchLoggerModel.getFindWordRequest().searchTranslate);
			wordDictionarySearchLog.setFindWordRequestInfo(wordDictionarySearchLoggerModel.getFindWordRequest().searchInfo);
			
			wordDictionarySearchLog.setFindWordRequestOnlyCommonWords(wordDictionarySearchLoggerModel.getFindWordRequest().searchOnlyCommonWord);
			
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
			
			wordDictionarySearchLog.setPriority(wordDictionarySearchLoggerModel.getPriority());
			
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertWordDictionarySearchLog(wordDictionarySearchLog);						
					}
					
					@Override
					public void changeDate() {						
						wordDictionarySearchLog.setFindWordRequestWord(Utils.stringToBase64String(wordDictionarySearchLog.getFindWordRequestWord()));
					}
				});
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			
			// wstawienie slowka do kolejki brakujacych slow
			if (wordDictionarySearchLoggerModel.getFindWordResult().result.size() == 0) {
				
				WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue = null;
				
				try {
					wordDictionarySearchMissingWordQueue = mySQLConnector.getWordDictionarySearchMissingWordsQueue(wordDictionarySearchLog.getFindWordRequestWord());

				} catch (SQLException e) {
					logger.error("Błąd podczas dostępu do bazy danych", e);

					throw new RuntimeException(e);
				}
				
				if (wordDictionarySearchMissingWordQueue == null) { // nowe slowko w kolejce
					
					wordDictionarySearchMissingWordQueue = new WordDictionarySearchMissingWordQueue();
					
					wordDictionarySearchMissingWordQueue.setMissingWord(wordDictionarySearchLog.getFindWordRequestWord());
					wordDictionarySearchMissingWordQueue.setCounter(1);
					wordDictionarySearchMissingWordQueue.setFirstAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
					wordDictionarySearchMissingWordQueue.setLastAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
					wordDictionarySearchMissingWordQueue.setLockTimestamp(null);
					wordDictionarySearchMissingWordQueue.setPriority(wordDictionarySearchLoggerModel.getPriority());
					
					try {
						final WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue2 = wordDictionarySearchMissingWordQueue;
						
						repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
							
							@Override
							public void operation() throws SQLException {
								mySQLConnector.insertWordDictionarySearchMissingWordsQueue(wordDictionarySearchMissingWordQueue2);						
							}
							
							@Override
							public void changeDate() {								
								wordDictionarySearchMissingWordQueue2.setMissingWord(Utils.stringToBase64String(wordDictionarySearchMissingWordQueue2.getMissingWord()));
							}
						});
						
					} catch (SQLException e) {
						logger.error("Błąd podczas zapisu do bazy danych", e);
						
						throw new RuntimeException(e);
					}
					
				} else { // uaktualnienie istniejacego wpisu
					
					wordDictionarySearchMissingWordQueue.setCounter(wordDictionarySearchMissingWordQueue.getCounter() + 1);
					
					if (wordDictionarySearchLoggerModel.getDate().getTime() < wordDictionarySearchMissingWordQueue.getFirstAppearanceTimestamp().getTime()) {
						wordDictionarySearchMissingWordQueue.setFirstAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
					}
					
					if (wordDictionarySearchLoggerModel.getDate().getTime() > wordDictionarySearchMissingWordQueue.getLastAppearanceTimestamp().getTime()) {
						wordDictionarySearchMissingWordQueue.setLastAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
					}					
					
					if (wordDictionarySearchLoggerModel.getPriority() > wordDictionarySearchMissingWordQueue.getPriority()) {
						wordDictionarySearchMissingWordQueue.setPriority(wordDictionarySearchLoggerModel.getPriority());
					}

					try {
						mySQLConnector.updateWordDictionarySearchMissingWordQueue(wordDictionarySearchMissingWordQueue);
						
					} catch (SQLException e) {
						logger.error("Błąd podczas zapisu do bazy danych", e);
						
						throw new RuntimeException(e);
					}
				}
			}		
						
			// wstawienie slowka do listy unikalnych slowek
			String uniqueWord = wordDictionarySearchLog.getFindWordRequestWord();
			
			if (uniqueWord.length() > 65) {
				uniqueWord = uniqueWord.substring(0, 65);
			}
			
			WordDictionaryUniqueSearch wordDictionaryUniqueSearch = null;
			
			try {
				wordDictionaryUniqueSearch = mySQLConnector.getWordDictionaryUniqueSearch(uniqueWord);

			} catch (SQLException e) {
				logger.error("Błąd podczas dostępu do bazy danych", e);

				throw new RuntimeException(e);
			}
			
			if (wordDictionaryUniqueSearch == null) { // nowe slowko do listy
				
				wordDictionaryUniqueSearch = new WordDictionaryUniqueSearch();
				
				wordDictionaryUniqueSearch.setWord(uniqueWord);
				wordDictionaryUniqueSearch.setCounter(1);
				wordDictionaryUniqueSearch.setFirstAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
				wordDictionaryUniqueSearch.setLastAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
				
				try {
					final WordDictionaryUniqueSearch wordDictionaryUniqueSearch2 = wordDictionaryUniqueSearch;
					
					repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
						
						@Override
						public void operation() throws SQLException {
							mySQLConnector.insertWordDictionaryUniqueSearch(wordDictionaryUniqueSearch2);						
						}
						
						@Override
						public void changeDate() {								
							wordDictionaryUniqueSearch2.setWord(Utils.stringToBase64String(wordDictionaryUniqueSearch2.getWord()));
						}
					});
					
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
				
			} else { // uaktualnienie istniejacego wpisu
				
				wordDictionaryUniqueSearch.setCounter(wordDictionaryUniqueSearch.getCounter() + 1);
				
				if (wordDictionarySearchLoggerModel.getDate().getTime() < wordDictionaryUniqueSearch.getFirstAppearanceTimestamp().getTime()) {
					wordDictionaryUniqueSearch.setFirstAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
				}
				
				if (wordDictionarySearchLoggerModel.getDate().getTime() > wordDictionaryUniqueSearch.getLastAppearanceTimestamp().getTime()) {
					wordDictionaryUniqueSearch.setLastAppearanceTimestamp(new Timestamp(wordDictionarySearchLoggerModel.getDate().getTime()));
				}					
				
				try {
					mySQLConnector.updateWordDictionaryUniqueSearch(wordDictionaryUniqueSearch);
					
				} catch (SQLException e) {
					logger.error("Błąd podczas zapisu do bazy danych", e);
					
					throw new RuntimeException(e);
				}
			}
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_DETAILS) {
			
			/*
			WordDictionaryDetailsLoggerModel wordDictionaryDetailsLoggerModel = (WordDictionaryDetailsLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final WordDictionaryDetailsLog wordDictionaryDetailsLog = new WordDictionaryDetailsLog();
			
			wordDictionaryDetailsLog.setGenericLogId(genericLog.getId());

			wordDictionaryDetailsLog.setDictionaryEntryId(wordDictionaryDetailsLoggerModel.getDictionaryEntry().getId());
			
			wordDictionaryDetailsLog.setDictionaryEntryKanji(wordDictionaryDetailsLoggerModel.getDictionaryEntry().getKanji());
			
			wordDictionaryDetailsLog.setDictionaryEntryKanaList(wordDictionaryDetailsLoggerModel.getDictionaryEntry().getKana());

			wordDictionaryDetailsLog.setDictionaryEntryRomajiList(wordDictionaryDetailsLoggerModel.getDictionaryEntry().getRomaji());

			wordDictionaryDetailsLog.setDictionaryEntryTranslateList(
					pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(
							wordDictionaryDetailsLoggerModel.getDictionaryEntry().getTranslates()));

			wordDictionaryDetailsLog.setDictionaryEntryInfo(
					wordDictionaryDetailsLoggerModel.getDictionaryEntry().getInfo());
			
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertWordDictionaryDetailsLog(wordDictionaryDetailsLog);						
					}
					
					@Override
					public void changeDate() {							
						wordDictionaryDetailsLog.setDictionaryEntryKanji(Utils.stringToBase64String(wordDictionaryDetailsLog.getDictionaryEntryKanji()));
					}
				});				
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			*/
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_CATALOG) {
			
			/*
			WordDictionaryCatalogLoggerModel wordDictionaryCatalogLoggerModel = (WordDictionaryCatalogLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			WordDictionaryCatalogLog wordDictionaryCatalogLog = new WordDictionaryCatalogLog();
			
			wordDictionaryCatalogLog.setGenericLogId(genericLog.getId());
			wordDictionaryCatalogLog.setPageNo(wordDictionaryCatalogLoggerModel.getPageNo());
						
			// wstawienie wpisu do bazy danych
			try {
				mySQLConnector.insertWordDictionaryCatalogLog(wordDictionaryCatalogLog);
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			*/
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_NAME_DETAILS) {
			
			/*
			WordDictionaryNameDetailsLoggerModel wordDictionaryNameDetailsLoggerModel = (WordDictionaryNameDetailsLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			WordDictionaryNameDetailsLog wordDictionaryNameDetailsLog = new WordDictionaryNameDetailsLog();
			
			wordDictionaryNameDetailsLog.setGenericLogId(genericLog.getId());

			wordDictionaryNameDetailsLog.setDictionaryEntryId(wordDictionaryNameDetailsLoggerModel.getDictionaryEntry().getId());
			
			wordDictionaryNameDetailsLog.setDictionaryEntryKanji(wordDictionaryNameDetailsLoggerModel.getDictionaryEntry().getKanji());
			
			wordDictionaryNameDetailsLog.setDictionaryEntryKanaList(wordDictionaryNameDetailsLoggerModel.getDictionaryEntry().getKana());

			wordDictionaryNameDetailsLog.setDictionaryEntryRomajiList(wordDictionaryNameDetailsLoggerModel.getDictionaryEntry().getRomaji());

			wordDictionaryNameDetailsLog.setDictionaryEntryTranslateList(
					pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(
							wordDictionaryNameDetailsLoggerModel.getDictionaryEntry().getTranslates()));

			wordDictionaryNameDetailsLog.setDictionaryEntryInfo(
					wordDictionaryNameDetailsLoggerModel.getDictionaryEntry().getInfo());
			
			// wstawienie wpisu do bazy danych
			try {
				mySQLConnector.insertWordDictionaryNameDetailsLog(wordDictionaryNameDetailsLog);
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			*/
			
		} else if (operation == GenericLogOperationEnum.WORD_DICTIONARY_NAME_CATALOG) {
			
			/*
			WordDictionaryNameCatalogLoggerModel wordDictionaryNameCatalogLoggerModel = (WordDictionaryNameCatalogLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			WordDictionaryNameCatalogLog wordDictionaryNameCatalogLog = new WordDictionaryNameCatalogLog();
			
			wordDictionaryNameCatalogLog.setGenericLogId(genericLog.getId());
			wordDictionaryNameCatalogLog.setPageNo(wordDictionaryNameCatalogLoggerModel.getPageNo());
						
			// wstawienie wpisu do bazy danych
			try {
				mySQLConnector.insertWordDictionaryNameCatalogLog(wordDictionaryNameCatalogLog);
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			*/
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_AUTOCOMPLETE) {
				
			KanjiDictionaryAutocompleteLoggerModel kanjiDictionaryAutocompleteLoggerModel = (KanjiDictionaryAutocompleteLoggerModel)loggerModelCommon;

			// utworzenie wpisu do bazy danych
			final KanjiDictionaryAutocompleteLog kanjiDictionaryAutocompleteLog = new KanjiDictionaryAutocompleteLog();

			kanjiDictionaryAutocompleteLog.setGenericLogId(genericLog.getId());
			kanjiDictionaryAutocompleteLog.setTerm(kanjiDictionaryAutocompleteLoggerModel.getTerm());
			kanjiDictionaryAutocompleteLog.setFoundElements(kanjiDictionaryAutocompleteLoggerModel.getFoundElemets());

			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertKanjiDictionaryAutocompleteLog(kanjiDictionaryAutocompleteLog);						
					}
					
					@Override
					public void changeDate() {						
						kanjiDictionaryAutocompleteLog.setTerm(Utils.stringToBase64String(kanjiDictionaryAutocompleteLog.getTerm()));
					}
				});
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);

				throw new RuntimeException(e);
			}

		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH) {
			
			KanjiDictionarySearchLoggerModel kanjiDictionarySearchLoggerModel = (KanjiDictionarySearchLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final KanjiDictionarySearchLog kanjiDictionarySearchLog = new KanjiDictionarySearchLog();
			
			kanjiDictionarySearchLog.setGenericLogId(genericLog.getId());
			
			kanjiDictionarySearchLog.setFindKanjiRequestWord(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().word);
			
			kanjiDictionarySearchLog.setFindKanjiRequestWordPlace(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().wordPlaceSearch.toString());
			
			kanjiDictionarySearchLog.setFindKanjiRequestStrokeCountFrom(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().strokeCountFrom);
			kanjiDictionarySearchLog.setFindKanjiRequestStrokeCountTo(kanjiDictionarySearchLoggerModel.getFindKanjiRequest().strokeCountTo);
			
			kanjiDictionarySearchLog.setFindKanjiResultResultSize(kanjiDictionarySearchLoggerModel.getFindKanjiResult().result.size());
							
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertKanjiDictionarySearchLog(kanjiDictionarySearchLog);						
					}
					
					@Override
					public void changeDate() {						
						kanjiDictionarySearchLog.setFindKanjiRequestWord(Utils.stringToBase64String(kanjiDictionarySearchLog.getFindKanjiRequestWord()));
					}
				});
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_RADICALS) {
			
			KanjiDictionaryRadicalsLoggerModel kanjiDictionaryRadicalsLoggerModel = (KanjiDictionaryRadicalsLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			KanjiDictionaryRadicalsLog kanjiDictionaryRadicalsLog = new KanjiDictionaryRadicalsLog();
			
			kanjiDictionaryRadicalsLog.setGenericLogId(genericLog.getId());
			
			String[] radicals = kanjiDictionaryRadicalsLoggerModel.getRadicals();
			
			if (radicals != null && radicals.length > 0) {
				
				StringBuffer radicalsJoined = new StringBuffer();
				
				for (String currentRadical : radicals) {
					
					if (currentRadical.equals("𠆢") == true) { // maly problem z mysql, znak U+201A2 (daszek)						
						currentRadical = "个";
					}
					
					if (currentRadical.equals("𠂉") == true) { // maly problem z mysql, znak U+20089 (daszek)
						currentRadical = "/-";
					}
					
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
			
			KanjiDictionaryDetectLoggerModel kanjiDictionaryDetectLoggerModel = (KanjiDictionaryDetectLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final KanjiDictionaryDetectLog kanjiDictionaryDetectLog = new KanjiDictionaryDetectLog();
			
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
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertKanjiDictionaryDetectLog(kanjiDictionaryDetectLog);			
					}
					
					@Override
					public void changeDate() {						
						kanjiDictionaryDetectLog.setDetectKanjiResult(Utils.stringToBase64String(kanjiDictionaryDetectLog.getDetectKanjiResult()));
					}
				});
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_DETAILS) {
			
			/*
			KanjiDictionaryDetailsLoggerModel kanjiDictionaryDetailsLoggerModel = (KanjiDictionaryDetailsLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final KanjiDictionaryDetailsLog kanjiDictionaryDetailsLog = new KanjiDictionaryDetailsLog();
			
			kanjiDictionaryDetailsLog.setGenericLogId(genericLog.getId());
			
			kanjiDictionaryDetailsLog.setKanjiEntryId(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getId());
						
			kanjiDictionaryDetailsLog.setKanjiEntryKanji(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getKanji());
			
			kanjiDictionaryDetailsLog.setKanjiEntryTranslateList(
					pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getPolishTranslates()));
			
			kanjiDictionaryDetailsLog.setKanjiEntryInfo(kanjiDictionaryDetailsLoggerModel.getKanjiEntry().getInfo());
						
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertKanjiDictionaryDetailsLog(kanjiDictionaryDetailsLog);			
					}
					
					@Override
					public void changeDate() {
						kanjiDictionaryDetailsLog.setKanjiEntryKanji(Utils.stringToBase64String(kanjiDictionaryDetailsLog.getKanjiEntryKanji()));
					}
				});
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			*/
			
		} else if (operation == GenericLogOperationEnum.KANJI_DICTIONARY_CATALOG) {
			
			/*
			KanjiDictionaryCatalogLoggerModel kanjiDictionaryCatalogLoggerModel = (KanjiDictionaryCatalogLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			KanjiDictionaryCatalogLog kanjiDictionaryCatalogLog = new KanjiDictionaryCatalogLog();
			
			kanjiDictionaryCatalogLog.setGenericLogId(genericLog.getId());
			kanjiDictionaryCatalogLog.setPageNo(kanjiDictionaryCatalogLoggerModel.getPageNo());
						
			// wstawienie wpisu do bazy danych
			try {
				mySQLConnector.insertKanjiDictionaryCatalogLog(kanjiDictionaryCatalogLog);
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			*/
		
		} else if (operation == GenericLogOperationEnum.ANDROID_SEND_MISSING_WORD) {
			
			AndroidSendMissingWordLoggerModel androidSendMissingWordLoggerModel = (AndroidSendMissingWordLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final AndroidSendMissingWordLog androidSendMissingWordLog = new AndroidSendMissingWordLog();
			
			androidSendMissingWordLog.setGenericLogId(genericLog.getId());
			androidSendMissingWordLog.setWord(androidSendMissingWordLoggerModel.getWord());
			androidSendMissingWordLog.setWordPlaceSearch(androidSendMissingWordLoggerModel.getWordPlaceSearch().toString());
			
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertAndroidSendMissingWordLog(androidSendMissingWordLog);						
					}
					
					@Override
					public void changeDate() {						
						androidSendMissingWordLog.setWord(Utils.stringToBase64String(androidSendMissingWordLog.getWord()));
					}
				});				
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}			
			
		} else if (operation == GenericLogOperationEnum.ANDROID_GET_SPELL_CHECKER_SUGGESTION) {
			
			AndroidGetSpellCheckerSuggestionLoggerModel androidGetSpellCheckerSuggestionLoggerModel = (AndroidGetSpellCheckerSuggestionLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final AndroidGetSpellCheckerSuggestionLog androidGetSpellCheckerSuggestionLog = new AndroidGetSpellCheckerSuggestionLog();
			
			androidGetSpellCheckerSuggestionLog.setGenericLogId(genericLog.getId());
			androidGetSpellCheckerSuggestionLog.setWord(androidGetSpellCheckerSuggestionLoggerModel.getWord());
			androidGetSpellCheckerSuggestionLog.setType(androidGetSpellCheckerSuggestionLoggerModel.getType());
			androidGetSpellCheckerSuggestionLog.setSpellCheckerSuggestionList(androidGetSpellCheckerSuggestionLoggerModel.getSpellCheckerSuggestionList().toString());
			
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertAndroidGetSpellCheckerSuggestionLog(androidGetSpellCheckerSuggestionLog);						
					}
					
					@Override
					public void changeDate() {						
						androidGetSpellCheckerSuggestionLog.setWord(Utils.stringToBase64String(androidGetSpellCheckerSuggestionLog.getWord()));
					}
				});
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}			
			
		} else if (operation == GenericLogOperationEnum.ANDROID_QUEUE_EVENT) { 
			
			AndroidQueueEventLoggerModel androidQueueEventLoggerModel = (AndroidQueueEventLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final AndroidQueueEventLog androidQueueEventLog = new AndroidQueueEventLog();
			
			androidQueueEventLog.setGenericLogId(genericLog.getId());
			
			androidQueueEventLog.setUserId(androidQueueEventLoggerModel.getUserId());
			androidQueueEventLog.setOperation(androidQueueEventLoggerModel.getOperation());
			androidQueueEventLog.setCreateDate(new Timestamp(androidQueueEventLoggerModel.getCreateDate().getTime()));
			androidQueueEventLog.setParams(androidQueueEventLoggerModel.getParams() != null ? androidQueueEventLoggerModel.getParamsAsJSON() : null);
			
			//
			
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertAndroidQueueEventLoggerModel(androidQueueEventLog);						
					}
					
					@Override
					public void changeDate() {	
						androidQueueEventLog.setParams(Utils.stringToBase64String(androidQueueEventLog.getParams()));
					}
				});
				
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			
		} else if (operation == GenericLogOperationEnum.SUGGESTION_SEND) {
			
			SuggestionSendLoggerModel suggestionSendLoggerModel = (SuggestionSendLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final SuggestionSendLog suggestionSendLog = new SuggestionSendLog();
			
			suggestionSendLog.setGenericLogId(genericLog.getId());

			suggestionSendLog.setTitle(suggestionSendLoggerModel.getTitle());
			suggestionSendLog.setSender(suggestionSendLoggerModel.getSender());
			suggestionSendLog.setBody(suggestionSendLoggerModel.getBody());
			
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertSuggestionSendLoggerModel(suggestionSendLog);						
					}
					
					@Override
					public void changeDate() {						
						suggestionSendLog.setTitle(Utils.stringToBase64String(suggestionSendLog.getTitle()));
						suggestionSendLog.setSender(Utils.stringToBase64String(suggestionSendLog.getSender()));
						suggestionSendLog.setBody(Utils.stringToBase64String(suggestionSendLog.getBody()));
					}
				});
				
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
			
			DailyReportLoggerModel dailyReportLoggerModel = (DailyReportLoggerModel)loggerModelCommon;
			
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
			
		} else if (operation == GenericLogOperationEnum.REDIRECT) {
			
			RedirectLoggerModel redirectLoggerModel = (RedirectLoggerModel)loggerModelCommon;
			
			logger.info("Przekierowanie: " + redirectLoggerModel.getRequestURL() + " -> " + redirectLoggerModel.getDestinationUrl());			
			
		} else if (operation == GenericLogOperationEnum.ADMIN_REQUEST) { 
			
			AdminLoggerModel adminLoggerModel = (AdminLoggerModel)loggerModelCommon;
			
			logger.info("Typ operacji: " + adminLoggerModel.getType() + ", resultat: " + adminLoggerModel.getResult() + ", parametry: " + adminLoggerModel.getParams());
			
			// utworzenie wpisu do bazy danych
			AdminRequestLog adminRequestLog = new AdminRequestLog();
			
			adminRequestLog.setGenericLogId(genericLog.getId());

			adminRequestLog.setType(adminLoggerModel.getType().toString());
			adminRequestLog.setResult(adminLoggerModel.getResult().toString());
			adminRequestLog.setParams(adminLoggerModel.getParams().toString());
			
			// wstawienie wpisu do bazy danych
			try {
				mySQLConnector.insertAdminRequestLog(adminRequestLog);
			} catch (SQLException e) {
				logger.error("Błąd podczas zapisu do bazy danych", e);
				
				throw new RuntimeException(e);
			}
			
			// jesli operacja to pobranie slow z kolejki wraz z blokada to wykonujemy te operacje
			if (adminLoggerModel.getType() == Type.ADMIN_GET_MISSING_WORDS_QUEUE && adminLoggerModel.getResult() == Result.OK) {
				
	    		// !!! uwaga !!! jesli cos tu zmieniasz to zmien rowniez w klasie AdminController, podtyp ADMIN_GET_MISSING_WORDS_QUEUE
				
				Boolean lock = (Boolean)adminLoggerModel.getParams().get("lock");				
				Timestamp lockTimestamp = (Timestamp)adminLoggerModel.getParams().get("lockTimestamp");
				
				AdminLoggerModel.ObjectWrapper unlockedWordDictionarySearchMissingWordQueueObjectWrapper = ((AdminLoggerModel.ObjectWrapper)adminLoggerModel.getParams().get("unlockedWordDictionarySearchMissingWordQueue"));
				
				List<WordDictionarySearchMissingWordQueue> unlockedWordDictionarySearchMissingWordQueue = null;
				
				if (unlockedWordDictionarySearchMissingWordQueueObjectWrapper != null) {
					unlockedWordDictionarySearchMissingWordQueue = (List<WordDictionarySearchMissingWordQueue>)unlockedWordDictionarySearchMissingWordQueueObjectWrapper.getObject();
				}
				
				//
				 
				if (lock != null && lock.booleanValue() == true && lockTimestamp != null && unlockedWordDictionarySearchMissingWordQueue != null) { // mozemy zakladac blokade
					
	        		for (WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue : unlockedWordDictionarySearchMissingWordQueue) {
	    				
	        			wordDictionarySearchMissingWordQueue.setLockTimestamp(lockTimestamp);
	        			
	        			try {
	        				mySQLConnector.updateWordDictionarySearchMissingWordQueue(wordDictionarySearchMissingWordQueue);
	        			} catch (SQLException e) {
	        				logger.error("Błąd podczas zapisu do bazy danych", e);
	        				
	        				throw new RuntimeException(e);
	        			}
	    			}    		
				}
			}
			
		} else if (operation == GenericLogOperationEnum.GENERAL_EXCEPTION) {
			
			GeneralExceptionLoggerModel generalExceptionLoggerModel = (GeneralExceptionLoggerModel)loggerModelCommon;
			
			// utworzenie wpisu do bazy danych
			final GeneralExceptionLog generalExceptionLog = new GeneralExceptionLog();
			
			generalExceptionLog.setGenericLogId(genericLog.getId());
			
			generalExceptionLog.setStatusCode(generalExceptionLoggerModel.getStatusCode());				
			
			Throwable throwable = generalExceptionLoggerModel.getThrowable();
							
			StringBuffer exceptionSb = new StringBuffer();
			
			exceptionSb.append(throwable.getClass() + ":").append(ExceptionUtils.getMessage(throwable)).append("\n\n").append(ExceptionUtils.getStackTrace(throwable));
			
			generalExceptionLog.setException(exceptionSb.toString());
			
			// wstawienie wpisu do bazy danych
			try {
				repeatIfNeededMysqlDataTruncationException(new IRepeatableOperation() {
					
					@Override
					public void operation() throws SQLException {
						mySQLConnector.insertGeneralExceptionLog(generalExceptionLog);						
					}
					
					@Override
					public void changeDate() {						
						generalExceptionLog.setException(Utils.stringToBase64String(generalExceptionLog.getException()));
					}
				});				
				
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

	}
	
	private GenericLogOperationEnum mapClassToGenericLogOperationEnum(Class<?> clazz) {
		
		if (StartAppLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.START_APP;
			
		} else if (StartLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.START;

		} else if (FaviconIconSendLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.FAVICON_ICON;
			
		} else if (RobotsGenerateLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.ROBOTS_GENERATE;
			
		} else if (BingSiteAuthGenerateLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.BING_SITE_AUTH;
			
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

		} else if (WordDictionaryPdfDictionaryLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_PDF_DICTIONARY;
			
		} else if (WordDictionaryCatalogLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_CATALOG;
			
		} else if (WordDictionaryGetTatoebaSentenceGroupLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_GET_TATOEBA_SENTENCES;
			
		} else if (WordDictionaryGetGroupDictionaryEntriesLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_GET_GROUP_DICT_ENTRIES;

		} else if (WordDictionaryGetDictionaryEntriesSizeLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_GET_DICT_ENT_SIZE;

		} else if (WordDictionaryGetDictionaryEntriesNameSizeLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_GET_DICT_ENT_NAME_SIZE;

		} else if (WordDictionaryGetDictionaryEntryGroupTypesLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_GET_DICT_ENT_GROUP_TYPES;

		} else if (WordDictionaryGetTransitiveIntransitivePairsLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_GET_TRANS_INTRANS_PAIR;

		} else if (WordDictionaryGetWordPowerListLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_GET_WORD_POWER_LIST;
			
		} else if (KanjiDictionaryStartLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_START;

		} else if (WordDictionaryNameDetailsLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_NAME_DETAILS;

		} else if (WordDictionaryNameCatalogLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.WORD_DICTIONARY_NAME_CATALOG;
			
		} else if (KanjiDictionaryAutocompleteLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_AUTOCOMPLETE;

		} else if (KanjiDictionarySearchLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH;

		} else if (KanjiDictionarySearchStrokeCountLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH_STROKE_COUNT;
			
		} else if (KanjiDictionaryAllKanjisLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_GET_ALL_KANJIS;
			
		} else if (KanjiDictionaryRadicalsLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_RADICALS;

		} else if (KanjiDictionaryDetectLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_DETECT;

		} else if (KanjiDictionaryDetailsLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_DETAILS;

		} else if (KanjiDictionaryCatalogLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.KANJI_DICTIONARY_CATALOG;
		
		} else if (KanjiDictionaryGetKanjiEntryListLoggerModel.class.isAssignableFrom(clazz) == true) {
		    return GenericLogOperationEnum.KANJI_DICTIONARY_GET_KANJI_ENTRY_LIST;
			
		} else if (AndroidSendMissingWordLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.ANDROID_SEND_MISSING_WORD;
			
		} else if (AndroidGetSpellCheckerSuggestionLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.ANDROID_GET_SPELL_CHECKER_SUGGESTION;
			
		} else if (AndroidQueueEventLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.ANDROID_QUEUE_EVENT;
			
		} else if (AndroidGetMessageLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.ANDROID_GET_MESSAGE;
			
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
		
		} else if (PageNoFoundExceptionLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.PAGE_NO_FOUND_EXCEPTION;
			
		} else if (ServiceUnavailableExceptionLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.SERVICE_UNAVAILABLE_EXCEPTION;
			
		} else if (MethodNotAllowedExceptionLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.METHOD_NOT_ALLOWED_EXCEPTION;
		
		} else if (RedirectLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.REDIRECT;
			
		} else if (AdminLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.ADMIN_REQUEST;
			
		} else if (ClientBlockLoggerModel.class.isAssignableFrom(clazz) == true) {
			return GenericLogOperationEnum.CLIENT_BLOCK;
					
		} else {
			throw new RuntimeException("Nieznany klasa: " + clazz);
		}
	}
	
	private void repeatIfNeededMysqlDataTruncationException(IRepeatableOperation repeatableOperation) throws SQLException {

		for (int idx = 0; idx < 2; ++idx) {
			
			try {
				repeatableOperation.operation();
				
				// jest ok, wychodzimy
				return;
				
			} catch (SQLException e) {
				
				// jesli byl blad typu MysqlDataTruncationException, proba zmiany danych i ponowienie
				if (idx == 0 && Utils.isMysqlDataTruncationException(e) == true) {					
					repeatableOperation.changeDate();
					
				} else {
					throw e;
				}
			}
		}
	}
		
	private static interface IRepeatableOperation {		
		
		public void operation() throws SQLException;

		public void changeDate();		
	}
}
