package pl.idedyk.japanese.dictionary.web.sitemap;

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.springframework.beans.factory.annotation.Autowired;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.sitemap.model.ObjectFactory;
import pl.idedyk.japanese.dictionary.web.sitemap.model.TChangeFreq;
import pl.idedyk.japanese.dictionary.web.sitemap.model.TUrl;
import pl.idedyk.japanese.dictionary.web.sitemap.model.Urlset;

public class SitemapManager {
	
	private JAXBContext jaxbContext;
	
	private Marshaller marshaller;
	
	private String baseServer;
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	public SitemapManager() throws Exception {
		
		jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		
		marshaller = jaxbContext.createMarshaller();
		marshaller.setEventHandler(new DefaultValidationEventHandler());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);		
	}
	
	public String generateSitemap(String contextPath) {
		
		// utworzenie fabryczki
		ObjectFactory objectFactory = new ObjectFactory();
		
		// utworzenie glownego elementu
		Urlset urlSet = objectFactory.createUrlset();
		
		// pobranie listy url'i
		List<TUrl> urlList = urlSet.getUrl();
		
		// pobranie ilosci slow
		int dictionaryEntriesSize = dictionaryManager.getDictionaryEntriesSize();
		
		for (int currentDictionaryEntryIdx = 1; currentDictionaryEntryIdx <= dictionaryEntriesSize; ++currentDictionaryEntryIdx) {
			
			// pobranie slowka
			DictionaryEntry currentDictionaryEntry = dictionaryManager.getDictionaryEntryById(currentDictionaryEntryIdx);
			
			// wygenerowanie linku
			String link = LinkGenerator.generateDictionaryEntryDetailsLink(baseServer + contextPath, currentDictionaryEntry, null);
			
			// dodanie linku
			TUrl url = objectFactory.createTUrl();
			
			url.setLoc(link);
			url.setChangefreq(TChangeFreq.WEEKLY);
			
			urlList.add(url);
		}
				
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
