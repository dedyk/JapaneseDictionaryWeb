package pl.idedyk.japanese.dictionary.web.dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;
import com.google.gson.Gson;

import jakarta.annotation.PostConstruct;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.DictionaryIndex;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.EntryIndex;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.SectionEntry;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.SectionEntryIndexEntry;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.SectionIndex;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.SectionIndexMetadata;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;

@Service
public class DirectoryIndexManager {
	
	private static final Logger logger = LogManager.getLogger(DirectoryIndexManager.class);

	private static final String MAIN_INDEX_FILE = "dictionaryindex.json";
	private static final String otherSectionName = "Inne";

	@Autowired
	private DictionaryManager dictionaryManager;
	
	private File directoryindexMainDir;
	private DictionaryIndex dictionaryIndex;

	@PostConstruct
	public void initialize() {
		
		// inicjalizacja indeksu
		logger.info("Inicjalizacja indeksu słownika");
		
		// pobranie glownego katalogu z baza danych
		String dbDir = dictionaryManager.getDbDir();

		// nazwa katalogu z zawartoscia indeksu
		directoryindexMainDir = new File(dbDir, MAIN_INDEX_FILE);
		
		if (directoryindexMainDir.isDirectory() == false) {
			logger.error("Błąd inicjalizacji indeksu słownika");
			
			throw new RuntimeException();
		}
		
		// wczytanie pliku glownego indeksu
		File directoryIndexMainFile = new File(directoryindexMainDir, "dictionaryindex.json");
		
		if (directoryIndexMainFile.canRead() == false) {
			logger.error("Błąd wczytanie głównego indeksu słownika");
			
			throw new RuntimeException();
		}
		
		// parsowanie pliku	
		dictionaryIndex = readDictionaryIndex(directoryIndexMainFile);
		
		logger.error("Zakończono inicjalizacje indeksu słownika");
	}
	
	private static DictionaryIndex readDictionaryIndex(File directoryIndexMainFile) {
		
		FileReader directoryIndexMainFileReader = null;
		
		try {
			Gson gson = new Gson();
			
			directoryIndexMainFileReader = new FileReader(directoryIndexMainFile);
			
			return gson.fromJson(directoryIndexMainFileReader, DictionaryIndex.class);
						
		} catch (Exception e) {
			logger.error("Błąd wczytanie głównego indeksu słownika", e);
			
			throw new RuntimeException();
			
		} finally {
			if (directoryIndexMainFileReader != null) {
				
				try {
					directoryIndexMainFileReader.close();
				} catch (IOException e) {
					// noop
				}
			}
		}	
	}
	
	public List<String> getSectionNamesList(IndexType indexType, IndexSectionType indexSectionType) {
		
		if (indexType == null || indexSectionType == null) {
			return null;
		}
		
		// pobieramy metadane sekcji
		List<SectionIndexMetadata> sectionIndexMetadataList = getSectionIndexMetadata(indexType, indexSectionType);
		
		if (sectionIndexMetadataList == null) {
			return null;
		}
		
		LinkedHashSet<String> resultList = new LinkedHashSet<>();
		
		SectionIndexMetadata otherSectionIndexMetadata = null;
		
		for (SectionIndexMetadata sectionIndexMetadata : sectionIndexMetadataList) {
			
			if (otherSectionName.equals(sectionIndexMetadata.getSectionName()) == true) { // sekcja inne jest na koncu (jezeli istnieje)
				otherSectionIndexMetadata = sectionIndexMetadata;
				continue;
			}
			
			resultList.add(sectionIndexMetadata.getSectionName());
		}
		
		if (otherSectionIndexMetadata != null) {
			resultList.add(otherSectionIndexMetadata.getSectionName());
		}
						
		return new ArrayList<>(resultList);
	}
	
	public List<Integer> getSectionNamePageList(IndexType indexType, IndexSectionType indexSectionType, String sectionName) {
		
		if (indexType == null || indexSectionType == null || sectionName == null) {
			return null;
		}
		
		// pobieramy metadane sekcji
		List<SectionIndexMetadata> sectionIndexMetadataList = getSectionIndexMetadata(indexType, indexSectionType);
		
		if (sectionIndexMetadataList == null) {
			return null;
		}
		
		List<Integer> resutList = new ArrayList<>();
		
		for (SectionIndexMetadata sectionIndexMetadata : sectionIndexMetadataList) {
			
			if (	sectionIndexMetadata.getSectionName().equals(sectionName) == true) { // mamy to
				resutList.add(sectionIndexMetadata.getPartNo());
			}
		}
		
		return resutList;
	}
	
	public SectionIndex getSectionNameEntries(IndexType indexType, IndexSectionType indexSectionType, String sectionName, int pageNo) {
		
		if (indexType == null || indexSectionType == null || sectionName == null) {
			return null;
		}
		
		// pobieramy metadane sekcji
		List<SectionIndexMetadata> sectionIndexMetadataList = getSectionIndexMetadata(indexType, indexSectionType);
		
		if (sectionIndexMetadataList == null) {
			return null;
		}

		// szukamy wlasciwej sekcji i strony
		SectionIndexMetadata sectionIndexMetadataToLoad = null;
		
		for (SectionIndexMetadata sectionIndexMetadata : sectionIndexMetadataList) {
			if (	sectionIndexMetadata.getSectionName().equals(sectionName) == true &&
					sectionIndexMetadata.getPartNo().intValue() == pageNo) { // mamy to
				
				sectionIndexMetadataToLoad = sectionIndexMetadata;
			}
		}
		
		if (sectionIndexMetadataToLoad == null) { // jezeli nie znaleslismy
			return null;
		}
		
		// wczytujemy dany plik
		File sectionIndexMetadataToLoadFile = new File(directoryindexMainDir, sectionIndexMetadataToLoad.getFileName());
		
		if (sectionIndexMetadataToLoadFile.canRead() == false) { // to nigdy nie powinny zdarzyc sie
			return null;
		}
		
		return readSectionIndex(sectionIndexMetadataToLoadFile);
	}
	
