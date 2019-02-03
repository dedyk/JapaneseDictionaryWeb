package pl.idedyk.japanese.dictionary.web.sitemap;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.sitemap.exception.NotInitializedException;

public class SitemapManager {
	
	private static final Logger logger = Logger.getLogger(SitemapManager.class);
		
	private String baseServer;
	
	private File sitemapFileIndex = null;
	
	private Map<String, Map<Integer, File>> sitemapFilesMap = Collections.synchronizedMap(new LinkedHashMap<String, Map<Integer, File>>());
						
	@Autowired
	private DictionaryManager dictionaryManager;
	
	//@Value("${sitemap.lastmod}")
	//private String lastMod;
	
	private boolean initialized = false;
	
	public SitemapManager() throws Exception {
		
		logger.info("Inicjalizacja manadzera sitemap");		
	}
	
	@PostConstruct
	public void cacheSitemap() {
		
		logger.info("Cache'owanie sitemap");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {				
					generateSitemaps();
				
				} catch (Exception e) {					
					logger.error("Bład generowania pliku sitemap", e);
					
					throw new RuntimeException(e);
				}
			}
		}).start();
	}	
	
	public void reload() {
		
		initialized = false;
		
		sitemapFilesMap.clear();
		sitemapFileIndex = null;
		
		cacheSitemap();
	}
	
	private synchronized void generateSitemaps() throws Exception {
				
		if (initialized == true) {
			return;
		}
		
		// czekamy 15 minut przed rozpoczeciem generowania
		Thread.sleep(15 * 60 * 1000);
		
		logger.info("Generowanie pliku sitemap");
				
		SitemapHelper sitemapHelper = new SitemapHelper();
		
		// dodanie statycznych linkow
		
		sitemapHelper.createUrl("main", "", ChangeFreqEnum.weekly, BigDecimal.valueOf(1.0));
		sitemapHelper.createUrl("main", "/wordDictionary", ChangeFreqEnum.weekly, BigDecimal.valueOf(1.0));
		sitemapHelper.createUrl("main", "/wordDictionary/dictionary.pdf", ChangeFreqEnum.weekly, BigDecimal.valueOf(1.0));
		sitemapHelper.createUrl("main", "/kanjiDictionary", ChangeFreqEnum.weekly, BigDecimal.valueOf(1.0));
		sitemapHelper.createUrl("main", "/suggestion", ChangeFreqEnum.weekly, BigDecimal.valueOf(0.4));
		sitemapHelper.createUrl("main", "/info", ChangeFreqEnum.weekly, BigDecimal.valueOf(0.4));
				
		// pobranie ilosci slow
		int dictionaryEntriesSize = dictionaryManager.getDictionaryEntriesSize();
		
		for (int currentDictionaryEntryIdx = 1; currentDictionaryEntryIdx <= dictionaryEntriesSize; ++currentDictionaryEntryIdx) {
			
			// pobranie slowka
			DictionaryEntry currentDictionaryEntry = dictionaryManager.getDictionaryEntryById(currentDictionaryEntryIdx);
			
			createWordDictionaryLink("wordDictionaryDetails", sitemapHelper, currentDictionaryEntry);
		}
		
		// katalog slow
		final int wordPageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w WordDictionaryController
		
		int dictionaryEntriesSizeLastPageNo = (dictionaryEntriesSize / wordPageSize) + (dictionaryEntriesSize % wordPageSize > 0 ? 1 : 0);
		
		for (int pageNo = 1; pageNo <= dictionaryEntriesSizeLastPageNo; ++pageNo) {
			
			String url = "/wordDictionaryCatalog/" + pageNo;
			
			sitemapHelper.createUrl("wordDictionaryCatalog", url, ChangeFreqEnum.monthly, BigDecimal.valueOf(0.1));
		}
		
		// pobranie ilosci slow (nazwa)
		int dictionaryEntriesNameSize = dictionaryManager.getDictionaryEntriesNameSize();
		
		for (int currentDictionaryEntryNameIdx = 1; currentDictionaryEntryNameIdx <= dictionaryEntriesNameSize; ++currentDictionaryEntryNameIdx) {
			
			// pobranie slowka
			DictionaryEntry currentDictionaryEntry = dictionaryManager.getDictionaryEntryNameById(currentDictionaryEntryNameIdx);
			
			createWordDictionaryLink("wordNameDictionaryDetails", sitemapHelper, currentDictionaryEntry);
		}

		// katalog slow(nazwa)
		final int wordNamePageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w WordDictionaryController
		
		int dictionaryEntriesNameSizeLastPageNo = (dictionaryEntriesNameSize / wordNamePageSize) + (dictionaryEntriesNameSize % wordNamePageSize > 0 ? 1 : 0);
		
		for (int pageNo = 1; pageNo <= dictionaryEntriesNameSizeLastPageNo; ++pageNo) {
			
			String url = "/wordDictionaryNameCatalog/" + pageNo;
			
			sitemapHelper.createUrl("wordDictionaryNameCatalog", url, ChangeFreqEnum.monthly, BigDecimal.valueOf(0.1));
		}
		
		// pobranie znakow kanji
		List<KanjiEntry> allKanjis = dictionaryManager.getAllKanjis(false, false);
		
		for (KanjiEntry kanjiEntry : allKanjis) {
			
			// wygenerowanie linku
			String link = LinkGenerator.generateKanjiDetailsLink("", kanjiEntry);
			
			// dodanie linku			
			sitemapHelper.createUrl("kanjiDetails", link, ChangeFreqEnum.weekly, BigDecimal.valueOf(0.6));
		}
		
		// katalog znakow kanji
		final int kanjiPageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w KanjiDictionaryController
		
		int allKanjisSizeLastPageNo = (allKanjis.size() / kanjiPageSize) + (allKanjis.size() % kanjiPageSize > 0 ? 1 : 0);
		
		for (int pageNo = 1; pageNo <= allKanjisSizeLastPageNo; ++pageNo) {
			
			String url = "/kanjiDictionaryCatalog/" + pageNo;
						
			sitemapHelper.createUrl("kanjiDictionaryCatalog", url, ChangeFreqEnum.monthly, BigDecimal.valueOf(0.1));
		}
		
		// zakonczenie generowania
		sitemapHelper.end();
		
		// generowanie indeksu
		File indexSitemap = new File(System.getProperty("java.io.tmpdir"), "japaneseDictionaryWeb_sitemap_index.xml");
		indexSitemap.deleteOnExit();
		
		sitemapFileIndex = indexSitemap;
		
		// utworzenie zapisywacza
		FileWriter sitemapFileWriter = new FileWriter(indexSitemap);
		
		// utworzenie zapisywacza xml'i
		XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(sitemapFileWriter);
		
		// zapis naglowka
		xmlStreamWriter.writeStartDocument("UTF-8", "1.0");

		// utworzenie glownego elementu
		xmlStreamWriter.writeStartElement("sitemapindex");
		xmlStreamWriter.writeDefaultNamespace("http://www.sitemaps.org/schemas/sitemap/0.9");				

		Set<String> sitemapFilesMapKeySet = sitemapFilesMap.keySet();
		
		Iterator<String> sitemapFilesMapKeySetIterator = sitemapFilesMapKeySet.iterator();
		
		while (sitemapFilesMapKeySetIterator.hasNext() == true) {
			
			String currentName = sitemapFilesMapKeySetIterator.next();
			
			Map<Integer, File> sitemapNameFileMap = sitemapFilesMap.get(currentName);
			
			Set<Integer> sitemapNameFileMapKeySet = sitemapNameFileMap.keySet();
			
			Iterator<Integer> sitemapNameFileMapKeySetIterator = sitemapNameFileMapKeySet.iterator();
			
			while (sitemapNameFileMapKeySetIterator.hasNext() == true) {
				
				Integer currentIndex = sitemapNameFileMapKeySetIterator.next();
				
				xmlStreamWriter.writeStartElement("sitemap"); // sitemap
				
				xmlStreamWriter.writeStartElement("loc");		
				xmlStreamWriter.writeCharacters(baseServer + "/sitemap/" + currentName + "/" + currentIndex);			
				xmlStreamWriter.writeEndElement(); // loc

				/*
				xmlStreamWriter.writeStartElement("lastmod");		
				xmlStreamWriter.writeCharacters(lastMod);			
				xmlStreamWriter.writeEndElement(); // lastmod			
				*/
				
				xmlStreamWriter.writeEndElement(); // sitemap				
			}			
		}		
				
		xmlStreamWriter.writeEndElement(); // sitemapindex		
		xmlStreamWriter.writeEndDocument();
		
		// zamkniecie
		xmlStreamWriter.flush();
		xmlStreamWriter.close();
		
		sitemapFileWriter.close();
		
		// koniec generowania indeksu
		
		initialized = true;
				
		logger.info("Generowanie pliku sitemap zakonczone");
	}
	
	private void createWordDictionaryLink(String groupName, SitemapHelper sitemapHelper, DictionaryEntry currentDictionaryEntry) throws Exception {
		
		// wygenerowanie linku standardowego
		String link = LinkGenerator.generateDictionaryEntryDetailsLink("", currentDictionaryEntry, null);
		
		// dodanie linku			
		sitemapHelper.createUrl(groupName, link, ChangeFreqEnum.weekly, BigDecimal.valueOf(currentDictionaryEntry.isName() == false ? 0.8 : 0.6));
		
		// pobranie listy typow
		List<DictionaryEntryType> dictionaryEntryTypeList = currentDictionaryEntry.getDictionaryEntryTypeList();
		
		if (dictionaryEntryTypeList != null && currentDictionaryEntry.isName() == false) {
			
			int addableDictionaryEntryTypeInfoCounter = 0;

			for (DictionaryEntryType currentDictionaryEntryType : dictionaryEntryTypeList) {

				boolean addableDictionaryEntryTypeInfo = DictionaryEntryType.isAddableDictionaryEntryTypeInfo(currentDictionaryEntryType);

				if (addableDictionaryEntryTypeInfo == true) {
					addableDictionaryEntryTypeInfoCounter++;
				}
			}
			
			if (addableDictionaryEntryTypeInfoCounter > 1) { // jesli wiecej niz jeden
				
				for (DictionaryEntryType currentDictionaryEntryType : dictionaryEntryTypeList) {

					boolean addableDictionaryEntryTypeInfo = DictionaryEntryType.isAddableDictionaryEntryTypeInfo(currentDictionaryEntryType);

					if (addableDictionaryEntryTypeInfo == true) {
						
						// wygenerowanie linku z typem
						String linkWithType = LinkGenerator.generateDictionaryEntryDetailsLink("", currentDictionaryEntry, currentDictionaryEntryType);
						
						// dodanie linku z typem
						sitemapHelper.createUrl(groupName, linkWithType, ChangeFreqEnum.weekly, BigDecimal.valueOf(currentDictionaryEntry.isName() == false ? 0.7 : 0.5));							
					}
				}					
			}				
		}		
	}
	
	public File getIndexSitemap() throws NotInitializedException {
		
		if (initialized == false) {
			throw new NotInitializedException();
		}
		
		return sitemapFileIndex;
	}
	
	public File getSitemap(String name, int id) throws NotInitializedException {
				
		if (name == null) {
			return null;
		}
		
		Map<Integer, File> sitemapNameFileMap = sitemapFilesMap.get(name);

		if (sitemapNameFileMap == null) {
			
			if (initialized == false) {
				throw new NotInitializedException();
				
			} else {
				return null;
			}			
		}
		
		File sitemapFile = sitemapNameFileMap.get(id);
		
		if (sitemapFile == null) {
			
			if (initialized == false) {
				throw new NotInitializedException();
				
			} else {
				return null;
			}			

		}
		
		return sitemapFile;
	}

	public String getBaseServer() {
		return baseServer;
	}

	public void setBaseServer(String baseServer) {
		this.baseServer = baseServer;
	}
	
	public class SitemapHelper {
		
		private File currentSitemapFile = null;

		private String currentGroupName = null;
		
		private FileWriter sitemapFileWriter = null;
		private XMLStreamWriter xmlStreamWriter = null;
		
		private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
		
		private int counter = 0;
		
		public void createUrl(String groupName, String link, ChangeFreqEnum changeFreq, BigDecimal priority) throws Exception {
			
			if (currentSitemapFile == null) {
				
				start(groupName);
				
			} else if (currentGroupName.endsWith(groupName) == false) {
				
				end();
				
				start(groupName);				
			}
			
			xmlStreamWriter.writeStartElement("url"); // url
			
			addElement("loc", baseServer + link);
			//addElement("lastmod", lastMod);
			addElement("changefreq", changeFreq.toString());
			addElement("priority", priority.toPlainString());			
			
			xmlStreamWriter.writeEndElement(); // url
			
			counter++;
			
			if (counter >= 30000) {
				end();
			}
		}
		
		private void addElement(String name, String value) throws Exception {
			
			xmlStreamWriter.writeStartElement(name);			
			xmlStreamWriter.writeCharacters(value);			
			xmlStreamWriter.writeEndElement();
		}
		
		public void start(String groupName) throws Exception {
			
			currentGroupName = groupName;
			
			Map<Integer, File> groupNameSitemapFiles = sitemapFilesMap.get(currentGroupName);
			
			if (groupNameSitemapFiles == null) {
				groupNameSitemapFiles = Collections.synchronizedMap(new LinkedHashMap<Integer, File>());
				
				sitemapFilesMap.put(currentGroupName, groupNameSitemapFiles);
			}			
			
			// utworzenie pliku z sitemap
			currentSitemapFile = new File(System.getProperty("java.io.tmpdir"), "japaneseDictionaryWeb_sitemap_" + currentGroupName + "_" + (groupNameSitemapFiles.size() + 1) + ".xml");		
			currentSitemapFile.deleteOnExit();
			
			// utworzenie zapisywacza
			sitemapFileWriter = new FileWriter(currentSitemapFile);
			
			// utworzenie zapisywacza xml'i
			xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(sitemapFileWriter);
			
			// zapis naglowka
			xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
			
			// utworzenie glownego elementu
			xmlStreamWriter.writeStartElement("urlset");
			xmlStreamWriter.writeDefaultNamespace("http://www.sitemaps.org/schemas/sitemap/0.9");						
		}
		
		public void end() throws Exception {
			
			if (currentSitemapFile != null) {
				
				xmlStreamWriter.writeEndElement(); // urlset
				
				xmlStreamWriter.writeEndDocument();
				
				// zamkniecie
				xmlStreamWriter.flush();
				xmlStreamWriter.close();
				
				sitemapFileWriter.close();
				
				Map<Integer, File> groupNameSitemapFiles = sitemapFilesMap.get(currentGroupName);
				
				int groupNameSitemapFilesSize = groupNameSitemapFiles.size();
				
				groupNameSitemapFiles.put(groupNameSitemapFilesSize + 1, currentSitemapFile);
				
				// czyszczenie
				currentSitemapFile = null;
				sitemapFileWriter = null;
				xmlStreamWriter = null;
				
				currentGroupName = null;
				
				counter = 0;				
			}			
		}		
	}
	
	public static enum ChangeFreqEnum {
		always,		
		hourly,
		daily,
		weekly,
		monthly,
		yearly,
		never;
	}
}
