package pl.idedyk.japanese.dictionary.web.sitemap;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

public class SitemapManager {
	
	private static final Logger logger = Logger.getLogger(SitemapManager.class);
		
	private String baseServer;
	
	private List<File> sitemapFiles;
		
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Value("${sitemap.lastmod}")
	private String lastMod;
	
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
	
	private synchronized void generateSitemaps() throws Exception {
		
		if (sitemapFiles != null) {
			return;
		}
		
		logger.info("Generowanie pliku sitemap");
		
		sitemapFiles = new ArrayList<File>();
		
		SitemapHelper sitemapHelper = new SitemapHelper();
		
		// dodanie statycznych linkow
		
		sitemapHelper.createUrl("", ChangeFreqEnum.weekly, BigDecimal.valueOf(1.0));
		sitemapHelper.createUrl("/wordDictionary", ChangeFreqEnum.weekly, BigDecimal.valueOf(1.0));
		sitemapHelper.createUrl("/kanjiDictionary", ChangeFreqEnum.weekly, BigDecimal.valueOf(1.0));
		sitemapHelper.createUrl("/suggestion", ChangeFreqEnum.weekly, BigDecimal.valueOf(0.4));
		sitemapHelper.createUrl("/info", ChangeFreqEnum.weekly, BigDecimal.valueOf(0.4));
				
		// pobranie ilosci slow
		int dictionaryEntriesSize = dictionaryManager.getDictionaryEntriesSize();
		
		for (int currentDictionaryEntryIdx = 1; currentDictionaryEntryIdx <= dictionaryEntriesSize; ++currentDictionaryEntryIdx) {
			
			// pobranie slowka
			DictionaryEntry currentDictionaryEntry = dictionaryManager.getDictionaryEntryById(currentDictionaryEntryIdx);
			
			createWordDictionaryLink(sitemapHelper, currentDictionaryEntry);
		}
		
		// pobranie ilosci slow (nazwa)
		int dictionaryEntriesNameSize = dictionaryManager.getDictionaryEntriesNameSize();
		
		for (int currentDictionaryEntryNameIdx = 1; currentDictionaryEntryNameIdx <= dictionaryEntriesNameSize; ++currentDictionaryEntryNameIdx) {
			
			// pobranie slowka
			DictionaryEntry currentDictionaryEntry = dictionaryManager.getDictionaryEntryNameById(currentDictionaryEntryNameIdx);
			
			createWordDictionaryLink(sitemapHelper, currentDictionaryEntry);
		}
		
		// katalog slow
		final int wordPageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w WordDictionaryController
		
		for (int pageNo = 0; pageNo <= dictionaryEntriesSize / wordPageSize; ++pageNo) {
			
			String url = "/wordDictionaryCatalog/" + (pageNo + 1);
			
			sitemapHelper.createUrl(url, ChangeFreqEnum.monthly, BigDecimal.valueOf(0.1));
		}

		// katalog slow(nazwa
		final int wordNamePageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w WordDictionaryController
		
		for (int pageNo = 0; pageNo <= dictionaryEntriesNameSize / wordNamePageSize; ++pageNo) {
			
			String url = "/wordDictionaryNameCatalog/" + (pageNo + 1);
			
			sitemapHelper.createUrl(url, ChangeFreqEnum.monthly, BigDecimal.valueOf(0.1));
		}
		
		// pobranie znakow kanji
		List<KanjiEntry> allKanjis = dictionaryManager.getAllKanjis(false, true, false);
		
		for (KanjiEntry kanjiEntry : allKanjis) {
			
			// wygenerowanie linku
			String link = LinkGenerator.generateKanjiDetailsLink("", kanjiEntry);
			
			// dodanie linku			
			sitemapHelper.createUrl(link, ChangeFreqEnum.weekly, BigDecimal.valueOf(0.6));
		}
		
		// katalog znakow kanji
		final int kanjiPageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w KanjiDictionaryController
		
		for (int pageNo = 0; pageNo <= allKanjis.size() / kanjiPageSize; ++pageNo) {
			
			String url = "/kanjiDictionaryCatalog/" + (pageNo + 1);
						
			sitemapHelper.createUrl(url, ChangeFreqEnum.monthly, BigDecimal.valueOf(0.1));
		}
		
		// zakonczenie generowania
		sitemapHelper.end();
		
		// generowanie indeksu
		File indexSitemap = File.createTempFile("japaneseDictionaryWeb_sitemap", "");		
		indexSitemap.deleteOnExit();
		
		sitemapFiles.add(0, indexSitemap);
		
		// utworzenie zapisywacza
		FileWriter sitemapFileWriter = new FileWriter(indexSitemap);
		
		// utworzenie zapisywacza xml'i
		XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(sitemapFileWriter);
		
		// zapis naglowka
		xmlStreamWriter.writeStartDocument("UTF-8", "1.0");

		// utworzenie glownego elementu
		xmlStreamWriter.writeStartElement("sitemapindex");
		xmlStreamWriter.writeDefaultNamespace("http://www.sitemaps.org/schemas/sitemap/0.9");				

		for (int idx = 1; idx < sitemapFiles.size(); ++idx) {
			
			xmlStreamWriter.writeStartElement("sitemap"); // sitemap
			
			xmlStreamWriter.writeStartElement("loc");		
			xmlStreamWriter.writeCharacters(baseServer + "/sitemap/" + idx);			
			xmlStreamWriter.writeEndElement(); // loc			
			
			xmlStreamWriter.writeEndElement(); // sitemap			
		}		
		
		xmlStreamWriter.writeEndElement(); // sitemapindex		
		xmlStreamWriter.writeEndDocument();
		
		// zamkniecie
		xmlStreamWriter.flush();
		xmlStreamWriter.close();
		
		sitemapFileWriter.close();
		
		// koniec generowania indeksu
				
		logger.info("Generowanie pliku sitemap zakonczone");
	}
	
