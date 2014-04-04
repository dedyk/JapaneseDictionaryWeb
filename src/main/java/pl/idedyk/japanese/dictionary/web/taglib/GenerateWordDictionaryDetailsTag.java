package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.Attribute;
import pl.idedyk.japanese.dictionary.api.dto.AttributeType;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.FuriganaEntry;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Button.ButtonType;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog;

public class GenerateWordDictionaryDetailsTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private DictionaryEntry dictionaryEntry;
	
	private DictionaryEntryType forceDictionaryEntryType;
	
	private String detailsLink;
	
	private String detailsLinkWithForceDictionaryEntryType;
	
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
            
            // tytul strony
            generateTitle(out);
            
            // generowanie informacji podstawowych
            generateMainInfo(out);
                                    
            return SKIP_BODY;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	private void generateTitle(JspWriter out) throws IOException {
		
        out.println("<h4 class=\"page-header\">");
        out.println(getMessage("wordDictionaryDetails.page.dictionaryEntry.title"));
        out.println("</h4>");		
	}
	
	private void generateMainInfo(JspWriter out) throws IOException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.mainInfo")));
		
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		// kanji
		Div kanjiDiv = generateKanjiSection();
		panelBody.addHtmlElement(kanjiDiv);
		
		// czytanie
		Div readingDiv = generateReadingSection();
		panelBody.addHtmlElement(readingDiv);
		
        // tlumaczenie
        Div translate = generateTranslateSection();
        panelBody.addHtmlElement(translate);
        
        // generuj informacje dodatkowe
        Div additionalInfo = generateAdditionalInfo();

        if (additionalInfo != null) {
        	panelBody.addHtmlElement(additionalInfo);
        }
        
        // czesc mowy
        Div wordTypeDiv = generateWordType();
        
        if (wordTypeDiv != null) {
        	panelBody.addHtmlElement(wordTypeDiv);
        }
        
        // dodatkowe atrybuty
		Div additionalAttribute = generateAttribute();

        if (additionalAttribute != null) {
        	panelBody.addHtmlElement(additionalAttribute);
        }
		
		panelDiv.addHtmlElement(panelBody);
		
		// renderowanie
		panelDiv.render(out);
	}
	
	private Div generateKanjiSection() throws IOException {
		
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
        
        if (addKanjiWrite == true) {
        	
        	// wiersz z tytulem
        	Div row1Div = new Div("row");
        	
        	// kanji - tytul
        	Div kanjiTitleDiv = new Div("col-md-1");
        	
        	H kanjiTitleH4 = new H(4, null, "margin-top: 0px");
        	kanjiTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.kanji.title")));
        	
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
        }
        
        kanjiDiv.addHtmlElement(new Hr());
                
        return kanjiDiv;
	}
	
	private Div generateReadingSection() throws IOException {
		
		Div readingDiv = new Div();
		
		String prefixKana = dictionaryEntry.getPrefixKana();
		String prefixRomaji = dictionaryEntry.getPrefixRomaji();
		
		List<String> kanaList = dictionaryEntry.getKanaList();
		List<String> romajiList = dictionaryEntry.getRomajiList();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// kanji - tytul
    	Div readingTitleDiv = new Div("col-md-1");
    	
    	H readingTitleH4 = new H(4, null, "margin-top: 0px");
    	readingTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.reading.title")));
    	
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
			
			Td readingTableRomajiTd = new Td(null, "font-size: 150%; padding: 0 50px 5px 0");
			readingTableTr.addHtmlElement(readingTableRomajiTd);
			
			readingTableRomajiTd.addHtmlElement(new Text(fullRomaji.toString()));
			
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
        
        readingDiv.addHtmlElement(new Hr());
        
        return readingDiv;
	}
	
	private Div generateTranslateSection() throws IOException {
		
		Div translateDiv = new Div();
		
		List<String> translates = dictionaryEntry.getTranslates();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tlumaczenie - tytul
    	Div translateTitleDiv = new Div("col-md-1");
    	
    	H translateTitleH4 = new H(4, null, "margin-top: 0px");
    	translateTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.translate.title")));
    	
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

    	translateDiv.addHtmlElement(new Hr());
		
		return translateDiv;
	}
	
	private Div generateAdditionalInfo() throws IOException {
		
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
    	
    	H additionalInfoTitleH4 = new H(4, null, "margin-top: 0px");
    	additionalInfoTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.info.title")));
    	
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
	
	private Div generateWordType() throws IOException {
		
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
    	
    	H wordTypeTitleH4 = new H(4, null, "margin-top: 0px");
    	wordTypeTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.title")));
    	
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
	    			
					String kanji = dictionaryEntry.getKanji();
					List<String> kanaList = dictionaryEntry.getKanaList();
					
		            String link = detailsLinkWithForceDictionaryEntryType.replaceAll("%ID%", String.valueOf(dictionaryEntry.getId())).
		            		replaceAll("%KANJI%", kanji != null ? kanji : "-").
		            		replaceAll("%KANA%", kanaList != null && kanaList.size() > 0 ? kanaList.get(0) : "-").
		            		replaceAll("%FORCEDICTIONARYENTRYTYPE%", currentDictionaryEntryType.toString());
		            
		            Td row3TableTrTd2 = new Td();
					row3TableTr.addHtmlElement(row3TableTrTd2);
		            
					Div row3TableTrTd2Div = new Div(null, "margin: 0 0 5px 50px");
					row3TableTrTd2.addHtmlElement(row3TableTrTd2Div);
					
					Button linkButton = new Button("btn btn-default");
					row3TableTrTd2Div.addHtmlElement(linkButton);

					linkButton.setButtonType(ButtonType.BUTTON);
					linkButton.setOnClick("window.location = '" + link + "'");
					
					linkButton.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.dictionaryType.forceDictionaryEntryType.show")));	    			
	    		}
			}
		}
		
		wordTypeDiv.addHtmlElement(new Hr());
		
		return wordTypeDiv;
	}
		
	private Div generateAttribute() throws IOException {
		
		List<Attribute> attributeList = dictionaryEntry.getAttributeList().getAttributeList();
		
		if (attributeList == null || attributeList.size() == 0) {
			return null;
		}
		
		Div attributeDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// czesc mowy - tytul
    	Div attributeTitleDiv = new Div("col-md-3");
    	
    	H attributeTitleH4 = new H(4, null, "margin-top: 0px");
    	attributeTitleH4.addHtmlElement(new Text(getMessage("wordDictionaryDetails.page.dictionaryEntry.atribute.title")));
    	
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
					
					String kanji = transitivityIntransitivityPairDictionaryEntry.getKanji();
					List<String> kanaList = transitivityIntransitivityPairDictionaryEntry.getKanaList();
					
		            String link = detailsLink.replaceAll("%ID%", String.valueOf(transitivityIntransitivityPairDictionaryEntry.getId())).
		            		replaceAll("%KANJI%", kanji != null ? kanji : "-").
		            		replaceAll("%KANA%", kanaList != null && kanaList.size() > 0 ? kanaList.get(0) : "-");

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

	/*

		private List<IScreenItem> generateDetails(final DictionaryEntry dictionaryEntry,
				DictionaryEntryType forceDictionaryEntryType, final ScrollView scrollMainLayout) {

			List<IScreenItem> report = new ArrayList<IScreenItem>();

			// dictionary groups
			List<GroupEnum> groups = dictionaryEntry.getGroups();

			report.add(new StringValue("", 15.0f, 2));
			report.add(new TitleItem(getString(R.string.word_dictionary_details_dictionary_groups), 0));

			for (int groupsIdx = 0; groupsIdx < groups.size(); ++groupsIdx) {
				report.add(new StringValue(String.valueOf(groups.get(groupsIdx).getValue()), 20.0f, 0));
			}

			/ *
			// dictionary position
			report.add(new TitleItem(getString(R.string.word_dictionary_details_dictionary_position), 0));

			report.add(new StringValue(String.valueOf(dictionaryEntry.getId()), 20.0f, 0));
			* /

			// known kanji
			List<KanjiEntry> knownKanji = null;

			if (dictionaryEntry.isKanjiExists() == true) {
				knownKanji = JapaneseAndroidLearnHelperApplication.getInstance().getDictionaryManager(this)
						.findKnownKanji(dictionaryEntry.getKanji());
			}

			if (knownKanji != null && knownKanji.size() > 0) {

				report.add(new StringValue("", 15.0f, 2));
				report.add(new TitleItem(getString(R.string.word_dictionary_known_kanji), 0));
				report.add(new StringValue(getString(R.string.word_dictionary_known_kanji_info), 12.0f, 0));

				for (int knownKanjiIdx = 0; knownKanjiIdx < knownKanji.size(); ++knownKanjiIdx) {

					final KanjiEntry kanjiEntry = knownKanji.get(knownKanjiIdx);

					OnClickListener kanjiOnClickListener = new OnClickListener() {

						@Override
						public void onClick(View v) {

							// show kanji details

							Intent intent = new Intent(getApplicationContext(), KanjiDetails.class);

							intent.putExtra("item", kanjiEntry);

							startActivity(intent);
						}
					};

					StringValue knownKanjiStringValue = new StringValue(kanjiEntry.getKanji(), 16.0f, 1);
					StringValue polishTranslateStringValue = new StringValue(kanjiEntry.getPolishTranslates().toString(),
							16.0f, 1);

					knownKanjiStringValue.setOnClickListener(kanjiOnClickListener);
					polishTranslateStringValue.setOnClickListener(kanjiOnClickListener);

					report.add(knownKanjiStringValue);
					report.add(polishTranslateStringValue);

					if (knownKanjiIdx != knownKanji.size() - 1) {
						report.add(new StringValue("", 10.0f, 1));
					}

				}
			}

			// index
			int indexStartPos = report.size();

			Map<GrammaFormConjugateResultType, GrammaFormConjugateResult> grammaCache = new HashMap<GrammaFormConjugateResultType, GrammaFormConjugateResult>();

			// Conjugater
			List<GrammaFormConjugateGroupTypeElements> grammaFormConjugateGroupTypeElementsList = GrammaConjugaterManager
					.getGrammaConjufateResult(JapaneseAndroidLearnHelperApplication.getInstance()
							.getDictionaryManager(this).getKeigoHelper(), dictionaryEntry, grammaCache,
							forceDictionaryEntryType);

			if (grammaFormConjugateGroupTypeElementsList != null) {
				report.add(new StringValue("", 15.0f, 2));
				report.add(new TitleItem(getString(R.string.word_dictionary_details_conjugater_label), 0));

				for (GrammaFormConjugateGroupTypeElements currentGrammaFormConjugateGroupTypeElements : grammaFormConjugateGroupTypeElementsList) {

					report.add(new TitleItem(currentGrammaFormConjugateGroupTypeElements.getGrammaFormConjugateGroupType()
							.getName(), 1));

					List<GrammaFormConjugateResult> grammaFormConjugateResults = currentGrammaFormConjugateGroupTypeElements
							.getGrammaFormConjugateResults();

					for (GrammaFormConjugateResult currentGrammaFormConjugateResult : grammaFormConjugateResults) {

						if (currentGrammaFormConjugateResult.getResultType().isShow() == true) {
							report.add(new TitleItem(currentGrammaFormConjugateResult.getResultType().getName(), 2));
						}

						addGrammaFormConjugateResult(report, currentGrammaFormConjugateResult);
					}

					report.add(new StringValue("", 15.0f, 1));
				}
			}

			// Exampler
			List<ExampleGroupTypeElements> exampleGroupTypeElementsList = ExampleManager.getExamples(
					JapaneseAndroidLearnHelperApplication.getInstance().getDictionaryManager(this).getKeigoHelper(),
					dictionaryEntry, grammaCache, forceDictionaryEntryType);

			if (exampleGroupTypeElementsList != null) {

				if (grammaFormConjugateGroupTypeElementsList == null) {
					report.add(new StringValue("", 15.0f, 2));
				}

				report.add(new TitleItem(getString(R.string.word_dictionary_details_exampler_label), 0));

				for (ExampleGroupTypeElements currentExampleGroupTypeElements : exampleGroupTypeElementsList) {

					report.add(new TitleItem(currentExampleGroupTypeElements.getExampleGroupType().getName(), 1));

					String exampleGroupInfo = currentExampleGroupTypeElements.getExampleGroupType().getInfo();

					if (exampleGroupInfo != null) {
						report.add(new StringValue(exampleGroupInfo, 12.0f, 1));
					}

					List<ExampleResult> exampleResults = currentExampleGroupTypeElements.getExampleResults();

					for (ExampleResult currentExampleResult : exampleResults) {
						addExampleResult(report, currentExampleResult);
					}

					report.add(new StringValue("", 15.0f, 1));
				}
			}

			// add index
			if (indexStartPos < report.size()) {

				int indexStopPos = report.size();

				List<IScreenItem> indexList = new ArrayList<IScreenItem>();

				indexList.add(new StringValue("", 15.0f, 2));
				indexList.add(new TitleItem(getString(R.string.word_dictionary_details_report_counters_index), 0));
				indexList.add(new StringValue(getString(R.string.word_dictionary_details_index_go), 12.0f, 1));

				for (int reportIdx = indexStartPos; reportIdx < indexStopPos; ++reportIdx) {

					IScreenItem currentReportScreenItem = report.get(reportIdx);

					if (currentReportScreenItem instanceof TitleItem == false) {
						continue;
					}

					final TitleItem currentReportScreenItemAsTitle = (TitleItem) currentReportScreenItem;

					final StringValue titleStringValue = new StringValue(currentReportScreenItemAsTitle.getTitle(), 15.0f,
							currentReportScreenItemAsTitle.getLevel() + 2);

					titleStringValue.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							backScreenPositionStack.push(scrollMainLayout.getScrollY());

							int counterPos = currentReportScreenItemAsTitle.getY();
							scrollMainLayout.scrollTo(0, counterPos - 3);
						}
					});

					indexList.add(titleStringValue);
				}

				for (int indexListIdx = 0, reportStartPos = indexStartPos; indexListIdx < indexList.size(); ++indexListIdx) {
					report.add(reportStartPos, indexList.get(indexListIdx));

					reportStartPos++;
				}
			}

			return report;
		}

		private void fillDetailsMainLayout(List<IScreenItem> generatedDetails, LinearLayout detailsMainLayout) {

			for (IScreenItem currentDetailsReportItem : generatedDetails) {
				currentDetailsReportItem.generate(this, getResources(), detailsMainLayout);
			}
		}

		private void addGrammaFormConjugateResult(List<IScreenItem> report,
				GrammaFormConjugateResult grammaFormConjugateResult) {

			TableLayout actionButtons = new TableLayout(TableLayout.LayoutParam.WrapContent_WrapContent, true, null);
			TableRow actionTableRow = new TableRow();

			String grammaFormKanji = grammaFormConjugateResult.getKanji();

			String prefixKana = grammaFormConjugateResult.getPrefixKana();
			String prefixRomaji = grammaFormConjugateResult.getPrefixRomaji();

			StringBuffer grammaFormKanjiSb = new StringBuffer();

			if (grammaFormKanji != null) {
				if (prefixKana != null && prefixKana.equals("") == false) {
					grammaFormKanjiSb.append("(").append(prefixKana).append(") ");
				}

				grammaFormKanjiSb.append(grammaFormKanji);

				report.add(new StringValue(grammaFormKanjiSb.toString(), 15.0f, 2));
			}

			List<String> grammaFormKanaList = grammaFormConjugateResult.getKanaList();
			List<String> grammaFormRomajiList = grammaFormConjugateResult.getRomajiList();

			for (int idx = 0; idx < grammaFormKanaList.size(); ++idx) {

				StringBuffer sb = new StringBuffer();

				if (prefixKana != null && prefixKana.equals("") == false) {
					sb.append("(").append(prefixKana).append(") ");
				}

				sb.append(grammaFormKanaList.get(idx));

				report.add(new StringValue(sb.toString(), 15.0f, 2));

				StringBuffer grammaFormRomajiSb = new StringBuffer();

				if (prefixRomaji != null && prefixRomaji.equals("") == false) {
					grammaFormRomajiSb.append("(").append(prefixRomaji).append(") ");
				}

				grammaFormRomajiSb.append(grammaFormRomajiList.get(idx));

				report.add(new StringValue(grammaFormRomajiSb.toString(), 15.0f, 2));

				// speak image
				Image speakImage = new Image(getResources().getDrawable(android.R.drawable.ic_lock_silent_mode_off), 2);
				speakImage.setOnClickListener(new TTSJapaneseSpeak(null, grammaFormKanaList.get(idx)));
				actionTableRow.addScreenItem(speakImage);

				// clipboard kanji
				if (grammaFormKanji != null) {
					Image clipboardKanji = new Image(getResources().getDrawable(R.drawable.clipboard_kanji), 0);
					clipboardKanji.setOnClickListener(new CopyToClipboard(grammaFormKanji));
					actionTableRow.addScreenItem(clipboardKanji);
				}

				// clipboard kana
				Image clipboardKana = new Image(getResources().getDrawable(R.drawable.clipboard_kana), 0);
				clipboardKana.setOnClickListener(new CopyToClipboard(grammaFormKanaList.get(idx)));
				actionTableRow.addScreenItem(clipboardKana);

				// clipboard romaji
				Image clipboardRomaji = new Image(getResources().getDrawable(R.drawable.clipboard_romaji), 0);
				clipboardRomaji.setOnClickListener(new CopyToClipboard(grammaFormRomajiList.get(idx)));
				actionTableRow.addScreenItem(clipboardRomaji);

				actionButtons.addTableRow(actionTableRow);

				report.add(actionButtons);
			}

			GrammaFormConjugateResult alternative = grammaFormConjugateResult.getAlternative();

			if (alternative != null) {
				report.add(new StringValue("", 5.0f, 1));

				addGrammaFormConjugateResult(report, alternative);
			}
		}

		private void addExampleResult(List<IScreenItem> report, ExampleResult exampleResult) {

			TableLayout actionButtons = new TableLayout(TableLayout.LayoutParam.WrapContent_WrapContent, true, null);
			TableRow actionTableRow = new TableRow();

			String exampleKanji = exampleResult.getKanji();
			String prefixKana = exampleResult.getPrefixKana();
			String prefixRomaji = exampleResult.getPrefixRomaji();

			StringBuffer exampleKanjiSb = new StringBuffer();

			if (exampleKanji != null) {
				if (prefixKana != null && prefixKana.equals("") == false) {
					exampleKanjiSb.append("(").append(prefixKana).append(") ");
				}

				exampleKanjiSb.append(exampleKanji);

				report.add(new StringValue(exampleKanjiSb.toString(), 15.0f, 2));
			}

			List<String> exampleKanaList = exampleResult.getKanaList();
			List<String> exampleRomajiList = exampleResult.getRomajiList();

			for (int idx = 0; idx < exampleKanaList.size(); ++idx) {

				StringBuffer sb = new StringBuffer();

				if (prefixKana != null && prefixKana.equals("") == false) {
					sb.append("(").append(prefixKana).append(") ");
				}

				sb.append(exampleKanaList.get(idx));

				report.add(new StringValue(sb.toString(), 15.0f, 2));

				StringBuffer exampleRomajiSb = new StringBuffer();

				if (prefixRomaji != null && prefixRomaji.equals("") == false) {
					exampleRomajiSb.append("(").append(prefixRomaji).append(") ");
				}

				exampleRomajiSb.append(exampleRomajiList.get(idx));

				report.add(new StringValue(exampleRomajiSb.toString(), 15.0f, 2));

				String exampleResultInfo = exampleResult.getInfo();

				if (exampleResultInfo != null) {
					report.add(new StringValue(exampleResultInfo, 12.0f, 2));
				}

				// speak image
				Image speakImage = new Image(getResources().getDrawable(android.R.drawable.ic_lock_silent_mode_off), 2);
				speakImage.setOnClickListener(new TTSJapaneseSpeak(null, exampleKanaList.get(idx)));
				actionTableRow.addScreenItem(speakImage);

				// clipboard kanji
				if (exampleKanji != null) {
					Image clipboardKanji = new Image(getResources().getDrawable(R.drawable.clipboard_kanji), 0);
					clipboardKanji.setOnClickListener(new CopyToClipboard(exampleKanji));
					actionTableRow.addScreenItem(clipboardKanji);
				}

				// clipboard kana
				Image clipboardKana = new Image(getResources().getDrawable(R.drawable.clipboard_kana), 0);
				clipboardKana.setOnClickListener(new CopyToClipboard(exampleKanaList.get(idx)));
				actionTableRow.addScreenItem(clipboardKana);

				// clipboard romaji
				Image clipboardRomaji = new Image(getResources().getDrawable(R.drawable.clipboard_romaji), 0);
				clipboardRomaji.setOnClickListener(new CopyToClipboard(exampleRomajiList.get(idx)));
				actionTableRow.addScreenItem(clipboardRomaji);

				actionButtons.addTableRow(actionTableRow);

				report.add(actionButtons);
			}

			ExampleResult alternative = exampleResult.getAlternative();

			if (alternative != null) {
				report.add(new StringValue("", 5.0f, 1));

				addExampleResult(report, alternative);
			}
		}


		private StringValue createSmTsukiNiKawatteOshiokiYo() {

			StringValue smStringValue = new StringValue(getString(R.string.sm_tsuki_ni_kawatte_oshioki_yo), 2.8f, 0);

			smStringValue.setTypeface(Typeface.MONOSPACE);
			smStringValue.setTextColor(Color.BLACK);
			smStringValue.setBackgroundColor(Color.WHITE);
			smStringValue.setGravity(Gravity.CENTER);

			return smStringValue;
		}

		private class TTSJapaneseSpeak implements OnClickListener {

			private final String prefix;

			private final String kanjiKana;

			public TTSJapaneseSpeak(String prefix, String kanjiKana) {
				this.prefix = prefix;
				this.kanjiKana = kanjiKana;
			}

			@Override
			public void onClick(View v) {

				StringBuffer text = new StringBuffer();

				if (prefix != null) {
					text.append(prefix);
				}

				if (kanjiKana != null) {
					text.append(kanjiKana);
				}

				if (ttsConnector != null && ttsConnector.getOnInitResult() != null
						&& ttsConnector.getOnInitResult().booleanValue() == true) {
					ttsConnector.speak(text.toString());
				} else {
					AlertDialog alertDialog = new AlertDialog.Builder(WordDictionaryDetails.this).create();

					alertDialog.setMessage(getString(R.string.tts_japanese_error));
					alertDialog.setCancelable(false);

					alertDialog.setButton(getString(R.string.tts_error_ok), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// noop
						}
					});

					alertDialog.setButton2(getString(R.string.tts_google_play_go), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							Uri marketUri = Uri.parse(getString(R.string.tts_svox_market_url));

							Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);

							startActivity(marketIntent);
						}
					});

					alertDialog.show();
				}
			}
		}

		private class CopyToClipboard implements OnClickListener {

			private final String text;

			public CopyToClipboard(String text) {
				this.text = text;
			}

			@Override
			public void onClick(View v) {

				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

				clipboardManager.setText(text);

				Toast.makeText(WordDictionaryDetails.this,
						getString(R.string.word_dictionary_details_clipboard_copy, text), Toast.LENGTH_SHORT).show();
			}
		}
	}

	*/
	
	private String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}

	/*
	private String getMessage(MessageSource messageSource, String code, String[] args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
	*/
	
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

	public String getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(String detailsLink) {
		this.detailsLink = detailsLink;
	}

	public String getDetailsLinkWithForceDictionaryEntryType() {
		return detailsLinkWithForceDictionaryEntryType;
	}

	public void setDetailsLinkWithForceDictionaryEntryType(String detailsLinkWithForceDictionaryEntryType) {
		this.detailsLinkWithForceDictionaryEntryType = detailsLinkWithForceDictionaryEntryType;
	}
}
