package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.api.dto.KanjiDic2Entry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.dto.KanjivgEntry;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.dictionary.dto.WebRadicalInfo;
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
import pl.idedyk.japanese.dictionary.web.taglib.utils.GenerateDrawStrokeDialog.GenerateDrawStrokeDialogParams;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;

public class GenerateKanjiDictionaryDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
	private static final long serialVersionUID = 1L;
	
	private KanjiEntry kanjiEntry;
			
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
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
		this.applicationProperties = (Properties)webApplicationContext.getBean("applicationProperties");
		
		try {
            JspWriter out = pageContext.getOut();

            if (kanjiEntry == null) {
            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.null")));
            	
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
            try {
            	contentDiv.addHtmlElement(generateMainInfo(mainMenu, mobile));
            	
            } catch (DictionaryException e) {
            	throw new JspException(e);
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
		
		pageHeader.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.title")));
		
		B kanjiBold = new B();
		
		kanjiBold.addHtmlElement(new Text(kanjiEntry.getKanji()));
		
		pageHeader.addHtmlElement(kanjiBold);
		
		return pageHeader;
	}
	
	private Div generateMainInfo(Menu mainMenu, boolean mobile) throws IOException, DictionaryException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");

		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("mainInfoId");

		h3Title.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.mainInfo")));
		
		Menu mainInfoMenu = new Menu(h3Title.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.mainInfo"));
		mainMenu.getChildMenu().add(mainInfoMenu);
		
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");

		// kanji
		Div kanjiDiv = generateKanjiSection(mainInfoMenu, mobile);

		panelBody.addHtmlElement(kanjiDiv);
		
		// liczba kresek
		Div strokeCountDiv = generateStrokeCountSection(mainInfoMenu);

		if (strokeCountDiv != null) {
			panelBody.addHtmlElement(new Hr());
			
			panelBody.addHtmlElement(strokeCountDiv);			
		}
		
		// elementy podstawowe
		Div radicalsDiv = generateRadicalsSection(mainInfoMenu);
		
		if (radicalsDiv != null) {
			panelBody.addHtmlElement(new Hr());
			
			panelBody.addHtmlElement(radicalsDiv);			
		}
		
		// czytanie kun'yomi
		Div kunYomiDiv = generateKunYomiSection(mainInfoMenu);

		if (kunYomiDiv != null) {
			panelBody.addHtmlElement(new Hr());
			
			panelBody.addHtmlElement(kunYomiDiv);
		}

		// czytanie on'yomi
		Div onYomiDiv = generateOnYomiSection(mainInfoMenu);

		if (onYomiDiv != null) {
			panelBody.addHtmlElement(new Hr());
			
			panelBody.addHtmlElement(onYomiDiv);
		}
		
		// tlumaczenie
		Div translateDiv = generateTranslateSection(mainInfoMenu);
		
		panelBody.addHtmlElement(new Hr());		
		panelBody.addHtmlElement(translateDiv);
		
		// informacje dodatkowe
		Div infoDiv = generateInfoSection(mainInfoMenu);
		
		if (infoDiv != null) {
			panelBody.addHtmlElement(new Hr());
			
			panelBody.addHtmlElement(infoDiv);			
		}
		
		// wystepowanie znaku
		Div kanjiGroups = generateKanjiGroupsSection(mainInfoMenu);
		
		if (kanjiGroups != null) {
			panelBody.addHtmlElement(new Hr());
			
			panelBody.addHtmlElement(kanjiGroups);			
		}
				
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private Div generateKanjiSection(Menu menu, boolean mobile) throws IOException, DictionaryException {
		
		Div kanjiDiv = new Div();
		
		final String kanjiDrawId = "kanjiDrawId";
		
		String kanji = kanjiEntry.getKanji();
		        	
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// kanji - tytul
    	Div kanjiTitleDiv = new Div("col-md-1");
    	
    	H kanjiTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	kanjiTitleH4.setId("kanjiTitleId");
    	
    	kanjiTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.kanji.title")));
    	menu.getChildMenu().add(new Menu(kanjiTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.kanji.title")));
    	
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
		
		kanjiTable.addHtmlElement(kanjiKanjiTr);
		
		Td kanjiKanjiTd = new Td();
    	
		kanjiKanjiTd.addHtmlElement(new Text(kanji));
		kanjiKanjiTr.addHtmlElement(kanjiKanjiTd);
		
		final KanjivgEntry kanjivsEntry = kanjiEntry.getKanjivgEntry();
		
		// komorka z guziczkiem
		if (kanjivsEntry != null && kanjivsEntry.getStrokePaths().size() > 0) {
			
			// przerwa
			kanjiKanjiTr.addHtmlElement(new Td("col-md-1"));

			Td kanjiDrawButtonTd = new Td();

			Div kanjiDrawButtonDivBody = new Div("col-md-1");

			Button kanjiDrawButton = GenerateDrawStrokeDialog.generateDrawStrokeButton(kanjiDrawId, 
					getMessage("kanjiDictionaryDetails.page.kanjiEntry.kanji.showKanjiDraw"));

			kanjiDrawButtonDivBody.addHtmlElement(kanjiDrawButton);
			kanjiDrawButtonTd.addHtmlElement(kanjiDrawButtonDivBody);

			kanjiKanjiTr.addHtmlElement(kanjiDrawButtonTd);
		}
		
		// wystapienie znaku w slowniku - guziczek
		WordDictionarySearchModel searchModel = new WordDictionarySearchModel();
		
		searchModel.setWord(kanjiEntry.getKanji());
		searchModel.setWordPlace(WordPlaceSearch.ANY_PLACE.toString());
		
		List<String> searchIn = new ArrayList<String>();
		searchIn.add("KANJI");
		searchIn.add("KANA");
		searchIn.add("ROMAJI");
		searchIn.add("TRANSLATE");
		searchIn.add("INFO");
				
		searchModel.setSearchIn(searchIn);
		
		List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();
		
		for (DictionaryEntryType dictionaryEntryType : addableDictionaryEntryList) {
			searchModel.addDictionaryType(dictionaryEntryType);
		}
		
		String link = LinkGenerator.generateWordSearchLink(pageContext.getServletContext().getContextPath(), searchModel);

		//		
		
		Td kanjiKanjiAppearanceButtonTd = new Td();		
		
		A kanjiKanjiAppearanceButton = new A("btn btn-default");
		kanjiKanjiAppearanceButtonTd.addHtmlElement(kanjiKanjiAppearanceButton);
		
		kanjiKanjiAppearanceButton.setHref(link);
		
		kanjiKanjiAppearanceButton.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.kanji.showKanjiAppearanceInWordDictionary")));

		//
		
		if (mobile == false) {
			
			// przerwa
			kanjiKanjiTr.addHtmlElement(new Td("col-md-1"));

			kanjiKanjiTr.addHtmlElement(kanjiKanjiAppearanceButtonTd);
			
		} else {
			
			Tr kanjiKanjiTrForMobileSearchIndictionary = new Tr(null, null);
			
			kanjiTable.addHtmlElement(kanjiKanjiTrForMobileSearchIndictionary);
			
			kanjiKanjiAppearanceButtonTd.setColspan("3");

			kanjiKanjiTrForMobileSearchIndictionary.addHtmlElement(kanjiKanjiAppearanceButtonTd);			
		}
		
		//		
				
		kanjiDivBody.addHtmlElement(kanjiTable);
		row2Div.addHtmlElement(kanjiDivBody);					
		
		kanjiDiv.addHtmlElement(row2Div);

		if (kanjivsEntry != null && kanjivsEntry.getStrokePaths().size() > 0) {
			
			// skrypt otwierajacy okienko
			kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanjiDrawId, mobile));

			// tworzenie okienka rysowania znaku kanji
			GenerateDrawStrokeDialogParams generateDrawStrokeDialogParams = new GenerateDrawStrokeDialogParams();

			generateDrawStrokeDialogParams.height = 300;
			generateDrawStrokeDialogParams.zoomFactory = 0.5f;
			generateDrawStrokeDialogParams.duration = 700;
			generateDrawStrokeDialogParams.addPathNum = true;

			kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeDialog(dictionaryManager, messageSource, kanji, kanjiDrawId, generateDrawStrokeDialogParams));
		}
		
        return kanjiDiv;
	}
	
	private Div generateStrokeCountSection(Menu menu) {
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
		
		if (kanjiDic2Entry == null) {
			return null;
		}
				
		return generateStandardDivWithStringList("strokeCountId", getMessage("kanjiDictionaryDetails.page.kanjiEntry.strokeCount.title"), 
				menu, Arrays.asList(new String [] { String.valueOf(kanjiDic2Entry.getStrokeCount()) }));
	}
	
	private Div generateRadicalsSection(Menu menu) throws IOException {
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
		
		if (kanjiDic2Entry == null) {
			return null;
		}
		
		List<String> radicals = kanjiDic2Entry.getRadicals();
		
		if (radicals == null || radicals.size() == 0) {
			return null;
		}
		
		String titleId = "radicalsId";
		String title = getMessage("kanjiDictionaryDetails.page.kanjiEntry.radicals.title");
		
		Div resultDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div divTitleDiv = new Div("col-md-10");
    	
    	H divTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	divTitleH4.setId(titleId);
    	
    	divTitleH4.addHtmlElement(new Text(title));
    	menu.getChildMenu().add(new Menu(divTitleH4.getId(), title));    	
    	
    	divTitleDiv.addHtmlElement(divTitleH4);
    	
    	row1Div.addHtmlElement(divTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	resultDiv.addHtmlElement(row1Div);

		// wiersz z lista
    	Div row2Div = new Div("row");
    	resultDiv.addHtmlElement(row2Div);
    	
    	row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    	
    	Div divBodyDiv = new Div("col-md-11");
    	row2Div.addHtmlElement(divBodyDiv);
    	
    	Table row2Table = new Table();
    	divBodyDiv.addHtmlElement(row2Table);
    	
		for (String currentRadical : radicals) {
			
			Tr row2TableTr = new Tr();
			row2Table.addHtmlElement(row2TableTr);
			
			Td row2TableTrTd1 = new Td();
			row2TableTr.addHtmlElement(row2TableTrTd1);
			
    		H currentListH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
    		row2TableTrTd1.addHtmlElement(currentListH);
			
			WebRadicalInfo webRadicalInfo = dictionaryManager.getWebRadicalInfo(currentRadical);
			
			String webRadicalInfoImage = null;
			
			if (webRadicalInfo != null) {
				webRadicalInfoImage = webRadicalInfo.getImage();
			}			
			
			if (webRadicalInfoImage == null) {
				currentListH.addHtmlElement(new Text(currentRadical));
				
			} else {	    					
				String staticPrefix = LinkGenerator.getStaticPrefix(pageContext.getServletContext().getContextPath(), applicationProperties);
				
				Img currentRadicalImg = new Img();
				
				currentRadicalImg.setSrc(staticPrefix + "/" + webRadicalInfoImage);
				currentRadicalImg.setAlt(currentRadical);
				currentRadicalImg.setWidthImg("80%");
				currentRadicalImg.setHeightImg("80%");
				
				currentListH.addHtmlElement(currentRadicalImg);
			}
		}
		
		return resultDiv;
	}
	
	private Div generateKunYomiSection(Menu menu) {
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
		
		if (kanjiDic2Entry == null) {
			return null;
		}
		
		List<String> kunReading = kanjiDic2Entry.getKunReading();
		
		if (kunReading == null || kunReading.size() == 0) {
			return null;
		}
		
		return generateStandardDivWithStringList("kunReadingId", getMessage("kanjiDictionaryDetails.page.kanjiEntry.kunReading.title"), menu, kunReading);
	}
	
	private Div generateOnYomiSection(Menu menu) {
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
		
		if (kanjiDic2Entry == null) {
			return null;
		}
		
		List<String> onReading = kanjiDic2Entry.getOnReading();
		
		if (onReading == null || onReading.size() == 0) {
			return null;
		}
		
		return generateStandardDivWithStringList("onReadingId", getMessage("kanjiDictionaryDetails.page.kanjiEntry.onReading.title"), menu, onReading);
	}
	
	private Div generateTranslateSection(Menu menu) {
				
		List<String> translate = kanjiEntry.getPolishTranslates();
		
		return generateStandardDivWithStringList("translateId", getMessage("kanjiDictionaryDetails.page.kanjiEntry.translate.title"), menu, translate);
	}
	
	private Div generateInfoSection(Menu menu) {
		
		String info = kanjiEntry.getInfo();
		
		if (info == null || info.length() <= 0) {
			return null;
		}
		
		return generateStandardDivWithStringList("infoId", getMessage("kanjiDictionaryDetails.page.kanjiEntry.info.title"), menu, Arrays.asList(new String [] { info }));
	}
	
	private Div generateKanjiGroupsSection(Menu menu) {
		
		// kanji groups
		List<GroupEnum> groups = kanjiEntry.getGroups();
		
		if (groups == null || groups.size() == 0) {
			return null;
		}
		
		List<String> groupsStringList = new ArrayList<String>();
		
		for (GroupEnum groupEnum : groups) {
			groupsStringList.add(groupEnum.getValue());
		}		
		
		return generateStandardDivWithStringList("kanjiGroupsId", getMessage("kanjiDictionaryDetails.page.kanjiEntry.groups.title"), menu, groupsStringList);
	}
	
	private IHtmlElement addSuggestionElements(Menu mainMenu) {
		
        addSuggestionMenuPos(mainMenu, messageSource);
        
        int id = kanjiEntry.getId();
        String kanji = kanjiEntry.getKanji();

		String defaultSuggestion = messageSource.getMessage("kanjiDictionaryDetails.page.suggestion.default", 
				new Object[] { kanji, String.valueOf(id) }, Locale.getDefault());
        
        // dodaj okienko z sugestia
        return addSuggestionDialog(messageSource, defaultSuggestion);		
	}
		
	private String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}
	
	public KanjiEntry getKanjiEntry() {
		return kanjiEntry;
	}

	public void setKanjiEntry(KanjiEntry kanjiEntry) {				
		this.kanjiEntry = kanjiEntry;
	}	
}
