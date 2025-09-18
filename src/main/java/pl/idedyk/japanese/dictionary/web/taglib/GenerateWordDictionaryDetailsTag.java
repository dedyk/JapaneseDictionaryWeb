package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.Attribute;
import pl.idedyk.japanese.dictionary.api.dto.AttributeList;
import pl.idedyk.japanese.dictionary.api.dto.AttributeType;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.FuriganaEntry;
import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.api.dto.GroupWithTatoebaSentenceList;
import pl.idedyk.japanese.dictionary.api.dto.TatoebaSentence;
import pl.idedyk.japanese.dictionary.api.dto.WordType;
import pl.idedyk.japanese.dictionary.api.example.ExampleManager;
import pl.idedyk.japanese.dictionary.api.example.dto.ExampleGroupTypeElements;
import pl.idedyk.japanese.dictionary.api.example.dto.ExampleRequest;
import pl.idedyk.japanese.dictionary.api.example.dto.ExampleResult;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.api.gramma.GrammaConjugaterManager;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateGroupTypeElements;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateRequest;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateResult;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateResultType;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.B;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.IHtmlElement;
import pl.idedyk.japanese.dictionary.web.html.Img;
import pl.idedyk.japanese.dictionary.web.html.Li;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.html.Ul;
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;
import pl.idedyk.japanese.dictionary.web.taglib.utils.WordDictionary2SenseUtils;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.KanjiKanaPair;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfoKana;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfoKanaType;
import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class GenerateWordDictionaryDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
	private static final long serialVersionUID = 1L;
	
	private DictionaryEntry dictionaryEntry;
	private DictionaryEntryType forceDictionaryEntryType;
	
	private JMdict.Entry dictionaryEntry2;
	private List<KanjiKanaPair> kanjiKanaPairList;
				
	private MessageSource messageSource;	
	private DictionaryManager dictionaryManager;
	private Properties applicationProperties;
	
	private Map<Integer, GrammaFormConjugateAndExampleEntry> grammaFormConjugateAndExampleEntryMap = new LinkedHashMap<>();
		
	@Override
	public int doStartTag() throws JspException {
		
		try {		
			ServletContext servletContext = pageContext.getServletContext();
			ServletRequest servletRequest = pageContext.getRequest();
			
			String userAgent = null;
			
			if (servletRequest instanceof HttpServletRequest) {			
				HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
				
				userAgent = httpServletRequest.getHeader("User-Agent");			
			}
			
			boolean mobile = Utils.isMobile(userAgent);
			
			//
			
			// pobieramy pary slowek do wyswietlenia
			if (dictionaryEntry2 != null) {			
				kanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(dictionaryEntry2, true);
							
			} else {
				kanjiKanaPairList = null;
			}		
	
			//
			
			WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			
			this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
			this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
			this.applicationProperties = (Properties)webApplicationContext.getBean("applicationProperties");
		
            JspWriter out = pageContext.getOut();

            if (dictionaryEntry == null && dictionaryEntry2 == null) {            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.null")));
            	
            	errorDiv.render(out);
            	
            	return SKIP_BODY;
            }

            Div mainContentDiv = new Div();
            
            if (dictionaryEntry != null && dictionaryEntry.isName() == true) {            	
            	Div infoDiv = new Div("alert alert-info");
            	
            	infoDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.name.info")));

            	mainContentDiv.addHtmlElement(infoDiv);
            }                        
            
            Menu mainMenu = new Menu(null, null);

            // tytul strony
            mainContentDiv.addHtmlElement(generateTitle());
                        
            Div contentDiv = new Div("col-md-10");
            mainContentDiv.addHtmlElement(contentDiv);
            
            Div exampleSentenceDiv = null;
            
            try {	         
            	// generowanie informacji podstawowych
	            contentDiv.addHtmlElement(generateMainInfo(mainMenu, mobile));
	            
	            // generowanie przykladowych zdan
	            exampleSentenceDiv = generateExampleSentence(mainMenu);
	            
            } catch (DictionaryException e) {
            	throw new JspException(e);
            }
                        
            if (exampleSentenceDiv != null) {
            	contentDiv.addHtmlElement(exampleSentenceDiv);
            }
                        
            // odmiany gramatyczne            
            Div grammaFormConjugateDiv = generateGrammaFormConjugate(mainMenu);
            
            if (grammaFormConjugateDiv != null) {
            	contentDiv.addHtmlElement(grammaFormConjugateDiv);
            }

            // przyklady gramatyczne
            Div exampleDiv = generateExample(mainMenu);
            
            if (exampleDiv != null) {
            	contentDiv.addHtmlElement(exampleDiv);
            }
                        
            // dodaj w menu pozycje do zglaszania sugestii
            mainContentDiv.addHtmlElement(addSuggestionElements(mainMenu));
            
            // dodaj menu
            if (mobile == false) {
            	mainContentDiv.addHtmlElement(generateMenu(mainMenu));
            }
            
            // renderowanie
            mainContentDiv.render(out);
            
            return SKIP_BODY;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
            
        } finally {
    		// czyscimy stan
    		dictionaryEntry = null;
    		forceDictionaryEntryType = null;
    		
    		dictionaryEntry2 = null;
    		kanjiKanaPairList = null;
    		
    		grammaFormConjugateAndExampleEntryMap.clear();
    		
    		messageSource = null;	
    		dictionaryManager = null;
    		applicationProperties = null;
        }
	}

	private H generateTitle() throws IOException {
				
		H pageHeader = new H(4);
				
		pageHeader.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.title")));
				
		if (dictionaryEntry != null) {
			
			if (dictionaryEntry.isKanjiExists() == true) {				
				B kanjiBold = new B();
				
				kanjiBold.addHtmlElement(new Text(dictionaryEntry.getKanji()));
				
				pageHeader.addHtmlElement(kanjiBold);
				pageHeader.addHtmlElement(new Text(" | "));
			}
			
			String kana = dictionaryEntry.getKana();
				
			B kanaBold = new B();
			
			kanaBold.addHtmlElement(new Text(kana));
			pageHeader.addHtmlElement(kanaBold);
			
		} else if (dictionaryEntry2 != null) {
			String[] uniqueKanjiKanaRomajiSetWithoutSearchOnly = Dictionary2HelperCommon.getUniqueKanjiKanaRomajiSetWithoutSearchOnly(dictionaryEntry2);
			
			String kanji = uniqueKanjiKanaRomajiSetWithoutSearchOnly[0].replaceAll(",", ", ");
			String kana = uniqueKanjiKanaRomajiSetWithoutSearchOnly[1].replaceAll(",", ", ");
			
			if (kanji.equals("-") == false) { // czy kanji istnieje
				B kanjiBold = new B();
				
				kanjiBold.addHtmlElement(new Text(kanji));
				
				pageHeader.addHtmlElement(kanjiBold);
				pageHeader.addHtmlElement(new Text(" | "));
			}
						
			B kanaBold = new B();
			
			kanaBold.addHtmlElement(new Text(kana));
			pageHeader.addHtmlElement(kanaBold);			
			
		} else {
			throw new RuntimeException(); // to nigdy nie powinno zdarzyc sie
		}
		
		return pageHeader;
	}
	
	private Div generateMainInfo(Menu mainMenu, boolean mobile) throws IOException, DictionaryException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("mainInfoId");
		
		h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.mainInfo")));
		
		Menu mainInfoMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.mainInfo"));
		mainMenu.getChildMenu().add(mainInfoMenu);
		
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		// slowa
		Div wordDiv = generateWordsSection(mainInfoMenu, mobile);
		
		panelBody.addHtmlElement(wordDiv);
		panelBody.addHtmlElement(new Hr());
						
		// znaczenie znakow kanji
        Div knownKanjiDiv = generateKnownKanjiDiv(mainInfoMenu, mobile);
        
        if (knownKanjiDiv != null) {
        	panelBody.addHtmlElement(knownKanjiDiv);
        	panelBody.addHtmlElement(new Hr());
        }
		
        // znaczenie
        Div translate = generateTranslateSection(mainInfoMenu);
        panelBody.addHtmlElement(translate);

        // generuj informacje dodatkowe
        Div additionalInfo = generateAdditionalInfo(mainInfoMenu);

        if (additionalInfo != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(additionalInfo);
        }
        
        // dodatkowe atrybuty
		Div additionalAttributeDiv = generateAttribute(mainInfoMenu, mobile);

        if (additionalAttributeDiv != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(additionalAttributeDiv);
        }

        // czesc mowy
        Div wordTypeDiv = generateWordType(mainInfoMenu, mobile);
        
        if (wordTypeDiv != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(wordTypeDiv);
        }        
        		
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private Div generateWordsSection(Menu menu, boolean mobile) throws DictionaryException, IOException {
		Div wordsDiv = new Div();
		
    	// wiersz z tytulem
    	Div wordsTitleRowDiv = new Div("row");
		
    	// slowo - tytul
    	Div wordsiTitleDiv = new Div("col-md-1");
    	
    	H kanjiTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	kanjiTitleH4.setId("wordsiTitleId");
    	
    	kanjiTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.words.title")));
    	menu.getChildMenu().add(new Menu(kanjiTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.words.title")));
    	
    	wordsiTitleDiv.addHtmlElement(kanjiTitleH4);
    	
    	wordsTitleRowDiv.addHtmlElement(wordsiTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	wordsDiv.addHtmlElement(wordsTitleRowDiv);
		
    	// pokazywanie kolejnych slow    	
	    Div wordDiv = new Div("row");
		wordsDiv.addHtmlElement(wordDiv);

		wordDiv.addHtmlElement(new Div("col-md-1")); // przerwa
		
		Div wordMainDiv = new Div("col-md-10");		
		wordDiv.addHtmlElement(wordMainDiv);
		
		Table singleWordTable = new Table();
		wordMainDiv.addHtmlElement(singleWordTable);
		
		if (kanjiKanaPairList != null) { // nowy format
			
	    	for (int kanjiKanaPairIdx = 0; kanjiKanaPairIdx < kanjiKanaPairList.size(); ++kanjiKanaPairIdx) {
	    		KanjiKanaPair kanjiKanaPair = kanjiKanaPairList.get(kanjiKanaPairIdx);
	        	    		    	    		   	    		
	    		// pobieramy wszystkie skladniki slowa    		    	        	
	    		createWordTableTr(singleWordTable, kanjiKanaPair, kanjiKanaPairIdx, mobile);
			}
			
		} else { // stary format
			
			// stworzenie wirtualnego KanjiKanaPair
			KanjiInfo kanjiInfo = new KanjiInfo();
			
			kanjiInfo.setKanji(dictionaryEntry.getKanji());
			
			ReadingInfo readingInfo = new ReadingInfo();
			
			ReadingInfoKana readingInfoKana = new ReadingInfoKana();
			readingInfo.setKana(readingInfoKana);
			
			if (dictionaryEntry.getWordType() != null) {
				readingInfoKana.setKanaType(ReadingInfoKanaType.valueOf(dictionaryEntry.getWordType().name()));
			}
			
			readingInfoKana.setValue(dictionaryEntry.getKana());
			readingInfoKana.setRomaji(dictionaryEntry.getRomaji());
						
			KanjiKanaPair virtualKanjiKanaPair = new KanjiKanaPair(null, kanjiInfo, readingInfo);
			
			createWordTableTr(singleWordTable, virtualKanjiKanaPair, 0, mobile);			
		}		
    	
		return wordsDiv;
	}
	
	private void createWordTableTr(Table singleWordTable, KanjiKanaPair kanjiKanaPair, int wordNo, boolean mobile) throws DictionaryException, IOException {
		
		String kanji = kanjiKanaPair.getKanji();
		String kana = kanjiKanaPair.getKana();
    	String romaji = kanjiKanaPair.getRomaji();
						
    	// kanji    	
    	if (kanji != null) {

        	// informacje dodatkowe do kanji
            if (kanjiKanaPair.getKanjiInfo().getKanjiAdditionalInfoList().size() > 0) {
            	
            	List<String> kanjiAdditionalInfoListString = Dictionary2HelperCommon.translateToPolishKanjiAdditionalInfoEnum(kanjiKanaPair.getKanjiInfo().getKanjiAdditionalInfoList());
            	
            	for (String currentKanjiAdditionalInfoListString : kanjiAdditionalInfoListString) {
            		Tr singleWordDivTableKanjiAdditionalInfoTr = new Tr();
                	singleWordTable.addHtmlElement(singleWordDivTableKanjiAdditionalInfoTr);
                	
                	Td singleWordDivTableKanjiAdditionalInfoTrTd = new Td(null, null);                	
                	singleWordDivTableKanjiAdditionalInfoTr.addHtmlElement(singleWordDivTableKanjiAdditionalInfoTrTd);
                	
                	singleWordDivTableKanjiAdditionalInfoTrTd.setColspan("3");
                	
                	singleWordDivTableKanjiAdditionalInfoTrTd.addHtmlElement(new Text(currentKanjiAdditionalInfoListString));
				}            	
            }
    		
    		Tr singleWordDivTableKanjiTr = new Tr();
    		singleWordTable.addHtmlElement(singleWordDivTableKanjiTr);
    		
    		// tytul
    		Td singleWordDivTableKanjiTd1 = new Td(null, null);
    		singleWordDivTableKanjiTr.addHtmlElement(singleWordDivTableKanjiTd1);
    		
    		/*
        	H kanjiTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");        	
        	kanjiTitleH4.setId("kanjiTitleId");
        	
        	kanjiTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.title")));    		
    		singleWordDivTableKanjiTd1.addHtmlElement(kanjiTitleH4);
    		*/
    		
    		// czesc glowna
    		Td singleWordDivTableKanjiTd2 = new Td(null, "padding-right: 25px; padding-bottom: 10px");
    		singleWordDivTableKanjiTr.addHtmlElement(singleWordDivTableKanjiTd2);
    		
    		List<FuriganaEntry> furiganaEntries = dictionaryManager.getFurigana(null, kanjiKanaPair);
    		    		
            // sprawdzenie, czy mamy dane do pisania wszystkich znakow
            boolean isAllCharactersStrokePathsAvailableForWord = dictionaryManager.isAllCharactersStrokePathsAvailableForWord(kanji);
                        
            if (furiganaEntries != null && furiganaEntries.size() > 0) {
            	
            	for (FuriganaEntry currentFuriganaEntry : furiganaEntries) {
            		
    				List<String> furiganaKanaParts = currentFuriganaEntry.getKanaPart();
    				List<String> furiganaKanjiParts = currentFuriganaEntry.getKanjiPart();
    				    	        	
    	        	// tabelka ze znakiem kanji
    				Table kanjiTable = new Table();
    				
    				// czytanie
    				Tr kanaPartTr = new Tr(null, "font-size: 123%; text-align:left;");
    							
    				for (int idx = 0; idx < furiganaKanaParts.size(); ++idx) {    					
    					String currentKanaPart = furiganaKanaParts.get(idx);
    					
    					Td currentKanaPartTd = new Td();
    					
    					currentKanaPartTd.addHtmlElement(new Text(currentKanaPart));    					
    					kanaPartTr.addHtmlElement(currentKanaPartTd);
    				}
    				
    				kanjiTable.addHtmlElement(kanaPartTr);
    							
    				// znaki kanji
    				Tr kanjiKanjiTr = new Tr(null, "font-size: 200%; text-align:left;");
    				
    				kanjiTable.addHtmlElement(kanjiKanjiTr);
    				
    				for (int idx = 0; idx < furiganaKanjiParts.size(); ++idx) {    					
    					String currentKanjiPart = furiganaKanjiParts.get(idx);
    					
    					Td currentKanjiPartTd = new Td();
    					
    					currentKanjiPartTd.addHtmlElement(new Text(currentKanjiPart));    					
    					kanjiKanjiTr.addHtmlElement(currentKanjiPartTd);
    				}	
    				
    				singleWordDivTableKanjiTd2.addHtmlElement(kanjiTable);
    				
    				// komorka z guziczkiem	
    				if (isAllCharactersStrokePathsAvailableForWord == true) {
    					
    					Td singleWordDivTableKanjiTd3 = new Td(null, null);
    		    		singleWordDivTableKanjiTr.addHtmlElement(singleWordDivTableKanjiTd3);
    					
    					final String kanjiDrawId = "kanjiDrawId" + wordNo;
    					
    					Button kanjiDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanjiDrawId, 
    							getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.showKanjiDraw"));

				        // skrypt otwierajacy okienko
    					singleWordDivTableKanjiTd3.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanjiDrawId, kanji.length(), mobile));
				        
				        // tworzenie okienka rysowania znaku kanji
    					singleWordDivTableKanjiTd3.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, kanji, kanjiDrawId));
    					
    					// singleWordKanjiDivbutton.addHtmlElement(kanjiDrawButton);
    					singleWordDivTableKanjiTd3.addHtmlElement(kanjiDrawButton);
    				}    				
            	}
            	
            } else {
            	// kanji bez furigama
            	Div kanjiDivText = new Div(null, "font-size: 200%");
            	Text kanjiText = new Text(kanji);
            	
            	kanjiDivText.addHtmlElement(kanjiText);
            	
            	singleWordDivTableKanjiTd2.addHtmlElement(kanjiDivText);
            	
				// komorka z guziczkiem	
				if (isAllCharactersStrokePathsAvailableForWord == true) {
					
					Td singleWordDivTableKanjiTd3 = new Td(null, null);
		    		singleWordDivTableKanjiTr.addHtmlElement(singleWordDivTableKanjiTd3);
					
					final String kanjiDrawId = "kanjiDrawId" + wordNo;
					
					Button kanjiDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanjiDrawId, 
							getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.showKanjiDraw"));

			        // skrypt otwierajacy okienko
					singleWordDivTableKanjiTd3.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanjiDrawId, kanji.length(), mobile));
			        
			        // tworzenie okienka rysowania znaku kanji
					singleWordDivTableKanjiTd3.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, kanji, kanjiDrawId));
					
					// singleWordKanjiDivbutton.addHtmlElement(kanjiDrawButton);
					singleWordDivTableKanjiTd3.addHtmlElement(kanjiDrawButton);
				}
            }              
    	}
    	
    	// kana
    	{       	
        	// informacje dodatkowe do kana
            if (kanjiKanaPair.getReadingInfo().getReadingAdditionalInfoList().size() > 0) {
            	
            	List<String> readingReadingAdditionalInfoListString = Dictionary2HelperCommon.translateToPolishReadingAdditionalInfoEnum(kanjiKanaPair.getReadingInfo().getReadingAdditionalInfoList());
            	
            	for (String currentReadingAdditionalInfoListString : readingReadingAdditionalInfoListString) {
            		Tr singleWordDivTableKanaReadingAdditionalInfoTr = new Tr();
                	singleWordTable.addHtmlElement(singleWordDivTableKanaReadingAdditionalInfoTr);
                	
                	Td singleWordDivTableKanaReadingAdditionalInfoTrTd = new Td(null, null);                	
                	singleWordDivTableKanaReadingAdditionalInfoTr.addHtmlElement(singleWordDivTableKanaReadingAdditionalInfoTrTd);
                	
                	singleWordDivTableKanaReadingAdditionalInfoTrTd.setColspan("3");
                	
                	singleWordDivTableKanaReadingAdditionalInfoTrTd.addHtmlElement(new Text(currentReadingAdditionalInfoListString));
				}            	
            }    		
    		
    		Tr singleWordDivTableKanaTr = new Tr();
    		singleWordTable.addHtmlElement(singleWordDivTableKanaTr);

    		// tytul
    		Td singleWordDivTableKanaTd1 = new Td(null, null);
    		singleWordDivTableKanaTr.addHtmlElement(singleWordDivTableKanaTd1);
    		
    		/*
        	H readingTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");        	
        	readingTitleH4.setId("readingId");
        	
        	readingTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.reading.title")));
        	singleWordDivTableKanaTd1.addHtmlElement(readingTitleH4);
        	*/
        	
    		// czesc glowna    		
    		Td singleWordDivTableKanaTd2 = new Td(null, "font-size: 130%; text-align:left; padding-right: 25px; padding-bottom: 10px");
    		singleWordDivTableKanaTr.addHtmlElement(singleWordDivTableKanaTd2);
    		
        	// tekst kana
    		singleWordDivTableKanaTd2.addHtmlElement(new Text(kana));    		
        	        	
    		// guzik do rysowania
    		Td singleWordDivTableKanaTd3 = new Td();
    		singleWordDivTableKanaTr.addHtmlElement(singleWordDivTableKanaTd3);
    		
    		// guzik rysowania kana
        	// Div singleWordKanaDivbutton = new Div(null, "display: inline-block; width: 20%; padding: 5px 15px 0px 0px; overflow-wrap: break-word;");
        	
        	final String kanaDrawId = "kanaDrawId" + wordNo;
    		
    		Button kanaDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanaDrawId, 
    				getMessage("wordDictionaryDetails.page.dictionaryEntry.reading.showKanaDraw"));

            // skrypt otwierajacy okienko
    		singleWordDivTableKanaTd3.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanaDrawId, kana.length(), mobile));
            
            // tworzenie okienka rysowania znaku kanji
    		singleWordDivTableKanaTd3.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, kana, kanaDrawId));
    		
    		singleWordDivTableKanaTd3.addHtmlElement(kanaDrawButton);   
    	}
    	    	
    	// romaji
    	{
    		Tr singleWordDivTableRomajiTr = new Tr();
    		singleWordTable.addHtmlElement(singleWordDivTableRomajiTr);
    		
    		// tytul (pusty)
    		Td singleWordDivTableRomajiTd1 = new Td(null, "padding-bottom: 40px");
    		singleWordDivTableRomajiTr.addHtmlElement(singleWordDivTableRomajiTd1);
    		
        	// czesc glowna
    		Td singleWordDivTableRomajiTd2 = new Td(null, kanjiKanaPairList != null && wordNo != kanjiKanaPairList.size() - 1 ? 
    				"font-size: 130%; text-align:left; padding-right: 25px; padding-bottom: 40px" : 
    				"font-size: 130%; text-align:left; padding-right: 25px; padding-bottom: 0px");
    		singleWordDivTableRomajiTr.addHtmlElement(singleWordDivTableRomajiTd2);
    		
    		singleWordDivTableRomajiTd2.addHtmlElement(new Text(romaji));
    	}		
	}
		
	private Div generateTranslateSection(Menu menu) throws IOException {
		
		final String titleId = "translateId";
		final String titleTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.translate.title");
		
		if (dictionaryEntry2 == null) { // generowanie po staremu
			return generateStandardDivWithStringList(titleId, titleTitle, menu, dictionaryEntry.getTranslates());
						
		} else { // generowanie z danych zawartych w dictionaryEntry2			
			// glowny div z zawartoscia
			Div resultDiv = new Div();
			
	    	// wiersz z tytulem
	    	Div row1Div = new Div("row");
			
	    	// tytul
	    	Div divTitleDiv = new Div("col-md-10");
			
	    	H divTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
	    	
	    	divTitleH4.setId(titleId);
	    	
	    	divTitleH4.addHtmlElement(new Text(titleTitle));
	    	menu.getChildMenu().add(new Menu(divTitleH4.getId(), titleTitle));    	
	    	
	    	divTitleDiv.addHtmlElement(divTitleH4);

	    	row1Div.addHtmlElement(divTitleDiv);
	    	resultDiv.addHtmlElement(row1Div);
	    	
	    	//
	    	
	    	Div senseDiv = new Div("row");
	    	resultDiv.addHtmlElement(senseDiv);
	    		    	
	    	Div senseBodyDiv = new Div("col-md-11", "font-size: 130%");
	    	senseDiv.addHtmlElement(senseBodyDiv);
	    	
	    	WordDictionary2SenseUtils.createSenseHtmlElements(messageSource, pageContext.getServletContext().getContextPath(), dictionaryEntry2, senseBodyDiv, null, true);
	    							
			return resultDiv;
		}
	}
	
	private Div generateAdditionalInfo(Menu menu) throws IOException {
		
		String info = null;	
		String kanji = null;
		
		if (dictionaryEntry != null) {
			info = dictionaryEntry.getInfo();		
			kanji = dictionaryEntry.getKanji();			
			
		} else if (kanjiKanaPairList != null) {
			
			for (KanjiKanaPair kanjiKanaPair : kanjiKanaPairList) {
				KanjiInfo kanjiInfo = kanjiKanaPair.getKanjiInfo();
				
				if (kanjiInfo != null) { // wystarczy badac tylko jeden element
					kanji = kanjiInfo.getKanji();					
				}
			}
			
			info = null;
			
		} else {
			throw new RuntimeException(); // to nigdy nie powinno zdarzyc sie
		}
				
		int special = 0;
		
		if (kanji != null && isSmTsukiNiKawatteOshiokiYo(kanji) == true) {
			special = 1;
			
		} else if (kanji != null && isButaMoOdateryaKiNiNoboru(kanji) == true) {
			special = 2;
			
		} else if (kanji != null && isTakakoOkamura(kanji) == true) {
			special = 3;
		}
		
		if (special == 0 && kanjiKanaPairList != null) { // dla slownika w formacie drugim nie generuj tej sekcji; informacje te znajda sie w sekcji znaczen
			return null;
		}
				
		if (!(info != null && info.length() > 0) && (special == 0)) {
			return null;		
		}	
		
		Div additionalInfoDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// informacje dodatkowe - tytul
    	Div additionalInfoTitleDiv = new Div("col-md-3");
    	
    	H additionalInfoTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	additionalInfoTitleH4.setId("additionalInfoId");
    	
    	additionalInfoTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.info.title")));
    	menu.getChildMenu().add(new Menu(additionalInfoTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.info.title")));
    	
    	additionalInfoTitleDiv.addHtmlElement(additionalInfoTitleH4);
    	
    	row1Div.addHtmlElement(additionalInfoTitleDiv);

    	// dodaj wiersz z tytulem
    	additionalInfoDiv.addHtmlElement(row1Div);

    	// wiersz z informacjami dodatkowymi
    	Div row2Div = new Div("row");
    	additionalInfoDiv.addHtmlElement(row2Div);

		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
		
		Div additionalInfoTextDiv = new Div("col-md-11");
		row2Div.addHtmlElement(additionalInfoTextDiv);

    	Table row2Table = new Table();
    	additionalInfoTextDiv.addHtmlElement(row2Table);
    	
		Tr row2TableTr = new Tr();
		row2Table.addHtmlElement(row2TableTr);
		
		Td row2TableTrTd1 = new Td();
		row2TableTr.addHtmlElement(row2TableTrTd1);
		
		if (info != null && (special == 0 || special == 3)) {
			
			H additionalInfoTextH4 = new H(4);
			row2TableTrTd1.addHtmlElement(additionalInfoTextH4);
			
			additionalInfoTextH4.addHtmlElement(new Text(info));						
		}
		
		if (special > 0) {
			
			Div specialDiv = new Div(null, "font-family:monospace; font-size: 40%");
			row2TableTrTd1.addHtmlElement(specialDiv);
			
			if (special == 1) {
				// specialDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.info.special.sm_tsuki_ni_kawatte_oshioki_yo")));
				
				String staticPrefix = LinkGenerator.getStaticPrefix(pageContext.getServletContext().getContextPath(), applicationProperties);
				
				Img smTsukiNiKawatteOshiokiYoImg = new Img();
				
				smTsukiNiKawatteOshiokiYoImg.setSrc(staticPrefix + "/img/sm_tsuki_ni_kawatte_oshioki_yo.jpg");
				smTsukiNiKawatteOshiokiYoImg.setWidthImg("100%");
				smTsukiNiKawatteOshiokiYoImg.setAlt("Tsuki ni kawatte oshioki yo");
				
				specialDiv.addHtmlElement(smTsukiNiKawatteOshiokiYoImg);
				
			} else if (special == 2) {
				// specialDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.info.special.buta_mo_odaterya_ki_ni_noboru")));
				
				String staticPrefix = LinkGenerator.getStaticPrefix(pageContext.getServletContext().getContextPath(), applicationProperties);
				
				Img smTsukiNiKawatteOshiokiYoImg = new Img();
				
				smTsukiNiKawatteOshiokiYoImg.setSrc(staticPrefix + "/img/buta_mo_odaterya_ki_ni_noboru.jpg");
				smTsukiNiKawatteOshiokiYoImg.setWidthImg("100%");
				smTsukiNiKawatteOshiokiYoImg.setAlt("Buta mo odaterya ki ni noboru");
				
				specialDiv.addHtmlElement(smTsukiNiKawatteOshiokiYoImg);

			} else if (special == 3) {
				
				String staticPrefix = LinkGenerator.getStaticPrefix(pageContext.getServletContext().getContextPath(), applicationProperties);
				
				Img takakoOkamuraImg = new Img();
				
				takakoOkamuraImg.setSrc(staticPrefix + "/img/takako_okamura.webp");
				takakoOkamuraImg.setWidthImg("80%");
				takakoOkamuraImg.setAlt("Takako Okamura");
				
				specialDiv.addHtmlElement(takakoOkamuraImg);
			}
		}		
				
		return additionalInfoDiv;
	}
	
	// special
	private boolean isSmTsukiNiKawatteOshiokiYo(String value) {

		if (value == null) {
			return false;
		}

		if (value.equals("月に代わって、お仕置きよ!") == true) {
			return true;
		}

		return false;
	}
	
	private boolean isButaMoOdateryaKiNiNoboru(String value) {

		if (value == null) {
			return false;
		}

		if (value.equals("豚もおだてりゃ木に登る") == true || value.equals("ブタもおだてりゃ木に登る") == true || value.equals("豚も煽てりゃ木に登る") == true) {
			return true;
		}

		return false;
	}
	
	private boolean isTakakoOkamura(String value) {

		if (value == null) {
			return false;
		}

		if (value.equals("岡村孝子") == true) {
			return true;
		}

		return false;
	}
		
	private Div generateWordType(Menu menu, boolean mobile) throws IOException {		
		List<DictionaryEntry> dictionaryEntryList = new ArrayList<>();
		
		if (dictionaryEntry != null) {
			dictionaryEntryList.add(dictionaryEntry);
			
		} else if (kanjiKanaPairList != null) {
			dictionaryEntryList.addAll(convertKanjiKanaPairListToOldDictionaryEntry(kanjiKanaPairList));
		}
		
		if (dictionaryEntryList.size() == 0) {
			return null;
		}
		
		Div wordTypeDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// czesc mowy - tytul
    	Div wordTypeTitleDiv = new Div("col-md-3");
    	
    	H wordTypeTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	wordTypeTitleH4.setId("wordTypeId");
    	
    	wordTypeTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.title")));
    	menu.getChildMenu().add(new Menu(wordTypeTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.title")));
    	
    	wordTypeTitleDiv.addHtmlElement(wordTypeTitleH4);
    	
    	row1Div.addHtmlElement(wordTypeTitleDiv);

    	// dodaj wiersz z tytulem
    	wordTypeDiv.addHtmlElement(row1Div);

		// wygenerowanie zakladek			
		Ul tabUl = new Ul("nav nav-tabs");
		wordTypeDiv.addHtmlElement(tabUl);
		
		for (int dictionaryEntryIdx = 0; dictionaryEntryIdx < dictionaryEntryList.size(); ++dictionaryEntryIdx) {
			
			DictionaryEntry dictionaryEntry = dictionaryEntryList.get(dictionaryEntryIdx);
			
			Li dictionaryEntryLi = new Li();				
			tabUl.addHtmlElement(dictionaryEntryLi);
			
			if (dictionaryEntryIdx == 0) {
				dictionaryEntryLi.setClazz("active");
			}
			
			A tabUlA = new A();
			dictionaryEntryLi.addHtmlElement(tabUlA);
			
			tabUlA.setDataToggle("tab");
			tabUlA.setHref("#dictionaryEntryWordTypeTabContentId" + dictionaryEntryIdx);
			tabUlA.setId("dictionaryEntryWordTypeTabId" + dictionaryEntryIdx);
							
			tabUlA.addHtmlElement(new Text((dictionaryEntry.isKanjiExists() == true ? dictionaryEntry.getKanji()  + ", " : "") + dictionaryEntry.getKana()));
		}
		
		Div tabContentDiv = new Div();			
		wordTypeDiv.addHtmlElement(tabContentDiv);
		
		tabContentDiv.setClazz("tab-content");
		
		for (int dictionaryEntryIdx = 0; dictionaryEntryIdx < dictionaryEntryList.size(); ++dictionaryEntryIdx) {
							
			DictionaryEntry dictionaryEntry = dictionaryEntryList.get(dictionaryEntryIdx);
			
			Div divForDictionaryEntry = new Div();
			tabContentDiv.addHtmlElement(divForDictionaryEntry);
			
			divForDictionaryEntry.setId("dictionaryEntryWordTypeTabContentId" + dictionaryEntryIdx);
			
			if (dictionaryEntryIdx == 0) {
				divForDictionaryEntry.setClazz("tab-pane fade in active col-md-12");
			} else {
				divForDictionaryEntry.setClazz("tab-pane fade col-md-12");
			}
			
			// dodanie krotkiej przerwy do zawartosci
			divForDictionaryEntry.addHtmlElement(new Div(null, "padding-bottom: 20px"));				
			
			Div wordTypeTabContentDiv = generateWordTypeTabContent(dictionaryEntry);
			
			if (wordTypeTabContentDiv != null) {
				divForDictionaryEntry.addHtmlElement(wordTypeTabContentDiv);
			}
		}
				
		return wordTypeDiv;
	}
	
	private List<DictionaryEntry> convertKanjiKanaPairListToOldDictionaryEntry(List<KanjiKanaPair> kanjiKanaPairList) {
		
		List<DictionaryEntry> dictionaryEntryList = new ArrayList<>();
		
		// pobranie starych elementow			
		for (KanjiKanaPair kanjiKanaPair : kanjiKanaPairList) {
			
			final String kanjiKanaPairKanji = kanjiKanaPair.getKanji() != null ? kanjiKanaPair.getKanji() : "-";
			final String kanjiKanaPairKana = kanjiKanaPair.getKana();
							
			// szukamy starego elementu
			DictionaryEntry oldDictionaryEntry = dictionaryEntry2.getMisc().getOldPolishJapaneseDictionary().getEntries().stream().filter(oldPolishJapaneseDictionary -> {
				
				String oldPolishJapaneseDictionaryKanji = oldPolishJapaneseDictionary.getKanji();
				String oldPolishJapaneseDictionaryKana = oldPolishJapaneseDictionary.getKana();
				
				if (oldPolishJapaneseDictionaryKanji == null) {
					oldPolishJapaneseDictionaryKanji = "-";
				}
				
				return 	kanjiKanaPairKanji.equals(oldPolishJapaneseDictionaryKanji) == true &&
						kanjiKanaPairKana.equals(oldPolishJapaneseDictionaryKana) == true;
				
			}).map(oldPolishJapaneseDictionary -> {
				DictionaryEntry oldVirtualDictionaryEntry = new DictionaryEntry();
				
				// id
				oldVirtualDictionaryEntry.setId((int)oldPolishJapaneseDictionary.getId());
				
				// dictionaryEntryTypeList
				oldVirtualDictionaryEntry.setDictionaryEntryTypeList(Arrays.asList(oldPolishJapaneseDictionary.getDictionaryEntryTypeList().split(",")).stream(). 
					map(m -> DictionaryEntryType.getDictionaryEntryType(m)).collect(Collectors.toList()));

				// attributeList
				dictionaryEntry2.getMisc().getOldPolishJapaneseDictionary().getAttributeList().stream().forEach(attr -> {
					if (oldVirtualDictionaryEntry.getAttributeList() == null) {
						oldVirtualDictionaryEntry.setAttributeList(new AttributeList());
					}
					
					oldVirtualDictionaryEntry.getAttributeList().addAttributeValue(AttributeType.valueOf(attr.getType()), attr.getValue());
				});
									
				// wordType
				oldVirtualDictionaryEntry.setWordType(WordType.valueOf(kanjiKanaPair.getKanaType().value()));
				
				// groups
				oldVirtualDictionaryEntry.setGroups(dictionaryEntry2.getMisc().getOldPolishJapaneseDictionary().getGroupsList().stream().
					map(grr -> GroupEnum.valueOf(grr)).collect(Collectors.toList()));
					
				// prefixKana, kanji, kana, prefixRomaji, romaji
				oldVirtualDictionaryEntry.setPrefixKana(oldPolishJapaneseDictionary.getPrefixKana());
				oldVirtualDictionaryEntry.setPrefixRomaji(oldPolishJapaneseDictionary.getPrefixRomaji());
				
				oldVirtualDictionaryEntry.setKanji(oldPolishJapaneseDictionary.getKanji());
				oldVirtualDictionaryEntry.setKana(oldPolishJapaneseDictionary.getKana());
				oldVirtualDictionaryEntry.setRomaji(oldPolishJapaneseDictionary.getRomaji());
				
				// translates, info, exampleSentenceGroupIdsList, name
				// tych elementow nie mapujemy
				
				//
				
				return oldVirtualDictionaryEntry;
								
			}).findFirst().orElse(null);
			
			if (oldDictionaryEntry != null) {
				dictionaryEntryList.add(oldDictionaryEntry);
			}
		}
		
		return dictionaryEntryList;
	}
	
	private Div generateWordTypeTabContent(DictionaryEntry dictionaryEntry) throws IOException {
		
		List<DictionaryEntryType> dictionaryEntryTypeList = dictionaryEntry.getDictionaryEntryTypeList();
		
		if (dictionaryEntryTypeList == null) {
			return null;
		}
		
		int addableDictionaryEntryTypeInfoCounter = 0;

		for (DictionaryEntryType currentDictionaryEntryType : dictionaryEntryTypeList) {

			boolean addableDictionaryEntryTypeInfo = DictionaryEntryType.isAddableDictionaryEntryTypeInfo(currentDictionaryEntryType);

			if (addableDictionaryEntryTypeInfo == true) {
				addableDictionaryEntryTypeInfoCounter++;
			}
		}
		
		if (addableDictionaryEntryTypeInfoCounter == 0) {
			return null;
		}
		
		Div wordTypeDiv = new Div();
		    	
    	/*
    	if (addableDictionaryEntryTypeInfoCounter > 1 && dictionaryEntry.isName() == false) { // info o odmianach
    		
        	Div row2Div = new Div("row");
        	wordTypeDiv.addHtmlElement(row2Div);

        	Div wordTypeInfoDiv = new Div("col-md-12", "margin: -15px 0 0px 0");
        	row2Div.addHtmlElement(wordTypeInfoDiv);
        	
    		H wordTypeInfoH5 = new H(5);
    		wordTypeInfoDiv.addHtmlElement(wordTypeInfoH5);
    		
    		wordTypeInfoH5.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.forceDictionaryEntryType.info")));	
    	}
    	*/
    	
    	// czesci mowy
    	Div row3Div = new Div("row");
    	wordTypeDiv.addHtmlElement(row3Div);
    	
    	row3Div.addHtmlElement(new Div("col-md-1")); // przerwa
    	
    	Div wordTypeBodyDiv = new Div("col-md-11");
    	row3Div.addHtmlElement(wordTypeBodyDiv);
    	
    	Table row3Table = new Table();
    	wordTypeBodyDiv.addHtmlElement(row3Table);
    	
		for (DictionaryEntryType currentDictionaryEntryType : dictionaryEntryTypeList) {

			boolean addableDictionaryEntryTypeInfo = DictionaryEntryType.isAddableDictionaryEntryTypeInfo(currentDictionaryEntryType);

			if (addableDictionaryEntryTypeInfo == true) {
				
				Tr row3TableTr = new Tr();
				row3Table.addHtmlElement(row3TableTr);
				
				Td row3TableTrTd1 = new Td();
				row3TableTr.addHtmlElement(row3TableTrTd1);
	    		
	    		H currentWordTypeH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
	    		currentWordTypeH.addHtmlElement(new Text(currentDictionaryEntryType.getName()));
	    		
	    		row3TableTrTd1.addHtmlElement(currentWordTypeH);
	    		
	    		/*
	    		if (addableDictionaryEntryTypeInfoCounter > 1 && dictionaryEntry.isName() == false) {
	    			
		            Td row3TableTrTd2 = new Td();
					row3TableTr.addHtmlElement(row3TableTrTd2);
		            
					Div row3TableTrTd2Div = new Div(null, "margin: 0 0 5px 50px");
					row3TableTrTd2.addHtmlElement(row3TableTrTd2Div);
					
					A linkButton = new A("btn btn-default");
					row3TableTrTd2Div.addHtmlElement(linkButton);
										
		            String link = LinkGenerator.generateDictionaryEntryDetailsLink(
		            		pageContext.getServletContext().getContextPath(), dictionaryEntry, currentDictionaryEntryType);

					linkButton.setHref(link);
					
					linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.forceDictionaryEntryType.show")));	    			
	    		}
	    		*/
			}
		}
		
		return wordTypeDiv;
	}
		
	private Div generateAttribute(Menu menu, boolean mobile) throws IOException, DictionaryException {
				
		final List<Attribute> attributeList;
		
		if (dictionaryEntry != null) {
			attributeList = dictionaryEntry.getAttributeList().getAttributeList();
			
		} else if (dictionaryEntry2 != null) {
			attributeList = new ArrayList<>();
			
			dictionaryEntry2.getMisc().getOldPolishJapaneseDictionary().getAttributeList().stream().forEach(attr -> {
				Attribute attribute = new Attribute();
				
				attribute.setAttributeType(AttributeType.valueOf(attr.getType()));
				attribute.setSingleAttributeValue(attr.getValue());

				attributeList.add(attribute);
			});
			
		} else {
			throw new RuntimeException(); // to nigdy nie powinno zdarzyc sie
		}		 
		
		if (attributeList == null || attributeList.size() == 0) {
			return null;
		}
		
		Div attributeDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// czesc mowy - tytul
    	Div attributeTitleDiv = new Div("col-md-3");
    	
    	H attributeTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	attributeTitleH4.setId("attributeId");
    	
    	attributeTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.attribute.title")));
    	menu.getChildMenu().add(new Menu(attributeTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.attribute.title")));
    	
    	attributeTitleDiv.addHtmlElement(attributeTitleH4);
    	
    	row1Div.addHtmlElement(attributeTitleDiv);

    	// dodaj wiersz z tytulem
    	attributeDiv.addHtmlElement(row1Div);
    	
    	// atrybuty
    	Div row2Div = new Div("row");
    	attributeDiv.addHtmlElement(row2Div);
    	
    	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    	
    	Div attributeBodyDiv = new Div("col-md-11");
    	row2Div.addHtmlElement(attributeBodyDiv);
    	
    	Table row2Table = new Table();
    	attributeBodyDiv.addHtmlElement(row2Table);

    	for (Attribute currentAttribute : attributeList) {

			AttributeType attributeType = currentAttribute.getAttributeType();

			if (attributeType.isShow() == true) {
				
				Tr row2TableTr = new Tr();
				row2Table.addHtmlElement(row2TableTr);
				
				Td row2TableTrTd1 = new Td();
				row2TableTr.addHtmlElement(row2TableTrTd1);
				
	    		H currentAttributeH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
	    		row2TableTrTd1.addHtmlElement(currentAttributeH);
	    		
	    		currentAttributeH.addHtmlElement(new Text(attributeType.getName()));
			}
			
			JMdict.Entry referenceDictionaryEntry;
			
			// pobieramy powiazane Entry
			if (attributeType == AttributeType.VERB_TRANSITIVITY_PAIR || attributeType == AttributeType.VERB_INTRANSITIVITY_PAIR) {
				Integer referenceWordId = Integer.parseInt(currentAttribute.getAttributeValue().get(0));

				referenceDictionaryEntry = dictionaryManager.getDictionaryEntry2ByOldPolishJapaneseDictionaryId(referenceWordId);
				
			} else if (attributeType == AttributeType.RELATED || attributeType == AttributeType.ANTONYM) {
				Integer referenceWordId = Integer.parseInt(currentAttribute.getAttributeValue().get(0));
				
				referenceDictionaryEntry = dictionaryManager.getDictionaryEntry2ById(referenceWordId);
				
			} else {
				referenceDictionaryEntry = null;
			}
					
			// if (attributeType == AttributeType.ALTERNATIVE || ) {
								
			if (referenceDictionaryEntry != null && (dictionaryEntry2 == null || dictionaryEntry2.getEntryId().intValue() != referenceDictionaryEntry.getEntryId().intValue())) {
				
				// pobieramy z powiazanego slowa wszystkie czyatnia
				List<KanjiKanaPair> referenceDictionaryEntryKanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(referenceDictionaryEntry, true);
				
				Tr row2TableTr = new Tr();
				row2Table.addHtmlElement(row2TableTr);
				
				Td row2TableTrTd1 = new Td();
				row2TableTr.addHtmlElement(row2TableTrTd1);

	    		H currentAttributeH = new H(4, null, "margin-top: 0px; margin-bottom: 5px; margin-left: 30px");
	    		row2TableTrTd1.addHtmlElement(currentAttributeH);
	    		
	    		currentAttributeH.addHtmlElement(new Text(attributeType.getName()));

	    		// czasownik przechodni / nieprzechodni / alternatywa
				String referenceDictionaryEntryKana = referenceDictionaryEntryKanjiKanaPairList.get(0).getKana();
				String referenceDictionaryEntryRomaji = referenceDictionaryEntryKanjiKanaPairList.get(0).getRomaji();

				StringBuffer referenceDictionaryEntrySb = new StringBuffer();

				if (referenceDictionaryEntryKanjiKanaPairList.get(0).getKanji() != null) {
					referenceDictionaryEntrySb.append(referenceDictionaryEntryKanjiKanaPairList.get(0).getKanji()).append(", ");
				}

				referenceDictionaryEntrySb.append(referenceDictionaryEntryKana).append(", ");					
				referenceDictionaryEntrySb.append(referenceDictionaryEntryRomaji);					
				
				if (mobile == false) {
					
					Td row2TableTrTd2 = new Td();
					
		    		H currentTransitivityIntrasitivityH = new H(4, null, "margin-top: 0px; margin-bottom: 5px; margin-left: 50px");
		    		row2TableTrTd2.addHtmlElement(currentTransitivityIntrasitivityH);
		    		
		    		currentTransitivityIntrasitivityH.addHtmlElement(new Text(referenceDictionaryEntrySb.toString()));
					
					row2TableTr.addHtmlElement(row2TableTrTd2);
					
				} else {
					
					Tr row2TableTrForMobile = new Tr();
					
					row2Table.addHtmlElement(row2TableTrForMobile);
					
					Td row2TableTrTd2ForMobile = new Td();
					
					row2TableTrTd2ForMobile.setColspan("2");
					
		    		H currentTransitivityIntrasitivityH = new H(4, null, "margin-top: 0px; margin-bottom: 5px; margin-left: 60px");
		    		row2TableTrTd2ForMobile.addHtmlElement(currentTransitivityIntrasitivityH);
		    		
		    		currentTransitivityIntrasitivityH.addHtmlElement(new Text(referenceDictionaryEntrySb.toString()));
					
		    		row2TableTrForMobile.addHtmlElement(row2TableTrTd2ForMobile);
				}
					    		
	    		// przycisk
				Td row2TableTrTd3 = new Td();
				row2TableTr.addHtmlElement(row2TableTrTd3);
				
				Div row2TableTrTd3Div = new Div(null, "margin: 0 0 5px 50px");
				row2TableTrTd3.addHtmlElement(row2TableTrTd3Div);
									
	            String link = LinkGenerator.generateDictionaryEntryDetailsLink(pageContext.getServletContext().getContextPath(), referenceDictionaryEntry);
	            
				A linkButton = new A("btn btn-default");
				row2TableTrTd3Div.addHtmlElement(linkButton);

				linkButton.setHref(link);
				
				linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.attribute.referenceDictionaryEntry.show")));			
			}				
		}
    			
		
		return attributeDiv;		
	}
	
	private Div generateKnownKanjiDiv(Menu menu, boolean mobile) throws DictionaryException {
		
		Set<String> allKanjis = new LinkedHashSet<String>(); 
		
		if (dictionaryEntry != null && dictionaryEntry.isKanjiExists() == true) { // obsluga starego formatu
			for (int idx = 0; idx < dictionaryEntry.getKanji().length(); ++idx) {
				allKanjis.add("" + dictionaryEntry.getKanji().charAt(idx));
			}
			
		} else if (kanjiKanaPairList != null) { // nowy format
			kanjiKanaPairList.stream().filter(f -> f.getKanjiInfo() != null).forEach(c -> {
				for (int idx = 0; idx < c.getKanji().length(); ++idx) {
					allKanjis.add("" + c.getKanji().charAt(idx));
				}	
			});			
			
		} else {
			return null;
		}
		
		if (allKanjis.size() == 0) {
			return null;
		}
		
		List<KanjiCharacterInfo> knownKanji = dictionaryManager.findKnownKanji(allKanjis.toString());
		
		if (knownKanji == null || knownKanji.size() == 0) {
			return null;
		}
		
		Div knownKanjiDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// znaczenie znakow kanji - tytul
    	Div knownKanjiTitleDiv = new Div("col-md-10");
    	
    	H knownKanjiTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	knownKanjiTitleH4.setId("knownKanjiId");
    	
    	knownKanjiTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.knownKanji.title")));
    	menu.getChildMenu().add(new Menu(knownKanjiTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.knownKanji.title")));
    	
    	knownKanjiTitleDiv.addHtmlElement(knownKanjiTitleH4);
    	
    	row1Div.addHtmlElement(knownKanjiTitleDiv);

    	// dodaj wiersz z tytulem
    	knownKanjiDiv.addHtmlElement(row1Div);

    	// znaki kanji
    	Div row2Div = new Div("row");
    	knownKanjiDiv.addHtmlElement(row2Div);
    	
    	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    	
    	Div knownKanjiBodyDiv = new Div("col-md-11");
    	row2Div.addHtmlElement(knownKanjiBodyDiv);
    	
    	Table row2Table = new Table();
    	knownKanjiBodyDiv.addHtmlElement(row2Table);

		for (KanjiCharacterInfo currentKnownKanjiEntry : knownKanji) {
			
			Tr row2TableTr = new Tr();
			row2Table.addHtmlElement(row2TableTr);
			
			// kanji
			Td row2TableTrKanjiTd = new Td(null, "font-size: 150%; padding: 0 50px 5px 0;");
			row2TableTr.addHtmlElement(row2TableTrKanjiTd);
			
			row2TableTrKanjiTd.addHtmlElement(new Text(currentKnownKanjiEntry.getKanji()));
			
			// znaczenie
			List<String> currentKnownKanjiPolishTranslates = pl.idedyk.japanese.dictionary.api.dictionary.Utils.getPolishTranslates(currentKnownKanjiEntry);
			
			StringBuffer currentKnownKanjiPolishTranslatesSb = new StringBuffer();
			
			for (int currentKnownKanjiPolishTranslatesIdx = 0; currentKnownKanjiPolishTranslatesIdx < currentKnownKanjiPolishTranslates.size(); ++currentKnownKanjiPolishTranslatesIdx) {
				
				currentKnownKanjiPolishTranslatesSb.append(currentKnownKanjiPolishTranslates.get(currentKnownKanjiPolishTranslatesIdx));
				
				if (currentKnownKanjiPolishTranslatesIdx != currentKnownKanjiPolishTranslates.size() - 1) {
					currentKnownKanjiPolishTranslatesSb.append(", ");
				}
			}
			
			Td row2TableTrPolishTranslateTd = new Td(null, "margin-top: 0px; padding: 0px 50px 5px 0;");
			row2TableTr.addHtmlElement(row2TableTrPolishTranslateTd);

			H row2TableTrPolishTranslateTdH4 = new H(4);
			row2TableTrPolishTranslateTd.addHtmlElement(row2TableTrPolishTranslateTdH4);
			
			row2TableTrPolishTranslateTdH4.addHtmlElement(new Text(currentKnownKanjiPolishTranslatesSb.toString()));
			
			// guziczek
			Td row2TableTrButtonTd = new Td(null, "margin-top: 0px; padding: 0px 50px 5px 0;");			
			
			String link = LinkGenerator.generateKanjiDetailsLink(pageContext.getServletContext().getContextPath(), currentKnownKanjiEntry);
			
			A linkButton = new A("btn btn-default");
			row2TableTrButtonTd.addHtmlElement(linkButton);

			linkButton.setHref(link);
			
			linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.knownKanji.kanji.show")));
			
			if (mobile == false) {				
				row2TableTr.addHtmlElement(row2TableTrButtonTd);
				
			} else {
				
				Tr row2TableTrButton = new Tr();
				
				row2Table.addHtmlElement(row2TableTrButton);
				
				row2TableTrButtonTd.setColspan("2");
				
				row2TableTrButton.addHtmlElement(row2TableTrButtonTd);				
			}			
		}
		
		return knownKanjiDiv;
	}
	
	private Div generateExampleSentence(Menu mainMenu) throws DictionaryException {
		
		List<String> exampleSentenceGroupIdsList = null;
		
		if (dictionaryEntry != null) {
			exampleSentenceGroupIdsList = dictionaryEntry.getExampleSentenceGroupIdsList();
			
		} else if (dictionaryEntry2 != null) {
			exampleSentenceGroupIdsList = dictionaryEntry2.getMisc().getOldPolishJapaneseDictionary().getExampleSentenceGroupIdsList();
		}
				
		if (exampleSentenceGroupIdsList == null || exampleSentenceGroupIdsList.size() == 0) {
			return null;
		}
		
		List<GroupWithTatoebaSentenceList> tatoebaSentenceGroupList = new ArrayList<GroupWithTatoebaSentenceList>();
		
    	for (String currentExampleSentenceGroupId : exampleSentenceGroupIdsList) {
			
    		GroupWithTatoebaSentenceList tatoebaSentenceGroup = dictionaryManager.getTatoebaSentenceGroup(currentExampleSentenceGroupId);
    		
    		if (tatoebaSentenceGroup != null) {    			
    			tatoebaSentenceGroupList.add(tatoebaSentenceGroup);
    		}
		}
    	
    	if (tatoebaSentenceGroupList.size() == 0) {
    		return null;
    	}
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		
		h3Title.setId("exampleSentenceId");
		
		h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.exampleSentences")));		
		Menu grammaFormConjugateMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.exampleSentences"));
				
		mainMenu.getChildMenu().add(grammaFormConjugateMenu);
		
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		panelDiv.addHtmlElement(panelBody);
		
		for (int tatoebaSentenceGroupListIdx = 0; tatoebaSentenceGroupListIdx < tatoebaSentenceGroupList.size(); ++tatoebaSentenceGroupListIdx) {
			
			GroupWithTatoebaSentenceList currentTatoebeSentenceGroup = tatoebaSentenceGroupList.get(tatoebaSentenceGroupListIdx);
			
			List<TatoebaSentence> tatoebaSentenceList = currentTatoebeSentenceGroup.getTatoebaSentenceList();
						
			List<TatoebaSentence> polishTatoebaSentenceList = new ArrayList<TatoebaSentence>();
			List<TatoebaSentence> japaneseTatoebaSentenceList = new ArrayList<TatoebaSentence>();
			
			for (TatoebaSentence currentTatoebaSentence : tatoebaSentenceList) {
				
				if (currentTatoebaSentence.getLang().equals("pol") == true) {
					polishTatoebaSentenceList.add(currentTatoebaSentence);
					
				} else if (currentTatoebaSentence.getLang().equals("jpn") == true) {
					japaneseTatoebaSentenceList.add(currentTatoebaSentence);
				}				
			}
			
			if (polishTatoebaSentenceList.size() > 0 && japaneseTatoebaSentenceList.size() > 0) {
								
				for (TatoebaSentence currentPolishTatoebaSentence : polishTatoebaSentenceList) {
					
					Div sentenceDiv = new Div("col-md-11");
					panelBody.addHtmlElement(sentenceDiv);
					
					Table sentenceDivTable = new Table();
					sentenceDiv.addHtmlElement(sentenceDivTable);
					
					Tr sentenceDivTableTr = new Tr();
					sentenceDivTable.addHtmlElement(sentenceDivTableTr);
					
					Td sentenceDivTableTd = new Td();
					sentenceDivTableTr.addHtmlElement(sentenceDivTableTd);
					
					H sentenceH4 = new H(4);
					
					sentenceH4.setStyle("margin-bottom: 0px");
					
					sentenceH4.addHtmlElement(new Text(currentPolishTatoebaSentence.getSentence()));
					
					sentenceDivTableTd.addHtmlElement(sentenceH4);
				}
				
				for (TatoebaSentence currentJapaneseTatoebaSentence : japaneseTatoebaSentenceList) {
					
					Div sentenceDiv = new Div("col-md-11");
					panelBody.addHtmlElement(sentenceDiv);
					
					Table sentenceDivTable = new Table();
					sentenceDiv.addHtmlElement(sentenceDivTable);
					
					Tr sentenceDivTableTr = new Tr();
					sentenceDivTable.addHtmlElement(sentenceDivTableTr);
					
					Td sentenceDivTableTd = new Td();
					sentenceDivTableTr.addHtmlElement(sentenceDivTableTd);
					
					H sentenceH4 = new H(4);
					
					sentenceH4.setStyle("margin-bottom: 0px");
					
					sentenceH4.addHtmlElement(new Text(currentJapaneseTatoebaSentence.getSentence()));
					
					sentenceDivTableTd.addHtmlElement(sentenceH4);
				}				
			
				if (tatoebaSentenceGroupListIdx != tatoebaSentenceGroupList.size() - 1) {
					
					Div div = new Div("col-md-12");
					panelBody.addHtmlElement(div);
					
					div.addHtmlElement(new Hr());
				}
			}
		}
		
		return panelDiv;
	}
	
	private Div generateGrammaFormConjugate(Menu mainMenu) throws IOException {
				
		List<DictionaryEntry> dictionaryEntryList = new ArrayList<>();
		
		if (dictionaryEntry != null) {
			dictionaryEntryList.add(dictionaryEntry);
			
		} else if (kanjiKanaPairList != null) {
			dictionaryEntryList.addAll(convertKanjiKanaPairListToOldDictionaryEntry(kanjiKanaPairList));
		}
		
		if (dictionaryEntryList.size() == 0) {
			return null;
		}
		
		// wyliczamy formy gramatyczne dla wszystkich rodzajow slow
		for (int dictionaryEntryIdx = 0; dictionaryEntryIdx < dictionaryEntryList.size(); ++dictionaryEntryIdx) {
			
			DictionaryEntry dictionaryEntry = dictionaryEntryList.get(dictionaryEntryIdx);
			
			List<DictionaryEntryType> dictionaryEntryTypeList = dictionaryEntry.getDictionaryEntryTypeList();
			
			for (DictionaryEntryType dictionaryEntryType : dictionaryEntryTypeList) {
				Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache = new HashMap<GrammaFormConjugateResultType, GrammaFormConjugateResult>();
				
				List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList = 
						GrammaConjugaterManager.getGrammaConjufateResult(dictionaryManager.getKeigoHelper(), new GrammaFormConjugateRequest(dictionaryEntry), grammaFormCache, dictionaryEntryType, false);
				
				if (grammaFormConjugateGroupTypeElementsList != null && grammaFormConjugateGroupTypeElementsList.size() > 0) { // mamy cos wyliczonego
					// zapisujemy to do pozniejszego wykorzystania
					final int dictionaryEntryIdxAsfinal = dictionaryEntryIdx;
					
					GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry = grammaFormConjugateAndExampleEntryMap.computeIfAbsent(dictionaryEntry.getId(), (id) -> {
						return new GrammaFormConjugateAndExampleEntry(dictionaryEntry, dictionaryEntryIdxAsfinal);
					});
					
					grammaFormConjugateAndExampleEntry.addDictionaryEntryTypeGrammaFormConjugate(dictionaryEntryType, grammaFormConjugateGroupTypeElementsList, grammaFormCache);
				}
			}			
		}
		
		if (grammaFormConjugateAndExampleEntryMap.size() > 0) { // jezeli udalo sie cos wyliczyc to pokazujemy to
			
			Div panelDiv = new Div("panel panel-default");
			Div panelHeading = new Div("panel-heading");
			
			// tytul sekcji
			H h3Title = new H(3, "panel-title");
			
			h3Title.setId("grammaFormConjugateId");
						
			h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugate")));			
			
			Menu grammaFormConjugateMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugate"));
			
			mainMenu.getChildMenu().add(grammaFormConjugateMenu);
			
			/*
			if (forceDictionaryEntryType == null) {
				h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugate")));
				
				grammaFormConjugateMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugate"));			
			} else {
				h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugateWithDictionaryEntryType", new String[] { forceDictionaryEntryType.getName() })));
				
				grammaFormConjugateMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugateWithDictionaryEntryType", new String[] { forceDictionaryEntryType.getName() }));
			}
			*/
			
			panelHeading.addHtmlElement(h3Title);			
			panelDiv.addHtmlElement(panelHeading);

			// zawartosc sekcji
			Div panelBody = new Div("panel-body");
			
			panelDiv.addHtmlElement(panelBody);

			// wygenerowanie zakladek
			createTabs(grammaFormConjugateMenu, panelBody,
					grammaFormConjugateAndExampleEntryMap.size(),
					(tabIdx) -> new ArrayList<>(grammaFormConjugateAndExampleEntryMap.values()).get(tabIdx),
					(tabIdx) -> "grammaFormConjugateEntry" + tabIdx,
					(tabIdx) -> "grammaFormConjugateContentEntry" + tabIdx,
					(objectToProcess) -> {
						GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry = (GrammaFormConjugateAndExampleEntry)objectToProcess;						
						DictionaryEntry dictionaryEntry = grammaFormConjugateAndExampleEntry.dictionaryEntry;
						
						return (dictionaryEntry.isKanjiExists() == true ? dictionaryEntry.getKanji()  + ", " : "") + dictionaryEntry.getKana();						
					},
					(tabObjectToProcessCreateDivWrapper) -> {
						GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry = (GrammaFormConjugateAndExampleEntry)tabObjectToProcessCreateDivWrapper.objectToProcess;						
						List<GrammaFormConjugateAndExampleEntryForDictionaryType> grammaFormConjugateGroupTypeElementsList = grammaFormConjugateAndExampleEntry.grammaFormConjugateAndExampleEntryForDictionaryTypeList;
						
						Div tabContent = new Div();
						
						// generujemy kolejne tab-y w podziale na rodzaj slowa
						createTabs(tabObjectToProcessCreateDivWrapper.menu, tabContent,
								grammaFormConjugateGroupTypeElementsList.size(),
								(tabIdx2) -> grammaFormConjugateGroupTypeElementsList.get(tabIdx2),
								(tabIdx2) -> "grammaFormConjugateEntry" + grammaFormConjugateAndExampleEntry.dictionaryEntryIdx + "_" + grammaFormConjugateGroupTypeElementsList.get(tabIdx2).dictionaryEntryType,
								(tabIdx2) -> "grammaFormConjugateContentEntry" + grammaFormConjugateAndExampleEntry.dictionaryEntryIdx + "_" + grammaFormConjugateGroupTypeElementsList.get(tabIdx2).dictionaryEntryType,
								(objectToProcess2) -> {
									GrammaFormConjugateAndExampleEntryForDictionaryType grammaFormConjugateAndExampleEntryForDictionaryType = (GrammaFormConjugateAndExampleEntryForDictionaryType)objectToProcess2;						
									
									return grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType.getName();						
								},
								(tabObjectToProcessCreateDivWrapper2) -> {
									GrammaFormConjugateAndExampleEntryForDictionaryType grammaFormConjugateAndExampleEntryForDictionaryType = (GrammaFormConjugateAndExampleEntryForDictionaryType)tabObjectToProcessCreateDivWrapper2.objectToProcess;
																		
									Div div = new Div();
																		
									for (int idx = 0; idx < grammaFormConjugateAndExampleEntryForDictionaryType.grammaFormConjugateGroupTypeElementsList.size(); ++idx) {
										
										GrammaFormConjugateGroupTypeElements currentGrammaFormConjugateGroupTypeElements = grammaFormConjugateAndExampleEntryForDictionaryType.grammaFormConjugateGroupTypeElementsList.get(idx);
										
										if (currentGrammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().isShow() == false) {
											continue;
										}
										
										int tabIdx = grammaFormConjugateAndExampleEntry.dictionaryEntryIdx;									
										String tab2LevelId = "grammaFormConjugateEntry" + tabIdx + "_" + grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType; 
										
										div.addHtmlElement(generateGrammaFormConjugateGroupTypeElements(currentGrammaFormConjugateGroupTypeElements, tabObjectToProcessCreateDivWrapper2.menu,
												(id) -> { return "grammaFormConjugateEntry" + tabIdx + "_" + grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType + "_" + id; },
												(id) -> { return createLevel2TabOnclickScrollScript("grammaFormConjugateEntry", tabIdx, tab2LevelId, id); }																								
											));
										
										if (idx != grammaFormConjugateAndExampleEntryForDictionaryType.grammaFormConjugateGroupTypeElementsList.size() - 1) {
											div.addHtmlElement(new Hr());
										}
									}
									
									return div;
								},
								(objectToProcess2) -> {
									GrammaFormConjugateAndExampleEntryForDictionaryType grammaFormConjugateAndExampleEntryForDictionaryType = (GrammaFormConjugateAndExampleEntryForDictionaryType)objectToProcess2;
									
									int tabIdx = grammaFormConjugateAndExampleEntry.dictionaryEntryIdx;									
									String tab2LevelId = "grammaFormConjugateEntry" + tabIdx + "_" + grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType; 
									
									return createLevel2TabOnclickScrollScript("grammaFormConjugateEntry", tabIdx, tab2LevelId, tab2LevelId);
								}								
							);
												
						return tabContent;
					},
					(objectToProcess) -> {
						GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry = (GrammaFormConjugateAndExampleEntry)objectToProcess;
						
						int tabIdx = grammaFormConjugateAndExampleEntry.dictionaryEntryIdx;
						
						return createLevel1TabOnclickScrollScript("grammaFormConjugateEntry", "grammaFormConjugateContentEntry", tabIdx);
					}					
				);					
			
			return panelDiv;
			
		} else {
			return null;
		}
	}
	
	private String createLevel1TabOnclickScrollScript(String tabId, String tab2Id, int tabIdx) {
		return "$('#" + tabId + tabIdx + "').tab('show');"
				+ "setTimeout(() => { $('html, body').animate({ " 
				+ "scrollTop: $('#" + tab2Id + tabIdx + "').offset().top - 15 " 
				+ "}, 1000); }, 300); return false; ";						
	}
	
	private String createLevel2TabOnclickScrollScript(String tabId, int tabIdx, String tab2LevelId, String scrollTargetId) {
		
		return "$('#" + tabId + tabIdx + "').tab('show');"
				+ "$('#" + tab2LevelId + "').tab('show');"
				+ "setTimeout(() => { $('html, body').animate({ " 
				+ "scrollTop: $('#" + scrollTargetId + "').offset().top - 15 " 
				+ "}, 1000); }, 300); return false; ";

	}
		
	private void createTabs(Menu menu, Div panelBody, int tabsNumbers,
			Function<Integer,Object> getObjectGetter,
			Function<Integer, String> tabIdHrefGetter,
			Function<Integer, String> tabContentIdHrefGetter,
			Function<Object, String> tabNameGetter,
			Function<TabObjectToProcessCreateDivWrapper, Div> contentDivGetter,
			Function<Object, String> customOnClickGenerator) {
		
		// wygenerowanie zakladek			
		Ul tabUl = new Ul("nav nav-tabs");
		panelBody.addHtmlElement(tabUl);
		
		for (int tabIdx = 0; tabIdx < tabsNumbers; ++tabIdx) {
			
			// pobranie obiektu, ktory wyswietlamy
			Object objectToProcess = getObjectGetter.apply(tabIdx);
						
			Li objectLi = new Li();				
			tabUl.addHtmlElement(objectLi);
			
			if (tabIdx == 0) {
				objectLi.setClazz("active");
			}
			
			A tabUlA = new A();
			objectLi.addHtmlElement(tabUlA);
			
			tabUlA.setDataToggle("tab");
			tabUlA.setHref("#" + tabContentIdHrefGetter.apply(tabIdx));
			tabUlA.setId(tabIdHrefGetter.apply(tabIdx));
							
			tabUlA.addHtmlElement(new Text(tabNameGetter.apply(objectToProcess)));
		}
		
		Div tabContentDiv = new Div();			
		panelBody.addHtmlElement(tabContentDiv);
		
		tabContentDiv.setClazz("tab-content");

		for (int tabIdx = 0; tabIdx < tabsNumbers; ++tabIdx) {
			
			// pobranie obiektu, ktory wyswietlamy
			Object objectToProcess = getObjectGetter.apply(tabIdx);
			
			Div objectToProcessEntryDiv = new Div();
			tabContentDiv.addHtmlElement(objectToProcessEntryDiv);
			
			objectToProcessEntryDiv.setId(tabContentIdHrefGetter.apply(tabIdx));
			
			if (tabIdx == 0) {
				objectToProcessEntryDiv.setClazz("tab-pane fade in active col-md-12");
			} else {
				objectToProcessEntryDiv.setClazz("tab-pane fade col-md-12");
			}
							
			// dodanie pozycji do menu
			Menu menuForObjectToProcess = new Menu(objectToProcessEntryDiv.getId(), tabNameGetter.apply(objectToProcess));
			
			// stworzenie skryptu do wyboru zakladki						
			menuForObjectToProcess.setCustomOnClick(customOnClickGenerator.apply(objectToProcess));							
			menu.getChildMenu().add(menuForObjectToProcess);
							
			// dodanie krotkiej przerwy do zawartosci
			objectToProcessEntryDiv.addHtmlElement(new Div(null, "padding-bottom: 20px"));				
			
			// wygenerowanie zawartosci
			objectToProcessEntryDiv.addHtmlElement(contentDivGetter.apply(new TabObjectToProcessCreateDivWrapper(objectToProcess, menuForObjectToProcess)));
		}
	}
	
	private Div generateGrammaFormConjugateGroupTypeElements(GrammaFormConjugateGroupTypeElements grammaFormConjugateGroupTypeElements, Menu menu,
			Function<String, String> idGenerator,
			Function<String, String> menuOnClickGenerator) {
		
		Div resultDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div row1TitleDiv = new Div("col-md-12");
    	
    	H row1TitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");    	
    	row1TitleH4.setId(idGenerator.apply(grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().toString()));  	
    	row1TitleH4.addHtmlElement(new Text(grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().getName()));
    	
    	String grammaFormConjugateGroupTypeInfo = grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().getInfo();
    	
    	if (grammaFormConjugateGroupTypeInfo != null) {
			H infoH5 = new H(5, null, "margin-top: 0px;");
			
			infoH5.addHtmlElement(new Text(grammaFormConjugateGroupTypeInfo));
			
			row1TitleH4.addHtmlElement(infoH5);			
    	}
    	
    	Menu row1Menu = new Menu(row1TitleH4.getId(), grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().getName());
    	row1Menu.setCustomOnClick(menuOnClickGenerator.apply(row1TitleH4.getId()));
    	
    	menu.getChildMenu().add(row1Menu);
    	
    	row1TitleDiv.addHtmlElement(row1TitleH4);
    	row1Div.addHtmlElement(row1TitleDiv);

    	// dodaj wiersz z tytulem
    	resultDiv.addHtmlElement(row1Div);

    	// zawartosc sekcji
    	Div row2Div = new Div("row");
    	resultDiv.addHtmlElement(row2Div);
    	
    	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    	
    	Div sectionBodyDiv = new Div("col-md-11");
    	row2Div.addHtmlElement(sectionBodyDiv);
    	
		List<GrammaFormConjugateResult> grammaFormConjugateResults = grammaFormConjugateGroupTypeElements.getGrammaFormConjugateResults();

		for (int idx = 0; idx < grammaFormConjugateResults.size(); ++idx) {
			
			GrammaFormConjugateResult currentGrammaFormConjugateResult = grammaFormConjugateResults.get(idx);

	    	// tytul sekcji dla elementu		 
			if (currentGrammaFormConjugateResult.getResultType().isShow() == true) {
		    	H currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4 = new H(4, "col-md-11", "margin-top: 0px; font-weight:bold; margin-left: -25px;");
		    	
		    	String currentGrammaFormConjugateResultId = grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().toString() + "_" + currentGrammaFormConjugateResult.getResultType().toString();
		    	
		    	currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4.setId(idGenerator.apply(currentGrammaFormConjugateResultId));
		    	
		    	currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4.addHtmlElement(new Text(currentGrammaFormConjugateResult.getResultType().getName()));
		    	
		    	Menu nextLeveLMenu = new Menu(currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4.getId(), currentGrammaFormConjugateResult.getResultType().getName());
		    	nextLeveLMenu.setCustomOnClick(menuOnClickGenerator.apply(nextLeveLMenu.getId()));
		    			
		    	row1Menu.getChildMenu().add(nextLeveLMenu);		    	
		    	
		    	sectionBodyDiv.addHtmlElement(currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4);
		    	
		    	//
		    	
		    	// dodatkowe info	 
				String currentGrammaFormConjugateResultResultTypeInfo = currentGrammaFormConjugateResult.getResultType().getInfo();
				
				if (currentGrammaFormConjugateResultResultTypeInfo != null) {
					
					Table exampleTable = new Table(null, "width: 100%");
					sectionBodyDiv.addHtmlElement(exampleTable);
					
					Tr sentenceTableTr = new Tr();
					exampleTable.addHtmlElement(sentenceTableTr);
					
					Td sentenceTableTd = new Td();
					sentenceTableTr.addHtmlElement(sentenceTableTd);
					
					H currentExampleResultSectionBodyDivInfoH5 = new H(5, "col-md-11", "margin-top: 0px; margin-left: 0px;");
					
					currentExampleResultSectionBodyDivInfoH5.addHtmlElement(new Text(currentGrammaFormConjugateResultResultTypeInfo));
					
					sectionBodyDiv.addHtmlElement(currentExampleResultSectionBodyDivInfoH5);
				}	    	
			}
			
			// sekcja dla grupy odmian
	    	Div currentGrammaFormConjugateResultSectionBodyDiv = new Div("col-md-11");
	    	sectionBodyDiv.addHtmlElement(currentGrammaFormConjugateResultSectionBodyDiv);

	    	// zawartosc sekcji dla elementu
	    	Table currentGraamaFormConjugateResultSectionBodyTable = new Table(null, "width: 100%");
	    	currentGrammaFormConjugateResultSectionBodyDiv.addHtmlElement(currentGraamaFormConjugateResultSectionBodyTable);
	    	
	    	generateGrammaFormConjugateResult(currentGraamaFormConjugateResultSectionBodyTable, currentGrammaFormConjugateResult);	
	    	
	    	if (idx != grammaFormConjugateResults.size() - 1) {
		    	// przerwa
				Tr spaceTr = new Tr();
				currentGraamaFormConjugateResultSectionBodyTable.addHtmlElement(spaceTr);
				
				Td spaceTrTd = new Td();
				spaceTr.addHtmlElement(spaceTrTd);
				
				spaceTrTd.addHtmlElement(new Div(null, "margin-bottom: 15px;"));
	    	}
		}

		return resultDiv;
	}
	
	private void generateGrammaFormConjugateResult(Table table, GrammaFormConjugateResult grammaFormConjugateResult) {
				
		Tr tr1 = new Tr();
		table.addHtmlElement(tr1);
				
		String grammaFormKanji = grammaFormConjugateResult.getKanji();

		String prefixKana = grammaFormConjugateResult.getPrefixKana();
		String prefixRomaji = grammaFormConjugateResult.getPrefixRomaji();

		StringBuffer grammaFormKanjiSb = new StringBuffer();

		Td kanjiTd = new Td();
		tr1.addHtmlElement(kanjiTd);
		
		if (grammaFormKanji != null) {
			if (prefixKana != null && prefixKana.equals("") == false) {
				grammaFormKanjiSb.append("(").append(prefixKana).append(") ");
			}

			grammaFormKanjiSb.append(grammaFormKanji);
						
			H kanjiTdH4 = new H(4, null, "margin-top: 0px; margin-bottom: 5px;");
			kanjiTd.addHtmlElement(kanjiTdH4);
			
			kanjiTdH4.addHtmlElement(new Text(grammaFormKanjiSb.toString()));
		}
		
		List<String> grammaFormKanaList = grammaFormConjugateResult.getKanaList();
		List<String> grammaFormRomajiList = grammaFormConjugateResult.getRomajiList();

		for (int idx = 0; idx < grammaFormKanaList.size(); ++idx) {

			StringBuffer sb = new StringBuffer();

			if (prefixKana != null && prefixKana.equals("") == false) {
				sb.append("(").append(prefixKana).append(") ");
			}

			sb.append(grammaFormKanaList.get(idx));
			
			Tr tr2 = new Tr();
			table.addHtmlElement(tr2);
			
			Td kanaTd = new Td();
			tr2.addHtmlElement(kanaTd);
			
			H kanaTdH4 = new H(4, null, "margin-top: 0px; margin-bottom: 5px;");
			kanaTd.addHtmlElement(kanaTdH4);
			
			kanaTdH4.addHtmlElement(new Text(sb.toString()));
		}
		
		for (int idx = 0; idx < grammaFormRomajiList.size(); ++idx) {

			StringBuffer grammaFormRomajiSb = new StringBuffer();

			if (prefixRomaji != null && prefixRomaji.equals("") == false) {
				grammaFormRomajiSb.append("(").append(prefixRomaji).append(") ");
			}
			
			grammaFormRomajiSb.append(grammaFormRomajiList.get(idx));

			Tr tr3 = new Tr();
			table.addHtmlElement(tr3);
			
			Td romajiTd = new Td();
			tr3.addHtmlElement(romajiTd);
			
			H romajiTdH4 = new H(4, null, "margin-top: 0px; margin-bottom: 5px;");
			romajiTd.addHtmlElement(romajiTdH4);
			
			romajiTdH4.addHtmlElement(new Text(grammaFormRomajiSb.toString()));
		}
		
		String info = grammaFormConjugateResult.getInfo();
		
		if (info != null) {
			
			Tr trInfo = new Tr();
			table.addHtmlElement(trInfo);
			
			Td infoTd = new Td();
			trInfo.addHtmlElement(infoTd);
			
			H infoH5 = new H(5, null, "margin-top: 0px;");
			
			infoH5.addHtmlElement(new Text(info));
			
			infoTd.addHtmlElement(infoH5);			
		}
		
		GrammaFormConjugateResult alternative = grammaFormConjugateResult.getAlternative();
		
		if (alternative != null) {
			Tr tr4 = new Tr();
			table.addHtmlElement(tr4);
			
			Td tr4Td = new Td();
			tr4.addHtmlElement(tr4Td);
			
			tr4Td.addHtmlElement(new Div(null, "margin-bottom: 15px;"));
			
			generateGrammaFormConjugateResult(table, alternative);
		}
	}
	
	private Div generateExample(Menu mainMenu) throws IOException {
		
		// jezeli wczesniej wyliczono odmiany gramatyczne, to przyklady tez powinno dac sie
		if (grammaFormConjugateAndExampleEntryMap.size() > 0) {
			
			// wyliczenie przykladow
			for (GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry : grammaFormConjugateAndExampleEntryMap.values()) {				
				for (GrammaFormConjugateAndExampleEntryForDictionaryType grammaFormConjugateAndExampleEntryForDictionaryType : grammaFormConjugateAndExampleEntry.grammaFormConjugateAndExampleEntryForDictionaryTypeList) {
					
					List<ExampleGroupTypeElements> exampleGroupTypeElementsList =
							ExampleManager.getExamples(dictionaryManager.getKeigoHelper(), new ExampleRequest(grammaFormConjugateAndExampleEntry.dictionaryEntry), 
									grammaFormConjugateAndExampleEntryForDictionaryType.grammaFormCache, grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType, false);
					
					if (exampleGroupTypeElementsList != null && exampleGroupTypeElementsList.size() > 0) { // mamy cos wyliczonego
						grammaFormConjugateAndExampleEntryForDictionaryType.setExampleGroupTypeElementsList(exampleGroupTypeElementsList);
					}
				}				
			}
			
			// pokazanie przykladow
			// tutaj();
			
			Div panelDiv = new Div("panel panel-default");		
			Div panelHeading = new Div("panel-heading");

			// tytul sekcji
			H h3Title = new H(3, "panel-title");
			
			h3Title.setId("exampleId");
			
			h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.example")));
			
			Menu exampleMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.example"));			
			mainMenu.getChildMenu().add(exampleMenu);
			
			// final int maxMenuSize = 20;
			// Menu exampleMenu = null;
			// int menuCounter = 0;
					
			panelHeading.addHtmlElement(h3Title);		
			panelDiv.addHtmlElement(panelHeading);

			// zawartosc sekcji
			Div panelBody = new Div("panel-body");
			
			panelDiv.addHtmlElement(panelBody);		
						
			// wygenerowanie zakladek
			createTabs(exampleMenu, panelBody,
					grammaFormConjugateAndExampleEntryMap.size(),
					(tabIdx) -> new ArrayList<>(grammaFormConjugateAndExampleEntryMap.values()).get(tabIdx),
					(tabIdx) -> "exampleEntry" + tabIdx,
					(tabIdx) -> "exampleContentEntry" + tabIdx,
					(objectToProcess) -> {
						GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry = (GrammaFormConjugateAndExampleEntry)objectToProcess;						
						DictionaryEntry dictionaryEntry = grammaFormConjugateAndExampleEntry.dictionaryEntry;
						
						return (dictionaryEntry.isKanjiExists() == true ? dictionaryEntry.getKanji()  + ", " : "") + dictionaryEntry.getKana();						
					},
					(tabObjectToProcessCreateDivWrapper) -> {
						GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry = (GrammaFormConjugateAndExampleEntry)tabObjectToProcessCreateDivWrapper.objectToProcess;						
						List<GrammaFormConjugateAndExampleEntryForDictionaryType> grammaFormConjugateGroupTypeElementsList = grammaFormConjugateAndExampleEntry.grammaFormConjugateAndExampleEntryForDictionaryTypeList;
						
						Div tabContent = new Div();
						
						// generujemy kolejne tab-y w podziale na rodzaj slowa
						createTabs(tabObjectToProcessCreateDivWrapper.menu, tabContent,
								grammaFormConjugateGroupTypeElementsList.size(),
								(tabIdx2) -> grammaFormConjugateGroupTypeElementsList.get(tabIdx2),
								(tabIdx2) -> "exampleEntry" + grammaFormConjugateAndExampleEntry.dictionaryEntryIdx + "_" + grammaFormConjugateGroupTypeElementsList.get(tabIdx2).dictionaryEntryType,
								(tabIdx2) -> "exampleContentEntry" + grammaFormConjugateAndExampleEntry.dictionaryEntryIdx + "_" + grammaFormConjugateGroupTypeElementsList.get(tabIdx2).dictionaryEntryType,
								(objectToProcess2) -> {
									GrammaFormConjugateAndExampleEntryForDictionaryType grammaFormConjugateAndExampleEntryForDictionaryType = (GrammaFormConjugateAndExampleEntryForDictionaryType)objectToProcess2;						
									
									return grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType.getName();						
								},
								(tabObjectToProcessCreateDivWrapper2) -> {
									GrammaFormConjugateAndExampleEntryForDictionaryType grammaFormConjugateAndExampleEntryForDictionaryType = (GrammaFormConjugateAndExampleEntryForDictionaryType)tabObjectToProcessCreateDivWrapper2.objectToProcess;
																		
									Div div = new Div();
									
									for (int idx = 0; idx < grammaFormConjugateAndExampleEntryForDictionaryType.exampleGroupTypeElementsList.size(); ++idx) {
										
										ExampleGroupTypeElements currentExampleGroupTypeElements = grammaFormConjugateAndExampleEntryForDictionaryType.exampleGroupTypeElementsList.get(idx);
										
										int tabIdx = grammaFormConjugateAndExampleEntry.dictionaryEntryIdx;									
										String tab2LevelId = "exampleEntry" + tabIdx + "_" + grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType; 
										
										div.addHtmlElement(generateExampleGroupTypeElements(currentExampleGroupTypeElements, tabObjectToProcessCreateDivWrapper2.menu,
												(id) -> { return "exampleEntry" + tabIdx + "_" + grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType + "_" + id; },
												(id) -> { return createLevel2TabOnclickScrollScript("exampleEntry", tabIdx, tab2LevelId, id); }																								
											));
										
										if (idx != grammaFormConjugateAndExampleEntryForDictionaryType.exampleGroupTypeElementsList.size() - 1) {
											div.addHtmlElement(new Hr());
										}
									}
																		
									return div;
								},
								(objectToProcess2) -> {
									GrammaFormConjugateAndExampleEntryForDictionaryType grammaFormConjugateAndExampleEntryForDictionaryType = (GrammaFormConjugateAndExampleEntryForDictionaryType)objectToProcess2;
									
									int tabIdx = grammaFormConjugateAndExampleEntry.dictionaryEntryIdx;									
									String tab2LevelId = "exampleEntry" + tabIdx + "_" + grammaFormConjugateAndExampleEntryForDictionaryType.dictionaryEntryType; 
									
									return createLevel2TabOnclickScrollScript("exampleEntry", tabIdx, tab2LevelId, tab2LevelId);
								}								
							);
												
						return tabContent;
					},
					(objectToProcess) -> {
						GrammaFormConjugateAndExampleEntry grammaFormConjugateAndExampleEntry = (GrammaFormConjugateAndExampleEntry)objectToProcess;
						
						int tabIdx = grammaFormConjugateAndExampleEntry.dictionaryEntryIdx;
						
						return createLevel1TabOnclickScrollScript("exampleEntry", "exampleContentEntry", tabIdx);
					}					
				);
						
			return panelDiv;
			
		} else {
			return null;
		}
		
		/*
		// stary kod, ale niech zostanie na pamiatke
		for (int exampleGroupTypeElementsListIdx = 0; exampleGroupTypeElementsListIdx < exampleGroupTypeElementsList.size(); ++exampleGroupTypeElementsListIdx) {
			
			if (exampleMenu == null || exampleMenu.getChildMenu().size() >= maxMenuSize) {
				
				String exampleMenuTitle = null;
				
				if (exampleMenu == null && exampleGroupTypeElementsList.size() <= maxMenuSize) {
					
					if (forceDictionaryEntryType == null) {
						exampleMenuTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.example");
						
					} else {
						exampleMenuTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.exampleWithDictionaryEntryType", new String[] { forceDictionaryEntryType.getName() });
					}
					
					h3Title.addHtmlElement(new Text(exampleMenuTitle));
					
				} else if (exampleMenu == null) {
					
					if (forceDictionaryEntryType == null) {
						exampleMenuTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.example.part", new String[] { String.valueOf(menuCounter + 1) });
						
						h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.example")));
						
					} else {
						exampleMenuTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.exampleWithDictionaryEntryType.part", new String[] { String.valueOf(menuCounter + 1), forceDictionaryEntryType.getName() });
						
						h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.exampleWithDictionaryEntryType")));
					}
					
				} else if (exampleMenu != null) {
					
					menuCounter++;
					
					if (forceDictionaryEntryType == null) {
						exampleMenuTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.example.part", new String[] { String.valueOf(menuCounter + 1) });
						
					} else {
						exampleMenuTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.exampleWithDictionaryEntryType.part", new String[] { String.valueOf(menuCounter + 1), forceDictionaryEntryType.getName() });
					}
				}
				
				exampleMenu = new Menu(h3Title.getId(), exampleMenuTitle);
				
				mainMenu.getChildMenu().add(exampleMenu);					
			}
			
			ExampleGroupTypeElements currentExampleGroupTypeElements = exampleGroupTypeElementsList.get(exampleGroupTypeElementsListIdx);
						
			panelBody.addHtmlElement(generateExampleGroupTypeElements(currentExampleGroupTypeElements, exampleMenu));
			
			if (exampleGroupTypeElementsListIdx != exampleGroupTypeElementsList.size() - 1) {
				panelBody.addHtmlElement(new Hr());
			}
		}		
		*/
	}
	
	private IHtmlElement generateExampleGroupTypeElements(ExampleGroupTypeElements exampleGroupTypeElements, Menu menu,
			Function<String, String> idGenerator,
			Function<String, String> menuOnClickGenerator) {
		
		Div resultDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div row1TitleDiv = new Div("col-md-12");
    	
    	H row1TitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	row1TitleH4.setId(idGenerator.apply(exampleGroupTypeElements.getExampleGroupType().toString()));  	
    	row1TitleH4.addHtmlElement(new Text(exampleGroupTypeElements.getExampleGroupType().getName()));
    	
    	Menu row1Menu = new Menu(row1TitleH4.getId(), exampleGroupTypeElements.getExampleGroupType().getName());
    	row1Menu.setCustomOnClick(menuOnClickGenerator.apply(row1TitleH4.getId()));
    	
    	menu.getChildMenu().add(row1Menu);
    	
    	row1TitleDiv.addHtmlElement(row1TitleH4);
    	row1Div.addHtmlElement(row1TitleDiv);
    	
    	// dodatkowe info
    	String exampleGroupTypeElementsInfo = exampleGroupTypeElements.getExampleGroupType().getInfo();
    	
    	if (exampleGroupTypeElementsInfo != null) {
			H infoH5 = new H(5, null, "margin-top: 0px;");
			
			infoH5.addHtmlElement(new Text(exampleGroupTypeElementsInfo));
			
			row1TitleH4.addHtmlElement(infoH5);			
    	}

    	// dodaj wiersz z tytulem
    	resultDiv.addHtmlElement(row1Div);

    	// zawartosc sekcji
    	Div row2Div = new Div("row");
    	resultDiv.addHtmlElement(row2Div);
    	
    	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    	
    	Div sectionBodyDiv = new Div("col-md-11");
    	row2Div.addHtmlElement(sectionBodyDiv);
    	
    	List<ExampleResult> exampleResults = exampleGroupTypeElements.getExampleResults();

		for (int idx = 0; idx < exampleResults.size(); ++idx) {
			
			ExampleResult currentExampleResult = exampleResults.get(idx);
						
			// sekcja dla grupy przykladow
	    	Div currentExampleResultSectionBodyDiv = new Div("col-md-11");
	    	sectionBodyDiv.addHtmlElement(currentExampleResultSectionBodyDiv);

	    	// zawartosc sekcji dla elementu
	    	Table currentExampleResultSectionBodyTable = new Table(null, "width: 100%");
	    	
	    	currentExampleResultSectionBodyDiv.addHtmlElement(currentExampleResultSectionBodyTable);
	    	
	    	generateExampleResult(currentExampleResultSectionBodyTable, currentExampleResult);	
	    	
	    	if (idx != exampleResults.size() - 1) {
		    	// przerwa
				Tr spaceTr = new Tr();
				currentExampleResultSectionBodyTable.addHtmlElement(spaceTr);
				
				Td spaceTrTd = new Td();
				spaceTr.addHtmlElement(spaceTrTd);
				
				spaceTrTd.addHtmlElement(new Div(null, "margin-bottom: 15px;"));
	    	}
		}

		return resultDiv;
	}

	private void generateExampleResult(Table table, ExampleResult exampleResult) {
				
		Tr tr1 = new Tr();
		table.addHtmlElement(tr1);
				
		String exampleKanji = exampleResult.getKanji();

		String prefixKana = exampleResult.getPrefixKana();
		String prefixRomaji = exampleResult.getPrefixRomaji();

		StringBuffer exampleKanjiSb = new StringBuffer();

		Td kanjiTd = new Td();
		tr1.addHtmlElement(kanjiTd);
		
		if (exampleKanji != null) {
			if (prefixKana != null && prefixKana.equals("") == false) {
				exampleKanjiSb.append("(").append(prefixKana).append(") ");
			}

			exampleKanjiSb.append(exampleKanji);
						
			H kanjiTdH4 = new H(4, null, "margin-top: 0px; margin-bottom: 5px;");
			kanjiTd.addHtmlElement(kanjiTdH4);
			
			kanjiTdH4.addHtmlElement(new Text(exampleKanjiSb.toString()));
		}
		
		List<String> exampleKanaList = exampleResult.getKanaList();
		List<String> exampleRomajiList = exampleResult.getRomajiList();

		for (int idx = 0; idx < exampleKanaList.size(); ++idx) {

			StringBuffer sb = new StringBuffer();

			if (prefixKana != null && prefixKana.equals("") == false) {
				sb.append("(").append(prefixKana).append(") ");
			}

			sb.append(exampleKanaList.get(idx));
			
			Tr tr2 = new Tr();
			table.addHtmlElement(tr2);
			
			Td kanaTd = new Td();
			tr2.addHtmlElement(kanaTd);
			
			H kanaTdH4 = new H(4, null, "margin-top: 0px; margin-bottom: 5px;");
			kanaTd.addHtmlElement(kanaTdH4);
			
			kanaTdH4.addHtmlElement(new Text(sb.toString()));
		}
		
		for (int idx = 0; idx < exampleRomajiList.size(); ++idx) {

			StringBuffer exampleRomajiSb = new StringBuffer();

			if (prefixRomaji != null && prefixRomaji.equals("") == false) {
				exampleRomajiSb.append("(").append(prefixRomaji).append(") ");
			}
			
			exampleRomajiSb.append(exampleRomajiList.get(idx));

			Tr tr3 = new Tr();
			table.addHtmlElement(tr3);
			
			Td romajiTd = new Td();
			tr3.addHtmlElement(romajiTd);
			
			H romajiTdH4 = new H(4, null, "margin-top: 0px; margin-bottom: 5px;");
			romajiTd.addHtmlElement(romajiTdH4);
			
			romajiTdH4.addHtmlElement(new Text(exampleRomajiSb.toString()));
		}
		
		String info = exampleResult.getInfo();
		
		if (info != null) {
			
			Tr trInfo = new Tr();
			table.addHtmlElement(trInfo);
			
			Td infoTd = new Td();
			trInfo.addHtmlElement(infoTd);
			
			H infoH5 = new H(5, null, "margin-top: 0px;");
			
			infoH5.addHtmlElement(new Text(info));
			
			infoTd.addHtmlElement(infoH5);			
		}
		
		ExampleResult alternative = exampleResult.getAlternative();
		
		if (alternative != null) {
			Tr tr4 = new Tr();
			table.addHtmlElement(tr4);
			
			Td tr4Td = new Td();
			tr4.addHtmlElement(tr4Td);
			
			tr4Td.addHtmlElement(new Div(null, "margin-bottom: 15px;"));
			
			generateExampleResult(table, alternative);
		}		
	}
	
	private IHtmlElement addSuggestionElements(Menu mainMenu) {
		
        addSuggestionMenuPos(mainMenu, messageSource);
        
        int id = -1;
		String dictionaryEntryKanji;
		String dictionaryEntryKana;
		String dictionaryEntryRomaji;
        
        if (dictionaryEntry != null) {
            id = dictionaryEntry.getId();
            
    		dictionaryEntryKanji = dictionaryEntry.getKanji();
    		dictionaryEntryKana = dictionaryEntry.getKana();
    		dictionaryEntryRomaji = dictionaryEntry.getRomaji();        
        	
        } else if (kanjiKanaPairList != null) {
        	id = kanjiKanaPairList.get(0).getEntry().getEntryId();
        	
        	dictionaryEntryKanji = kanjiKanaPairList.get(0).getKanji();
    		dictionaryEntryKana = kanjiKanaPairList.get(0).getKana();
    		dictionaryEntryRomaji = kanjiKanaPairList.get(0).getRomaji();
    		
        } else {
        	throw new RuntimeException(); // to nigdy nie powinno zdarzyc sie
        }
        
		String defaultSuggestion = messageSource.getMessage("wordDictionaryDetails.page.suggestion.default", 
				new Object[] { dictionaryEntryKanji != null ? dictionaryEntryKanji : "-",
						dictionaryEntryKana, dictionaryEntryRomaji, String.valueOf(id)
				}, Locale.getDefault());
		
        
        // dodaj okienko z sugestia
        return addSuggestionDialog(messageSource, defaultSuggestion);		
	}
	
	private String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}

	/*
	private String getMessage(String code, String[] args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
	*/
	
	public DictionaryEntry getDictionaryEntry() {
		return dictionaryEntry;
	}

	public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}

	public JMdict.Entry getDictionaryEntry2() {
		return dictionaryEntry2;
	}

	public void setDictionaryEntry2(JMdict.Entry dictionaryEntry2) {
		this.dictionaryEntry2 = dictionaryEntry2;
	}

	public DictionaryEntryType getForceDictionaryEntryType() {
		return forceDictionaryEntryType;
	}

	public void setForceDictionaryEntryType(DictionaryEntryType forceDictionaryEntryType) {
		this.forceDictionaryEntryType = forceDictionaryEntryType;
	}
	
	private static class GrammaFormConjugateAndExampleEntry {
		private int dictionaryEntryIdx;
		private DictionaryEntry dictionaryEntry;
		
		private List<GrammaFormConjugateAndExampleEntryForDictionaryType> grammaFormConjugateAndExampleEntryForDictionaryTypeList = new ArrayList<>();
		
		public GrammaFormConjugateAndExampleEntry(DictionaryEntry dictionaryEntry, int dictionaryEntryIdx) {
			this.dictionaryEntry = dictionaryEntry;
			this.dictionaryEntryIdx = dictionaryEntryIdx;
		}
		
		public void addDictionaryEntryTypeGrammaFormConjugate(DictionaryEntryType dictionaryEntryType, List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList, Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache) {
			grammaFormConjugateAndExampleEntryForDictionaryTypeList.add(new GrammaFormConjugateAndExampleEntryForDictionaryType(dictionaryEntryType, grammaFormConjugateGroupTypeElementsList, grammaFormCache));
		}
	}
	
	private static class GrammaFormConjugateAndExampleEntryForDictionaryType {
		private DictionaryEntryType dictionaryEntryType;
		
		private List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList;
		private Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache;
		
		private List<ExampleGroupTypeElements> exampleGroupTypeElementsList;
		
		public GrammaFormConjugateAndExampleEntryForDictionaryType(DictionaryEntryType dictionaryEntryType, 
				List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList, Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache) {
			
			this.dictionaryEntryType = dictionaryEntryType;
			
			this.grammaFormConjugateGroupTypeElementsList = grammaFormConjugateGroupTypeElementsList;
			this.grammaFormCache = grammaFormCache;
		}

		public void setExampleGroupTypeElementsList(List<ExampleGroupTypeElements> exampleGroupTypeElementsList) {
			this.exampleGroupTypeElementsList = exampleGroupTypeElementsList;			
		}		
	}
	
	private static class TabObjectToProcessCreateDivWrapper {
		private Object objectToProcess;
		private Menu menu;
		
		public TabObjectToProcessCreateDivWrapper(Object objectToProcess, Menu menu) {
			super();
			this.objectToProcess = objectToProcess;
			this.menu = menu;
		}		
	}
}
