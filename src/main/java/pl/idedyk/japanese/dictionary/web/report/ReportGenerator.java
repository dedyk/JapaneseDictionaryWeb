package pl.idedyk.japanese.dictionary.web.report;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyLogProcessedMinMaxIds;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericTextStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchMissingWordQueue;
import pl.idedyk.japanese.dictionary.web.service.UserAgentService;
import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo;
import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo.DesktopInfo;

@Service
public class ReportGenerator {
	
	private static final Logger logger = Logger.getLogger(ReportGenerator.class);

	@Autowired
	private MySQLConnector mySQLConnector;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private UserAgentService userAgentService;
	
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
				
				// statystyki operacji
				Div operationStatDiv = new Div();
				reportDiv.addHtmlElement(operationStatDiv);
				
				P operationStatDivP = new P();
				operationStatDiv.addHtmlElement(operationStatDivP);
				
				operationStatDivP.addHtmlElement(new Text(messageSource.getMessage("report.generate.daily.report.operation.stat",
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
				*/
				
				// statystyki user agentow
				List<GenericTextStat> userAgentClientRawStat = mySQLConnector.getUserAgentClientStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
				// podzielenie statystyk na Desktop, Phone + Mobile, Tablet, Robot + Robot Mobile oraz inne
				SplitUserAgentStatByTypeResult splitUserAgentStatByType = splitUserAgentStatByType(userAgentClientRawStat);
				
				// generowanie statystyk dla aplikacji na androida
				generateStatForJapanaeseAndroidLearnHelper(reportDiv, splitUserAgentStatByType);
				
				// generowanie statystyk dla komputera (desktop)
				generateStatForDesktop(reportDiv, splitUserAgentStatByType);
				
				
				int fixme = 1;
				
				/////////////////////////
				
				// statystyki odnosnikow
				List<GenericTextStat> refererStat = mySQLConnector.getRefererStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId(), baseServer);
				
				appendGenericTextStat(reportDiv, "report.generate.daily.report.referer.stat", refererStat);				

				// statystyki nieznalezionych stron
				List<GenericTextStat> pageNotFoundStat = mySQLConnector.getPageNotFoundStat(dailyLogProcessedMinMaxIds.getMinId(), dailyLogProcessedMinMaxIds.getMaxId());
				
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
	
	private SplitUserAgentStatByTypeResult splitUserAgentStatByType(List<GenericTextStat> userAgentClientRawStat) {
		
		SplitUserAgentStatByTypeResult result = new SplitUserAgentStatByTypeResult();
		
		//
		
		for (GenericTextStat currentUserAgentClientStat : userAgentClientRawStat) {
			
			UserAgentInfo userAgentInfo = userAgentService.getUserAgentInfo(currentUserAgentClientStat.getText());
			
			switch (userAgentInfo.getType()) {
				
				case JAPANESE_ANDROID_LEARNER_HELPER:
					
					result.japaneseAndroidLearnerHelperList.add(new ImmutablePair<GenericTextStat, UserAgentInfo>(currentUserAgentClientStat, userAgentInfo));
					
					break;
					
				case DESKTOP:
					
					result.desktopList.add(new ImmutablePair<GenericTextStat, UserAgentInfo>(currentUserAgentClientStat, userAgentInfo));
					
					break;
					
				case PHONE:
					
					result.phoneList.add(new ImmutablePair<GenericTextStat, UserAgentInfo>(currentUserAgentClientStat, userAgentInfo));
					
					break;
					
				case TABLET:
					
					result.tableList.add(new ImmutablePair<GenericTextStat, UserAgentInfo>(currentUserAgentClientStat, userAgentInfo));
					
					break;
					
				case ROBOT:
					
					result.robotList.add(new ImmutablePair<GenericTextStat, UserAgentInfo>(currentUserAgentClientStat, userAgentInfo));
					
					break;
					
				case OTHER:
					
					result.otherList.add(new ImmutablePair<GenericTextStat, UserAgentInfo>(currentUserAgentClientStat, userAgentInfo));
					
					break;
					
				case NULL:
					
					result.nullList.add(new ImmutablePair<GenericTextStat, UserAgentInfo>(currentUserAgentClientStat, userAgentInfo));
					
					break;
			}
		}
		
		return result;
	}
	
	private void generateStatForJapanaeseAndroidLearnHelper(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
				
		List<ImmutablePair<GenericTextStat, UserAgentInfo>> japaneseAndroidLearnerHelperList = splitUserAgentStatByType.japaneseAndroidLearnerHelperList;
		
		//
		
		List<GenericTextStat> japaneseAndroidLearnerHelperListStat = groupByStat(japaneseAndroidLearnerHelperList, new IGroupByStatFunction() {
			
			@Override
			public String getKey(ImmutablePair<GenericTextStat, UserAgentInfo> pair) {				
				return pair.right.getJapaneseAndroidLearnerHelperInfo().getCode() + " - " + pair.right.getJapaneseAndroidLearnerHelperInfo().getCodeName();		
			}
		});
				
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.japanese.android.learn.helper.stat", japaneseAndroidLearnerHelperListStat);		
	}
	
	private void generateStatForDesktop(Div reportDiv, SplitUserAgentStatByTypeResult splitUserAgentStatByType) {
		
		List<ImmutablePair<GenericTextStat, UserAgentInfo>> desktopList = splitUserAgentStatByType.desktopList;
	
		//
		
		List<GenericTextStat> desktopTypeList = groupByStat(desktopList, new IGroupByStatFunction() {
			
			@Override
			public String getKey(ImmutablePair<GenericTextStat, UserAgentInfo> pair) {
				
				DesktopInfo desktopInfo = pair.right.getDesktopInfo();
				
				return desktopInfo.getDesktopType();
			}
		});

		List<GenericTextStat> operationSystemList = groupByStat(desktopList, new IGroupByStatFunction() {
			
			@Override
			public String getKey(ImmutablePair<GenericTextStat, UserAgentInfo> pair) {
				
				DesktopInfo desktopInfo = pair.right.getDesktopInfo();
				
				return desktopInfo.getOperationSystem();
			}
		});

		List<GenericTextStat> browserTypeList = groupByStat(desktopList, new IGroupByStatFunction() {
			
			@Override
			public String getKey(ImmutablePair<GenericTextStat, UserAgentInfo> pair) {
				
				DesktopInfo desktopInfo = pair.right.getDesktopInfo();
				
				return desktopInfo.getBrowserType();
			}
		});
						
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.desktopType.stat", desktopTypeList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.operationSystem.stat", operationSystemList);
		appendGenericTextStat(reportDiv, "report.generate.daily.report.user.agent.desktop.browserType.stat", browserTypeList);		
	}
	
	private List<GenericTextStat> groupByStat(List<ImmutablePair<GenericTextStat, UserAgentInfo>> list, IGroupByStatFunction groupByStatFunction) {
		
		Map<String, Long> resultMap = new TreeMap<String, Long>();
		
		//
		
		for (ImmutablePair<GenericTextStat, UserAgentInfo> currentPair : list) {
			
			String key = groupByStatFunction.getKey(currentPair);
			
			Long count = resultMap.get(key);
			
			if (count == null) {
				count = 0l;
			}
			
			count += currentPair.left.getStat();
			
			resultMap.put(key, count);
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
		
		private List<ImmutablePair<GenericTextStat, UserAgentInfo>> japaneseAndroidLearnerHelperList = new ArrayList<>();
		
		private List<ImmutablePair<GenericTextStat, UserAgentInfo>> desktopList = new ArrayList<>();
		
		private List<ImmutablePair<GenericTextStat, UserAgentInfo>> phoneList = new ArrayList<>();
		
		private List<ImmutablePair<GenericTextStat, UserAgentInfo>> tableList = new ArrayList<>();
		
		private List<ImmutablePair<GenericTextStat, UserAgentInfo>> robotList = new ArrayList<>();
		
		private List<ImmutablePair<GenericTextStat, UserAgentInfo>> otherList = new ArrayList<>();
		
		private List<ImmutablePair<GenericTextStat, UserAgentInfo>> nullList = new ArrayList<>();
	}
	
	private static interface IGroupByStatFunction {		
		public String getKey(ImmutablePair<GenericTextStat, UserAgentInfo> pair);
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
