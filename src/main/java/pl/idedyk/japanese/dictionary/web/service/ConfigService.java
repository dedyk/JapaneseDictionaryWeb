package pl.idedyk.japanese.dictionary.web.service;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config;

@Service
public class ConfigService {
	
	private static final Logger logger = LogManager.getLogger(ConfigService.class);
	
	private boolean temporaryStopProcessing = false;
	
	// konfiguracja
	private Config config;
	
	private File configFile = null;
	private Long configLastModified = null;
	
	public ConfigWrapper getConfig() {
		checkAndReloadConfigFile();
		
		return new ConfigWrapper(config, configLastModified);
	}
	
	private synchronized Config checkAndReloadConfigFile() {
		
		if (configFile == null) {
			configFile = new File(getCatalinaConfDirStatic(), "configService.config.xml");
		}
		
		// nie ma pliku lub nie mozna go przeczytac
		if (configFile.exists() == false || configFile.canRead() == false) {			
			configLastModified = null;
			
			return config;
		}
		
		// plik nie zmienil sie
		if (configLastModified != null && configLastModified.longValue() == configFile.lastModified()) {
			return config;
		}
		
		// probujemy wczytac plik
		try {
			logger.info("Wczytywanie pliku: " + configFile);
			
			// walidacja xsd
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			
			Schema schema = factory.newSchema(Config.class.getResource("/xsd/config.xsd"));
			Validator validator = schema.newValidator();
						
			validator.validate(new StreamSource(configFile));			

			// wczytanie pliku
			JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
			Config newConfig = (Config)jaxbUnmarshaller.unmarshal(configFile);
			
			// zastapienie starej konfiguracji nowa
			config = newConfig;
			
			configLastModified = configFile.lastModified();
						
		} catch (Exception e) {			
			logger.error("Błąd podczas wczytywania pliku: " + configFile, e);
			
			configLastModified = null;
			
			return config;	
		}
		
		return config;
	}	
		
	private boolean isStopAllSchedulers() {
		
		// pobranie konfiguracji
		Config config = checkAndReloadConfigFile();
		
		// czy zatrzymac wszystkie schedulery
		return config.getSchedulers().isStopAllSchedulers();
		
		/*
		// stary kod
		// pobranie katalogu z konfiguracja
		File catalinaConfDir = getCatalinaConfDir();
		
		if (catalinaConfDir == null) {
			return true;
		}
		
		// sprawdzamy, czy plik configService.isStopAllSchedulers wystepuje
		File isStopAllSchedulersFile = new File(catalinaConfDir, "configService.isStopAllSchedulers");
		
		if (isStopAllSchedulersFile.exists() == true) { // zatrzymujemy wszystkie schedulery
			return true;
			
		} else {
			return false;
			
		}
		*/
	}
	
	public File getCatalinaConfDir() {
		return getCatalinaConfDirStatic();
	}
	
	public static File getCatalinaConfDirStatic() {
		
		String catalinaBase = System.getProperty("catalina.base");
		
		if (catalinaBase == null) {
			logger.error("Nie ustawiona zmienna catalina.base!");
			
			return null;
		}
		
		//
		
		File catalinaConfDir = new File(catalinaBase, "conf");
		
		if (catalinaConfDir.exists() == false || catalinaConfDir.isDirectory() == false) {
			logger.error("Dziwna zmienna catalina.base! Brak w niej katalogu conf!");
			
			return null;
		}
		
		return catalinaConfDir;
	}
	
	public void enableTemporaryStopProcessing() {
		temporaryStopProcessing = true;
	}
	
	public void disableTemporaryStopProcessing() {
		temporaryStopProcessing = false;
	}

	public boolean isGenerateDailyReport() {
		return isStopAllSchedulers() == false && temporaryStopProcessing == false;
	}
	
	public boolean isProcessLogQueueItem() {
		return isStopAllSchedulers() == false && temporaryStopProcessing == false;
	}

	public boolean isProcessLocalDirQueueItems() {
		return isStopAllSchedulers() == false && temporaryStopProcessing == false;
	}
	
	public boolean isDeleteDatabaseOldQueueItems() {
		return isStopAllSchedulers() == false && temporaryStopProcessing == false;
	}
	
	public boolean isDeleteLocalDirArchiveOldQueueItems() {
		return isStopAllSchedulers() == false && temporaryStopProcessing == false;
	}
	
	public boolean isProcessDBCleanup() {
		return isStopAllSchedulers() == false;
	}
	
	public static class ConfigWrapper {
		private Config config;
		private Long lastModified;
		
		public ConfigWrapper(Config config, Long lastModified) {
			this.config = config;
			this.lastModified = lastModified;
		}
		
		public Config getConfig() {
			return config;
		}
		
		public void setConfig(Config config) {
			this.config = config;
		}
		
		public Long getLastModified() {
			return lastModified;
		}
		
		public void setLastModified(Long lastModified) {
			this.lastModified = lastModified;
		}
	}
}