	private static SectionIndex readSectionIndex(File sectionIndexMetadataToLoadFile) {
		
		// mamy automatyczne zamkniecie strumyka
		try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(sectionIndexMetadataToLoadFile))) {
			
			Gson gson = new Gson();
			
			return gson.fromJson(inputStreamReader, SectionIndex.class);			
			
		} catch (Exception e) {
			throw new RuntimeException("Error during load section entries", e);		
		}
	}
	
	public IndexSectionType findIndexSectionType(String indexSectionTypeName) {
		
		for (IndexSectionType indexSectionType : IndexSectionType.values()) {
			
			if (indexSectionType.indexSectionTypeName.equals(indexSectionTypeName) == true) {
				return indexSectionType; 
			}			
		}
		
		return null;
	}
	
	private List<SectionIndexMetadata> getSectionIndexMetadata(IndexType indexType, IndexSectionType indexSectionType) {
		
		if (indexType == IndexType.entry) {
			
			switch (indexSectionType) {
				case japaneseIndexSection:
					return dictionaryIndex.getEntryIndex().getJapaneseIndexSectionIndex();
					
				case polishIndexSection:
					return dictionaryIndex.getEntryIndex().getPolishIndexSectionIndex();
					
					default:
						return null;
			}			
			
		} else if (indexType == IndexType.nameEntry) {
			
			switch (indexSectionType) {
				case japaneseIndexSection:
					return dictionaryIndex.getNameEntryIndex().getJapaneseIndexSectionIndex();
				
				
				default:
					return null;
			}
			
		} else if (indexType == IndexType.kanji) {
			
			switch (indexSectionType) {				
				case polishIndexSection:
					return dictionaryIndex.getKanjiCharacterInfoListIndex().getPolishIndexSectionIndex();
				
				default:
					return null;
			}
		}
		
		return null;
	}
	
	//
	
	public static enum IndexType {
		entry,
		nameEntry,
		kanji;
	}
	
	public static enum IndexSectionType {
				
		japaneseIndexSection("japaneseIndex"),
		polishIndexSection("polishIndex");
		
		private String indexSectionTypeName;
		
		IndexSectionType(String indexSectionTypeName) {
			this.indexSectionTypeName = indexSectionTypeName;
		}
	}
	
	//
	
	public void generateFromMain(DictionaryManager dictionaryManager, String baseServer, String sourcePath, String destinationPath) throws IOException {
		
		// glowny zrodlowy plik
		File mainSourceIndexFile = new File(sourcePath, MAIN_INDEX_FILE);
		File destinationIndexFile = new File(destinationPath, MAIN_INDEX_FILE);
		
		// glowny plik kopiujemy bez zmian		
		Files.copy(mainSourceIndexFile, destinationIndexFile);
		
		// wczytujemy plik indeksu
		DictionaryIndex dictionaryIndex = readDictionaryIndex(destinationIndexFile);
		
		// chodzimy po elementach i uzupelniamy o url
		EntryIndex entryIndex = dictionaryIndex.getEntryIndex();
		
		if (entryIndex != null) {
			
			Map<Integer, JMdict.Entry> cache = new TreeMap<>();
			
			for (SectionIndexMetadata sectionIndexMetadata : entryIndex.getJapaneseIndexSectionIndex()) {
				
				// wczytujemy plik z zawartoscia sekcji
				File sourceSectionIndexFile = new File(sourcePath, sectionIndexMetadata.getFileName());
				File destinationSectionIndexFile = new File(destinationPath, sectionIndexMetadata.getFileName());
				
				SectionIndex sectionIndex = readSectionIndex(sourceSectionIndexFile);
				
				// chodzimy po wszystkich elementach i uzupelniamy w nim url
				List<SectionEntry> sectionEntryList = sectionIndex.getSectionEntry();
				
				for (SectionEntry sectionEntry : sectionEntryList) {
					
					List<SectionEntryIndexEntry> sectionEntryEntries = sectionEntry.getEntries();
					
					for (SectionEntryIndexEntry sectionEntryIndexEntry : sectionEntryEntries) {
						
						// pobieramy slowko po entryId
						JMdict.Entry dictionaryEntry2 = cache.computeIfAbsent(sectionEntryIndexEntry.getEntryId(), (e) -> {
							try {
								return dictionaryManager.getDictionaryEntry2ById(e);
								
							} catch (DictionaryException e1) {
								throw new RuntimeException();
							}
						});
						
						if (dictionaryEntry2 != null) { // tutaj zawsze cos powinno byc
							System.out.println(dictionaryEntry2.getEntryId());
							
							// generujemy url-a
							String url = LinkGenerator.generateDictionaryEntryDetailsLink(baseServer, dictionaryEntry2);
							
							// ustawiamy go
							sectionEntryIndexEntry.setUrl(url);
						}
					}					
				}
				
				// zapisujemy zmodyfikowany plik w lokalizacji docelowej				
				Gson gson = new Gson();		
				Files.write(gson.toJson(sectionIndex).getBytes(), destinationSectionIndexFile);
			}
		}
	}
}
