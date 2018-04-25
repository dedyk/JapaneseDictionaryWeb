package pl.idedyk.japanese.dictionary.web.dictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xdump.android.zinnia.Zinnia;

import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;

// INFO: FreeBSD: obejscie problemu (jesli wystepuje) z libzinniajni.so: Undefined symbol "zinnia_recognizer_new"
// export LD_LIBRARY_PATH=/katalog_do_pliku libzinnia.so

@Service
public class ZinniaManager {
	
	private static final Logger logger = Logger.getLogger(ZinniaManager.class);
	
	private String zinniaArch;
	
	private String libzinnia;
	private String libzinniajni;
	
	private boolean nativeLibInitialized = false;
	
	private Zinnia zinnia = new Zinnia();
	
	private long zinniaHandler = 0;
	
	private boolean initialized = false;

	@Value("${db.dir}")
	private String dbDir;
	
	@PostConstruct
	public void init() {

		initialized = false;
		
		logger.info("Inicjalizacja Zinnia Manager");
		
		if (nativeLibInitialized == false) {

			String libZinniaPath = ZinniaManager.class.getResource("/zinnia/" + zinniaArch + "/" + libzinnia).getPath();
			String libZinniaJniPath = ZinniaManager.class.getResource("/zinnia/" + zinniaArch + "/" + libzinniajni).getPath();
			
			System.load(libZinniaPath);
			System.load(libZinniaJniPath);

			nativeLibInitialized = true;			
		}		
		
		String kanjiRecognizeModelDbPath = new File(dbDir, "kanji_recognizer.model.db").getPath();
		
		zinniaHandler = zinnia.zinnia_recognizer_new();
		
		int zinniaRecognizerOpenResult = zinnia.zinnia_recognizer_open(zinniaHandler, kanjiRecognizeModelDbPath);
		
		if (zinniaRecognizerOpenResult == 0) {
			throw new RuntimeException("Nie moge zainicjalizowac Zinnia Manager: " + zinnia.zinnia_recognizer_strerror(zinniaHandler));
		}
		
		initialized = true;
		
		logger.info("Zakonczono inicjalizacje Zinnia Manager");		
	}
	
	@PreDestroy
	public void destroy() {
		zinnia.zinnia_recognizer_destroy(zinniaHandler);
	}
	
	public void waitForDatabaseReady() {
		
		if (initialized == true) {
			return;
		}
		
		while (true) {
						
			try {
				Thread.sleep(300);
				
			} catch (InterruptedException e) {
				// noop
			}
			
			if (initialized == true) {
				return;
			}			
		}		
	}
	
	public void reload() {
		
		try {
			Thread.sleep(2000);
			
		} catch (InterruptedException e) {
			// noop
		}
				
		initialized = false;
				
		logger.info("Zablokowano bazę danych zinnia. Czekanie 8 sekund na rozpoczęcie procedury przeładowania bazy");
		
		try {
			Thread.sleep(8000);
			
			destroy();
			
		} catch (Exception e) {
			// noop
		}
		
		zinnia = new Zinnia();
		
		zinniaHandler = 0;
		
		init();		
	}
		
	public Character createNewCharacter() {
		
		waitForDatabaseReady();
				
		return new Character();		
	}
	
	public class Character {
		
		private long character;
		
		private Character() {
			character = zinnia.zinnia_character_new();
		}
		
		public void clear() {
			
			waitForDatabaseReady();
			
			zinnia.zinnia_character_clear(character);
		}
		
		public void setWidth(int width) {
			
			waitForDatabaseReady();
			
			zinnia.zinnia_character_set_width(character, width);
		}
		
		public void setHeight(int width) {
			
			waitForDatabaseReady();
			
			zinnia.zinnia_character_set_height(character, width);
		}
		
		public void add(int strokeNo, int x, int y) {
			
			waitForDatabaseReady();
			
			zinnia.zinnia_character_add(character, strokeNo, x, y);
		}
		
		public List<KanjiRecognizerResultItem> recognize(int limit) {
			
			waitForDatabaseReady();
			
			List<KanjiRecognizerResultItem> result = new ArrayList<KanjiRecognizerResultItem>();
			
			long recognizerResult = zinnia.zinnia_recognizer_classify(zinniaHandler, character, limit);

			if (recognizerResult != 0) {
				for (int i = 0; i < zinnia.zinnia_result_size(recognizerResult); ++i) {
					
					String kanji = zinnia.zinnia_result_value(recognizerResult, i);
					float score = zinnia.zinnia_result_score(recognizerResult, i);
					
					if (kanji.equals("ð") == true) { // workaround: dziwny blad, funkcja z jni NewStringUTF z ciagu bajtów 240 160 174 159 odpowiadającemu znakowi 𠮟 zwraca znak ð
						kanji = "𠮟";
					}
					
					result.add(new KanjiRecognizerResultItem(kanji, score));
				}
				
				zinnia.zinnia_result_destroy(recognizerResult);
			}
			
			return result;
		}
		
		public void destroy() {
			
			waitForDatabaseReady();
			
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
