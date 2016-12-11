package pl.idedyk.japanese.dictionary.web.schedule;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerListener;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.DailyReportLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
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
	
	/*
	@Scheduled(cron="0 * * * * ?") // co minute
	public void runGC() {
		System.gc();
	}
	*/
}
