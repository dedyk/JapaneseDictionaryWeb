package pl.idedyk.japanese.dictionary.web.schedule;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import pl.idedyk.japanese.dictionary.web.html.B;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.P;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.logger.LoggerListener;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.DailyReportLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyLogProcessedMinMaxIds;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericTextStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
import pl.idedyk.japanese.dictionary.web.mysql.model.RemoteClientStat;
import pl.idedyk.japanese.dictionary.web.queue.QueueService;

@Service
public class ScheduleTask {
	
	private static final Logger logger = Logger.getLogger(ScheduleTask.class);

	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private QueueService queueService;
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private LoggerListener loggerListener;
	
	@Value("${base.server}")
	private String baseServer;
	
	public DailyReport generateDailyReportBody() throws Exception {
		
		logger.info("Generowanie dziennego raportu");
		
		// pobranie przedzialu wpisow
		try {
			DailyLogProcessedMinMaxIds dailyLogProcessedMinMaxIds = mySQLConnector.getCurrentDailyLogProcessedMinMaxIds();
			
			if (dailyLogProcessedMinMaxIds == null) {
				logger.info("Brak wpisow do przetworzenia");
				
				return null;
				
			} else {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				logger.info("Przetwarzam wpisy od " + dailyLogProcessedMinMaxIds.getMinId() + " do " + dailyLogProcessedMinMaxIds.getMaxId() + 
						" (" + simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMinDate()) + " - " + simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMaxDate()));

				Div reportDiv = new Div();
				
				// tytul
				String title = messageSource.getMessage("schedule.task.generate.daily.title", 
						new Object[] { dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId(), simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMinDate()),
						simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMaxDate())}, Locale.getDefault());

				P titleP = new P();
				reportDiv.addHtmlElement(titleP);
				
				B titlePB = new B();
				titleP.addHtmlElement(titlePB);
				
				titlePB.addHtmlElement(new Text(title));

				// data raportu
				String currentDateString = simpleDateFormat.format(new Date());
				
				P dateP = new P();
				reportDiv.addHtmlElement(dateP);

				dateP.addHtmlElement(new Text(messageSource.getMessage("schedule.task.generate.daily.report.date",
						new Object[] { currentDateString }, Locale.getDefault())));

				reportDiv.addHtmlElement(new Hr());
				
				// statystyki operacji
				Div operationStatDiv = new Div();
				reportDiv.addHtmlElement(operationStatDiv);
				
				P operationStatDivP = new P();
				operationStatDiv.addHtmlElement(operationStatDivP);
				
				operationStatDivP.addHtmlElement(new Text(messageSource.getMessage("schedule.task.generate.daily.report.operation.stat",
						new Object[] { }, Locale.getDefault())));
				
				Table operationStatDivTable = new Table(null, "border: 1px solid black");
				operationStatDiv.addHtmlElement(operationStatDivTable);
				
				List<GenericLogOperationStat> genericLogOperationStatList = mySQLConnector.getGenericLogOperationStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				for (GenericLogOperationStat genericLogOperationStat : genericLogOperationStatList) {
					
					Tr operationStatDivTableTr = new Tr();
					operationStatDivTable.addHtmlElement(operationStatDivTableTr);
					
					String operationString = genericLogOperationStat.getOperation().toString();
					
					Td operationStatDivTableTrTd1 = new Td(null, "padding: 5px;");
					operationStatDivTableTr.addHtmlElement(operationStatDivTableTrTd1);
					
					operationStatDivTableTrTd1.addHtmlElement(new Text(operationString));
					
					Td operationStatDivTableTrTd2 = new Td(null, "padding: 5px;");
					operationStatDivTableTr.addHtmlElement(operationStatDivTableTrTd2);

					operationStatDivTableTrTd2.addHtmlElement(new Text("" + genericLogOperationStat.getStat()));
				}
				
				reportDiv.addHtmlElement(new Hr());

				// wyszukiwanie slowek bez wynikow				
				List<GenericTextStat> wordDictionarySearchNoFoundStatList = mySQLConnector.getWordDictionarySearchNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.word.dictionary.no.found", wordDictionarySearchNoFoundStatList);
				
				// statystyki wyszukiwania slowek
				List<GenericTextStat> wordDictionarySearchStat = mySQLConnector.getWordDictionarySearchStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.word.dictionary.search", wordDictionarySearchStat);
				
				// wyszukiwanie automatycznego uzupelniania slowek bez wynikow
				List<GenericTextStat> wordDictionaryAutocompleteNoFoundStat = mySQLConnector.getWordDictionaryAutocompleteNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.word.dictionary.autocomplete.no.found", wordDictionaryAutocompleteNoFoundStat);
								
				// statystyki wyszukiwanie automatycznego uzupelniania slowek
				List<GenericTextStat> wordDictionaryAutocompleteStat = mySQLConnector.getWordDictionaryAutocompleteStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.word.dictionary.autocomplete.search", wordDictionaryAutocompleteStat);
				
				// statystyki zglaszania slow od androida
				List<GenericTextStat> androidSendMissingWordStat = mySQLConnector.getAndroidSendMissingWordStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.android.send.missing.word", androidSendMissingWordStat);
				
				// wyszukiwanie kanji bez wynikow				
				List<GenericTextStat> kanjiDictionarySearchNoFoundStatList = mySQLConnector.getKanjiDictionarySearchNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.kanji.dictionary.no.found", kanjiDictionarySearchNoFoundStatList);
				
				// statystyki wyszukiwania kanji
				List<GenericTextStat> kanjiDictionarySearchStat = mySQLConnector.getKanjiDictionarySearchStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.kanji.dictionary.search", kanjiDictionarySearchStat);				
				
				// wyszukiwanie automatycznego uzupelniania kanji bez wynikow
				List<GenericTextStat> kanjiDictionaryAutocompleteNoFoundStat = mySQLConnector.getKanjiDictionaryAutocompleteNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.kanji.dictionary.autocomplete.no.found", kanjiDictionaryAutocompleteNoFoundStat);

				// statystyki wyszukiwanie automatycznego uzupelniania kanji
				List<GenericTextStat> kanjiDictionaryAutocompleteStat = mySQLConnector.getKanjiDictionaryAutocompleteStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.kanji.dictionary.autocomplete.search", kanjiDictionaryAutocompleteStat);				
				
				// statystyki ilosci wywolan
				List<RemoteClientStat> remoteClientStat = mySQLConnector.getRemoteClientStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendRemoteClientStat(reportDiv, "schedule.task.generate.daily.report.remote.client", remoteClientStat);
				
				// statystyki user agentow
				List<GenericTextStat> userAgentClientStat = mySQLConnector.getUserAgentClientStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.user.agent.client", userAgentClientStat);
				
				// statystyki odnosnikow
				List<GenericTextStat> refererStat = mySQLConnector.getRefererStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId(), baseServer);
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.referer.stat", refererStat);				

				// statystyki nieznalezionych stron
				List<GenericTextStat> pageNotFoundStat = mySQLConnector.getPageNotFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "schedule.task.generate.daily.report.page.not.found.stat", pageNotFoundStat);				
				
				StringWriter stringWriter = new StringWriter();
				
				reportDiv.render(stringWriter);
								
				DailyReport dailyReport = new DailyReport();
				
				dailyReport.body = stringWriter.toString();
				dailyReport.title = title;
				dailyReport.minId = dailyLogProcessedMinMaxIds.getMinId();
				dailyReport.maxId = dailyLogProcessedMinMaxIds.getMaxId();
				
				return dailyReport;
			}
			
		} catch (Exception e) {
			throw new Exception("Blad generowania dziennego raportu", e);
		}
	}
	
	//@Scheduled(cron="0 * * * * ?") // co minute
	@Scheduled(cron="0 0 20 * * ?") // o 20
	public void generateDailyReport() {
										
		// pobranie przedzialu wpisow
		try {
			DailyReport dailyReport = generateDailyReportBody();
			
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
	
	private void appendGenericTextStat(Div reportDiv, String titleCode, List<GenericTextStat> genericTextStatList) {
		
		Div div = new Div();
		
		P titleP = new P();
		reportDiv.addHtmlElement(titleP);
				
		titleP.addHtmlElement(new Text(messageSource.getMessage(titleCode, new Object[] { }, Locale.getDefault())));
		
		Table table = new Table(null, "border: 1px solid black");
		div.addHtmlElement(table);
		
		for (GenericTextStat genericTextStat : genericTextStatList) {
			
			Tr tr = new Tr();
			table.addHtmlElement(tr);
			
			String text = genericTextStat.getText();
			
			if (text == null) {
				text = "-";
			}
			
			Td td1 = new Td(null, "padding: 5px;");
			tr.addHtmlElement(td1);
			
			td1.addHtmlElement(new Text(HtmlUtils.htmlEscape(text)));

			Td td2 = new Td(null, "padding: 5px;");
			tr.addHtmlElement(td2);
			
			td2.addHtmlElement(new Text("" + genericTextStat.getStat()));			
		}
		
		reportDiv.addHtmlElement(div);		
		reportDiv.addHtmlElement(new Hr());
	}

	private void appendRemoteClientStat(Div reportDiv, String titleCode, List<RemoteClientStat> remoteClientStatList) {
		
		Div div = new Div();
		
		P titleP = new P();
		reportDiv.addHtmlElement(titleP);
				
		titleP.addHtmlElement(new Text(messageSource.getMessage(titleCode, new Object[] { }, Locale.getDefault())));
		
		Table table = new Table(null, "border: 1px solid black");
		div.addHtmlElement(table);
		
		for (RemoteClientStat remoteClientStat : remoteClientStatList) {
			
			String remoteIp = remoteClientStat.getRemoteIp();
			String remoteHost = remoteClientStat.getRemoteHost();
			
			if (remoteIp == null) {
				remoteIp = "-";
			}

			if (remoteHost == null) {
				remoteHost = "-";
			}
			
			Tr tr = new Tr();
			table.addHtmlElement(tr);
			
			Td td1 = new Td(null, "padding: 5px;");
			tr.addHtmlElement(td1);
			
			td1.addHtmlElement(new Text(remoteIp));

			Td td2 = new Td(null, "padding: 5px;");
			tr.addHtmlElement(td2);
			
			td2.addHtmlElement(new Text(remoteHost));
			
			Td td3 = new Td(null, "padding: 5px;");
			tr.addHtmlElement(td3);
			
			td3.addHtmlElement(new Text("" + remoteClientStat.getStat()));			
		}
		
		reportDiv.addHtmlElement(div);		
		reportDiv.addHtmlElement(new Hr());
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
			
			logger.error("Blad podczas przetwarzania log'ow z kolejki", e);
			
			// opoznij zadanie
			if (currentQueueItem != null) {
				try {
					queueService.delayQueueItem(currentQueueItem);
					
				} catch (SQLException e1) {					
					logger.error("Blad podcza opozniania zadania z kolejki", e);
				}
			}
			
		}
	}
	
	@Scheduled(cron="0 0 0 * * ?") // o polnocy
	public void deleteOldQueueItems() {
		
		logger.info("Kasuje stare przetworzone wpisy z kolejki");
		
		try {			
			mySQLConnector.deleteOldQueueItems(7);
			
		} catch (Exception e) {
			
			logger.error("Blad podczas kasowania starych wpisow z kolejki", e);
		}
	}
	
	public static class DailyReport {
		
		public String body;
		
		public String title;
		
		public Long minId;
		
		public Long maxId;		
	}
}
