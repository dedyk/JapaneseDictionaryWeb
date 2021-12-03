package pl.idedyk.japanese.dictionary.web.schedule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.csvreader.CsvWriter;

import pl.idedyk.japanese.dictionary.api.android.queue.event.QueueEventOperation;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerListener;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.DailyReportLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector.ProcessRecordCallback;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector.Transaction;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidGetSpellCheckerSuggestionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidQueueEventLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidSendMissingWordLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryRadicalsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.queue.QueueService;
import pl.idedyk.japanese.dictionary.web.report.ReportGenerator;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;
import pl.idedyk.japanese.dictionary.web.service.SemaphoreService;

@Service
public class ScheduleTask {
	
	private static final Logger logger = Logger.getLogger(ScheduleTask.class);

	@Autowired
	private MySQLConnector mySQLConnector;
		
	@Autowired
	private QueueService queueService;
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private LoggerListener loggerListener;
	
	@Autowired
	private ReportGenerator reportGenerator;
	
	@Autowired
	private SemaphoreService semaphoreService;
	
	@Autowired
	private ConfigService configService;
	
	@Value("${db.arch.dir}")
	private String dbArchDir; 
	
	//@Scheduled(cron="0 * * * * ?") // co minute
	@Scheduled(cron="0 0 19 * * ?") // o 19
	public void generateDailyReport() {
		
		if (configService.isGenerateDailyReport() == false) {
			return;
		}
		
		generateDailyReport(true);
	}
			
