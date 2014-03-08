package pl.idedyk.japanese.dictionary.web.dictionary;

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
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.api.dictionary.DictionaryManagerAbstract;
import pl.idedyk.japanese.dictionary.api.dictionary.Utils;
import pl.idedyk.japanese.dictionary.api.dto.KanjivgEntry;
import pl.idedyk.japanese.dictionary.api.dto.RadicalInfo;
import pl.idedyk.japanese.dictionary.api.dto.TransitiveIntransitivePair;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.api.keigo.KeigoHelper;
import pl.idedyk.japanese.dictionary.api.tools.KanaHelper;
import pl.idedyk.japanese.dictionary.web.dictionary.lucene.LuceneDatabase;

import com.csvreader.CsvReader;

@Service
public class DictionaryManager extends DictionaryManagerAbstract {

	private static final Logger logger = Logger.getLogger(DictionaryManager.class);
	
	private static final String RADICAL_FILE = "radical.csv";
	private static final String TRANSITIVE_INTRANSTIVE_PAIRS_FILE = "transitive_intransitive_pairs.csv";
	private static final String KANA_FILE = "kana.csv";
	
	private static final String LUCENE_DB_DIR = "db-lucene";

	private LuceneDatabase luceneDatabase;
	
	private KanaHelper kanaHelper;
	private KeigoHelper keigoHelper;
	
	private List<RadicalInfo> radicalList = null;
	private List<TransitiveIntransitivePair> transitiveIntransitivePairsList = null;
	
	public DictionaryManager() {
		super();
	}

	@PostConstruct
	public void init() {
		
		logger.info("Inicjalizacja Dictionary Manager");
		
		logger.info("Otwieranie bazy danych");
		
		try {
			databaseConnector = luceneDatabase = new LuceneDatabase(DictionaryManager.class.getResource("/db/" + LUCENE_DB_DIR + "/").getPath()); 
			
			luceneDatabase.open();
			
		} catch (IOException e) {
			
			logger.error("Otwieranie bazy danych zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		logger.info("Inicjalizuje Keigo Helper");
		keigoHelper = new KeigoHelper();
		
		logger.info("Wczytywanie informacji o pisaniu znakow kana");
		
		try {
			InputStream kanaFileInputStream = DictionaryManager.class.getResourceAsStream("/db/" + KANA_FILE);

			readKanaFile(kanaFileInputStream);

		} catch (IOException e) {
			logger.error("Wczytywanie informacji o pisaniu znakow kana zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		logger.info("Wczytywanie informacji o znakach podstawowych");
		
		try {
			InputStream radicalInputStream = DictionaryManager.class.getResourceAsStream("/db/" + RADICAL_FILE);
			
			readRadicalEntriesFromCsv(radicalInputStream);

		} catch (Exception e) {
			logger.error("Wczytywanie informacji o znakach podstawowych zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		logger.info("Wczytywanie informacji o parach czasownikow przechodnich i nieprzechodnich");
		
		try {
			InputStream transitiveIntransitivePairsInputStream = DictionaryManager.class.getResourceAsStream("/db/" + TRANSITIVE_INTRANSTIVE_PAIRS_FILE);
			
			readTransitiveIntransitivePairsFromCsv(transitiveIntransitivePairsInputStream);

		} catch (IOException e) {
			logger.error("Wczytywanie informacji o parach czasownikow przechodnich i nieprzechodnich zakonczylo sie bledem", e);

			throw new RuntimeException(e);
		}
		
		logger.info("Inicjalizacja Dictionary Manager zakonczona sukcesem");
	}
	
	@PreDestroy
	private void close() throws IOException {
		luceneDatabase.close();
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

	@Override
	public KanaHelper getKanaHelper() {
		return kanaHelper;
	}

	@Override
	public List<RadicalInfo> getRadicalList() {
		return radicalList;
	}

	@Override
	public KeigoHelper getKeigoHelper() {
		return keigoHelper;
	}

	@Override
	public List<TransitiveIntransitivePair> getTransitiveIntransitivePairsList() {
		return transitiveIntransitivePairsList;
	}

	public List<String> getWordAutocomplete(String term, int limit) throws DictionaryException {
		
		return luceneDatabase.getWordAutocomplete(term, limit);
	}
}
