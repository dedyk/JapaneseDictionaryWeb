package pl.idedyk.japanese.dictionary.web.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class BlacklistManager {
	
	private static final Logger logger = LogManager.getLogger(BlacklistManager.class);
	
	private final String blacklistsourceURL = "file:///tmp/a/ipsum/ipsum.txt";
	// private final String blacklistsourceURL = "https://raw.githubusercontent.com/stamparm/ipsum/master/ipsum.txt";
	
	//
	
	@Autowired
	private ConfigService configService;
	
	private Set<String> blacklist;
	
	@PostConstruct
	public void readBlacklist() throws IOException {		
		logger.info("Wczytawanie czarnej listy adresów ip");
		
		// nazwa pliku
		File currentBlackListFile = getCurrentBlackList();
		
		if (currentBlackListFile.canRead() == false) {
			logger.error("Nie udało się wczytać pliku z czarną listą adresów ip. Brak pliku lub brak uprawnień");
			
			return;
		}
		
		// wczytanie zawartosci pliku
		blacklist = readIpsumBlackListFile(currentBlackListFile);
		
		logger.info("Nowa czarna lista liczy " + blacklist.size() + " pozycji.");
	}
	
	public void downloadNewBlackList() {
		
		try {
			// przygotowanie nazwy plikow blacklist
			File currentBlackListFile = getCurrentBlackList();
			File oldBlackBlackListFile = getOldBlackList();
			File newBlackListFile = getNewBlackList();			
			
			// usuniecie nowego pliku (jesli istnieje)
			newBlackListFile.delete();
			
			// sciagniecie nowej zawartosci pliku blacklist
			FileUtils.copyURLToFile(new URL(blacklistsourceURL), newBlackListFile, 5000, 10000);
			
			// wczytanie nowego pliku blacklist
			Set<String> newBlacklist = readIpsumBlackListFile(newBlackListFile);
			
			if (newBlacklist.size() == 0) {
				logger.error("Nowy plik blacklist liczy zero pozycji");
				return;
			}

			// kasujemy stary plik
			oldBlackBlackListFile.delete();
			
			// zamiana aktualnego pliku na stary			
			currentBlackListFile.renameTo(oldBlackBlackListFile);
			
			// zamiana nowego pliku na aktualny
			newBlackListFile.renameTo(currentBlackListFile);
			
			// podmiana set-a, aby zaczela byc uzywana nowa
			blacklist = newBlacklist;
			
			logger.info("Nowa czarna lista liczy " + blacklist.size() + " pozycji.");
			
		} catch (Exception e) {
			logger.error("Błąd podczas aktualizacji czarnych list", e);
		}
	}
	
	private File getCurrentBlackList() {
		return new File(configService.getCatalinaConfDir(), "blacklist.txt");
	}
	
	private File getNewBlackList() {
		return new File(configService.getCatalinaConfDir(), "blacklist.txt.new");
	}

	private File getOldBlackList() {
		return new File(configService.getCatalinaConfDir(), "blacklist.txt.old");
	}

	private Set<String> readIpsumBlackListFile(File ipsumBlackListFile) throws IOException {
		
		Set<String> blacklist = new TreeSet<>();
		
		BufferedReader bufferedReader = null;
		
		try {
			bufferedReader = new BufferedReader(new FileReader(ipsumBlackListFile));
		    			
			String line;
	        while ((line = bufferedReader.readLine()) != null) {
	            line = line.trim();
	            
	            if (line.startsWith("#") == true) { // to nas nie interesuje
	            	continue;
	            }
	            
	            // wczytanie adresu ip i liczby czarnych list
	            String[] lineSplited = line.split("\t");
	            
	            if (lineSplited.length != 2) { // to jest cos dziwnego, ignorujemy
	            	continue;
	            }
	            
	            // sprawdzenie wyrazenia regularnego
	            if (lineSplited[0].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") == true) {
	            	blacklist.add(lineSplited[0]);	            	
	            }	            
	        }
	        
	        return blacklist;
	        
		} finally {
		    if (bufferedReader != null) {
		        try {
		        	bufferedReader.close();
		        	
		        } catch (IOException e) {
		        	logger.error("Błąd zamykania pliku", e);
		        }
		    }
		}
	}
}
