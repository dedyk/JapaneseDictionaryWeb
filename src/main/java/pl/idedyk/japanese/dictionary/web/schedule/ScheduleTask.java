package pl.idedyk.japanese.dictionary.web.schedule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.csvreader.CsvWriter;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerListener;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.DailyReportLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector.ProcessRecordCallback;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector.Transaction;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameDetailsLog;
import pl.idedyk.japanese.dictionary.web.queue.QueueService;
import pl.idedyk.japanese.dictionary.web.report.ReportGenerator;
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
	
	@Value("${db.arch.dir}")
	private String dbArchDir; 
	
	//@Scheduled(cron="0 * * * * ?") // co minute
	@Scheduled(cron="0 0 19 * * ?") // o 19
	public void generateDailyReport() {
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
	public void deleteOldQueueItems() {
		
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
	
	@Scheduled(cron="* * * * * ?")
	public void processLocalDirQueueItems() {
		
		queueService.processLocalDirQueueItems();
	}
	
	//@Scheduled(cron="* * * * * ?") // tymczasowo
	@Scheduled(cron="0 0 2 * * ?") // o 2 w nocy
	public void processDBCleanup() {

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
			archiveOperations(transaction);
						
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
	}

	@SuppressWarnings("unused")
	private void archiveOperations(final Transaction transaction) throws SQLException, IOException {
		
		final int dayOlderThan = 365;
		
		final boolean doDelete = false; // na razie nie kasujemy
		
		List<GenericLogOperationEnum> operationTypeToArchive = GenericLogOperationEnum.getAllExportableOperationList();
				
		// dla kazdego rodzaju
		for (final GenericLogOperationEnum genericLogOperationEnum : operationTypeToArchive) {
			
			// pobranie starych dat operacji
			List<String> oldGenericLogOperationDateList = mySQLConnector.getOldGenericLogOperationDateList(transaction, genericLogOperationEnum, dayOlderThan);
			
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
					case KANJI_DICTIONARY_START:
					case SUGGESTION_START:
					case INFO:
					case PAGE_NO_FOUND_EXCEPTION:
					case SERVICE_UNAVAILABLE_EXCEPTION:
					case METHOD_NOT_ALLOWED_EXCEPTION:
					case REDIRECT:
						
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
			
			return new File(descDir, prefix + "-" + dateString + ".csv");
		}
		
		public void init(File fileName) throws IOException {
			csvWriter = new CsvWriter(new FileWriter(fileName), ',');
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
					
				} else if (fieldValue instanceof Timestamp == true) {
					
					Timestamp timestamp = (Timestamp)fieldValue;
					
					return timestamp.toString();
					
				} else if (fieldValue instanceof GenericLogOperationEnum == true) {
					
					GenericLogOperationEnum genericLogOperationEnum = (GenericLogOperationEnum)fieldValue;
					
					return genericLogOperationEnum.toString();					
					
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
