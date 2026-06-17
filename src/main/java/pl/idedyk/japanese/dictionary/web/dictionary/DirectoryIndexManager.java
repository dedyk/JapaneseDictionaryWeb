package pl.idedyk.japanese.dictionary.web.dictionary;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import jakarta.annotation.PostConstruct;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.DictionaryIndex;

@Service
public class DirectoryIndexManager {
	
	private static final Logger logger = LogManager.getLogger(DirectoryIndexManager.class);

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
		directoryindexMainDir = new File(dbDir, "directoryindex");
		
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
		dictionaryIndex = null;
		FileReader directoryIndexMainFileReader = null;
		
		try {
			Gson gson = new Gson();
			
			directoryIndexMainFileReader = new FileReader(directoryIndexMainFile);
			
			dictionaryIndex = gson.fromJson(directoryIndexMainFileReader, DictionaryIndex.class);
						
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
		
		logger.error("Zakończono inicjalizacje indeksu słownika");
	}
}
