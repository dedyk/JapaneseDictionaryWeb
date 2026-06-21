package pl.idedyk.japanese.dictionary.web.dictionary;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

public class DirectoryIndexManagerGenerator {

	public static void main(String[] args) throws IOException {
		
		// inicjalizacja log4j
		Configurator.initialize(new DefaultConfiguration());
	    Configurator.setRootLevel(Level.INFO);
		
		// inicjalizacja bazy danych
		DictionaryManager dictionaryManager = new DictionaryManager();
		
		dictionaryManager.initFromMain(args[0]);
		
		// utworzenie katalogu docelowego
		File destDirFile = new File(args[0], "directoryindex");
		
		if (destDirFile.isDirectory() == false) {
			destDirFile.mkdirs();
		}
		
		// tworzenie manadzera sitemap
		DirectoryIndexManager directoryIndexManager = new DirectoryIndexManager();
				
		directoryIndexManager.generateFromMain(dictionaryManager, "db/directoryindex", destDirFile.getAbsolutePath());
		
		// zamykamy baze danych
		dictionaryManager.close();
	}
}
