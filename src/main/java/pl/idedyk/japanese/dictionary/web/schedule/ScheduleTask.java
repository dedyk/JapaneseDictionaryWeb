package pl.idedyk.japanese.dictionary.web.schedule;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyLogProcessedMinMaxIds;

@Service
public class ScheduleTask {
	
	private static final Logger logger = Logger.getLogger(ScheduleTask.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	//@Scheduled(cron="*/5 * * * * ?")
	public synchronized void generateDailyReport() {
		
		int fixme = 1; // scheduled
		
		logger.info("Generowanie dziennego raportu");
		
		// logowanie
		int fixme2 = 1;
						
		// pobranie przedzialu wpisow
		try {
			DailyLogProcessedMinMaxIds dailyLogProcessedMinMaxIds = mySQLConnector.getCurrentDailyLogProcessedMinMaxIds();
			
			if (dailyLogProcessedMinMaxIds == null) {
				logger.info("Brak wpisow do przetworzenia");
				
			} else {	
				logger.info("Przetwarzam wpisy od " + dailyLogProcessedMinMaxIds.getMinId() + " do " + dailyLogProcessedMinMaxIds.getMaxId());
				
				
			}
			
			
			
			
			
		} catch (Exception e) {
			logger.error("Blad generowania dziennego raportu", e);
		}
		
		//a:
		
		logger.info("Generowanie raportu zakonczone");
	}
	
}
