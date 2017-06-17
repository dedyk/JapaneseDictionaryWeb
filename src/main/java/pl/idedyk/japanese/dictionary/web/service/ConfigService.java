package pl.idedyk.japanese.dictionary.web.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
	
	private static final Logger logger = Logger.getLogger(ConfigService.class);
	
	public boolean isStopAllSchedulers() {
		
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
	
	private File getCatalinaConfDir() {
		
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
}
