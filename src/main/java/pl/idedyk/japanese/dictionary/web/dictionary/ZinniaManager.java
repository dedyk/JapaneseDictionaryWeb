package pl.idedyk.japanese.dictionary.web.dictionary;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.xdump.android.zinnia.Zinnia;

import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;

@Service
public class ZinniaManager {
	
	private static final Logger logger = Logger.getLogger(ZinniaManager.class);
	
	private String zinniaArch;
	
	private String libzinnia;
	private String libzinniajni;
	
	private Zinnia zinnia = new Zinnia();
	
	private long zinniaHandler = 0;

	@PostConstruct
	public void init() {

		logger.info("Inicjalizacja Zinnia Manager");
		
		String libZinniaPath = ZinniaManager.class.getResource("/zinnia/" + zinniaArch + "/" + libzinnia).getPath();
		String libZinniaJniPath = ZinniaManager.class.getResource("/zinnia/" + zinniaArch + "/" + libzinniajni).getPath();
		
		System.load(libZinniaPath);
		System.load(libZinniaJniPath);
		
		String kanjiRecognizeModelDbPath = DictionaryManager.class.getResource("/db/kanji_recognizer.model.db").getPath();
		
		zinniaHandler = zinnia.zinnia_recognizer_new();
		
		int zinniaRecognizerOpenResult = zinnia.zinnia_recognizer_open(zinniaHandler, kanjiRecognizeModelDbPath);
		
		if (zinniaRecognizerOpenResult == 0) {
			throw new RuntimeException("Nie moge zainicjalizowac Zinnia Manager: " + zinnia.zinnia_recognizer_strerror(zinniaHandler));
		}
		
		logger.info("Zakonczono inicjalizacje Zinnia Manager");		
	}
	
	@PreDestroy
	public void destroy() {
		zinnia.zinnia_recognizer_destroy(zinniaHandler);
	}
		
	public Character createNewCharacter() {
				
		return new Character();		
	}
	
	public class Character {
		
		private long character;
		
		private Character() {
			character = zinnia.zinnia_character_new();
		}
		
		public void clear() {
			zinnia.zinnia_character_clear(character);
		}
		
		public void setWidth(int width) {
			zinnia.zinnia_character_set_width(character, width);
		}
		
		public void setHeight(int width) {
			zinnia.zinnia_character_set_height(character, width);
		}
		
		public void add(int strokeNo, int x, int y) {
			zinnia.zinnia_character_add(character, strokeNo, x, y);
		}
		
		public List<KanjiRecognizerResultItem> recognize(int limit) {
			
			List<KanjiRecognizerResultItem> result = new ArrayList<KanjiRecognizerResultItem>();
			
			long recognizerResult = zinnia.zinnia_recognizer_classify(zinniaHandler, character, limit);

			if (recognizerResult != 0) {
				for (int i = 0; i < zinnia.zinnia_result_size(recognizerResult); ++i) {	
					result.add(new KanjiRecognizerResultItem(zinnia.zinnia_result_value(recognizerResult, i), zinnia.zinnia_result_score(recognizerResult, i)));
				}
				
				zinnia.zinnia_result_destroy(recognizerResult);
			}
			
			return result;
		}
		
		public void destroy() {
			zinnia.zinnia_character_destroy(character);
		}
	}
	
	public String getZinniaArch() {
		return zinniaArch;
	}

	public void setZinniaArch(String zinniaArch) {	
		this.zinniaArch = zinniaArch;
	}

	public String getLibzinnia() {
		return libzinnia;
	}

	public void setLibzinnia(String libzinnia) {
		this.libzinnia = libzinnia;
	}

	public String getLibzinniajni() {
		return libzinniajni;
	}

	public void setLibzinniajni(String libzinniajni) {
		this.libzinniajni = libzinniajni;
	}
}
