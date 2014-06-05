package pl.idedyk.japanese.dictionary.web.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.DailyReportLoggerModel;
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
	
	@Autowired
	private LoggerSender loggerSender;
	
	//@Scheduled(cron="0 * * * * ?") // co minute
	@Scheduled(cron="0 0 0 * * ?") // o polnocy
	public synchronized void generateDailyReport() {
				
		logger.info("Generowanie dziennego raportu");
								
		// pobranie przedzialu wpisow
		try {
			DailyLogProcessedMinMaxIds dailyLogProcessedMinMaxIds = mySQLConnector.getCurrentDailyLogProcessedMinMaxIds();
			
			if (dailyLogProcessedMinMaxIds == null) {
				logger.info("Brak wpisow do przetworzenia");
				
			} else {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				logger.info("Przetwarzam wpisy od " + dailyLogProcessedMinMaxIds.getMinId() + " do " + dailyLogProcessedMinMaxIds.getMaxId() + 
						" (" + simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMinDate()) + " - " + simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMaxDate()));
				
				StringBuffer report = new StringBuffer();
				
				// tytul
				String title = messageSource.getMessage("schedule.task.generate.daily.title", 
						new Object[] { dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId(), simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMinDate()),
						simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMaxDate())}, Locale.getDefault());
				
				report.append(title);
				
				report.append("\n\n");
				
				// data raportu
				String currentDateString = simpleDateFormat.format(new Date());
				
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
				
				appendWordKanjiSearchNoFoundStat(report, "schedule.task.generate.daily.report.word.dictionary.no.found", wordDictionarySearchNoFoundStatList);
				
				// wyszukiwanie automatycznego uzupelniania slowek bez wynikow
				List<WordKanjiSearchNoFoundStat> wordDictionaryAutocompleteNoFoundStat = mySQLConnector.getWordDictionaryAutocompleteNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendWordKanjiSearchNoFoundStat(report, "schedule.task.generate.daily.report.word.dictionary.autocomplete.no.found", wordDictionaryAutocompleteNoFoundStat);
				
				// wyszukiwanie kanji bez wynikow				
				List<WordKanjiSearchNoFoundStat> kanjiDictionarySearchNoFoundStatList = mySQLConnector.getKanjiDictionarySearchNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendWordKanjiSearchNoFoundStat(report, "schedule.task.generate.daily.report.kanji.dictionary.no.found", kanjiDictionarySearchNoFoundStatList);
				
				// wyszukiwanie automatycznego uzupelniania kanji bez wynikow
				List<WordKanjiSearchNoFoundStat> kanjiDictionaryAutocompleteNoFoundStat = mySQLConnector.getKanjiDictionaryAutocompleteNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendWordKanjiSearchNoFoundStat(report, "schedule.task.generate.daily.report.kanji.dictionary.autocomplete.no.found", kanjiDictionaryAutocompleteNoFoundStat);
				
				logger.info("Wygenerowano dzienny raport: \n\n" + report.toString());
				
				// zablokuj wpisy
				mySQLConnector.blockDailyLogProcessedIds(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				// publikacja do kolejki i do wyslania
				loggerSender.sendLog(new DailyReportLoggerModel(title, report.toString()));
			}
			
		} catch (Exception e) {
			logger.error("Blad generowania dziennego raportu", e);
		}
				
		logger.info("Generowanie raportu zakonczone");
	}
	
	private void appendWordKanjiSearchNoFoundStat(StringBuffer report, String titleCode, List<WordKanjiSearchNoFoundStat> wordKanjiSearchNoFoundStatList) {
		
		report.append(messageSource.getMessage(titleCode, new Object[] { }, Locale.getDefault()));
		
		report.append("\n\n");
		
		for (WordKanjiSearchNoFoundStat wordKanjiSearchNoFoundStat : wordKanjiSearchNoFoundStatList) {
			
			String word = wordKanjiSearchNoFoundStat.getWord();
			
			report.append(word);
			
			for (int idx = word.length(); idx < 40; ++idx) {
				report.append(" ");
			}
			
			report.append(" ").append(wordKanjiSearchNoFoundStat.getStat()).append("\n");
		}
		
		report.append("\n\n");
	}
}
