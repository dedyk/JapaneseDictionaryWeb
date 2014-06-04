package pl.idedyk.japanese.dictionary.web.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyLogProcessedMinMaxIds;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordKanjiSearchNoFoundStat;

@Service
public class ScheduleTask {
	
	private static final Logger logger = Logger.getLogger(ScheduleTask.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Autowired
	private MessageSource messageSource;
	
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
				
				StringBuffer report = new StringBuffer();
				
				// tytul
				report.append(messageSource.getMessage("schedule.task.generate.daily.title", 
						new Object[] { dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId() }, Locale.getDefault()));
				
				report.append("\n\n");
				
				// data raportu
				String currentDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				
				report.append(messageSource.getMessage("schedule.task.generate.daily.report.date",
						new Object[] { currentDateString }, Locale.getDefault()));
				
				report.append("\n\n");
				
				// statystyki operacji
				report.append(messageSource.getMessage("schedule.task.generate.daily.report.operation.stat",
						new Object[] { }, Locale.getDefault()));
				
				report.append("\n\n");
				
				List<GenericLogOperationStat> genericLogOperationStatList = mySQLConnector.getGenericLogOperationStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				for (GenericLogOperationStat genericLogOperationStat : genericLogOperationStatList) {
					
					String operationString = genericLogOperationStat.getOperation().toString();
					
					report.append(operationString);
					
					for (int idx = operationString.length(); idx < 40; ++idx) {
						report.append(" ");
					}
					
					report.append(" ").append(genericLogOperationStat.getStat()).append("\n");
				}
				
				report.append("\n\n");
				
				// wyszukiwanie slowek bez wynikow
				List<WordKanjiSearchNoFoundStat> wordDictionarySearchNoFoundStatList = mySQLConnector.getWordDictionarySearchNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				for (WordKanjiSearchNoFoundStat wordKanjiSearchNoFoundStat : wordDictionarySearchNoFoundStatList) {
					
					String word = wordKanjiSearchNoFoundStat.getWord();
					
					report.append(word);
					
					for (int idx = word.length(); idx < 40; ++idx) {
						report.append(" ");
					}
					
					report.append(" ").append(wordKanjiSearchNoFoundStat.getStat()).append("\n");
				}
				
				report.append("\n\n");

				
				
				
				
				
				
				// slowa wyszukiwania slowek i kanji bez wynikow
				// uzupelnienie slowek i kanji bez wynikow
								
				// na koniec zablokowanie wpisow
				
				
				
				int fixme3 = 1;
				
				System.out.println(report.toString());
			}
			
			
			
			
			
		} catch (Exception e) {
			logger.error("Blad generowania dziennego raportu", e);
		}
		
		//a:
		
		logger.info("Generowanie raportu zakonczone");
	}
	
}
