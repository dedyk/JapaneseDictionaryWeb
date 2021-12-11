package pl.idedyk.japanese.dictionary.web.sitemap;

import java.io.File;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

public class SitemapGenerator {

	public static void main(String[] args) throws Exception {
		
		// inicjalizacja log4j
		Configurator.initialize(new DefaultConfiguration());
	    Configurator.setRootLevel(Level.INFO);
		
		// inicjalizacja bazy danych
		DictionaryManager dictionaryManager = new DictionaryManager();
		
		dictionaryManager.initFromMain(args[0]);
		
		// utworzenie katalogu docelowego
		File destDirFile = new File(args[0], "sitemap");
		
		if (destDirFile.isDirectory() == false) {
			destDirFile.mkdirs();
		}
		
		// wczytanie pliku properties
		Properties configProperties = new Properties();
		
		configProperties.load(SitemapGenerator.class.getResourceAsStream("/config/config.properties"));
		
		// tworzenie manadzera sitemap
		SitemapManager sitemapManager = new SitemapManager();
		
		sitemapManager.setBaseServer(configProperties.getProperty("base.server"));
		
		sitemapManager.generateFromMain(dictionaryManager, destDirFile.getAbsolutePath());

		// zamykamy baze danych
		dictionaryManager.close();
	}

}
