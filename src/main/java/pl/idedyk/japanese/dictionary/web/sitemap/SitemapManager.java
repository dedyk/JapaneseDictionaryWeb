package pl.idedyk.japanese.dictionary.web.sitemap;

import java.io.StringWriter;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
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
	
	private Urlset templateSitemap;
	
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
	
	private synchronized void generateSitemap() {
		
		if (templateSitemap != null) {
			return;
		}
		
		logger.info("Generowanie pliku sitemap");
		
		// utworzenie fabryczki
		ObjectFactory objectFactory = new ObjectFactory();
		
		// utworzenie glownego elementu szablonu
		templateSitemap = objectFactory.createUrlset();
		
		// pobranie listy url'i
		List<TUrl> urlList = templateSitemap.getUrl();
		
		// dodanie statycznych linkow
		
		urlList.add(createUrl(objectFactory, "", TChangeFreq.WEEKLY));
		urlList.add(createUrl(objectFactory, "/wordDictionary", TChangeFreq.WEEKLY));
		urlList.add(createUrl(objectFactory, "/kanjiDictionary", TChangeFreq.WEEKLY));
		urlList.add(createUrl(objectFactory, "/suggestion", TChangeFreq.WEEKLY));
		urlList.add(createUrl(objectFactory, "/info", TChangeFreq.WEEKLY));
				
		// pobranie ilosci slow
		int dictionaryEntriesSize = dictionaryManager.getDictionaryEntriesSize();
		
		for (int currentDictionaryEntryIdx = 1; currentDictionaryEntryIdx <= dictionaryEntriesSize; ++currentDictionaryEntryIdx) {
			
			// pobranie slowka
			DictionaryEntry currentDictionaryEntry = dictionaryManager.getDictionaryEntryById(currentDictionaryEntryIdx);
			
			// wygenerowanie linku
			String link = LinkGenerator.generateDictionaryEntryDetailsLink("", currentDictionaryEntry, null);
			
			// dodanie linku			
			urlList.add(createUrl(objectFactory, link, TChangeFreq.WEEKLY));
		}
		
		// pobranie znakow kanji
		List<KanjiEntry> allKanjis = dictionaryManager.getAllKanjis(false, true);
		
		for (KanjiEntry kanjiEntry : allKanjis) {
			
			// wygenerowanie linku
			String link = LinkGenerator.generateKanjiDetailsLink("", kanjiEntry);
			
			// dodanie linku			
			urlList.add(createUrl(objectFactory, link, TChangeFreq.WEEKLY));			
		}
		
		logger.info("Generowanie pliku sitemap zakonczone");
	}
	
	private TUrl createUrl(ObjectFactory objectFactory, String link, TChangeFreq changeFreq) {
		
		TUrl url = objectFactory.createTUrl();
		
		url.setLoc(link);
		url.setLastmod(lastMod);
		url.setChangefreq(changeFreq);
		
		return url;
	}
	
	public Urlset getSitemap(String contextPath) {
		
		// wygenerowanie pliku sitemap jesli nie zostal wygenerowany wczesniej
		generateSitemap();
		
		// utworzenie fabryczki
		ObjectFactory objectFactory = new ObjectFactory();
		
		// utworzenie glownego elementu
		Urlset urlSet = objectFactory.createUrlset();
		
		for (TUrl templateUrl : templateSitemap.getUrl()) {
			
			TUrl newUrl = objectFactory.createTUrl();
			
			newUrl.setLoc(baseServer + contextPath + templateUrl.getLoc());
			newUrl.setChangefreq(templateUrl.getChangefreq());
			newUrl.setLastmod(templateUrl.getLastmod());
			newUrl.setPriority(templateUrl.getPriority());
			
			urlSet.getUrl().add(newUrl);			
		}
		
		return urlSet;
	}
	
	public String getSitemapAsString(String contextPath) {
		
		Urlset urlSet = getSitemap(contextPath);
		
		StringWriter stringWriter = new StringWriter();
		
		try {
			marshaller.marshal(urlSet, stringWriter);
			
			return stringWriter.toString();
			
		} catch (JAXBException e) {
			throw new RuntimeException("Błąd wygenerowanie pliku sitemap", e);
		}		
	}

	public String getBaseServer() {
		return baseServer;
	}

	public void setBaseServer(String baseServer) {
		this.baseServer = baseServer;
	}
}
