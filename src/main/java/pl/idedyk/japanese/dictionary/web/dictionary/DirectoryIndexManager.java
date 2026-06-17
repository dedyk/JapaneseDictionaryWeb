package pl.idedyk.japanese.dictionary.web.dictionary;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.DictionaryIndex;

@Service
public class DirectoryIndexManager {
	
	private static final Logger logger = LogManager.getLogger(DirectoryIndexManager.class);

	@Autowired
	private DictionaryManager dictionaryManager;
	
	private File directoryindexMainDir;

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
		File directoryIndexMainFile = new File(directoryindexMainDir, "dictionaryindex.xml");
		
		if (directoryIndexMainFile.canRead() == false) {
			logger.error("Błąd wczytanie głównego indeksu słownika");
			
			throw new RuntimeException();
		}
		
		// parsowanie pliku	
		DictionaryIndex dictionaryIndex = null;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(DictionaryIndex.class);              		
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
			dictionaryIndex = (DictionaryIndex)jaxbUnmarshaller.unmarshal(directoryIndexMainFile);
			
		} catch (Exception e) {
			logger.error("Błąd wczytanie głównego indeksu słownika", e);
			
			throw new RuntimeException();			
		}
		
		int a = 0;
		a++;
		
	}
}
