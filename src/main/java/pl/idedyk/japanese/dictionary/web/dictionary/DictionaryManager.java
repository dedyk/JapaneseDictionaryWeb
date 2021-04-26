package pl.idedyk.japanese.dictionary.web.dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.api.dictionary.DictionaryManagerAbstract;
import pl.idedyk.japanese.dictionary.api.dictionary.Utils;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPowerList;
import pl.idedyk.japanese.dictionary.api.dto.KanjivgEntry;
import pl.idedyk.japanese.dictionary.api.dto.RadicalInfo;
import pl.idedyk.japanese.dictionary.api.dto.TransitiveIntransitivePair;
import pl.idedyk.japanese.dictionary.api.dto.TransitiveIntransitivePairWithDictionaryEntry;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.api.keigo.KeigoHelper;
import pl.idedyk.japanese.dictionary.api.tools.KanaHelper;
import pl.idedyk.japanese.dictionary.lucene.LuceneDatabase;
import pl.idedyk.japanese.dictionary.lucene.LuceneDatabaseSuggesterAndSpellCheckerSource;
import pl.idedyk.japanese.dictionary.web.dictionary.dto.WebRadicalInfo;

import com.csvreader.CsvReader;

@Service
public class DictionaryManager extends DictionaryManagerAbstract {

	private static final Logger logger = Logger.getLogger(DictionaryManager.class);
	
	private static final String RADICAL_FILE = "radical.csv";
	private static final String TRANSITIVE_INTRANSTIVE_PAIRS_FILE = "transitive_intransitive_pairs.csv";
	private static final String KANA_FILE = "kana.csv";
	private static final String WORD_POWER_FILE = "word-power.csv";
	
	private static final String LUCENE_DB_DIR = "db-lucene";

	private LuceneDatabase luceneDatabase;
	
	private KanaHelper kanaHelper;
	private KeigoHelper keigoHelper;
	
	private List<RadicalInfo> radicalList = null;
	private List<TransitiveIntransitivePair> transitiveIntransitivePairsList = null;

	private WordPowerList wordPowerList;
	
	private boolean initialized = false;
	
	@Value("${db.dir}")
	private String dbDir;
	
	public DictionaryManager() {
		super();
	}
	
	public void initFromMain(String dbDir) {
		
		this.dbDir = dbDir; 
		
		init(false);
	}
	
	@PostConstruct
	public void initFromServer() {
		init(true);
	}

