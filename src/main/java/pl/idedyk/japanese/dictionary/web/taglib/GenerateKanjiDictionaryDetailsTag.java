package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog;
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog.GenerateDrawStrokeDialogParams;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;

public class GenerateKanjiDictionaryDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
	private static final long serialVersionUID = 1L;
	
	private KanjiEntry kanjiEntry;
			
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

            if (kanjiEntry == null) {
            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.dictionaryEntry.null")));
            	
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
		
		pageHeader.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.dictionaryEntry.title")));
		
		return pageHeader;
	}
	
	private Div generateMainInfo(Menu mainMenu) throws IOException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");

		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("mainInfoId");

		h3Title.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.dictionaryEntry.mainInfo")));
		
		Menu mainInfoMenu = new Menu(h3Title.getId(), getMessage("kanjiDictionaryDetails.page.dictionaryEntry.mainInfo"));
		mainMenu.getChildMenu().add(mainInfoMenu);
		
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");

		// kanji
		Div kanjiDiv = generateKanjiSection(mainInfoMenu);

		panelBody.addHtmlElement(kanjiDiv);	
		panelBody.addHtmlElement(new Hr());

		
		
		/*
				
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
        		
		*/
		
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private Div generateKanjiSection(Menu menu) throws IOException {
		
		Div kanjiDiv = new Div();
		
		final String kanjiDrawId = "kanjiDrawId";
		
		String kanji = kanjiEntry.getKanji();
		        	
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// kanji - tytul
    	Div kanjiTitleDiv = new Div("col-md-1");
    	
    	H kanjiTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	kanjiTitleH4.setId("kanjiTitleId");
    	
    	kanjiTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.dictionaryEntry.kanji.title")));
    	menu.getChildMenu().add(new Menu(kanjiTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.dictionaryEntry.kanji.title")));
    	
    	kanjiTitleDiv.addHtmlElement(kanjiTitleH4);
    	
    	row1Div.addHtmlElement(kanjiTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	kanjiDiv.addHtmlElement(row1Div);
    	
		// wiersz ze znakiem kanji
    	Div row2Div = new Div("row");
    	
    	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
		
    	// komorka ze znakiem kanji
    	Div kanjiDivBody = new Div("col-md-10");

    	// tabelka ze znakiem kanji
		Table kanjiTable = new Table();

		// znaki kanji
		Tr kanjiKanjiTr = new Tr(null, "font-size: 300%; text-align:center;");
		
		Td kanjiKanjiTd = new Td();
    	
		kanjiKanjiTd.addHtmlElement(new Text(kanji));
		kanjiKanjiTr.addHtmlElement(kanjiKanjiTd);

		// przerwa
		kanjiKanjiTr.addHtmlElement(new Td("col-md-1"));
		
		// komorka z guziczkiem
		Td kanjiDrawButtonTd = new Td();

		Div kanjiDrawButtonDivBody = new Div("col-md-1");
		
		Button kanjiDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanjiDrawId, 
				getMessage("kanjiDictionaryDetails.page.dictionaryEntry.kanji.showKanjiDraw"));

		kanjiDrawButtonDivBody.addHtmlElement(kanjiDrawButton);
		kanjiDrawButtonTd.addHtmlElement(kanjiDrawButtonDivBody);

		kanjiKanjiTr.addHtmlElement(kanjiDrawButtonTd);
		kanjiTable.addHtmlElement(kanjiKanjiTr);
		
		kanjiDivBody.addHtmlElement(kanjiTable);
		row2Div.addHtmlElement(kanjiDivBody);					
		
		kanjiDiv.addHtmlElement(row2Div);

        // skrypt otwierajacy okienko
        kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanjiDrawId));
        
        // tworzenie okienka rysowania znaku kanji
        GenerateDrawStrokeDialogParams generateDrawStrokeDialogParams = new GenerateDrawStrokeDialogParams();
        
        generateDrawStrokeDialogParams.height = 300;
        generateDrawStrokeDialogParams.zoomFactory = 0.5f;
        generateDrawStrokeDialogParams.duration = 700;
        generateDrawStrokeDialogParams.addPathNum = true;
        
        kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, kanji, kanjiDrawId, generateDrawStrokeDialogParams));
                
        return kanjiDiv;
	}
		
	private String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}

	/*
	private String getMessage(String code, String[] args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
	*/

	public KanjiEntry getKanjiEntry() {
		return kanjiEntry;
	}

	public void setKanjiEntry(KanjiEntry kanjiEntry) {
		this.kanjiEntry = kanjiEntry;
	}
	
	/*

	private List<IScreenItem> generateDetails(final KanjiEntry kanjiEntry) {
		
		List<IScreenItem> report = new ArrayList<IScreenItem>();
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();

		// Kanji		
		report.add(new TitleItem(getString(R.string.kanji_details_kanji_label), 0));
		
		StringValue kanjiStringValue = new StringValue(kanjiEntry.getKanji(), 35.0f, 0);
		
		report.add(kanjiStringValue);
		
		final KanjivgEntry kanjivsEntry = kanjiEntry.getKanjivgEntry();
		
		if (kanjivsEntry != null && kanjivsEntry.getStrokePaths().size() > 0) {
			report.add(new StringValue(getString(R.string.kanji_details_kanji_info), 12.0f, 0));
			
			kanjiStringValue.setOnClickListener(new OnClickListener() {
				
				public void onClick(View view) {

					StrokePathInfo strokePathInfo = new StrokePathInfo();
					
					List<KanjivgEntry> kanjivsEntryStrokePathsList = new ArrayList<KanjivgEntry>();
					kanjivsEntryStrokePathsList.add(kanjivsEntry);
					strokePathInfo.setStrokePaths(kanjivsEntryStrokePathsList);
					
					Intent intent = new Intent(getApplicationContext(), SodActivity.class);
										
					intent.putExtra("strokePathsInfo", strokePathInfo);
					
					startActivity(intent);
				}
			});
		}
		
		// Stroke count
		report.add(new TitleItem(getString(R.string.kanji_details_stroke_count_label), 0));
		
		if (kanjiDic2Entry != null) {
			report.add(new StringValue(String.valueOf(kanjiDic2Entry.getStrokeCount()), 20.0f, 0));
		} else {
			report.add(new StringValue("-", 20.0f, 0));
		}
		
		// Radicals
		report.add(new TitleItem(getString(R.string.kanji_details_radicals), 0));
		
		if (kanjiDic2Entry != null) {
			List<String> radicals = kanjiDic2Entry.getRadicals();
			
			for (String currentRadical : radicals) {
				report.add(new StringValue(currentRadical, 20.0f, 0));
			}
		} else {
			report.add(new StringValue("-", 20.0f, 0));
		}
				
		// Kun reading
		report.add(new TitleItem(getString(R.string.kanji_details_kun_reading), 0));
		
		if (kanjiDic2Entry != null) {
			List<String> kunReading = kanjiDic2Entry.getKunReading();
			
			for (String currentKun : kunReading) {
				report.add(new StringValue(currentKun, 20.0f, 0));
			}
		} else {
			report.add(new StringValue("-", 20.0f, 0));
		}
		
		// On reading
		report.add(new TitleItem(getString(R.string.kanji_details_on_reading), 0));
		
		if (kanjiDic2Entry != null) {
			List<String> onReading = kanjiDic2Entry.getOnReading();
			
			for (String currentOn : onReading) {
				report.add(new StringValue(currentOn, 20.0f, 0));
			}
		} else {
			report.add(new StringValue("-", 20.0f, 0));
		}
			
		// Translate
		report.add(new TitleItem(getString(R.string.kanji_details_translate_label), 0));
		
		List<String> translates = kanjiEntry.getPolishTranslates();
		
		for (int idx = 0; idx < translates.size(); ++idx) {
			report.add(new StringValue(translates.get(idx), 20.0f, 0));
		}
		
		// Additional info
		report.add(new TitleItem(getString(R.string.kanji_details_additional_info_label), 0));
		
		String info = kanjiEntry.getInfo();
		
		if (info != null && info.length() > 0) {
			report.add(new StringValue(info, 20.0f, 0));
		} else {
			report.add(new StringValue("-", 20.0f, 0));
		}
		
		// kanji appearance
		List<GroupEnum> groups = kanjiEntry.getGroups();
		
		if (groups != null && groups.size() > 0) {
			report.add(new TitleItem(getString(R.string.kanji_details_kanji_appearance_label), 0));
			
			for (int idx = 0; idx < groups.size(); ++idx) {
				report.add(new StringValue(groups.get(idx).getValue(), 20.0f, 0));
			}			
		}		
		
		report.add(new StringValue("", 15.0f, 2));
		
		// find kanji in words
		pl.idedyk.android.japaneselearnhelper.screen.Button findWordWithKanji = new pl.idedyk.android.japaneselearnhelper.screen.Button(
				getString(R.string.kanji_details_find_kanji_in_words));
		
		findWordWithKanji.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {

				Intent intent = new Intent(getApplicationContext(), WordDictionaryTab.class);
				
				FindWordRequest findWordRequest = new FindWordRequest();
				
				findWordRequest.word = kanjiEntry.getKanji();
				findWordRequest.searchKanji = true;
				findWordRequest.searchKana = false;
				findWordRequest.searchRomaji = false;
				findWordRequest.searchTranslate = false;
				findWordRequest.searchInfo = false;
				findWordRequest.searchGrammaFormAndExamples = false;
				
				findWordRequest.wordPlaceSearch = WordPlaceSearch.ANY_PLACE;
				
				findWordRequest.dictionaryEntryList = null;
				
				intent.putExtra("findWordRequest", findWordRequest);
				
				startActivity(intent);
			}
		});
		
		report.add(findWordWithKanji);
		
		return report;
	}

	 */
}
