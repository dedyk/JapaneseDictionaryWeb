package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.Attribute;
import pl.idedyk.japanese.dictionary.api.dto.AttributeType;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.FuriganaEntry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.example.ExampleManager;
import pl.idedyk.japanese.dictionary.api.example.dto.ExampleGroupTypeElements;
import pl.idedyk.japanese.dictionary.api.example.dto.ExampleResult;
import pl.idedyk.japanese.dictionary.api.gramma.GrammaConjugaterManager;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateGroupTypeElements;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateResult;
import pl.idedyk.japanese.dictionary.api.gramma.dto.GrammaFormConjugateResultType;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Button.ButtonType;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.IHtmlElement;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;

public class GenerateWordDictionaryDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
	private static final long serialVersionUID = 1L;
	
	private DictionaryEntry dictionaryEntry;
	
	private DictionaryEntryType forceDictionaryEntryType;
		
	private MessageSource messageSource;
	
	private DictionaryManager dictionaryManager;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
		
		try {
            JspWriter out = pageContext.getOut();

            if (dictionaryEntry == null) {
            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.null")));
            	
            	errorDiv.render(out);
            	
            	return SKIP_BODY;
            }
            
            Div mainContentDiv = new Div();
            
            Menu mainMenu = new Menu(null, null);

            // tytul strony
            mainContentDiv.addHtmlElement(generateTitle());
                        
            Div contentDiv = new Div("col-md-10");
            mainContentDiv.addHtmlElement(contentDiv);
            
            // generowanie informacji podstawowych
            contentDiv.addHtmlElement(generateMainInfo(mainMenu));
            
            // odmiany gramatyczne
    		Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache = new HashMap<GrammaFormConjugateResultType, GrammaFormConjugateResult>();
            
            Div grammaFormConjugateDiv = generateGrammaFormConjugate(mainMenu, grammaFormCache);
            
            if (grammaFormConjugateDiv != null) {
            	contentDiv.addHtmlElement(grammaFormConjugateDiv);
            }
            
            // przyklady
            Div exampleDiv = generateExample(mainMenu, grammaFormCache);
            
            if (exampleDiv != null) {
            	contentDiv.addHtmlElement(exampleDiv);
            }
            
            // dodaj menu
            mainContentDiv.addHtmlElement(generateMenu(mainMenu));

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
		
		return pageHeader;
	}
	
	private Div generateMainInfo(Menu mainMenu) throws IOException {
		
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
		Div kanjiDiv = generateKanjiSection(mainInfoMenu);
		
		if (kanjiDiv != null) {
			panelBody.addHtmlElement(kanjiDiv);	
			
			panelBody.addHtmlElement(new Hr());
		}
		
		// znaczenie znakow kanji
        Div knownKanjiDiv = generateKnownKanjiDiv(mainInfoMenu);
        
        if (knownKanjiDiv != null) {
        	panelBody.addHtmlElement(knownKanjiDiv);
        	panelBody.addHtmlElement(new Hr());
        }
		
		// czytanie
		Div readingDiv = generateReadingSection(mainInfoMenu);
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
		Div additionalAttributeDiv = generateAttribute(mainInfoMenu);

        if (additionalAttributeDiv != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(additionalAttributeDiv);
        }
        		
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private Div generateKanjiSection(Menu menu) throws IOException {
		
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
    	            
        if (furiganaEntries != null && furiganaEntries.size() > 0 && addKanjiWrite == true) {
        	
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
				
				for (int idx = 0; idx < furiganaKanjiParts.size(); ++idx) {
					
					String currentKanjiPart = furiganaKanjiParts.get(idx);
					
					Td currentKanjiPartTd = new Td();
					
					currentKanjiPartTd.addHtmlElement(new Text(currentKanjiPart));
					
					kanjiKanjiTr.addHtmlElement(currentKanjiPartTd);
				}	

				// przerwa
				kanjiKanjiTr.addHtmlElement(new Td("col-md-1"));
				
				// komorka z guziczkiem
				Td kanjiDrawButtonTd = new Td();
				
				Div kanjiDrawButtonDivBody = new Div("col-md-1");
				
				Button kanjiDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanjiDrawId, 
						getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.showKanjiDraw"));

				kanjiDrawButtonDivBody.addHtmlElement(kanjiDrawButton);
				kanjiDrawButtonTd.addHtmlElement(kanjiDrawButtonDivBody);

				kanjiKanjiTr.addHtmlElement(kanjiDrawButtonTd);
				kanjiTable.addHtmlElement(kanjiKanjiTr);
				
				kanjiDivBody.addHtmlElement(kanjiTable);
				row2Div.addHtmlElement(kanjiDivBody);					
				
				kanjiDiv.addHtmlElement(row2Div);
        	}
        	
        } else {
        	
        	Div row2Div = new Div("row");
        	
        	row2Div.addHtmlElement(new Div("col-md-1"));
        	
        	Div kanjiDivText = new Div(null, "font-size: 200%");
        	Text kanjiText = new Text(kanjiSb.toString());
        	
        	kanjiDivText.addHtmlElement(kanjiText);
        	row2Div.addHtmlElement(kanjiDivText);
        	
        	kanjiDiv.addHtmlElement(row2Div);
        }

        // skrypt otwierajacy okienko
        kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanjiDrawId));
        
        // tworzenie okienka rysowania znaku kanji
        kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, dictionaryEntry.getKanji(), kanjiDrawId));
        
                
        return kanjiDiv;
	}
	
	private Div generateReadingSection(Menu menu) throws IOException {
		
		Div readingDiv = new Div();
		
		String prefixKana = dictionaryEntry.getPrefixKana();
		String prefixRomaji = dictionaryEntry.getPrefixRomaji();
		
		List<String> kanaList = dictionaryEntry.getKanaList();
		List<String> romajiList = dictionaryEntry.getRomajiList();
		
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
        
        Table readingTable = new Table();
        row2Div.addHtmlElement(readingTable);

        for (int idx = 0; idx < kanaList.size(); ++idx) {
        	
        	final String kanaDrawId = "kanaDrawId" + idx;
        	
    		StringBuffer fullKana = new StringBuffer();
    		StringBuffer fullRomaji = new StringBuffer();
    		
			if (prefixKana != null && prefixKana.equals("") == false) {
				fullKana.append("(").append(prefixKana).append(") ");
			}

			fullKana.append(kanaList.get(idx));

			if (prefixRomaji != null && prefixRomaji.equals("") == false) {
				fullRomaji.append("(").append(prefixRomaji).append(") ");
			}

			fullRomaji.append(romajiList.get(idx));
			
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
			
			Td readingTableButtonTd = new Td(null, "padding: 0 50px 5px 0;");
			readingTableTr.addHtmlElement(readingTableButtonTd);
			
			// guzik rysowania
			Button kanaDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanaDrawId, 
					getMessage("wordDictionaryDetails.page.dictionaryEntry.reading.showKanaDraw"));

			readingTableButtonTd.addHtmlElement(kanaDrawButton);
        }

        for (IdAndText idAndText : idAndTextList) {
        	        	
            // skrypt otwierajacy okienko
        	readingDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(idAndText.id));
            
            // tworzenie okienka rysowania znaku kanji
        	readingDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, idAndText.text, idAndText.id));
		}
                
        return readingDiv;
	}
	
	private Div generateTranslateSection(Menu menu) throws IOException {
		
		Div translateDiv = new Div();
		
		List<String> translates = dictionaryEntry.getTranslates();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tlumaczenie - tytul
    	Div translateTitleDiv = new Div("col-md-1");
    	
    	H translateTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	translateTitleH4.setId("translateId");
    	
    	translateTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.translate.title")));
    	menu.getChildMenu().add(new Menu(translateTitleH4.getId(), getMessage("wordDictionaryDetails.page.dictionaryEntry.translate.title")));    	
    	
    	translateTitleDiv.addHtmlElement(translateTitleH4);
    	
    	row1Div.addHtmlElement(translateTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	translateDiv.addHtmlElement(row1Div);

		// wiersz z tlumaczeniem
    	Div row2Div = new Div("row");
    	translateDiv.addHtmlElement(row2Div);
    	
    	for (int idx = 0; idx < translates.size(); ++idx) {
    		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    		
    		Div currentTranslateDiv = new Div("col-md-11");
    		row2Div.addHtmlElement(currentTranslateDiv);
    		
    		H currentTranslateH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
    		currentTranslateH.addHtmlElement(new Text(translates.get(idx)));
    		
    		currentTranslateDiv.addHtmlElement(currentTranslateH);
    	}
		
		return translateDiv;
	}
	
	private Div generateAdditionalInfo(Menu menu) throws IOException {
		
		String info = dictionaryEntry.getInfo();
		
		String kanji = dictionaryEntry.getKanji();
		
		boolean special = false;
		
		if (kanji != null && isSmTsukiNiKawatteOshiokiYo(kanji) == true) {
			special = true;
		}
		
		if (!(info != null && info.length() > 0) && (special == false)) {
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

		if (special == false) {
			
			H additionalInfoTextH4 = new H(4);
			additionalInfoTextDiv.addHtmlElement(additionalInfoTextH4);
			
			additionalInfoTextH4.addHtmlElement(new Text(info));
						
		} else {
			
			Div specialDiv = new Div(null, "font-family:monospace; font-size: 40%");
			additionalInfoTextDiv.addHtmlElement(specialDiv);
			
			specialDiv.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.info.special")));	
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
    	
    	if (addableDictionaryEntryTypeInfoCounter > 1) { // info o odmianach
    		
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
	    		
	    		if (addableDictionaryEntryTypeInfoCounter > 1) {
	    			
		            Td row3TableTrTd2 = new Td();
					row3TableTr.addHtmlElement(row3TableTrTd2);
		            
					Div row3TableTrTd2Div = new Div(null, "margin: 0 0 5px 50px");
					row3TableTrTd2.addHtmlElement(row3TableTrTd2Div);
					
					Button linkButton = new Button("btn btn-default");
					row3TableTrTd2Div.addHtmlElement(linkButton);
					
		            String link = LinkGenerator.generateDictionaryEntryDetailsLink(
		            		pageContext.getServletContext().getContextPath(), dictionaryEntry, currentDictionaryEntryType);

					linkButton.setButtonType(ButtonType.BUTTON);
					linkButton.setOnClick("window.location = '" + link + "'");
					
					linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.forceDictionaryEntryType.show")));	    			
	    		}
			}
		}
		
		return wordTypeDiv;
	}
		
	private Div generateAttribute(Menu menu) throws IOException {
		
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
			
			if (attributeType == AttributeType.VERB_TRANSITIVITY_PAIR || attributeType == AttributeType.VERB_INTRANSITIVITY_PAIR) {
				
				Integer transitivityIntransitivityPairWordId = Integer.parseInt(currentAttribute.getAttributeValue().get(0));

				final DictionaryEntry transitivityIntransitivityPairDictionaryEntry = dictionaryManager.getDictionaryEntryById(transitivityIntransitivityPairWordId);

				if (transitivityIntransitivityPairDictionaryEntry != null) {
					
					Tr row2TableTr = new Tr();
					row2Table.addHtmlElement(row2TableTr);
					
					Td row2TableTrTd1 = new Td();
					row2TableTr.addHtmlElement(row2TableTrTd1);

		    		H currentAttributeH = new H(4, null, "margin-top: 0px; margin-bottom: 5px; margin-left: 30px");
		    		row2TableTrTd1.addHtmlElement(currentAttributeH);
		    		
		    		currentAttributeH.addHtmlElement(new Text(attributeType.getName()));

		    		// czasownik przechodni / nieprzechodni
					List<String> transitivityIntransitivityPairDictionaryEntryKanaList = transitivityIntransitivityPairDictionaryEntry.getKanaList();
					List<String> transitivityIntransitivityPairDictionaryEntryRomajiList = transitivityIntransitivityPairDictionaryEntry.getRomajiList();

					for (int transitivityIntransitivityPairDictionaryEntryKanaListIdx = 0; transitivityIntransitivityPairDictionaryEntryKanaListIdx < transitivityIntransitivityPairDictionaryEntryKanaList
							.size(); transitivityIntransitivityPairDictionaryEntryKanaListIdx++) {

						StringBuffer transitivityIntrasitivitySb = new StringBuffer();

						if (transitivityIntransitivityPairDictionaryEntry.isKanjiExists() == true) {
							transitivityIntrasitivitySb.append(transitivityIntransitivityPairDictionaryEntry.getKanji()).append(", ");
						}

						transitivityIntrasitivitySb.append(transitivityIntransitivityPairDictionaryEntryKanaList.get(transitivityIntransitivityPairDictionaryEntryKanaListIdx)).append(", ");
						
						transitivityIntrasitivitySb.append(transitivityIntransitivityPairDictionaryEntryRomajiList.get(transitivityIntransitivityPairDictionaryEntryKanaListIdx));
						
						Td row2TableTrTd2 = new Td();
						row2TableTr.addHtmlElement(row2TableTrTd2);

			    		H currentTransitivityIntrasitivityH = new H(4, null, "margin-top: 0px; margin-bottom: 5px; margin-left: 50px");
			    		row2TableTrTd2.addHtmlElement(currentTransitivityIntrasitivityH);
			    		
			    		currentTransitivityIntrasitivityH.addHtmlElement(new Text(transitivityIntrasitivitySb.toString()));
					}		    		
		    		
		    		// przycisk
					Td row2TableTrTd3 = new Td();
					row2TableTr.addHtmlElement(row2TableTrTd3);
					
					Div row2TableTrTd3Div = new Div(null, "margin: 0 0 5px 50px");
					row2TableTrTd3.addHtmlElement(row2TableTrTd3Div);
										
		            String link = LinkGenerator.generateDictionaryEntryDetailsLink(
		            		pageContext.getServletContext().getContextPath(), transitivityIntransitivityPairDictionaryEntry, null);
		            
					Button linkButton = new Button("btn btn-default");
					row2TableTrTd3Div.addHtmlElement(linkButton);

					linkButton.setButtonType(ButtonType.BUTTON);
					linkButton.setOnClick("window.location = '" + link + "'");
					
					linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.atribute.transitivityIntransitivityPairDictionaryEntry.show")));			
				}				
			}			
    	}		
		
		return attributeDiv;		
	}
	
	private Div generateKnownKanjiDiv(Menu menu) {
		
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
    	Div knownKanjiTitleDiv = new Div("col-md-3");
    	
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
			row2TableTr.addHtmlElement(row2TableTrButtonTd);
			
			String link = LinkGenerator.generateKanjiDetailsLink(pageContext.getServletContext().getContextPath(), currentKnownKanjiEntry);
			
			Button linkButton = new Button("btn btn-default");
			row2TableTrButtonTd.addHtmlElement(linkButton);

			linkButton.setButtonType(ButtonType.BUTTON);
			linkButton.setOnClick("window.location = '" + link + "'");
			
			linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.knownKanji.kanji.show")));			
		}
		
		return knownKanjiDiv;
	}
	
	private Div generateGrammaFormConjugate(Menu mainMenu, Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaFormCache) throws IOException {
		
		// wylicz odmiany gramatyczne
		List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList = 
				GrammaConjugaterManager.getGrammaConjufateResult(dictionaryManager.getKeigoHelper(), dictionaryEntry, grammaFormCache, forceDictionaryEntryType);
		
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
				dictionaryManager.getKeigoHelper(), dictionaryEntry, grammaFormCache, forceDictionaryEntryType);
		
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
			
	    	// dodatkowe info	 
			String exampleGroupTypeElementsInfo = exampleGroupTypeElements.getExampleGroupType().getInfo();
			
			if (exampleGroupTypeElementsInfo != null) {
				H currentExampleResultSectionBodyDivInfoH5 = new H(5, "col-md-11", "margin-top: 0px; margin-left: -8%;");
				
				currentExampleResultSectionBodyDivInfoH5.addHtmlElement(new Text(exampleGroupTypeElementsInfo));
				
				sectionBodyDiv.addHtmlElement(currentExampleResultSectionBodyDivInfoH5);
			}	    	
			
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
		
		String info = exampleResult.getInfo();
		
		if (info != null) {
			
			Tr tr0 = new Tr();
			table.addHtmlElement(tr0);
			
			Td infoTd = new Td();
			tr0.addHtmlElement(infoTd);
			
			H infoH5 = new H(5, null, "margin-top: 0px;");
			
			infoH5.addHtmlElement(new Text(info));
			
			infoTd.addHtmlElement(infoH5);			
		}
		
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

	public DictionaryEntryType getForceDictionaryEntryType() {
		return forceDictionaryEntryType;
	}

	public void setForceDictionaryEntryType(DictionaryEntryType forceDictionaryEntryType) {
		this.forceDictionaryEntryType = forceDictionaryEntryType;
	}
}