	private void init(boolean initSuggester) {
		
		initialized = false;
		
		logger.info("Inicjalizacja Dictionary Manager");
		
		logger.info("Otwieranie bazy danych: " + dbDir);
		
		try {
			databaseConnector = luceneDatabase = new LuceneDatabase(new File(dbDir, LUCENE_DB_DIR).getPath()); 
			
			luceneDatabase.open();
			
		} catch (IOException e) {
			
			logger.error("Otwieranie bazy danych zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		if (initSuggester == true) {
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					try {					
						// czekamy minute przed rozpoczeciem generowania
						// Thread.sleep(60 * 1000);
						
						logger.info("Inicjalizacja podpowiadacza");
						luceneDatabase.openSuggester();				
						logger.info("Zakończono inicjalizację podpowiadacza");
						
						//
						
						logger.info("Inicjalizacja poprawiacza słów");					
						luceneDatabase.openSpellChecker();					
						logger.info("Zakończono inicjalizację poprawiacza słów");
						
					} catch (Exception e) {
						
						logger.error("Błąd inicjalizacji podpowiadacza lub poprawiacza słów", e);
					}
				}
			}).start();	
		}
				
		logger.info("Inicjalizuje Keigo Helper");
		keigoHelper = new KeigoHelper();
		
		logger.info("Wczytywanie informacji o pisaniu znakow kana");
		
		try {
			InputStream kanaFileInputStream = new FileInputStream(new File(dbDir, KANA_FILE));

			readKanaFile(kanaFileInputStream);

		} catch (IOException e) {
			logger.error("Wczytywanie informacji o pisaniu znakow kana zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		logger.info("Wczytywanie informacji o znakach podstawowych");
		
		try {
			InputStream radicalInputStream = new FileInputStream(new File(dbDir, RADICAL_FILE));
			
			readRadicalEntriesFromCsv(radicalInputStream);

		} catch (Exception e) {
			logger.error("Wczytywanie informacji o znakach podstawowych zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		logger.info("Wczytywanie informacji o mocach słówek");
		
		try {
			InputStream wordPowerInputStream = new FileInputStream(new File(dbDir, WORD_POWER_FILE));
			
			readWordPowerFromCsv(wordPowerInputStream);

		} catch (Exception e) {
			logger.error("Wczytywanie informacji o mocach słówek zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		logger.info("Wczytywanie informacji o parach czasownikow przechodnich i nieprzechodnich");
		
		try {
			InputStream transitiveIntransitivePairsInputStream = new FileInputStream(new File(dbDir, TRANSITIVE_INTRANSTIVE_PAIRS_FILE));
			
			readTransitiveIntransitivePairsFromCsv(transitiveIntransitivePairsInputStream);

		} catch (IOException e) {
			logger.error("Wczytywanie informacji o parach czasownikow przechodnich i nieprzechodnich zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		initialized = true;
		
		logger.info("Inicjalizacja Dictionary Manager zakończona sukcesem");
	}
	
	@PreDestroy
	public void close() throws IOException {
		luceneDatabase.close();
	}
	
	@Override
	public void waitForDatabaseReady() {
		
		if (initialized == true) {
			return;
		}
		
		while (true) {
						
			try {
				Thread.sleep(300);
				
			} catch (InterruptedException e) {
				// noop
			}
			
			if (initialized == true) {
				return;
			}			
		}		
	}
	
	public void reload() {
		
		try {
			Thread.sleep(2000);
			
		} catch (InterruptedException e) {
			// noop
		}
				
		initialized = false;
				
		logger.info("Zablokowano bazę danych. Czekanie 8 sekund na rozpoczęcie procedury przeładowania bazy");
		
		try {
			Thread.sleep(8000);
			
			close();
			
		} catch (Exception e) {
			// noop
		}
				
		databaseConnector = null;
		luceneDatabase = null;
		
		kanaHelper = null;
		keigoHelper = null;
		
		radicalList = null;
		transitiveIntransitivePairsList = null;
		
		init(true);		
	}
	
	private void readKanaFile(InputStream kanaFileInputStream) throws IOException {

		CsvReader csvReader = new CsvReader(new InputStreamReader(kanaFileInputStream), ',');

		Map<String, List<KanjivgEntry>> kanaAndStrokePaths = new HashMap<String, List<KanjivgEntry>>();

		while (csvReader.readRecord()) {

			//int id = Integer.parseInt(csvReader.get(0));

			String kana = csvReader.get(1);

			String strokePath1String = csvReader.get(2);

			String strokePath2String = csvReader.get(3);

			List<KanjivgEntry> strokePaths = new ArrayList<KanjivgEntry>();

			strokePaths.add(new KanjivgEntry(Utils.parseStringIntoList(strokePath1String, false)));

			if (strokePath2String == null || strokePath2String.equals("") == false) {
				strokePaths.add(new KanjivgEntry(Utils.parseStringIntoList(strokePath2String, false)));
			}

			kanaAndStrokePaths.put(kana, strokePaths);
		}

		kanaHelper = new KanaHelper(kanaAndStrokePaths);

		csvReader.close();
	}
	
	private void readRadicalEntriesFromCsv(InputStream radicalInputStream) throws IOException, DictionaryException {
		
		radicalList = new ArrayList<RadicalInfo>();

		CsvReader csvReader = new CsvReader(new InputStreamReader(radicalInputStream), ',');

		while (csvReader.readRecord()) {

			int id = Integer.parseInt(csvReader.get(0));

			String radical = csvReader.get(1);

			if (radical.equals("") == true) {
				throw new DictionaryException("Pusty znak podstawowy: " + radical);
			}

			String strokeCountString = csvReader.get(2);

			int strokeCount = Integer.parseInt(strokeCountString);

			RadicalInfo entry = new RadicalInfo();

			entry.setId(id);
			entry.setRadical(radical);
			entry.setStrokeCount(strokeCount);

			radicalList.add(entry);
		}

		csvReader.close();
	}
	
	private void readTransitiveIntransitivePairsFromCsv(InputStream transitiveIntransitivePairsInputStream) throws IOException {

		CsvReader csvReader = new CsvReader(new InputStreamReader(transitiveIntransitivePairsInputStream), ',');
		
		transitiveIntransitivePairsList = new ArrayList<TransitiveIntransitivePair>();

		while (csvReader.readRecord()) {

			Integer transitiveId = Integer.valueOf(csvReader.get(0));
			Integer intransitiveId = Integer.valueOf(csvReader.get(1));

			TransitiveIntransitivePair transitiveIntransitivePair = new TransitiveIntransitivePair();

			transitiveIntransitivePair.setTransitiveId(transitiveId);
			transitiveIntransitivePair.setIntransitiveId(intransitiveId);

			transitiveIntransitivePairsList.add(transitiveIntransitivePair);
		}

		csvReader.close();
	}
	
	private void readWordPowerFromCsv(InputStream wordPowerInputStream) throws IOException {
		
		CsvReader wordPowerInputStreamCsvReader = null;
		
		try {
			wordPowerInputStreamCsvReader = new CsvReader(new InputStreamReader(wordPowerInputStream), ',');	
			
			wordPowerList = new WordPowerList();
			
			while (wordPowerInputStreamCsvReader.readRecord()) {
				
				int columnCount = wordPowerInputStreamCsvReader.getColumnCount();
				
				int power = Integer.parseInt(wordPowerInputStreamCsvReader.get(0));
				
				for (int columnNo = 1; columnNo < columnCount; ++columnNo) {
					
					int currentDictionaryEntryIdx = Integer.parseInt(wordPowerInputStreamCsvReader.get(columnNo));
					
					wordPowerList.addPower(power, currentDictionaryEntryIdx);
				}
			}
			
		} finally {
			
			if (wordPowerInputStreamCsvReader != null) {
				wordPowerInputStreamCsvReader.close();
			}
		}	
	}

	@Override
	public KanaHelper getKanaHelper() {
		
		waitForDatabaseReady();
		
		return kanaHelper;
	}

	@Override
	public List<RadicalInfo> getRadicalList() {
		
		waitForDatabaseReady();
		
		return radicalList;
	}
	
	public List<WebRadicalInfo> getWebRadicalList() {
		
		waitForDatabaseReady();
		
		List<RadicalInfo> radicalList = getRadicalList();
		
		List<WebRadicalInfo> result = new ArrayList<WebRadicalInfo>();
		
		for (RadicalInfo radicalInfo : radicalList) {
			
			WebRadicalInfo webRadicalInfo = new WebRadicalInfo();
			
			String radical = radicalInfo.getRadical();
			
			webRadicalInfo.setId(radicalInfo.getId());
			webRadicalInfo.setRadical(radical);
			webRadicalInfo.setStrokeCount(radicalInfo.getStrokeCount());			
			webRadicalInfo.setImage(getRadicalImage(radical));
		
			result.add(webRadicalInfo);			
		}
		
		return result;
	}
	
	private String getRadicalImage(String radical) {
		
		if (radical.equals("𠆢") == true) { // znak U+201A2 (daszek)
			return "img/radical/dash.png";
			
		} else if (radical.equals("⺌") == true) { // trzy kreski
			return "img/radical/small.png";
			
		} else {
			
			return null;
		}
	}
	
	public WebRadicalInfo getWebRadicalInfo(String radicalToSearch) {
		
		waitForDatabaseReady();
		
		List<RadicalInfo> radicalList = getRadicalList();
		
		for (RadicalInfo currentRadicalInfo : radicalList) {
			
			String currentRadicalInfoRadical = currentRadicalInfo.getRadical();
			
			if (currentRadicalInfoRadical.equals(radicalToSearch) == true) {
				
				WebRadicalInfo webRadicalInfo = new WebRadicalInfo();
				
				webRadicalInfo.setId(currentRadicalInfo.getId());
				webRadicalInfo.setRadical(currentRadicalInfoRadical);
				webRadicalInfo.setStrokeCount(currentRadicalInfo.getStrokeCount());			
				webRadicalInfo.setImage(getRadicalImage(currentRadicalInfoRadical));
				
				return webRadicalInfo;
			}
		}
		
		return null;
	}

	@Override
	public KeigoHelper getKeigoHelper() {
		
		waitForDatabaseReady();
		
		return keigoHelper;
	}

	@Override
	public List<TransitiveIntransitivePairWithDictionaryEntry> getTransitiveIntransitivePairsList() throws DictionaryException {
		
		waitForDatabaseReady();
		
		List<TransitiveIntransitivePairWithDictionaryEntry> result = new ArrayList<>();
		
		for (TransitiveIntransitivePair currentTransitiveIntransitivePair : transitiveIntransitivePairsList) {
			
			Integer transitiveId = currentTransitiveIntransitivePair.getTransitiveId();
			Integer intransitiveId = currentTransitiveIntransitivePair.getIntransitiveId();
			
			//
			
			TransitiveIntransitivePairWithDictionaryEntry newTransitiveIntransitivePairWithDictionaryEntry = new TransitiveIntransitivePairWithDictionaryEntry();
			
			newTransitiveIntransitivePairWithDictionaryEntry.setTransitiveId(transitiveId);
			newTransitiveIntransitivePairWithDictionaryEntry.setTransitiveDictionaryEntry(getDictionaryEntryById(transitiveId));
			
			newTransitiveIntransitivePairWithDictionaryEntry.setIntransitiveId(intransitiveId);
			newTransitiveIntransitivePairWithDictionaryEntry.setIntransitiveDictionaryEntry(getDictionaryEntryById(intransitiveId));
			
			//
			
			result.add(newTransitiveIntransitivePairWithDictionaryEntry);
		}
		
		return result;
	}

	public boolean isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource source) {
		
		waitForDatabaseReady();
		
		return luceneDatabase.isAutocompleteInitialized(source);
	}
	
	public List<String> getAutocomplete(LuceneDatabaseSuggesterAndSpellCheckerSource source, String term, int limit) throws DictionaryException {
		
		waitForDatabaseReady();
		
		return luceneDatabase.getAutocomplete(source, term, limit);
	}
		
	public boolean isSpellCheckerInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource source) {
		
		waitForDatabaseReady();
		
		return luceneDatabase.isSpellCheckerInitialized(source);
	}
	
	public List<String> getSpellCheckerSuggestion(LuceneDatabaseSuggesterAndSpellCheckerSource source, String term, int limit) throws DictionaryException {
		
		waitForDatabaseReady();
		
		return luceneDatabase.getSpellCheckerSuggestion(source, term, limit);
	}
	
	public FindWordResult findDictionaryEntriesForRemoteDatabaseConnector(FindWordRequest findWordRequest) throws DictionaryException {
		
		waitForDatabaseReady();
		
		return luceneDatabase.findDictionaryEntries(findWordRequest);
	}
	
	public File getPdfDictionary() {		
		return new File(dbDir, "dictionary.pdf");		
	}

	@Override
	public WordPowerList getWordPowerList() throws DictionaryException {
		return wordPowerList;
	}
	
	public String getDbDir() {
		return dbDir;
	}
}