	private void createWordDictionaryLink(SitemapHelper sitemapHelper, DictionaryEntry currentDictionaryEntry) throws Exception {
		
		// wygenerowanie linku standardowego
		String link = LinkGenerator.generateDictionaryEntryDetailsLink("", currentDictionaryEntry, null);
		
		// dodanie linku			
		sitemapHelper.createUrl(link, ChangeFreqEnum.weekly, BigDecimal.valueOf(currentDictionaryEntry.isName() == false ? 0.8 : 0.6));
		
		// pobranie listy typow
		List<DictionaryEntryType> dictionaryEntryTypeList = currentDictionaryEntry.getDictionaryEntryTypeList();
		
		if (dictionaryEntryTypeList != null) {
			
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
						sitemapHelper.createUrl(linkWithType, ChangeFreqEnum.weekly, BigDecimal.valueOf(currentDictionaryEntry.isName() == false ? 0.7 : 0.5));							
					}
				}					
			}				
		}		
	}
	
	public File getIndexSitemap() throws Exception {
		
		// wygenerowanie pliku sitemap jesli nie zostal wygenerowany wczesniej
		generateSitemaps();

		return sitemapFiles.get(0);
	}
	
	public File getSitemap(int id) throws Exception {
		
		// wygenerowanie pliku sitemap jesli nie zostal wygenerowany wczesniej
		generateSitemaps();

		if (id < 1 || id >= sitemapFiles.size()) {
			return null;
		}
		
		return sitemapFiles.get(id);
	}	

	public String getBaseServer() {
		return baseServer;
	}

	public void setBaseServer(String baseServer) {
		this.baseServer = baseServer;
	}
	
	public class SitemapHelper {
		
		private File currentSitemapFile = null;

		private FileWriter sitemapFileWriter = null;
		private XMLStreamWriter xmlStreamWriter = null;
		
		private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
		
		private int counter = 0;
		
		public void createUrl(String link, ChangeFreqEnum changeFreq, BigDecimal priority) throws Exception {
			
			if (currentSitemapFile == null) {
				
				// utworzenie pliku z sitemap
				currentSitemapFile = File.createTempFile("japaneseDictionaryWeb_sitemap", "");		
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
			
			xmlStreamWriter.writeStartElement("url"); // url
			
			addElement("loc", baseServer + link);
			addElement("lastmod", lastMod);
			addElement("changefreq", changeFreq.toString());
			addElement("priority", priority.toPlainString());			
			
			xmlStreamWriter.writeEndElement(); // url
			
			counter++;
			
			if (counter >= 20000) {
				end();
			}
		}
		
		private void addElement(String name, String value) throws Exception {
			
			xmlStreamWriter.writeStartElement(name);			
			xmlStreamWriter.writeCharacters(value);			
			xmlStreamWriter.writeEndElement();
		}
		
		public void end() throws Exception {
			
			if (currentSitemapFile != null) {
				
				xmlStreamWriter.writeEndElement(); // urlset
				
				xmlStreamWriter.writeEndDocument();
				
				// zamkniecie
				xmlStreamWriter.flush();
				xmlStreamWriter.close();
				
				sitemapFileWriter.close();
				
				sitemapFiles.add(currentSitemapFile);

				// czyszczenie
				currentSitemapFile = null;
				sitemapFileWriter = null;
				xmlStreamWriter = null;
				
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
