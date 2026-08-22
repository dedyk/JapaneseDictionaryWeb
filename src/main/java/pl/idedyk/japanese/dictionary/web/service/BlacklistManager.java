package pl.idedyk.japanese.dictionary.web.service;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlacklistManager {
	
	private static final Logger logger = LogManager.getLogger(BlacklistManager.class);
	
	private final String blacklistsourceURL = "file:///tmp/a/ipsum/ipsum.txt";
	// private final String blacklistsourceURL = "https://raw.githubusercontent.com/stamparm/ipsum/master/ipsum.txt";
	
	//
	
	@Autowired
	private ConfigService configService;
	
	public void downloadNewBlackList() {
		
		try {
			// przygotowanie nazwy plikow blacklist
			File currentBlackListFile = getCurrentBlackList();
			File newBlackListFile = getNewBlackList();			
			
			// usuniecie nowego pliku (jesli istnieje)
			newBlackListFile.delete();
			
			// sciagniecie nowej zawartosci pliku blacklist
			FileUtils.copyURLToFile(new URL(blacklistsourceURL), newBlackListFile, 5000, 10000);
			
			
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
}
