package pl.idedyk.japanese.dictionary.web.service;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
	
	private static final Logger logger = LogManager.getLogger(ConfigService.class);
	
	private boolean temporaryStopProcessing = false;
		
	private boolean isStopAllSchedulers() {
		
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
}