	public void generateDailyReport(boolean checkLock) {
		
		final String lockName = "generateDailyReport";
		
		boolean canDoOperation = true;
		
		if (checkLock == true) { // sprawdzamy blokade
			
			try {
				canDoOperation = semaphoreService.canDoOperation(lockName, 30 * 60);
							
			} catch (Exception e) {
				logger.error("Blad generowania dziennego raportu", e);
				
				return;
			}			
		}
		
		if (canDoOperation == false) {
			logger.info("Nie uzyskano blokady dla: " + lockName);
			
			return;
		}
		
		// pobranie przedzialu wpisow
		try {
			ReportGenerator.DailyReport dailyReport = reportGenerator.generateDailyReportBody();
			
			if (dailyReport == null) {
				logger.info("Brak elementow do generowania dziennego raportu");
				
				return;
			}
			
			logger.info("Wygenerowano dzienny raport: \n\n" + dailyReport.body);
			
			// zablokuj wpisy
			mySQLConnector.blockDailyLogProcessedIds(dailyReport.minId, dailyReport.maxId);
			
			// publikacja do kolejki i do wyslania
			loggerSender.sendLog(new DailyReportLoggerModel(dailyReport.title, dailyReport.body));			
			
		} catch (Exception e) {
			logger.error("Blad generowania dziennego raportu", e);
		}
				
		logger.info("Generowanie raportu zakonczone");
	}
	
	
	@Scheduled(cron="* * * * * ?")
	public void processLogQueueItem() {
		
		if (configService.isProcessLogQueueItem() == false) {
			return;
		}
				
		QueueItem currentQueueItem = null;
				
		try {
			// pobranie elementow do przetworzenia
			List<QueueItem> queueItemList = queueService.getNextItemQueueItem("log");
						
			if (queueItemList.size() == 0) {
				return;
			}
			
			for (QueueItem queueItem : queueItemList) { // dla kazdego wpisu
				
				currentQueueItem = queueItem;
				
				byte[] objectBytes = queueItem.getObject();
				
				ByteArrayInputStream byteArrayItemStream = new ByteArrayInputStream(objectBytes);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayItemStream);
				
				LoggerModelCommon loggerModelCommon = (LoggerModelCommon)objectInputStream.readObject();
				
				// przetworz wpis
				loggerListener.onMessage(loggerModelCommon);
				
				// ustaw wpis jako przetworzony
				queueService.setQueueItemDone(queueItem);
				
				currentQueueItem = null;
			}
			
		} catch (Exception e) {
			
			logger.error("Blad podczas przetwarzania log'ow z kolejki: " + e);
						
			// czy opoznij zadanie
			boolean delayQueueItem = true;
			
			// bledow typu com.mysql.jdbc.MysqlDataTruncation: Data truncation: Incorrect string value: '\xF0\x9F\x88\xB5' - nie ponawiamy
			if (Utils.isMysqlDataTruncationException(e) == true) {
				delayQueueItem = false;
			}
			
			if (currentQueueItem != null && delayQueueItem == true) {
				
				try {
					queueService.delayQueueItem(currentQueueItem);
					
				} catch (SQLException e1) {					
					logger.error("Blad podcza opozniania zadania z kolejki", e);
				}
				
			} else if (currentQueueItem != null && delayQueueItem == false) {
				
				// ustaw wpis jako przetworzony z bledem
				try {
					queueService.setQueueItemError(currentQueueItem);
					
				} catch (Exception e2) {
					logger.error("Blad podczas przetwarzania log'ow z kolejki: " + e2);
				}
			}
			
			if (currentQueueItem != null && currentQueueItem.getDeliveryCount() <= 1) {

				// przygotowanie info do logger'a
				GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(
						LoggerModelCommon.createLoggerModelCommon(null, null, null, null, null), -1, e);
				
				// wyslanie do logger'a
				loggerSender.sendLog(generalExceptionLoggerModel);				
			}			
		}
	}
	
	@Scheduled(cron="0 0 0 * * ?") // o polnocy
	public void deleteDatabaseOldQueueItems() {
		
		if (configService.isDeleteDatabaseOldQueueItems() == false) {
			return;
		}
		
		final String lockName = "deleteOldQueueItems";
		
		boolean canDoOperation = false;
		
		// sprawdzamy blokade			
		try {
			canDoOperation = semaphoreService.canDoOperation(lockName, 30 * 60);
						
		} catch (Exception e) {
			logger.error("Blad podczas kasowania starych wpisow z kolejki", e);
			
			return;
		}			

		if (canDoOperation == false) {
			logger.info("Nie uzyskano blokady dla: " + lockName);
			
			return;
		}
		
		logger.info("Kasuje stare przetworzone wpisy z kolejki");
		
		try {			
			mySQLConnector.deleteOldQueueItems(1);
			
		} catch (Exception e) {
			logger.error("Blad podczas kasowania starych wpisow z kolejki", e);
		}
	}
	
	@Scheduled(cron="0 0 0 * * ?") // o polnocy
	//@Scheduled(cron="* * * * * ?") // dev
	public void deleteLocalDirArchiveOldQueueItems() {
		
		if (configService.isDeleteLocalDirArchiveOldQueueItems() == false) {
			return;
		}
		
		logger.info("Kasuje stare wpisy z lokalnego archiwum kolejki");
		
		queueService.deleteLocalDirArchiveOldQueueItems(10); // starsze niz 10 dni
	}
	
	@Scheduled(cron="* * * * * ?")
	public void processLocalDirQueueItems() {
		
		if (configService.isProcessLocalDirQueueItems() == false) {
			return;
		}
		
		queueService.processLocalDirQueueItems();
	}
	
	//@Scheduled(cron="* * * * * ?") // tymczasowo
	@Scheduled(cron="0 0 2 * * ?") // o 2 w nocy
	public void processDBCleanup() {
		
		if (configService.isProcessDBCleanup() == false) {
			return;
		}
		
		try {
			configService.enableTemporaryStopProcessing();
			
			try {
				Thread.sleep(10000);
				
			} catch (InterruptedException e) {
				// noop
			}
			
			final boolean doDelete = true;

			logger.info("Czyszczenie bazy danych");

			Transaction transaction = null;

			try {
				transaction = mySQLConnector.beginTransaction();

			} catch (Exception e) {
				logger.error("Blad podczas czyszczenia bazy danych", e);

				return;
			}
			
			// zaczynamy przetwarzanie
			try {
				
				// skasuj stare wpisy z przetworzonych id'kow
				logger.info("Kasowanie starych przetworzonych id'kow");			
				mySQLConnector.deleteOldDailyLogProcessedIds(transaction);
				
				// archiwizacja starych operacji
				archiveOperations(transaction, doDelete);
							
				// zakomitowanie zmian
				mySQLConnector.commitTransaction(transaction);
				
				transaction = null;
				
			} catch (Exception e) {
				
				logger.error("Blad podczas czyszczenia bazy danych", e);

				if (transaction != null) {
					
					try {
						mySQLConnector.rollbackTransaction(transaction);
						
					} catch (SQLException e2) {				
						logger.error("Drugi błąd podczas czyszczenia bazy danych", e);
					}			
				}			
			}
			
		} finally {			
			configService.disableTemporaryStopProcessing();
		}
	}

	private void archiveOperations(final Transaction transaction, final boolean doDelete) throws SQLException, IOException {
				
		List<GenericLogOperationEnum> operationTypeToArchive = GenericLogOperationEnum.getAllExportableOperationList();
				
		// dla kazdego rodzaju
		for (final GenericLogOperationEnum genericLogOperationEnum : operationTypeToArchive) {
			
			// pobranie starych dat operacji
			List<String> oldGenericLogOperationDateList = mySQLConnector.getOldGenericLogOperationDateList(transaction, genericLogOperationEnum, genericLogOperationEnum.getDayOlderThan());
			
			for (String dateString : oldGenericLogOperationDateList) {
				
				logger.info("Archiwizacja dla operacji: " + genericLogOperationEnum.toString() + " dla daty: " + dateString);
				
				class GenericLogExporter extends CsvExporter<GenericLog> {

					@Override
					protected void callExport(String prefix, String dateString) throws SQLException {
						mySQLConnector.processGenericLogRecords(transaction, genericLogOperationEnum, dateString, this);						
					}
				}
				
				new GenericLogExporter().export("generic_log-" + genericLogOperationEnum.toString(), dateString);	
				
				//
								
				// dodatkowo jeszcze dla wybranych typow, eksportowanie danych szczegolowych oraz ich usuniecie
				switch (genericLogOperationEnum) {
				
					case START_APP:
					case START:
					case FAVICON_ICON:
					case ROBOTS_GENERATE:
					case BING_SITE_AUTH:
					case SITEMAP_GENERATE:
					case WORD_DICTIONARY_START:
					case WORD_DICTIONARY_PDF_DICTIONARY:
					case WORD_DICTIONARY_GET_TATOEBA_SENTENCES:
					case WORD_DICTIONARY_GET_GROUP_DICT_ENTRIES:
					case WORD_DICTIONARY_GET_DICT_ENT_SIZE:
					case WORD_DICTIONARY_GET_DICT_ENT_NAME_SIZE:
					case WORD_DICTIONARY_GET_DICT_ENT_GROUP_TYPES:
					case WORD_DICTIONARY_GET_TRANS_INTRANS_PAIR:
					case WORD_DICTIONARY_GET_WORD_POWER_LIST:
					case WORD_DICTIONARY2_DETAILS:
					case KANJI_DICTIONARY_START:
					case KANJI_DICTIONARY_SEARCH_STROKE_COUNT:
					case KANJI_DICTIONARY_GET_ALL_KANJIS:
					case KANJI_DICTIONARY_GET_KANJI_ENTRY_LIST:
					case SUGGESTION_START:
					case INFO:
					case PAGE_NO_FOUND_EXCEPTION:
					case SERVICE_UNAVAILABLE_EXCEPTION:
					case METHOD_NOT_ALLOWED_EXCEPTION:
					case REDIRECT:
					case ANDROID_GET_MESSAGE:
					case CLIENT_BLOCK:
						
						// noop
						break;					
					
					case WORD_DICTIONARY_DETAILS:
						
						class WordDictionaryDetailsExporter extends CsvExporter<WordDictionaryDetailsLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processWordDictionaryDetailsLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteWordDictionaryDetailsLogRecords(transaction, dateString);
								}
							}
						}
						
						new WordDictionaryDetailsExporter().export("word-dictionary-details", dateString);	
						
						break;					
						
					case WORD_DICTIONARY_CATALOG:
						
						class WordDictionaryCatalogExporter extends CsvExporter<WordDictionaryCatalogLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processWordDictionaryCatalogLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteWordDictionaryCatalogLogRecords(transaction, dateString);
								}
							}
						}
						
						new WordDictionaryCatalogExporter().export("word-dictionary-catalog", dateString);	
						
						break;
					
					case WORD_DICTIONARY_SEARCH:
						
						class WordDictionarySearchExporter extends CsvExporter<WordDictionarySearchLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processWordDictionarySearchLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteWordDictionarySearchLogRecords(transaction, dateString);
								}
							}
						}
						
						new WordDictionarySearchExporter().export("word-dictionary-search", dateString);	
						
						break;
						
					case WORD_DICTIONARY_NAME_DETAILS:
						
						class WordDictionaryNameDetailsExporter extends CsvExporter<WordDictionaryNameDetailsLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processWordDictionaryNameDetailsLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteWordDictionaryNameDetailsLogRecords(transaction, dateString);
								}
							}
						}
						
						new WordDictionaryNameDetailsExporter().export("word-dictionary-name-details", dateString);	
						
						break;
						
					case WORD_DICTIONARY_NAME_CATALOG:
						
						class WordDictionaryNameCatalogExporter extends CsvExporter<WordDictionaryNameCatalogLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processWordDictionaryNameCatalogLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteWordDictionaryNameCatalogLogRecords(transaction, dateString);
								}
							}
						}
						
						new WordDictionaryNameCatalogExporter().export("word-dictionary-name-catalog", dateString);	
						
						break;
						
					case WORD_DICTIONARY_AUTOCOMPLETE:
						
						class WordDictionaryAutocompleteExporter extends CsvExporter<WordDictionaryAutocompleteLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processWordDictionaryAutocompleteLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteWordDictionaryAutocompleteLogRecords(transaction, dateString);
								}
							}
						}
						
						new WordDictionaryAutocompleteExporter().export("word-dictionary-autocomplete", dateString);	
						
						break;
												
					case KANJI_DICTIONARY_DETAILS:
						
						class KanjiDictionaryDetailsExporter extends CsvExporter<KanjiDictionaryDetailsLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processKanjiDictionaryDetailsLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteKanjiDictionaryDetailsLogRecords(transaction, dateString);
								}
							}
						}
						
						new KanjiDictionaryDetailsExporter().export("kanji-dictionary-details", dateString);	
						
						break;	
						
					case KANJI_DICTIONARY_SEARCH:
						
						class KanjiDictionarySearchExporter extends CsvExporter<KanjiDictionarySearchLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processKanjiDictionarySearchLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteKanjiDictionarySearchLogRecords(transaction, dateString);
								}
							}
						}
						
						new KanjiDictionarySearchExporter().export("kanji-dictionary-search", dateString);	
						
						break;						
						
					case KANJI_DICTIONARY_CATALOG:
						
						class KanjiDictionaryCatalogExporter extends CsvExporter<KanjiDictionaryCatalogLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processKanjiDictionaryCatalogLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteKanjiDictionaryCatalogLogRecords(transaction, dateString);
								}
							}
						}
						
						new KanjiDictionaryCatalogExporter().export("kanji-dictionary-catalog", dateString);	
						
						break;
						
					case KANJI_DICTIONARY_AUTOCOMPLETE:
						
						class KanjiDictionaryAutocompleteExporter extends CsvExporter<KanjiDictionaryAutocompleteLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processKanjiDictionaryAutocompleteLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteKanjiDictionaryAutocompleteLogRecords(transaction, dateString);
								}
							}
						}
						
						new KanjiDictionaryAutocompleteExporter().export("kanji-dictionary-autocomplete", dateString);	
						
						break;

					case KANJI_DICTIONARY_RADICALS:
						
						class KanjiDictionaryRadicalsExporter extends CsvExporter<KanjiDictionaryRadicalsLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processKanjiDictionaryRadicalsLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteKanjiDictionaryRadicalsLogRecords(transaction, dateString);
								}
							}
						}
						
						new KanjiDictionaryRadicalsExporter().export("kanji-dictionary-radicals", dateString);	
						
						break;						
						
					case DAILY_REPORT:
						
						class DailyReportSendExporter extends CsvExporter<DailyReportSendLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processDailyReportSendLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteDailyReportSendLogRecords(transaction, dateString);
								}
							}
						}
						
						new DailyReportSendExporter().export("daily-report-send", dateString);	
						
						break;
						
					case ANDROID_SEND_MISSING_WORD:

						class AndroidSendMissingWordExporter extends CsvExporter<AndroidSendMissingWordLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processAndroidSendMissingWordLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteAndroidSendMissingWordLogRecords(transaction, dateString);
								}
							}
						}
						
						new AndroidSendMissingWordExporter().export("android-send-missing-word", dateString);	
						
						break;
					
					case ANDROID_GET_SPELL_CHECKER_SUGGESTION:
						
						class AndroidGetSpellCheckerSuggestionExporter extends CsvExporter<AndroidGetSpellCheckerSuggestionLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processAndroidGetSpellCheckerSuggestionLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteAndroidGetSpellCheckerSuggestionLogRecords(transaction, dateString);
								}
							}
						}
						
						new AndroidGetSpellCheckerSuggestionExporter().export("android-get-spell-checker-suggestion", dateString);	
						
						break;
						
					case ANDROID_QUEUE_EVENT:
						
						class AndroidQueueEventExporter extends CsvExporter<AndroidQueueEventLog> {

							@Override
							protected void callExport(String prefix, String dateString) throws SQLException {								
								mySQLConnector.processAndroidQueueEventLogRecords(transaction, dateString, this);
								
								if (doDelete == true) {
									mySQLConnector.deleteAndroidQueueEventLogRecords(transaction, dateString);
								}
							}
						}
						
						new AndroidQueueEventExporter().export("android-queue-event", dateString);	
						
						break;
						
					default:
						throw new RuntimeException("Nieznana operacji do archiwizacji: " + genericLogOperationEnum);
				}
				
				if (doDelete == true) {
					mySQLConnector.deleteGenericLogRecords(transaction, genericLogOperationEnum, dateString);
				}
			}
		}
		
		logger.info("Zakończono archiwizację");
	}
		
	private abstract class CsvExporter<T> implements ProcessRecordCallback<T> {
		
		private CsvWriter csvWriter = null;
		
		private boolean isFirst = true;
		
		public void export(String prefix, String dateString) throws IOException, SQLException {
			
			// utworz nazwe pliku
			File exportFile = createCsvExportFile(prefix, dateString);
			
			try {
				// inicjalizacja
				init(exportFile);
				
				// eksportowanie danych
				callExport(prefix, dateString);
				
			} finally {
				
				// zakonczenie exportowania do pliku csv
				finish();			
			}
		}
		
		private File createCsvExportFile(String prefix, String dateString) {
			
			String[] dateStringSplited = dateString.split("-");
			
			File descDir = new File(dbArchDir + "/" + dateStringSplited[0], dateStringSplited[1]);
			
			if (descDir.exists() == false) {
				descDir.mkdirs();
			}
			
			return new File(descDir, prefix + "-" + dateString + ".csv.gz");
		}
		
		public void init(File fileName) throws IOException {
			csvWriter = new CsvWriter(new GZIPOutputStream(new FileOutputStream(fileName)), ',', StandardCharsets.UTF_8);
		}
		
		public void finish() {
			csvWriter.close();
		}
		
		protected abstract void callExport(String prefix, String dateString) throws SQLException;
		
		@Override
		public void callback(T object) {
						
			try {				
				exportObjectToCsv(csvWriter, object);
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private void exportObjectToCsv(CsvWriter csvWriter, T object) throws Exception {
						
			// pobranie listy zadeklarowanych pol
			Field[] declaredFields = object.getClass().getDeclaredFields();
			
			if (isFirst == true) { // dodanie naglowka
				
				for (Field field : declaredFields) {
					
					if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) == true) { // pole statyczne
						continue;
					}
					
					csvWriter.write(field.getName());
				}
				
				csvWriter.endRecord();
			
				isFirst = false;
			}
			
			for (Field field : declaredFields) {
				
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) == true) { // pole statyczne
					continue;
				}
				
				// pobranie wartosci pola
				field.setAccessible(true);
				
				Object fieldValue = field.get(object);
				
				csvWriter.write(toStringValue(fieldValue));				
			}
			
			csvWriter.endRecord();
		}
		
		private String toStringValue(Object fieldValue) {
			
			if (fieldValue == null) {
				return "<null>";
				
			} else {
				
				if (fieldValue instanceof String == true) {
					return (String)fieldValue;
					
				} else if (fieldValue instanceof Integer == true) {
					return ((Integer)fieldValue).toString();					
					
				} else if (fieldValue instanceof Long == true) {
					return ((Long)fieldValue).toString();					
					
				} else if (fieldValue instanceof Boolean == true) {
					return ((Boolean)fieldValue).toString();
					
				} else if (fieldValue instanceof Timestamp == true) {
					
					Timestamp timestamp = (Timestamp)fieldValue;
					
					return timestamp.toString();
					
				} else if (fieldValue instanceof GenericLogOperationEnum == true) {
					
					GenericLogOperationEnum genericLogOperationEnum = (GenericLogOperationEnum)fieldValue;
					
					return genericLogOperationEnum.toString();					
					
				} else if (fieldValue instanceof QueueEventOperation == true) { 
					
					QueueEventOperation queueEventOperation = (QueueEventOperation)fieldValue;
					
					return queueEventOperation.toString();
					
				} else {					
					throw new RuntimeException("Nieznany rodzaj klasy: " + fieldValue.getClass());
				}
			}
		}
	}
	
	/*
	@Scheduled(cron="0 * * * * ?") // co minute
	public void runGC() {
		System.gc();
	}
	*/
}
