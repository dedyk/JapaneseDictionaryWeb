package pl.idedyk.japanese.dictionary.web.sitemap;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

public class SitemapGenerator {
	
	private static final String DB = "db";
	
	private static final Logger logger = LogManager.getLogger(SitemapGenerator.class);

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
		
		// wczytanie mapy z lista dat zmodyfikowanych stron
		Map<String, Date> lastmodMap = generateLastmod();
		
		// tworzenie manadzera sitemap
		SitemapManager sitemapManager = new SitemapManager();
		
		sitemapManager.setBaseServer(configProperties.getProperty("base.server"));
		
		sitemapManager.generateFromMain(dictionaryManager, destDirFile.getAbsolutePath(), lastmodMap);

		// zamykamy baze danych
		dictionaryManager.close();
	}
		
	private static Map<String, Date> generateLastmod() throws DictionaryException{
		
		Git git = null;
		
		try {
			// repozytorium git-a
			Repository repository = new FileRepositoryBuilder()
					.setWorkTree(new File("."))
	                .readEnvironment()
	                .findGitDir()
	                .build();
			
			// otwieramy repozytorium git-a
			git = new Git(repository);
			
			// otwieramy katalog db i z plikow wczytujemy z niego daty
			File dbDir = new File(git.getRepository().getDirectory(), "../" + DB);
			
			// mapa z wynikami, kluczem bedzie nazwa klasy i identyfikator wpisu
			Map<String, Date> results = new TreeMap<String, Date>();
			
			File[] dbListFiles = dbDir.listFiles();
			
			for (File currentDbFile : dbListFiles) {
				
				if (currentDbFile.getName().startsWith("word2.xml") == true) { // obsluga slowa
					generateLastmodForFile(results, "JMdict.Entry", git, currentDbFile, "<entry>", "</entry>", "(^<ent_seq>)(\\d*)(<\\/ent_seq>$)");
					
				} else if (currentDbFile.getName().startsWith("name2.xml") == true) { // obsluga slowa ze slownika nazw
					generateLastmodForFile(results, "JMnedict.Entry", git, currentDbFile, "<entry>", "</entry>", "(^<ent_seq>)(\\d*)(<\\/ent_seq>$)");
					
				} else if (currentDbFile.getName().startsWith("kanji2.xml") == true) { // obsluga slownika kanji
					generateLastmodForFile(results, "KanjiCharacterInfo", git, currentDbFile, "<character>", "</character>", "(^<id>)(\\d*)(<\\/id>$)");
				}
			}
			
			return results;
		
		} catch (Exception e) {
			throw new DictionaryException(e);
			
		} finally {
			// zamykamy repozytorium
			if (git != null) {
				git.close();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void generateLastmodForFile(Map<String, Date> results, String keyPrefix, 
			Git git, File file,
			final String entryStartTag, String entryEndTag, String entryIdRegex) throws GitAPIException {
		
		logger.info("Git blame: " + file.getName());
		
		// robimy blame na pliku i sprobujemy ustalic kiedy dany wpis ulegl zmianie
        BlameResult blameResult = git.blame()
                .setFilePath(DB + "/" + file.getName())
                .call();

        // ile linii ma dany plik
        int lineCount = blameResult.getResultContents().size();
        
        // maszyna stanow
        enum StateMachine {
        	WAITING_FOR_ENTRY_START,
        	ENTRY_CONTENT;        	
        }
        
        StateMachine stateMachine = StateMachine.WAITING_FOR_ENTRY_START;
        
        Integer entryId = null;
        Date newestDate = null;
        
        // wyrazenie regularne do ustalania identyfikatora wpisu
        Pattern entryIdRegexPattern = Pattern.compile(entryIdRegex);
        
        // chodzimy po calym pliku
        for (int lineNo  = 0; lineNo < lineCount; lineNo++) {
        	
        	// pobieramy linie;
        	String lineContent = blameResult.getResultContents().getString(lineNo).trim();
        	
        	// i commit z ktorej data linijka pochodzi
        	RevCommit commit = blameResult.getSourceCommit(lineNo);
        	
        	if (stateMachine == StateMachine.WAITING_FOR_ENTRY_START && lineContent.equals(entryStartTag) == true) { // rozpoczecie nowego wpisu        		
        		stateMachine = StateMachine.ENTRY_CONTENT;
        		
        	} else if (stateMachine == StateMachine.ENTRY_CONTENT && lineContent.equals(entryEndTag) == true) { // zakonczenie wpisu        	
        		
        		// dodajemy wpis do wyniku
        		String key = keyPrefix + "_" + entryId;
        		
        		results.put(key, newestDate);
        		
        		logger.info(key + ": " + newestDate);
        		
        		// resetujemy maszyne stanu
        		stateMachine = StateMachine.WAITING_FOR_ENTRY_START;        
        		entryId = null;
        		newestDate = null;
        		
        	} else if (stateMachine == StateMachine.ENTRY_CONTENT) {
        		
            	// proba pobrania identyfikatora wpisu
            	Matcher matcher = entryIdRegexPattern.matcher(lineContent);
            	
            	if (matcher.matches() == true) { // mamy identyfikator wpisu
            		entryId = Integer.parseInt(matcher.group(2));        		
            	}
            	
            	// pobranie daty linijki
            	Date lineWhen = commit.getAuthorIdent().getWhen();
            	
            	if (newestDate == null || newestDate.before(lineWhen) == true) { // ta linijka jest nowsza
            		newestDate = lineWhen;
            	}        		
        	}
        }
	}
}
