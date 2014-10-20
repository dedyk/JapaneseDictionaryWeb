package pl.idedyk.japanese.dictionary.web.sitemap;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.sitemap.model.ObjectFactory;
import pl.idedyk.japanese.dictionary.web.sitemap.model.TChangeFreq;
import pl.idedyk.japanese.dictionary.web.sitemap.model.TUrl;
import pl.idedyk.japanese.dictionary.web.sitemap.model.Urlset;

public class SitemapManager {
	
	private static final Logger logger = Logger.getLogger(SitemapManager.class);
	
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	
	private String baseServer;
	
	private File sitemapFile;
		
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Value("${sitemap.lastmod}")
	private String lastMod;
	
	public SitemapManager() throws Exception {
		
		logger.info("Inicjalizacja manadzera sitemap");
		
		jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		
		marshaller = jaxbContext.createMarshaller();
		marshaller.setEventHandler(new DefaultValidationEventHandler());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	}
	
	@PostConstruct
	public void cacheSitemap() {
		
		logger.info("Cache'owanie sitemap");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {				
					generateSitemap();
				
				} catch (Exception e) {					
					logger.error("Bład generowania pliku sitemap", e);
					
					throw new RuntimeException(e);
				}
			}
		}).start();
	}	
	
	private synchronized void generateSitemap() throws Exception {
		
		if (sitemapFile != null && sitemapFile.canRead() == true) {
			return;
		}
		
		logger.info("Generowanie pliku sitemap");
		
		// utworzenie fabryczki
		ObjectFactory objectFactory = new ObjectFactory();
		
		// utworzenie glownego elementu szablonu
		Urlset sitemapUrlset = objectFactory.createUrlset();
		
		// pobranie listy url'i
		List<TUrl> urlList = sitemapUrlset.getUrl();
		
		// dodanie statycznych linkow
		
		urlList.add(createUrl(objectFactory, "", TChangeFreq.WEEKLY, BigDecimal.valueOf(1.0)));
		urlList.add(createUrl(objectFactory, "/wordDictionary", TChangeFreq.WEEKLY, BigDecimal.valueOf(1.0)));
		urlList.add(createUrl(objectFactory, "/kanjiDictionary", TChangeFreq.WEEKLY, BigDecimal.valueOf(1.0)));
		urlList.add(createUrl(objectFactory, "/suggestion", TChangeFreq.WEEKLY, BigDecimal.valueOf(0.4)));
		urlList.add(createUrl(objectFactory, "/info", TChangeFreq.WEEKLY, BigDecimal.valueOf(0.4)));
				
		// pobranie ilosci slow
		int dictionaryEntriesSize = dictionaryManager.getDictionaryEntriesSize();
		
		for (int currentDictionaryEntryIdx = 1; currentDictionaryEntryIdx <= dictionaryEntriesSize; ++currentDictionaryEntryIdx) {
			
			// pobranie slowka
			DictionaryEntry currentDictionaryEntry = dictionaryManager.getDictionaryEntryById(currentDictionaryEntryIdx);
			
			// wygenerowanie linku standardowego
			String link = LinkGenerator.generateDictionaryEntryDetailsLink("", currentDictionaryEntry, null);
			
			// dodanie linku			
			urlList.add(createUrl(objectFactory, link, TChangeFreq.WEEKLY, BigDecimal.valueOf(0.8)));
			
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
							urlList.add(createUrl(objectFactory, linkWithType, TChangeFreq.WEEKLY, BigDecimal.valueOf(0.7)));							
						}
					}					
				}				
			}
		}
		
		// katalog slow
		final int wordPageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w WordDictionaryController
		
		for (int pageNo = 0; pageNo <= dictionaryEntriesSize / wordPageSize; ++pageNo) {
			
			String url = "/wordDictionaryCatalog/" + (pageNo + 1);
			
			urlList.add(createUrl(objectFactory, url, TChangeFreq.MONTHLY, BigDecimal.valueOf(0.1)));
		}
		
		// pobranie znakow kanji
		List<KanjiEntry> allKanjis = dictionaryManager.getAllKanjis(false, true, false);
		
		for (KanjiEntry kanjiEntry : allKanjis) {
			
			// wygenerowanie linku
			String link = LinkGenerator.generateKanjiDetailsLink("", kanjiEntry);
			
			// dodanie linku			
			urlList.add(createUrl(objectFactory, link, TChangeFreq.WEEKLY, BigDecimal.valueOf(0.6)));		
		}
		
		// katalog znakow kanji
		final int kanjiPageSize = 50; // zmiana tego parametru wiaze sie ze zmiana w KanjiDictionaryController
		
		for (int pageNo = 0; pageNo <= allKanjis.size() / kanjiPageSize; ++pageNo) {
			
			String url = "/kanjiDictionaryCatalog/" + (pageNo + 1);
						
			urlList.add(createUrl(objectFactory, url, TChangeFreq.MONTHLY, BigDecimal.valueOf(0.1)));
		}
		
		sitemapFile = File.createTempFile("japaneseDictionaryWeb_sitemap", "");		
		sitemapFile.deleteOnExit();
		
		marshaller.marshal(sitemapUrlset, sitemapFile);
		
		logger.info("Generowanie pliku sitemap zakonczone");
	}
	
	private TUrl createUrl(ObjectFactory objectFactory, String link, TChangeFreq changeFreq, BigDecimal priority) {
		
		TUrl url = objectFactory.createTUrl();
		
		url.setLoc(baseServer + link);
		url.setLastmod(lastMod);
		url.setChangefreq(changeFreq);
		url.setPriority(priority);
		
		return url;
	}
	
	public File getSitemap() throws Exception {
		
		// wygenerowanie pliku sitemap jesli nie zostal wygenerowany wczesniej
		generateSitemap();

		return sitemapFile;		
	}

	public String getBaseServer() {
		return baseServer;
	}

	public void setBaseServer(String baseServer) {
		this.baseServer = baseServer;
	}
}
