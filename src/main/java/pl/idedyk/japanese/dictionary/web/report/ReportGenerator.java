package pl.idedyk.japanese.dictionary.web.report;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.google.gson.internal.LinkedTreeMap;

import pl.idedyk.japanese.dictionary.api.android.queue.event.QueueEventOperation;
import pl.idedyk.japanese.dictionary.web.html.B;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.P;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidQueueEventLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyLogProcessedMinMaxIds;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericTextStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchMissingWordQueue;
import pl.idedyk.japanese.dictionary.web.service.GeoIPService;
import pl.idedyk.japanese.dictionary.web.service.UserAgentService;
import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo;
import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo.DesktopInfo;
import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo.JapaneseAndroidLearnerHelperInfo;
import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo.JapaneseAndroidLearnerHelperInfo.SubType;
import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo.PhoneTabletInfo;

@Service
public class ReportGenerator {
	
	private static final Logger logger = LogManager.getLogger(ReportGenerator.class);

	@Autowired
	private MySQLConnector mySQLConnector;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private UserAgentService userAgentService;
	
	@Autowired
	private GeoIPService geoIPService;
	
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
				
				// pobieramy surowe dane zrodlowe
				List<GenericLog> genericLogRawList = mySQLConnector.getGenericLogList(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());

				// pobieramy dane dotyczace zdarzen z androida
				List<AndroidQueueEventLog> androidQueueEventLogList = mySQLConnector.getAndroidQueueEventLogList(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				//
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				logger.info("Przetwarzam wpisy od " + dailyLogProcessedMinMaxIds.getMinId() + " do " + dailyLogProcessedMinMaxIds.getMaxId() + 
						" (" + (dailyLogProcessedMinMaxIds.getMinDate() != null ? simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMinDate()) : "") + 
						" - " + (dailyLogProcessedMinMaxIds.getMaxDate() != null ? simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMaxDate()) : ""));

				Div reportDiv = new Div();
				
				// tytul
				String title = messageSource.getMessage("report.generate.daily.title", 
						new Object[] { dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId(), 
						(dailyLogProcessedMinMaxIds.getMinDate() != null ? simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMinDate()) : ""),
						(dailyLogProcessedMinMaxIds.getMaxDate() != null ? simpleDateFormat.format(dailyLogProcessedMinMaxIds.getMaxDate()) : "")}, Locale.getDefault());

				P titleP = new P();
				reportDiv.addHtmlElement(titleP);
				
				B titlePB = new B();
				titleP.addHtmlElement(titlePB);
				
				titlePB.addHtmlElement(new Text(title));

				// data raportu
				String currentDateString = simpleDateFormat.format(new Date());
				
				P dateP = new P();
				reportDiv.addHtmlElement(dateP);

				dateP.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.report.date",
						new Object[] { currentDateString }, Locale.getDefault())));

				reportDiv.addHtmlElement(new Hr());
				
				// statystyki ogolne - tytul
				H statGeneralH1Title = new H(2);
				
				statGeneralH1Title.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.general.title", new Object[] { }, Locale.getDefault())));
				
				reportDiv.addHtmlElement(statGeneralH1Title);
				
				// statystyki operacji
				List<GenericTextStat> genericLogOperationStatList = groupByStat(genericLogRawList, new IGroupByFunction() {

					@Override
					public String getKey(Object o) {
						
						GenericLog genericLog = (GenericLog)o;
						
						return genericLog.getOperation().name();
					} 
				});
				
				// podzielenie statystyk na Desktop, Phone + Mobile, Tablet, Robot + Robot Mobile oraz inne
				SplitUserAgentStatByTypeResult splitUserAgentStatByType = splitUserAgentStatByType(genericLogRawList);				

				//

				appendGenericTextStat(reportDiv, "report.generate.daily.report.operation.stat", genericLogOperationStatList);
				
				// statystyki krajow (bez robotow i innych)		
				appendGenericTextStat(reportDiv, "report.generate.daily.report.city.stat", groupByStat(splitUserAgentStatByType.getHumanList(), new CountryCityGroupBy(0)));
												
				//
								
				// reportDiv.addHtmlElement(new Hr());
								
				// wyszukiwanie slowek bez wynikow				
				/*
				List<GenericTextStat> wordDictionarySearchNoFoundStatList = mySQLConnector.getWordDictionarySearchNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.word.dictionary.no.found", wordDictionarySearchNoFoundStatList);
				
				// statystyki wyszukiwania slowek
				List<GenericTextStat> wordDictionarySearchStat = mySQLConnector.getWordDictionarySearchStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.word.dictionary.search", wordDictionarySearchStat);
				
				// wyszukiwanie automatycznego uzupelniania slowek bez wynikow
				List<GenericTextStat> wordDictionaryAutocompleteNoFoundStat = mySQLConnector.getWordDictionaryAutocompleteNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.word.dictionary.autocomplete.no.found", wordDictionaryAutocompleteNoFoundStat);
								
				// statystyki wyszukiwanie automatycznego uzupelniania slowek
				List<GenericTextStat> wordDictionaryAutocompleteStat = mySQLConnector.getWordDictionaryAutocompleteStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.word.dictionary.autocomplete.search", wordDictionaryAutocompleteStat);
				
				// statystyki zglaszania slow od androida
				List<GenericTextStat> androidSendMissingWordStat = mySQLConnector.getAndroidSendMissingWordStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.android.send.missing.word", androidSendMissingWordStat);
				
				// wyszukiwanie kanji bez wynikow				
				List<GenericTextStat> kanjiDictionarySearchNoFoundStatList = mySQLConnector.getKanjiDictionarySearchNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.kanji.dictionary.no.found", kanjiDictionarySearchNoFoundStatList);
				
				// statystyki wyszukiwania kanji
				List<GenericTextStat> kanjiDictionarySearchStat = mySQLConnector.getKanjiDictionarySearchStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.kanji.dictionary.search", kanjiDictionarySearchStat);				
				
				// wyszukiwanie automatycznego uzupelniania kanji bez wynikow
				List<GenericTextStat> kanjiDictionaryAutocompleteNoFoundStat = mySQLConnector.getKanjiDictionaryAutocompleteNoFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.kanji.dictionary.autocomplete.no.found", kanjiDictionaryAutocompleteNoFoundStat);

				// statystyki wyszukiwanie automatycznego uzupelniania kanji
				List<GenericTextStat> kanjiDictionaryAutocompleteStat = mySQLConnector.getKanjiDictionaryAutocompleteStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.kanji.dictionary.autocomplete.search", kanjiDictionaryAutocompleteStat);				

				// statystyki ilosci wywolan
				List<RemoteClientStat> remoteClientStat = mySQLConnector.getRemoteClientStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				appendRemoteClientStat(reportDiv, "report.generate.daily.report.remote.client", remoteClientStat);

				// statystyki user agentow
				*/				
				
				// generowanie statystyk dla aplikacji na androida
				generateStatForJapanaeseAndroidLearnHelper(reportDiv, splitUserAgentStatByType, androidQueueEventLogList);
								
				// generowanie statystyk dla komputera (desktop)
				generateStatForDesktop(reportDiv, splitUserAgentStatByType);
				
				// generowanie statystyk dla telefonu
				generateStatForPhone(reportDiv, splitUserAgentStatByType);
				
				// generowanie statystyk dla tabletu
				generateStatForTablet(reportDiv, splitUserAgentStatByType);
				
				// generowanie statystyk dla robota
				generateStatForRobot(reportDiv, splitUserAgentStatByType);
				
				// generowanie statystyk dla innych
				generateStatForOther(reportDiv, splitUserAgentStatByType);
				
				// generowanie statystyk dla pustych
				generateStatForNull(reportDiv, splitUserAgentStatByType);
				
				//
				
				// statystyki odnosnikow
				List<GenericTextStat> refererStat = groupByStat(genericLogRawList, new IGroupByFunction() {

					@Override
					public String getKey(Object o) {
						
						GenericLog genericLog = (GenericLog)o;
						
						String refererURL = genericLog.getRefererURL();
						
						if (refererURL == null) {
							return null;
						}
						
						if (refererURL.contains(baseServer) == true) {
							return null;
						}
						
						if (refererURL.length() > 150) {
							refererURL = refererURL.substring(0, 150);
						}
						
						return refererURL;						
					}
				});
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.referer.stat", refererStat);				

				// statystyki nieznalezionych stron
				List<GenericTextStat> pageNotFoundStat = groupByStat(genericLogRawList, new IGroupByFunction() {

					@Override
					public String getKey(Object o) {
						
						GenericLog genericLog = (GenericLog)o;
						
						if (genericLog.getOperation() != GenericLogOperationEnum.PAGE_NO_FOUND_EXCEPTION) {
							return null;
						}
						
						return genericLog.getRequestURL();						
					}
				});
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.page.not.found.stat", pageNotFoundStat);				
				
				// kolejka brakujacych slow
				/*
				List<WordDictionarySearchMissingWordQueue> allUnlockedWordDictionarySearchMissingWordQueue = mySQLConnector.getUnlockedWordDictionarySearchMissingWordQueue(Integer.MAX_VALUE);
				
				appendWordDictionarySearchMissingWordQueueStat(reportDiv, "report.generate.daily.report.word.dictionary.missing.words.queue.full", allUnlockedWordDictionarySearchMissingWordQueue, null, false);				
				*/
				
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
	
	public Report generateMissingWordsQueueReportBody(List<WordDictionarySearchMissingWordQueue> missingWordsQueueList, long showMaxSize) throws Exception {
		
		logger.info("Generowanie raportu z brakujacej listy slow");
		
		Div reportDiv = new Div();
				
		// obecna kolejka brakujacych slow				
		appendWordDictionarySearchMissingWordQueueStat(reportDiv, "report.generate.daily.report.word.dictionary.missing.words.queue.part", missingWordsQueueList, null, false);				
		
		// calkowita kolejka brakujacych slow
		List<WordDictionarySearchMissingWordQueue> allUnlockedWordDictionarySearchMissingWordQueue = mySQLConnector.getUnlockedWordDictionarySearchMissingWordQueue(showMaxSize);
		
		long unlockedWordDictionarySearchMissingWordQueueLength = mySQLConnector.getUnlockedWordDictionarySearchMissingWordQueueLength();
		
		appendWordDictionarySearchMissingWordQueueStat(reportDiv, "report.generate.daily.report.word.dictionary.missing.words.queue.full", allUnlockedWordDictionarySearchMissingWordQueue, unlockedWordDictionarySearchMissingWordQueueLength, true);
		
		// cala zawartosc kolejki
		// List<WordDictionarySearchMissingWordQueue> allWordDictionarySearchMissingWordQueue = mySQLConnector.getAllWordDictionarySearchMissingWordQueue();
		
		// appendWordDictionarySearchMissingWordQueueStat(reportDiv, "report.generate.daily.report.word.dictionary.missing.words.full", allWordDictionarySearchMissingWordQueue, true);

		///
		
		StringWriter stringWriter = new StringWriter();
		
		reportDiv.render(stringWriter);
						
		Report report = new Report();
		
		report.body = stringWriter.toString();
		report.title = messageSource.getMessage("report.generate.daily.report.word.dictionary.missing.words.queue.part", new Object[] { }, Locale.getDefault());
		
		return report;
	}
	
	private void appendGenericTextStat(Div reportDiv, String titleCode, List<GenericTextStat> genericTextStatList) {
		
		// liczymy sume
		
		long statSum = 0;
		
		for (GenericTextStat genericTextStat : genericTextStatList) {			
			statSum += genericTextStat.getStat();			
		}
		
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
			
			BigDecimal percent = (new BigDecimal(genericTextStat.getStat()).divide(new BigDecimal(statSum), 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
			
			td2.addHtmlElement(new Text("" + genericTextStat.getStat() + " (" + percent + "%)"));			
		}
		
		reportDiv.addHtmlElement(div);		
		reportDiv.addHtmlElement(new Hr());
	}
	
	/*
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
	*/
	
	private void appendWordDictionarySearchMissingWordQueueStat(Div reportDiv, String titleCode, List<WordDictionarySearchMissingWordQueue> wordDictionarySearchMissingWordQueueList, Long wordDictionarySearchMissingWordQueueListLength, boolean full) {
		
		Div div = new Div();
		
		P titleP = new P();
		reportDiv.addHtmlElement(titleP);
		
		if (wordDictionarySearchMissingWordQueueListLength == null) {
			titleP.addHtmlElement(new Text(messageSource.getMessage(titleCode, new Object[] { }, Locale.getDefault()) + ": " + wordDictionarySearchMissingWordQueueList.size()));
			
		} else {
			titleP.addHtmlElement(new Text(messageSource.getMessage(titleCode, new Object[] { }, Locale.getDefault()) + ": " + wordDictionarySearchMissingWordQueueListLength));
		}
		
		Table table = new Table(null, "border: 1px solid black");
		div.addHtmlElement(table);
		
		int counter = 1;
		
		for (WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue : wordDictionarySearchMissingWordQueueList) {
			
			Tr tr = new Tr();
			table.addHtmlElement(tr);
			
			String missingWord = wordDictionarySearchMissingWordQueue.getMissingWord();
			
			if (missingWord == null) {
				missingWord = "-";
			}
			
			Td td1 = new Td(null, (full == false ? "padding: 5px;" : "padding: 5px; border: 1px solid black; "));
			tr.addHtmlElement(td1);
			
			td1.addHtmlElement(new Text(HtmlUtils.htmlEscape(missingWord)));

			Td td2 = new Td(null, (full == false ? "padding: 5px;" : "padding: 5px; border: 1px solid black; "));
			tr.addHtmlElement(td2);
			
			td2.addHtmlElement(new Text("" + wordDictionarySearchMissingWordQueue.getCounter()));
			
			if (full == true) {
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				Td td3 = new Td(null, (full == false ? "padding: 5px;" : "padding: 5px; border: 1px solid black; "));
				tr.addHtmlElement(td3);
				
				td3.addHtmlElement(new Text(sdf.format(wordDictionarySearchMissingWordQueue.getFirstAppearanceTimestamp())));

				Td td4 = new Td(null, (full == false ? "padding: 5px;" : "padding: 5px; border: 1px solid black; "));
				tr.addHtmlElement(td4);
				
				td4.addHtmlElement(new Text(sdf.format(wordDictionarySearchMissingWordQueue.getLastAppearanceTimestamp())));
				
				Td td5 = new Td(null, (full == false ? "padding: 5px;" : "padding: 5px; border: 1px solid black; "));
				tr.addHtmlElement(td5);
				
				if (wordDictionarySearchMissingWordQueue.getLockTimestamp() != null) {
					td5.addHtmlElement(new Text(sdf.format(wordDictionarySearchMissingWordQueue.getLockTimestamp())));
				}

				Td td6 = new Td(null, (full == false ? "padding: 5px;" : "padding: 5px; border: 1px solid black; "));
				tr.addHtmlElement(td6);
				
				td6.addHtmlElement(new Text(String.valueOf(counter)));
				
				Td td7 = new Td(null, (full == false ? "padding: 5px;" : "padding: 5px; border: 1px solid black; "));
				tr.addHtmlElement(td7);
				
				td7.addHtmlElement(new Text(String.valueOf(wordDictionarySearchMissingWordQueue.getPriority())));				
				
				counter++;
			}			
		}
		
		reportDiv.addHtmlElement(div);		
		reportDiv.addHtmlElement(new Hr());
	}
	
	/*
	private List<GenericTextStat> regenerateUserAgentClientStat(List<GenericTextStat> userAgentClientRawStatList) {

		Map<String, Long> userAgentInPrintableVersionGroupBy = new TreeMap<String, Long>();
		
		//
		
		for (GenericTextStat currentGenericTextRawStat : userAgentClientRawStatList) {
			
			String userAgentInPrintableForm = userAgentService.getUserAgentInPrintableForm(currentGenericTextRawStat.getText());
			
			Long userAgentInPrintableFormSize = userAgentInPrintableVersionGroupBy.get(userAgentInPrintableForm);
			
			if (userAgentInPrintableFormSize == null) {				
				userAgentInPrintableFormSize = 0l;
			}
			
			userAgentInPrintableVersionGroupBy.put(userAgentInPrintableForm, userAgentInPrintableFormSize + currentGenericTextRawStat.getStat());			
		}
		
		//
		
		List<GenericTextStat> result = new ArrayList<GenericTextStat>();
		
		//
		
		Set<Entry<String, Long>> userAgentInPrintableVersionGroupByEntrySet = userAgentInPrintableVersionGroupBy.entrySet();
		
		Iterator<Entry<String, Long>> userAgentInPrintableVersionGroupByEntrySetIterator = userAgentInPrintableVersionGroupByEntrySet.iterator();
		
		while (userAgentInPrintableVersionGroupByEntrySetIterator.hasNext() == true) {
			
			Entry<String, Long> currentEntryInSet = userAgentInPrintableVersionGroupByEntrySetIterator.next();
			
			GenericTextStat genericTextStat = new GenericTextStat();
			
			genericTextStat.setText(currentEntryInSet.getKey());
			genericTextStat.setStat(currentEntryInSet.getValue());
			
			result.add(genericTextStat);
		}
		
		//
		
		Collections.sort(result, new Comparator<GenericTextStat>() {

			@Override
			public int compare(GenericTextStat o1, GenericTextStat o2) {
				
				Long o1Stat = o1.getStat();
				Long o2Stat = o2.getStat();
				
				int result = o2Stat.compareTo(o1Stat);
				
				if (result != 0) {
					return result;
				}
				
				String o1Text = o1.getText();
				String o2Text = o2.getText();
				
				return o1Text.compareTo(o2Text);				
			}
		});
				
		return result;
	}
	*/
	
	private SplitUserAgentStatByTypeResult splitUserAgentStatByType(List<GenericLog> genericLogList) {
		
		SplitUserAgentStatByTypeResult result = new SplitUserAgentStatByTypeResult();
		
		//
		
		for (GenericLog genericLog : genericLogList) {
			
			UserAgentInfo userAgentInfo = userAgentService.getUserAgentInfo(genericLog.getUserAgent());
			
			switch (userAgentInfo.getType()) {
				
				case JAPANESE_ANDROID_LEARNER_HELPER:
					
					result.japaneseAndroidLearnerHelperList.add(new ImmutablePair<GenericLog, UserAgentInfo>(genericLog, userAgentInfo));
					
					break;
					
				case DESKTOP:
					
					result.desktopList.add(new ImmutablePair<GenericLog, UserAgentInfo>(genericLog, userAgentInfo));
					
					break;
					
				case PHONE:
					
					result.phoneList.add(new ImmutablePair<GenericLog, UserAgentInfo>(genericLog, userAgentInfo));
					
					break;
					
				case TABLET:
					
					result.tableList.add(new ImmutablePair<GenericLog, UserAgentInfo>(genericLog, userAgentInfo));
					
					break;
					
				case ROBOT:
					
					result.robotList.add(new ImmutablePair<GenericLog, UserAgentInfo>(genericLog, userAgentInfo));
					
					break;
					
				case OTHER:
					
					result.otherList.add(new ImmutablePair<GenericLog, UserAgentInfo>(genericLog, userAgentInfo));
					
					break;
					
				case NULL:
					
					result.nullList.add(new ImmutablePair<GenericLog, UserAgentInfo>(genericLog, userAgentInfo));
					
					break;
			}
		}
		
		return result;
	}
	
	private void generateStatForJapanaeseAndroidLearnHelper(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType, List<AndroidQueueEventLog> androidQueueEventLogList) {
		
		H androidTitle = new H(2);
		
		androidTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.android.title", new Object[] { }, Locale.getDefault())));
		
		reportDiv.addHtmlElement(androidTitle);
				
		//
		
		List<ImmutablePair<GenericLog, UserAgentInfo>> japaneseAndroidLearnerHelperList = splitUserAgentStatByType.japaneseAndroidLearnerHelperList;
		
		//
		
		// maly cache ogolnych operacji
		final Map<Long, ImmutablePair<GenericLog, UserAgentInfo>> genericIdCache = new TreeMap<>();
		
		for (ImmutablePair<GenericLog, UserAgentInfo> pair : japaneseAndroidLearnerHelperList) {					
			genericIdCache.put(pair.getKey().getId(), pair);
		}
		
		//
		
		// pogrupowanie po identyfikatorze uzytkownika (ostatnia operacja)
		LinkedTreeMap<String, AndroidQueueEventLog> androidQueueEventLogListGroupByUserId = new LinkedTreeMap<>();
		
		for (AndroidQueueEventLog currentAndroidQueueEventLog : androidQueueEventLogList) {			
			androidQueueEventLogListGroupByUserId.put(currentAndroidQueueEventLog.getUserId(), currentAndroidQueueEventLog);
		}
		
		Collection<AndroidQueueEventLog> androidQueueEventLogListGroupByUserIdAsCollection = androidQueueEventLogListGroupByUserId.values();
		
		//
		
		for (int i = 0; i < 2; ++i) {
			
			final boolean onlyUnique = i == 1;
			
			if (onlyUnique == false) {
				
				// statystyki ilosciowe		
				H androidUniqueFalseTitle = new H(3);
				
				androidUniqueFalseTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.android.unique.false.title", new Object[] { }, Locale.getDefault())));
				
				reportDiv.addHtmlElement(androidUniqueFalseTitle);
				
			} else {
				
				// statystyki unikalne		
				H androidUniqueFalseTitle = new H(3);
				
				androidUniqueFalseTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.android.unique.true.title", new Object[] { }, Locale.getDefault())));
				
				reportDiv.addHtmlElement(androidUniqueFalseTitle);
			}
						
			// statystyki wersji aplikacji
			appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.version.stat", groupByStat(onlyUnique == false ? japaneseAndroidLearnerHelperList : androidQueueEventLogListGroupByUserIdAsCollection, new IGroupByFunction() {
				
				@SuppressWarnings("unchecked")
				@Override
				public String getKey(Object o) {
					
					ImmutablePair<GenericLog, UserAgentInfo> pair;
										
					if (o instanceof ImmutablePair) {
						
						pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
						
					} else {
						
						AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;						
						
						pair = genericIdCache.get(androidQueueEventLog.getGenericLogId());							
					}
					
					int code = pair.right.getJapaneseAndroidLearnerHelperInfo().getCode();
					String codeName = pair.right.getJapaneseAndroidLearnerHelperInfo().getCodeName();
					JapaneseAndroidLearnerHelperInfo.SubType subType = pair.right.getJapaneseAndroidLearnerHelperInfo().getSubType();
					
					return code + " - " + codeName + (subType == SubType.FULL ? "" : " (slim)");
				}
			}));
			
			// statystyki wersji androida
			appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.android.version.stat", groupByStat(
					onlyUnique == false ? androidQueueEventLogList : androidQueueEventLogListGroupByUserIdAsCollection, new IGroupByFunction() {
				
				@Override
				public String getKey(Object o) {
					
					AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
					
					Map<String, String> paramsAsMap = androidQueueEventLog.getParamsAsMap();
					
					return paramsAsMap.get("androidVersion");
				}
			}));
			
			// statystyki producenta urzadzenia
			appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.android.device.manufacturer.stat", groupByStat(
					onlyUnique == false ? androidQueueEventLogList : androidQueueEventLogListGroupByUserIdAsCollection, new IGroupByFunction() {
				
				@Override
				public String getKey(Object o) {
					
					AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
					
					Map<String, String> paramsAsMap = androidQueueEventLog.getParamsAsMap();
					
					return paramsAsMap.get("androidDeviceManufacturer");
				}
			}));

			// statystyki modelu urzadzenia
			appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.android.device.model.stat", groupByStat(
					onlyUnique == false ? androidQueueEventLogList : androidQueueEventLogListGroupByUserIdAsCollection, new IGroupByFunction() {
				
				@Override
				public String getKey(Object o) {
					
					AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
					
					Map<String, String> paramsAsMap = androidQueueEventLog.getParamsAsMap();
					
					return paramsAsMap.get("androidDeviceModel");
				}
			}));
			
			// statystyki krajow na podstawie adresu IP
			if (onlyUnique == false) {
				appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.country.stat", groupByStat(japaneseAndroidLearnerHelperList, new CountryCityGroupBy(0)));
				
			} else {
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.country.stat", groupByStat(androidQueueEventLogListGroupByUserIdAsCollection, new IGroupByFunction() {
					
					CountryCityGroupBy countryCityGroupBy = new CountryCityGroupBy(0);
					
					@Override
					public String getKey(Object o) {
						
						AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;						
						
						ImmutablePair<GenericLog, UserAgentInfo> pair = genericIdCache.get(androidQueueEventLog.getGenericLogId());

						return countryCityGroupBy.getKey(pair);
					}
				}));
			}
			
			// statystyki krajow na podstawie jezyka urzadzenia
			List<GenericTextStat> androidQueueEventLocaleCountryStat = groupByStat(
					onlyUnique == false ? androidQueueEventLogList : androidQueueEventLogListGroupByUserIdAsCollection, new IGroupByFunction() {
				
				@Override
				public String getKey(Object o) {
					
					AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
					
					Map<String, String> paramsAsMap = androidQueueEventLog.getParamsAsMap();
					
					return paramsAsMap.get("localeCountry");
				}
			});
			
			appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.queue.event.locale.country", androidQueueEventLocaleCountryStat);	

			// statystyki jezykow na podstawie jezyka urzadzenia
			List<GenericTextStat> androidQueueEventLocaleLanguageStat = groupByStat(
					onlyUnique == false ? androidQueueEventLogList : androidQueueEventLogListGroupByUserIdAsCollection, new IGroupByFunction() {
				
				@Override
				public String getKey(Object o) {
					
					AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
					
					Map<String, String> paramsAsMap = androidQueueEventLog.getParamsAsMap();
					
					return paramsAsMap.get("localeLanguage");
				}
			});
			
			appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.queue.event.locale.language", androidQueueEventLocaleLanguageStat);	
			
			// statystyki krajow i miast
			// appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.country.city.stat", groupByStat(japaneseAndroidLearnerHelperList, new CountryCityGroupBy(1)));
			
			// statystyki rodzaju operacji na androidzie
			if (onlyUnique == false) {
			
				List<GenericTextStat> androidQueueEventOperationStat = groupByStat(androidQueueEventLogList, new IGroupByFunction() {
					
					@Override
					public String getKey(Object o) {
						
						AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
						
						return androidQueueEventLog.getOperation().toString();
					}
				});
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.queue.event.operation.stat", androidQueueEventOperationStat);
				
				// statystyki ekranow
				List<GenericTextStat> androidQueueEventScreenStat = groupByStat(androidQueueEventLogList, new IGroupByFunction() {
					
					@Override
					public String getKey(Object o) {
						
						AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
						
						if (androidQueueEventLog.getOperation() != QueueEventOperation.STAT_LOG_SCREEN_EVENT) {
							return null;
						}
						
						Map<String, String> paramsAsMap = androidQueueEventLog.getParamsAsMap();
						
						return paramsAsMap.get("screenName");
					}
				});
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.queue.event.operation.screen.stat", androidQueueEventScreenStat);
				
				// statystyki zdarzen
				List<GenericTextStat> androidQueueEventEventStat = groupByStat(androidQueueEventLogList, new IGroupByFunction() {
					
					@Override
					public String getKey(Object o) {
						
						AndroidQueueEventLog androidQueueEventLog = (AndroidQueueEventLog)o;
						
						if (androidQueueEventLog.getOperation() != QueueEventOperation.STAT_LOG_EVENT_EVENT) {
							return null;
						}
						
						Map<String, String> paramsAsMap = androidQueueEventLog.getParamsAsMap();
						
						return paramsAsMap.get("actionName");
					}
				});
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.queue.event.operation.event.stat", androidQueueEventEventStat);
			}
		}		
	}
	
	private void generateStatForDesktop(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
		
		H desktopTitle = new H(2);
		
		desktopTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.desktop.title", new Object[] { }, Locale.getDefault())));
		
		reportDiv.addHtmlElement(desktopTitle);

		//
		
		List<ImmutablePair<GenericLog, UserAgentInfo>> desktopList = splitUserAgentStatByType.desktopList;
	
		//
		
		List<GenericTextStat> desktopTypeList = groupByStat(desktopList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				DesktopInfo desktopInfo = pair.right.getDesktopInfo();
				
				return desktopInfo.getDesktopType();
			}
		});

		List<GenericTextStat> operationSystemList = groupByStat(desktopList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				DesktopInfo desktopInfo = pair.right.getDesktopInfo();
				
				return desktopInfo.getOperationSystem();
			}
		});

		List<GenericTextStat> browserTypeList = groupByStat(desktopList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				DesktopInfo desktopInfo = pair.right.getDesktopInfo();
				
				return desktopInfo.getBrowserType();
			}
		});
		
		// statystyki krajow		
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.country.stat", groupByStat(desktopList, new CountryCityGroupBy(0)));
		
		// statystyki krajow i miast
		// appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.country.city.stat", groupByStat(desktopList, new CountryCityGroupBy(1)));
						
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.desktopType.stat", desktopTypeList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.operationSystem.stat", operationSystemList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.browserType.stat", browserTypeList);				
	}
	
	private void generateStatForPhone(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
		
		H phoneTitle = new H(2);
		
		phoneTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.phone.title", new Object[] { }, Locale.getDefault())));
		
		reportDiv.addHtmlElement(phoneTitle);

		//
		
		List<ImmutablePair<GenericLog, UserAgentInfo>> phoneList = splitUserAgentStatByType.phoneList;
		
		//
		
		List<GenericTextStat> deviceNameList = groupByStat(phoneList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				PhoneTabletInfo phoneInfo = pair.right.getPhoneTabletInfo();
				
				return phoneInfo.getDeviceName();
			}
		});

		List<GenericTextStat> operationSystemList = groupByStat(phoneList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				PhoneTabletInfo phoneInfo = pair.right.getPhoneTabletInfo();
				
				return phoneInfo.getOperationSystem();
			}
		});

		List<GenericTextStat> browserTypeList = groupByStat(phoneList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				PhoneTabletInfo phoneInfo = pair.right.getPhoneTabletInfo();
				
				return phoneInfo.getBrowserType();
			}
		});
		
		// statystyki krajow		
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.phone.country.stat", groupByStat(phoneList, new CountryCityGroupBy(0)));
		
		// statystyki krajow i miast
		// appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.phone.country.city.stat", groupByStat(phoneList, new CountryCityGroupBy(1)));
						
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.phone.deviceName.stat", deviceNameList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.phone.operationSystem.stat", operationSystemList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.phone.browserType.stat", browserTypeList);		
	}

	private void generateStatForTablet(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
		
		H tableTitle = new H(2);
		
		tableTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.tablet.title", new Object[] { }, Locale.getDefault())));
		
		reportDiv.addHtmlElement(tableTitle);
		
		List<ImmutablePair<GenericLog, UserAgentInfo>> tabletList = splitUserAgentStatByType.tableList;
		
		//
		
		List<GenericTextStat> deviceNameList = groupByStat(tabletList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				PhoneTabletInfo phoneInfo = pair.right.getPhoneTabletInfo();
				
				return phoneInfo.getDeviceName();
			}
		});

		List<GenericTextStat> operationSystemList = groupByStat(tabletList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				PhoneTabletInfo phoneInfo = pair.right.getPhoneTabletInfo();
				
				return phoneInfo.getOperationSystem();
			}
		});

		List<GenericTextStat> browserTypeList = groupByStat(tabletList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				PhoneTabletInfo phoneInfo = pair.right.getPhoneTabletInfo();
				
				return phoneInfo.getBrowserType();
			}
		});
		
		// statystyki krajow		
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.tablet.country.stat", groupByStat(tabletList, new CountryCityGroupBy(0)));
		
		// statystyki krajow i miast
		// appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.tablet.country.city.stat", groupByStat(tabletList, new CountryCityGroupBy(1)));
						
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.tablet.deviceName.stat", deviceNameList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.tablet.operationSystem.stat", operationSystemList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.tablet.browserType.stat", browserTypeList);		
	}
	
	private void generateStatForRobot(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
		
		H robotTitle = new H(2);
		
		robotTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.robot.title", new Object[] { }, Locale.getDefault())));
		
		reportDiv.addHtmlElement(robotTitle);
		
		//
		
		List<ImmutablePair<GenericLog, UserAgentInfo>> robotList = splitUserAgentStatByType.robotList;
		
		//
		
		List<GenericTextStat> robotStatList = groupByStat(robotList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				return pair.right.getRobotInfo().getRobotName() + " - " + pair.right.getRobotInfo().getRobotUrl();		
			}
		});
				
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.robot.robotName.stat", robotStatList);		
	}

	private void generateStatForOther(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
		
		H otherTitle = new H(2);
		
		otherTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.other.title", new Object[] { }, Locale.getDefault())));
		
		reportDiv.addHtmlElement(otherTitle);
		
		//
		
		List<ImmutablePair<GenericLog, UserAgentInfo>> otherList = splitUserAgentStatByType.otherList;
		
		//
		
		List<GenericTextStat> otherStatList = groupByStat(otherList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				return pair.right.getOtherInfo().getUserAgent();		
			}
		});
				
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.other.stat", otherStatList);		
	}

	private void generateStatForNull(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
		
		H nullTitle = new H(2);
		
		nullTitle.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.stat.null.title", new Object[] { }, Locale.getDefault())));
		
		reportDiv.addHtmlElement(nullTitle);
		
		//
		
		List<ImmutablePair<GenericLog, UserAgentInfo>> nullList = splitUserAgentStatByType.nullList;
		
		//
		
		List<GenericTextStat> nullStatList = groupByStat(nullList, new IGroupByFunction() {
			
			@Override
			public String getKey(Object o) {
				
				@SuppressWarnings("unchecked")
				ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
				
				String text = pair.left.getUserAgent();
				
				if (text == null) {
					text = "-";
				}
				
				return text;
			}
		});
				
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.null.stat", nullStatList);		
	}
	
	private List<GenericTextStat> groupByStat(Collection<?> list, IGroupByFunction groupByFunction) {
		
		Map<String, Long> resultMap = new TreeMap<String, Long>();
		
		// chodzenie po rekordach i grupowanie 
		for (Object currentObject : list) {
			
			String objectKey = groupByFunction.getKey(currentObject);
			
			if (objectKey == null) {
				continue;
			}
			
			Long count = resultMap.get(objectKey);
			
			if (count == null) {
				count = 0l;
			}
			
			count++;
			
			resultMap.put(objectKey, count);
		}
		
		//
		
		List<GenericTextStat> result = new ArrayList<GenericTextStat>();
		
		//
		
		Set<Entry<String, Long>> resultMapEntrySet = resultMap.entrySet();
		
		Iterator<Entry<String, Long>> resultMapEntrySetIterator = resultMapEntrySet.iterator();
		
		while (resultMapEntrySetIterator.hasNext() == true) {
			
			Entry<String, Long> currentEntryInSet = resultMapEntrySetIterator.next();
			
			GenericTextStat genericTextStat = new GenericTextStat();
			
			genericTextStat.setText(currentEntryInSet.getKey());
			genericTextStat.setStat(currentEntryInSet.getValue());
			
			result.add(genericTextStat);
		}
		
		//
		
		Collections.sort(result, new Comparator<GenericTextStat>() {

			@Override
			public int compare(GenericTextStat o1, GenericTextStat o2) {
				
				Long o1Stat = o1.getStat();
				Long o2Stat = o2.getStat();
				
				int result = o2Stat.compareTo(o1Stat);
				
				if (result != 0) {
					return result;
				}
				
				String o1Text = o1.getText();
				String o2Text = o2.getText();
				
				return o1Text.compareTo(o2Text);				
			}
		});
		
		return result;
	}
			
	private static class SplitUserAgentStatByTypeResult {
		
		private List<ImmutablePair<GenericLog, UserAgentInfo>> japaneseAndroidLearnerHelperList = new ArrayList<>();
		
		private List<ImmutablePair<GenericLog, UserAgentInfo>> desktopList = new ArrayList<>();
		
		private List<ImmutablePair<GenericLog, UserAgentInfo>> phoneList = new ArrayList<>();
		
		private List<ImmutablePair<GenericLog, UserAgentInfo>> tableList = new ArrayList<>();
		
		private List<ImmutablePair<GenericLog, UserAgentInfo>> robotList = new ArrayList<>();
		
		private List<ImmutablePair<GenericLog, UserAgentInfo>> otherList = new ArrayList<>();
		
		private List<ImmutablePair<GenericLog, UserAgentInfo>> nullList = new ArrayList<>();
		
		//
		
		public List<ImmutablePair<GenericLog, UserAgentInfo>> getHumanList() {
			
			List<ImmutablePair<GenericLog, UserAgentInfo>> result = new ArrayList<>();
			
			//
			
			result.addAll(japaneseAndroidLearnerHelperList);
			result.addAll(desktopList);
			result.addAll(phoneList);
			result.addAll(tableList);
			
			return result;
		}
	}
	
	private static interface IGroupByFunction {		
		public String getKey(Object object);
	}
	
	private class CountryCityGroupBy implements IGroupByFunction {
		
		private int mode;
		
		public CountryCityGroupBy(int mode) {
			this.mode = mode;
		}
		
		@Override
		public String getKey(Object o) {
			
			@SuppressWarnings("unchecked")
			ImmutablePair<GenericLog, UserAgentInfo> pair = (ImmutablePair<GenericLog, UserAgentInfo>)o;
			
			String remoteIp = pair.getKey().getRemoteIp();
			
			if (remoteIp == null || remoteIp.equals("127.0.0.1") == true) {
				return null;
			}
			
			String[] remoteIpSplited = remoteIp.split(", ");
			
			if (mode == 0) {
				return geoIPService.getCountry(remoteIpSplited[0].trim()); // bierzemy pierwszy adres, gdyz drugi to adres proxy
				
			} else {
				return geoIPService.getCountryAndCity(remoteIpSplited[0].trim()); // bierzemy pierwszy adres, gdyz drugi to adres proxy
			}
		} 
	}
	
	public static class Report {
		
		public String body;
		
		public String title;
	}
	
	public static class DailyReport extends Report {
				
		public Long minId;
		
		public Long maxId;		
	}
}
