package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.Attribute;
import pl.idedyk.japanese.dictionary.api.dto.AttributeType;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.FuriganaEntry;
import pl.idedyk.japanese.dictionary.api.dto.GroupWithTatoebaSentenceList;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.dto.TatoebaSentence;
import pl.idedyk.japanese.dictionary.api.example.ExampleManager;
import pl.idedyk.japanese.dictionary.api.example.dto.ExampleGroupTypeElements;
import pl.idedyk.japanese.dictionary.api.example.dto.ExampleResult;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.api.gramma.GrammaConjugaterManager;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateGroupTypeElements;
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
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.KanjiKanaPair;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSense;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSenseEntry;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSenseEntryGloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiAdditionalInfoEnum;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingAdditionalInfoEnum;

public class GenerateWordDictionaryDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
	private static final long serialVersionUID = 1L;
	
	private DictionaryEntry dictionaryEntry;
	private DictionaryEntryType forceDictionaryEntryType;
	
	private JMdict.Entry dictionaryEntry2;
	private KanjiKanaPair dictionaryEntry2KanjiKanaPair;
			
	private MessageSource messageSource;
	
	private DictionaryManager dictionaryManager;
	private Properties applicationProperties;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		ServletRequest servletRequest = pageContext.getRequest();
		
		String userAgent = null;
		
		if (servletRequest instanceof HttpServletRequest) {			
			HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
			
			userAgent = httpServletRequest.getHeader("User-Agent");			
		}
		
		boolean mobile = Utils.isMobile(userAgent);
		
		//
		
		// pobieramy sens dla wybranej pary kanji i kana
		if (dictionaryEntry2 != null) {
			
			List<KanjiKanaPair> kanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(dictionaryEntry2);
			
			// szukamy konkretnego znaczenia dla naszego slowa
			dictionaryEntry2KanjiKanaPair = Dictionary2HelperCommon.findKanjiKanaPair(kanjiKanaPairList, dictionaryEntry);
			
		} else {
			dictionaryEntry2KanjiKanaPair = null;
		}		

		//
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
		this.applicationProperties = (Properties)webApplicationContext.getBean("applicationProperties");
		
		try {
            JspWriter out = pageContext.getOut();

            if (dictionaryEntry == null) {
            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.null")));
            	
            	errorDiv.render(out);
            	
            	return SKIP_BODY;
            }

            Div mainContentDiv = new Div();
            
            if (dictionaryEntry.isName() == true) {
            	
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
    		Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache = new HashMap<GrammaFormConjugateResultType, GrammaFormConjugateResult>();
            
            Div grammaFormConjugateDiv = generateGrammaFormConjugate(mainMenu, grammaFormCache);
            
            if (grammaFormConjugateDiv != null) {
            	contentDiv.addHtmlElement(grammaFormConjugateDiv);
            }
            
            // przyklady gramatyczne
            Div exampleDiv = generateExample(mainMenu, grammaFormCache);
            
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
        }
	}

	private H generateTitle() throws IOException {
			
		H pageHeader = new H(4);
				
		pageHeader.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.title")));
		
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
		
		// kanji
		Div kanjiDiv = generateKanjiSection(mainInfoMenu, mobile);
		
		if (kanjiDiv != null) {
			panelBody.addHtmlElement(kanjiDiv);	
			
			panelBody.addHtmlElement(new Hr());
		}
		
		// znaczenie znakow kanji
        Div knownKanjiDiv = generateKnownKanjiDiv(mainInfoMenu, mobile);
        
        if (knownKanjiDiv != null) {
        	panelBody.addHtmlElement(knownKanjiDiv);
        	panelBody.addHtmlElement(new Hr());
        }
		
		// czytanie
		Div readingDiv = generateReadingSection(mainInfoMenu, mobile);
		panelBody.addHtmlElement(readingDiv);
		panelBody.addHtmlElement(new Hr());
		
        // tlumaczenie
        Div translate = generateTranslateSection(mainInfoMenu);
        panelBody.addHtmlElement(translate);
        
        // generuj informacje dodatkowe
        Div additionalInfo = generateAdditionalInfo(mainInfoMenu);

        if (additionalInfo != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(additionalInfo);
        }
        
        // czesc mowy
        Div wordTypeDiv = generateWordType(mainInfoMenu);
        
        if (wordTypeDiv != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(wordTypeDiv);
        }
        
        // dodatkowe atrybuty
		Div additionalAttributeDiv = generateAttribute(mainInfoMenu, mobile);

        if (additionalAttributeDiv != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(additionalAttributeDiv);
        }
        		
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private Div generateKanjiSection(Menu menu, boolean mobile) throws IOException, DictionaryException {
		
		Div kanjiDiv = new Div();
		
		final String kanjiDrawId = "kanjiDrawId";
		        
		String prefixKana = dictionaryEntry.getPrefixKana();

		if (prefixKana != null && prefixKana.length() == 0) {
			prefixKana = null;
		}
		
		final StringBuffer kanjiSb = new StringBuffer();
        
		boolean addKanjiWrite = false;
		
        if (dictionaryEntry.isKanjiExists() == true) {
        	
			if (prefixKana != null) {
				kanjiSb.append("(").append(prefixKana).append(") ");
			}

			kanjiSb.append(dictionaryEntry.getKanji());

			addKanjiWrite = true;
        	
        } else {
			kanjiSb.append("-");

			addKanjiWrite = false;
		}
        
        if (addKanjiWrite == false) {
        	return null;
        }
        	
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// kanji - tytul
    	Div kanjiTitleDiv = new Div("col-md-1");
    	
    	H kanjiTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	kanjiTitleH4.setId("kanjiTitleId");
    	
    	kanjiTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.title")));
    	menu.getChildMenu().add(new Menu(kanjiTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.title")));
    	
    	kanjiTitleDiv.addHtmlElement(kanjiTitleH4);
    	
    	row1Div.addHtmlElement(kanjiTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	kanjiDiv.addHtmlElement(row1Div);
    	    	        	        	       		
        List<FuriganaEntry> furiganaEntries = dictionaryManager.getFurigana(dictionaryEntry);
        
        // sprawdzenie, czy mamy dane do pisania wszystkich znakow
        boolean isAllCharactersStrokePathsAvailableForWord = dictionaryManager.isAllCharactersStrokePathsAvailableForWord(dictionaryEntry.getKanji());
    	            
        if (furiganaEntries != null && furiganaEntries.size() > 0 && addKanjiWrite == true && isAllCharactersStrokePathsAvailableForWord == true) {
        	
        	for (FuriganaEntry currentFuriganaEntry : furiganaEntries) {
        		
				List<String> furiganaKanaParts = currentFuriganaEntry.getKanaPart();
				List<String> furiganaKanjiParts = currentFuriganaEntry.getKanjiPart();
				
				// wiersz ze znakiem kanji
	        	Div row2Div = new Div("row");
	        	
	        	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
				
	        	// komorka ze znakiem kanji
	        	Div kanjiDivBody = new Div("col-md-10");
	        	
	        	// tabelka ze znakiem kanji
				Table kanjiTable = new Table();
				
				// czytanie
				Tr kanaPartTr = new Tr(null, "font-size: 123%; text-align:center;");
							
				for (int idx = 0; idx < furiganaKanaParts.size(); ++idx) {
					
					String currentKanaPart = furiganaKanaParts.get(idx);
					
					Td currentKanaPartTd = new Td();
					
					currentKanaPartTd.addHtmlElement(new Text(currentKanaPart));
					
					kanaPartTr.addHtmlElement(currentKanaPartTd);
				}
				
				kanjiTable.addHtmlElement(kanaPartTr);
							
				// znaki kanji
				Tr kanjiKanjiTr = new Tr(null, "font-size: 300%; text-align:center;");
				
				kanjiTable.addHtmlElement(kanjiKanjiTr);
				
				for (int idx = 0; idx < furiganaKanjiParts.size(); ++idx) {
					
					String currentKanjiPart = furiganaKanjiParts.get(idx);
					
					Td currentKanjiPartTd = new Td();
					
					currentKanjiPartTd.addHtmlElement(new Text(currentKanjiPart));
					
					kanjiKanjiTr.addHtmlElement(currentKanjiPartTd);
				}	
				
				// komorka z guziczkiem
				Td kanjiDrawButtonTd = new Td();
				
				Div kanjiDrawButtonDivBody = new Div("col-md-1");
				
				Button kanjiDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanjiDrawId, 
						getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.showKanjiDraw"));

				kanjiDrawButtonDivBody.addHtmlElement(kanjiDrawButton);
				kanjiDrawButtonTd.addHtmlElement(kanjiDrawButtonDivBody);
				
				if (mobile == false) {
					
					// przerwa
					kanjiKanjiTr.addHtmlElement(new Td("col-md-1"));

					kanjiKanjiTr.addHtmlElement(kanjiDrawButtonTd);
										
				} else {
					
					Tr kanjiKanjiForWritingButtonTr = new Tr(null, null);
					
					kanjiTable.addHtmlElement(kanjiKanjiForWritingButtonTr);
					
					kanjiDrawButtonTd.setColspan(String.valueOf(furiganaKanjiParts.size() + 2));

					kanjiKanjiForWritingButtonTr.addHtmlElement(kanjiDrawButtonTd);			
				}
				
				kanjiDivBody.addHtmlElement(kanjiTable);
				row2Div.addHtmlElement(kanjiDivBody);					
				
				kanjiDiv.addHtmlElement(row2Div);
        	}
        	
        } else {
        	
        	Div row2Div = new Div("row");
        	
        	if (mobile == false) {
        		row2Div.addHtmlElement(new Div("col-md-1"));
        	}
        	
        	Div kanjiDivBody = new Div("col-md-10");
        	
        	row2Div.addHtmlElement(kanjiDivBody);
        	
        	Div kanjiDivText = new Div(null, "font-size: 200%");
        	Text kanjiText = new Text(kanjiSb.toString());
        	
        	kanjiDivText.addHtmlElement(kanjiText);
        	kanjiDivBody.addHtmlElement(kanjiDivText);
        	
        	kanjiDiv.addHtmlElement(row2Div);
        }

        // skrypt otwierajacy okienko
        kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanjiDrawId, dictionaryEntry.getKanji().length(), mobile));
        
        // tworzenie okienka rysowania znaku kanji
        kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, dictionaryEntry.getKanji(), kanjiDrawId));
        
    	// informacje dodatkowe do kanji
        if (dictionaryEntry2KanjiKanaPair != null && dictionaryEntry2KanjiKanaPair.getKanjiInfo() != null) {
        	
        	List<KanjiAdditionalInfoEnum> kanjiAdditionalInfoList = dictionaryEntry2KanjiKanaPair.getKanjiInfo().getKanjiAdditionalInfoList();
        	
        	List<String> kanjiAdditionalInfoListString = Dictionary2HelperCommon.translateToPolishKanjiAdditionalInfoEnum(kanjiAdditionalInfoList);
        	
        	if (kanjiAdditionalInfoList != null && kanjiAdditionalInfoList.size() > 0) {
        		
            	Div kanjiAdditionalInfoDiv = new Div("row");
            	
            	kanjiAdditionalInfoDiv.addHtmlElement(new Div());
            	
            	Div kanjiAdditionalInfoDivBody = new Div("col-md-10", "margin-top: 15px");
            	
            	kanjiAdditionalInfoDivBody.addHtmlElement(new Text(pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(kanjiAdditionalInfoListString, "; ")));

            	kanjiAdditionalInfoDiv.addHtmlElement(kanjiAdditionalInfoDivBody);
            	
            	kanjiDiv.addHtmlElement(kanjiAdditionalInfoDiv);
        	}        	
        }
        
        return kanjiDiv;
	}
	
	private Div generateReadingSection(Menu menu, boolean mobile) throws IOException, DictionaryException {
		
		Div readingDiv = new Div();
		
		String prefixKana = dictionaryEntry.getPrefixKana();
		String prefixRomaji = dictionaryEntry.getPrefixRomaji();
		
		String kana = dictionaryEntry.getKana();
		String romaji = dictionaryEntry.getRomaji();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// kanji - tytul
    	Div readingTitleDiv = new Div("col-md-1");
    	
    	H readingTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	readingTitleH4.setId("readingId");
    	
    	readingTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.reading.title")));
    	menu.getChildMenu().add(new Menu(readingTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.reading.title")));
    	
    	readingTitleDiv.addHtmlElement(readingTitleH4);
    	
    	row1Div.addHtmlElement(readingTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	readingDiv.addHtmlElement(row1Div);

		// wiersz ze znakiem kanji
    	Div row2Div = new Div("row");
    	readingDiv.addHtmlElement(row2Div);
		
    	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    	
        class IdAndText {
        	
        	public String id;
        	
        	public String text;

			public IdAndText(String id, String text) {
				this.id = id;
				this.text = text;
			}        	
        }
        
        List<IdAndText> idAndTextList = new ArrayList<IdAndText>();
        
    	Div readingBodyDiv = new Div("col-md-11");
    	row2Div.addHtmlElement(readingBodyDiv);

        Table readingTable = new Table();
        readingBodyDiv.addHtmlElement(readingTable);
        	
    	final String kanaDrawId = "kanaDrawId";
    	
		StringBuffer fullKana = new StringBuffer();
		StringBuffer fullRomaji = new StringBuffer();
		
		if (prefixKana != null && prefixKana.equals("") == false) {
			fullKana.append("(").append(prefixKana).append(") ");
		}

		fullKana.append(kana);

		if (prefixRomaji != null && prefixRomaji.equals("") == false) {
			fullRomaji.append("(").append(prefixRomaji).append(") ");
		}

		fullRomaji.append(romaji);
		
		Tr readingTableTr = new Tr();
		readingTable.addHtmlElement(readingTableTr);
		
		Td readingTableKanaTd = new Td(null, "font-size: 150%; padding: 0 50px 5px 0;");
		readingTableTr.addHtmlElement(readingTableKanaTd);
					
		readingTableKanaTd.addHtmlElement(new Text(fullKana.toString()));
					
		idAndTextList.add(new IdAndText(kanaDrawId, fullKana.toString()));
		
		Td readingTableRomajiTd = new Td(null, "margin-top: 0px; margin-bottom: 5px; padding: 5px 50px 5px 0;");
		readingTableTr.addHtmlElement(readingTableRomajiTd);
		
		H readingTableRomajiTdH4 = new H(4);
		readingTableRomajiTd.addHtmlElement(readingTableRomajiTdH4);
		
		readingTableRomajiTdH4.addHtmlElement(new Text(fullRomaji.toString()));

		// guzik rysowania
		Td readingTableButtonTd = new Td(null, "padding: 0 50px 5px 0;");
		
		Button kanaDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanaDrawId, 
				getMessage("wordDictionaryDetails.page.dictionaryEntry.reading.showKanaDraw"));
		
		readingTableButtonTd.addHtmlElement(kanaDrawButton);
		
		if (mobile == false) {
			
			readingTableTr.addHtmlElement(readingTableButtonTd);			
			
		} else {
			
			Tr readingTableTrForMobile = new Tr();
			
			readingTable.addHtmlElement(readingTableTrForMobile);
			
			readingTableButtonTd.setColspan("2");
			
			readingTableTrForMobile.addHtmlElement(readingTableButtonTd);
		}

        for (IdAndText idAndText : idAndTextList) {
        	        	
            // skrypt otwierajacy okienko
        	readingDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(idAndText.id, idAndText.text.length(), mobile));
            
            // tworzenie okienka rysowania znaku kanji
        	readingDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, idAndText.text, idAndText.id));
		}
        
    	// informacje dodatkowe do czytania
        if (dictionaryEntry2KanjiKanaPair != null && dictionaryEntry2KanjiKanaPair.getReadingInfo() != null) {
        	
        	List<ReadingAdditionalInfoEnum> readingAdditionalInfoList = dictionaryEntry2KanjiKanaPair.getReadingInfo().getReadingAdditionalInfoList();
        	
        	List<String> readingAdditionalInfoListString = Dictionary2HelperCommon.translateToPolishReadingAdditionalInfoEnum(readingAdditionalInfoList);
        	
        	if (readingAdditionalInfoList != null && readingAdditionalInfoList.size() > 0) {
        		
            	Div readingAdditionalInfoDiv = new Div("row");
            	
            	readingAdditionalInfoDiv.addHtmlElement(new Div());
            	
            	Div readingAdditionalInfoDivBody = new Div("col-md-10", "margin-top: 15px");
            	
            	readingAdditionalInfoDivBody.addHtmlElement(new Text(pl.idedyk.japanese.dictionary.api.dictionary.Utils.convertListToString(readingAdditionalInfoListString, "; ")));

            	readingAdditionalInfoDiv.addHtmlElement(readingAdditionalInfoDivBody);
            	
            	readingDiv.addHtmlElement(readingAdditionalInfoDiv);
        	}        	
        }

                
        return readingDiv;
	}
	
	private Div generateTranslateSection(Menu menu) throws IOException {
		
		final String titleId = "translateId";
		final String titleTitle = getMessage("wordDictionaryDetails.page.dictionaryEntry.translate.title");
		
		
		if (dictionaryEntry2KanjiKanaPair == null) { // generowanie po staremu
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
	    	
	    	PrintableSense printableSense = Dictionary2HelperCommon.getPrintableSense(dictionaryEntry2KanjiKanaPair);
			
			// mamy znaczenia
			for (int senseIdx = 0; senseIdx < printableSense.getSenseEntryList().size(); ++senseIdx) {
				
				PrintableSenseEntry printableSenseEntry = printableSense.getSenseEntryList().get(senseIdx);
								
				Div senseDiv = new Div("row");
				
				// numer znaczenia
				Div senseNoDiv = new Div("col-md-1");
				
				H senseNoDivH = new H(4, null, "margin-top: 20px; text-align: right");
				
				senseNoDivH.addHtmlElement(new Text("" + (senseIdx + 1)));
				
				senseNoDiv.addHtmlElement(senseNoDivH);
									
				senseDiv.addHtmlElement(senseNoDiv);
												
				//
				
				Div glossPolListDiv = new Div("col-md-11");
												
				Table table = new Table();
				
				// czesci mowy
				if (printableSenseEntry.getPolishPartOfSpeechValue() != null) {					
					Tr tr = new Tr();
					
					Td translateToPolishPartOfSpeechEnumTd = new Td();
					
					translateToPolishPartOfSpeechEnumTd.addHtmlElement(new Text(printableSenseEntry.getPolishPartOfSpeechValue()));
					
					tr.addHtmlElement(translateToPolishPartOfSpeechEnumTd);
					
					table.addHtmlElement(tr);
				}				
				
				for (int currentGlossIdx = 0; currentGlossIdx < printableSenseEntry.getGlossList().size(); ++currentGlossIdx) {
					
					PrintableSenseEntryGloss printableSenseEntryGloss = printableSenseEntry.getGlossList().get(currentGlossIdx);
					
					//
					
					int marginBottom = currentGlossIdx != printableSenseEntry.getGlossList().size() - 1 ? 5 : 0; 
					
					Tr tr = new Tr();

					// dodanie pojedynczego znaczenia
					Td glossPolValueTd = new Td();
					
					// wyroznienie znaczenia
					H glossPolTdH4 = new H(4, null, "margin-top: 0px;margin-bottom: " + marginBottom + "px");
					
					glossPolTdH4.addHtmlElement(new Text(printableSenseEntryGloss.getGlossValue()));
					
					glossPolValueTd.addHtmlElement(glossPolTdH4);
											
					tr.addHtmlElement(glossPolValueTd);
					
					// sprawdzenie, czy wystepuje dodatkowy typ znaczenia
					if (printableSenseEntryGloss.getGlossValueGType() != null) {						
						Td glossPolGTypeTd = new Td();
						
						Div glossPolGTypeTdDiv = new Div(null, "margin-top: 0px;margin-left: 25px;margin-bottom: " + marginBottom + "px");
						
						glossPolGTypeTdDiv.addHtmlElement(new Text(printableSenseEntryGloss.getGlossValueGType()));
						
						glossPolGTypeTd.addHtmlElement(glossPolGTypeTdDiv);
						
						tr.addHtmlElement(glossPolGTypeTd);
					}					
										
					table.addHtmlElement(tr);					
				}
				
				// informacje dodatkowe												
				if (printableSenseEntry.getAdditionalInfoValue() != null) {					
					Tr tr = new Tr();
					
					Td senseAdditionalPolTd = new Td();
					
					senseAdditionalPolTd.addHtmlElement(new Text(printableSenseEntry.getAdditionalInfoValue()));
					
					tr.addHtmlElement(senseAdditionalPolTd);
					
					table.addHtmlElement(tr);
				}
				
				// przerwa
				{
					Tr tr = new Tr();
					
					Td td = new Td();
					
					H h4 = new H(4, null, "margin-bottom: 15px");
					
					td.addHtmlElement(h4);
					
					tr.addHtmlElement(td);
					
					table.addHtmlElement(tr);
				}
				
				glossPolListDiv.addHtmlElement(table);
				
				//
								
				senseDiv.addHtmlElement(glossPolListDiv);
				
				resultDiv.addHtmlElement(senseDiv);
			}
						
			return resultDiv;
		}
	}
	
	private Div generateAdditionalInfo(Menu menu) throws IOException {
		
		if (dictionaryEntry2KanjiKanaPair != null) { // dla slownika w formacie drugim nie generuj tej sekcji; informacje te znajda sie w sekcji znaczen
			return null;
		}
		
		String info = dictionaryEntry.getInfo();
		
		String kanji = dictionaryEntry.getKanji();
		
		int special = 0;
		
		if (kanji != null && isSmTsukiNiKawatteOshiokiYo(kanji) == true) {
			special = 1;
			
		} else if (kanji != null && isButaMoOdateryaKiNiNoboru(kanji) == true) {
			special = 2;
			
		} else if (kanji != null && isTakakoOkamura(kanji) == true) {
			special = 3;
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
		
		if (special == 0 || special == 3) {
			
			H additionalInfoTextH4 = new H(4);
			row2TableTrTd1.addHtmlElement(additionalInfoTextH4);
			
			additionalInfoTextH4.addHtmlElement(new Text(info));						
		}
		
		if (special > 0) {
			
			Div specialDiv = new Div(null, "font-family:monospace; font-size: 40%");
			row2TableTrTd1.addHtmlElement(specialDiv);
			
			if (special == 1) {
				specialDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.info.special.sm_tsuki_ni_kawatte_oshioki_yo")));
				
			} else if (special == 2) {
				specialDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.info.special.buta_mo_odaterya_ki_ni_noboru")));
				
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
	
	private Div generateWordType(Menu menu) throws IOException {
		
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
    	
    	if (addableDictionaryEntryTypeInfoCounter > 1 && dictionaryEntry.isName() == false) { // info o odmianach
    		
        	Div row2Div = new Div("row");
        	wordTypeDiv.addHtmlElement(row2Div);

        	Div wordTypeInfoDiv = new Div("col-md-12", "margin: -15px 0 0px 0");
        	row2Div.addHtmlElement(wordTypeInfoDiv);
        	
    		H wordTypeInfoH5 = new H(5);
    		wordTypeInfoDiv.addHtmlElement(wordTypeInfoH5);
    		
    		wordTypeInfoH5.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.forceDictionaryEntryType.info")));	
    	}
    	
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
			}
		}
		
		return wordTypeDiv;
	}
		
	private Div generateAttribute(Menu menu, boolean mobile) throws IOException, DictionaryException {
		
		List<Attribute> attributeList = dictionaryEntry.getAttributeList().getAttributeList();
		
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
    	
    	attributeTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.atribute.title")));
    	menu.getChildMenu().add(new Menu(attributeTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.atribute.title")));
    	
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
			
			if (attributeType == AttributeType.VERB_TRANSITIVITY_PAIR || attributeType == AttributeType.VERB_INTRANSITIVITY_PAIR || attributeType == AttributeType.ALTERNATIVE ||
					attributeType == AttributeType.RELATED || attributeType == AttributeType.ANTONYM) {
				
				Integer referenceWordId = Integer.parseInt(currentAttribute.getAttributeValue().get(0));

				final DictionaryEntry referenceDictionaryEntry = dictionaryManager.getDictionaryEntryById(referenceWordId);

				if (referenceDictionaryEntry != null) {
					
					Tr row2TableTr = new Tr();
					row2Table.addHtmlElement(row2TableTr);
					
					Td row2TableTrTd1 = new Td();
					row2TableTr.addHtmlElement(row2TableTrTd1);

		    		H currentAttributeH = new H(4, null, "margin-top: 0px; margin-bottom: 5px; margin-left: 30px");
		    		row2TableTrTd1.addHtmlElement(currentAttributeH);
		    		
		    		currentAttributeH.addHtmlElement(new Text(attributeType.getName()));

		    		// czasownik przechodni / nieprzechodni / alternatywa
					String referenceDictionaryEntryKana = referenceDictionaryEntry.getKana();
					String referenceDictionaryEntryRomaji = referenceDictionaryEntry.getRomaji();

					StringBuffer referenceDictionaryEntrySb = new StringBuffer();

					if (referenceDictionaryEntry.isKanjiExists() == true) {
						referenceDictionaryEntrySb.append(referenceDictionaryEntry.getKanji()).append(", ");
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
										
		            String link = LinkGenerator.generateDictionaryEntryDetailsLink(
		            		pageContext.getServletContext().getContextPath(), referenceDictionaryEntry, null);
		            
					A linkButton = new A("btn btn-default");
					row2TableTrTd3Div.addHtmlElement(linkButton);

					linkButton.setHref(link);
					
					linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.atribute.referenceDictionaryEntry.show")));			
				}				
			}
    	}		
		
		return attributeDiv;		
	}
	
	private Div generateKnownKanjiDiv(Menu menu, boolean mobile) throws DictionaryException {
		
		if (dictionaryEntry.isKanjiExists() == false) {
			return null;
		}
		
		List<KanjiEntry> knownKanji = dictionaryManager.findKnownKanji(dictionaryEntry.getKanji());
		
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

		for (KanjiEntry currentKnownKanjiEntry : knownKanji) {
			
			Tr row2TableTr = new Tr();
			row2Table.addHtmlElement(row2TableTr);
			
			// kanji
			Td row2TableTrKanjiTd = new Td(null, "font-size: 150%; padding: 0 50px 5px 0;");
			row2TableTr.addHtmlElement(row2TableTrKanjiTd);
			
			row2TableTrKanjiTd.addHtmlElement(new Text(currentKnownKanjiEntry.getKanji()));
			
			// znaczenie
			List<String> currentKnownKanjiPolishTranslates = currentKnownKanjiEntry.getPolishTranslates();
			
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

		List<String> exampleSentenceGroupIdsList = dictionaryEntry.getExampleSentenceGroupIdsList();
		
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
	
	private Div generateGrammaFormConjugate(Menu mainMenu, Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache) throws IOException {
		
		// wylicz odmiany gramatyczne
		List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList = 
				GrammaConjugaterManager.getGrammaConjufateResult(dictionaryManager.getKeigoHelper(), dictionaryEntry, grammaFormCache, forceDictionaryEntryType, false);
		
		if (grammaFormConjugateGroupTypeElementsList == null || grammaFormConjugateGroupTypeElementsList.size() == 0) {
			return null;
		}
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		
		h3Title.setId("grammaFormConjugateId");
		
		Menu grammaFormConjugateMenu = null;
		
		if (forceDictionaryEntryType == null) {
			h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugate")));
			
			grammaFormConjugateMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugate"));			
		} else {
			h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugateWithDictionaryEntryType", new String[] { forceDictionaryEntryType.getName() })));
			
			grammaFormConjugateMenu = new Menu(h3Title.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.grammaFormConjugateWithDictionaryEntryType", new String[] { forceDictionaryEntryType.getName() }));
		}	
		
		mainMenu.getChildMenu().add(grammaFormConjugateMenu);
		
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		panelDiv.addHtmlElement(panelBody);
		
		for (int grammaFormConjugateGroupTypeElementsListIdx = 0; grammaFormConjugateGroupTypeElementsListIdx < grammaFormConjugateGroupTypeElementsList.size(); ++grammaFormConjugateGroupTypeElementsListIdx) {
			
			GrammaFormConjugateGroupTypeElements currentGrammaFormConjugateGroupTypeElements = grammaFormConjugateGroupTypeElementsList.get(grammaFormConjugateGroupTypeElementsListIdx);
			
			if (currentGrammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().isShow() == false) {
				continue;
			}
			
			panelBody.addHtmlElement(generateGrammaFormConjugateGroupTypeElements(currentGrammaFormConjugateGroupTypeElements, grammaFormConjugateMenu));
			
			if (grammaFormConjugateGroupTypeElementsListIdx != grammaFormConjugateGroupTypeElementsList.size() - 1) {
				panelBody.addHtmlElement(new Hr());
			}
		}
		
		return panelDiv;
	}
	
	private Div generateGrammaFormConjugateGroupTypeElements(GrammaFormConjugateGroupTypeElements grammaFormConjugateGroupTypeElements, Menu menu) {
		
		Div resultDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div row1TitleDiv = new Div("col-md-12");
    	
    	H row1TitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	row1TitleH4.setId(grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().toString());  	
    	
    	row1TitleH4.addHtmlElement(new Text(grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().getName()));
    	
    	String grammaFormConjugateGroupTypeInfo = grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().getInfo();
    	
    	if (grammaFormConjugateGroupTypeInfo != null) {
			H infoH5 = new H(5, null, "margin-top: 0px;");
			
			infoH5.addHtmlElement(new Text(grammaFormConjugateGroupTypeInfo));
			
			row1TitleH4.addHtmlElement(infoH5);			
    	}
    	
    	Menu row1Menu = new Menu(row1TitleH4.getId(), grammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType().getName());
    	
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
		    	
		    	currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4.setId(currentGrammaFormConjugateResultId);
		    	
		    	currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4.addHtmlElement(new Text(currentGrammaFormConjugateResult.getResultType().getName()));
		    	
		    	row1Menu.getChildMenu().add(new Menu(currentGrammaFormConjugateResultSectionBodyDivTitleDivTitleH4.getId(), currentGrammaFormConjugateResult.getResultType().getName()));
		    	
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
	
	private Div generateExample(Menu mainMenu, Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache) throws IOException {
		
		List<ExampleGroupTypeElements> exampleGroupTypeElementsList = ExampleManager.getExamples(
				dictionaryManager.getKeigoHelper(), dictionaryEntry, grammaFormCache, forceDictionaryEntryType, false);
		
		if (exampleGroupTypeElementsList == null || exampleGroupTypeElementsList.size() == 0) {
			return null;
		}
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		
		h3Title.setId("exampleId");
		
		final int maxMenuSize = 20;
		Menu exampleMenu = null;
		int menuCounter = 0;
				
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		panelDiv.addHtmlElement(panelBody);		
		
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
	
		return panelDiv;
	}
	
	private IHtmlElement generateExampleGroupTypeElements(ExampleGroupTypeElements exampleGroupTypeElements, Menu menu) {
		
		Div resultDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div row1TitleDiv = new Div("col-md-12");
    	
    	H row1TitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	row1TitleH4.setId(exampleGroupTypeElements.getExampleGroupType().toString());  	
    	
    	row1TitleH4.addHtmlElement(new Text(exampleGroupTypeElements.getExampleGroupType().getName()));
    	
    	Menu row1Menu = new Menu(row1TitleH4.getId(), exampleGroupTypeElements.getExampleGroupType().getName());
    	
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
        
        int id = dictionaryEntry.getId();
		String dictionaryEntryKanji = dictionaryEntry.getKanji();
		String dictionaryEntryKana = dictionaryEntry.getKana();
		String dictionaryEntryRomaji = dictionaryEntry.getRomaji();        

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

	private String getMessage(String code, String[] args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
	
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
}
