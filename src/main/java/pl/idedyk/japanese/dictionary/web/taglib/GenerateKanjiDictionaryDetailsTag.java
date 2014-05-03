package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.api.dto.KanjiDic2Entry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.dto.KanjivgEntry;
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
		
		pageHeader.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.title")));
		
		return pageHeader;
	}
	
	private Div generateMainInfo(Menu mainMenu) throws IOException {
		
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
		Div kanjiDiv = generateKanjiSection(mainInfoMenu);

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
		
		kanjiTable.addHtmlElement(kanjiKanjiTr);
		
		kanjiDivBody.addHtmlElement(kanjiTable);
		row2Div.addHtmlElement(kanjiDivBody);					
		
		kanjiDiv.addHtmlElement(row2Div);

		if (kanjivsEntry != null && kanjivsEntry.getStrokePaths().size() > 0) {
			
			// skrypt otwierajacy okienko
			kanjiDiv.addHtmlElement(GenerateDrawStrokeDialog.generateDrawStrokeButtonScript(kanjiDrawId));

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
				
		Div strokeCountDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div strokeCountTitleDiv = new Div("col-md-10");
    	
    	H strokeCountTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	strokeCountTitleH4.setId("strokeCountId");
    	
    	strokeCountTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.strokeCount.title")));
    	menu.getChildMenu().add(new Menu(strokeCountTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.strokeCount.title")));
    	
    	strokeCountTitleDiv.addHtmlElement(strokeCountTitleH4);
    	
    	row1Div.addHtmlElement(strokeCountTitleDiv);

    	// dodaj wiersz z tytulem
    	strokeCountDiv.addHtmlElement(row1Div);

    	// wiersz z liczba kresek
    	Div row2Div = new Div("row");
    	strokeCountDiv.addHtmlElement(row2Div);

		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
		
		Div strokeCountTextDiv = new Div("col-md-11");
		row2Div.addHtmlElement(strokeCountTextDiv);

		H strokeCountTextH4 = new H(4);
		strokeCountTextDiv.addHtmlElement(strokeCountTextH4);
		
		strokeCountTextH4.addHtmlElement(new Text(String.valueOf(kanjiDic2Entry.getStrokeCount())));
						
		return strokeCountDiv;
	}
	
	private Div generateRadicalsSection(Menu menu) throws IOException {
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
		
		if (kanjiDic2Entry == null) {
			return null;
		}
		
		List<String> radicals = kanjiDic2Entry.getRadicals();
		
		Div radicalsDiv = new Div();
				
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div radicalsTitleDiv = new Div("col-md-10");
    	
    	H radicalsTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	radicalsTitleH4.setId("radicalsId");
    	
    	radicalsTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.radicals.title")));
    	menu.getChildMenu().add(new Menu(radicalsTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.radicals.title")));    	
    	
    	radicalsTitleDiv.addHtmlElement(radicalsTitleH4);
    	
    	row1Div.addHtmlElement(radicalsTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	radicalsDiv.addHtmlElement(row1Div);

		// wiersz z elementami podstawowywmi
    	Div row2Div = new Div("row");
    	radicalsDiv.addHtmlElement(row2Div);
    	
    	for (int idx = 0; idx < radicals.size(); ++idx) {
    		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    		
    		Div currentRadicalDiv = new Div("col-md-11");
    		row2Div.addHtmlElement(currentRadicalDiv);
    		
    		H currentRadicalH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
    		currentRadicalH.addHtmlElement(new Text(radicals.get(idx)));
    		
    		currentRadicalDiv.addHtmlElement(currentRadicalH);
    	}
		
		return radicalsDiv;
	}
	
	private Div generateKunYomiSection(Menu menu) {
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
		
		if (kanjiDic2Entry == null) {
			return null;
		}
		
		List<String> kunReading = kanjiDic2Entry.getKunReading();
		
		Div kunReadingDiv = new Div();
				
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div kunReadingTitleDiv = new Div("col-md-10");
    	
    	H kunReadingTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	kunReadingTitleH4.setId("kunReadingId");
    	
    	kunReadingTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.kunReading.title")));
    	menu.getChildMenu().add(new Menu(kunReadingTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.kunReading.title")));    	
    	
    	kunReadingTitleDiv.addHtmlElement(kunReadingTitleH4);
    	
    	row1Div.addHtmlElement(kunReadingTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	kunReadingDiv.addHtmlElement(row1Div);

		// wiersz z czytaniem kun
    	Div row2Div = new Div("row");
    	kunReadingDiv.addHtmlElement(row2Div);
    	
    	for (int idx = 0; idx < kunReading.size(); ++idx) {
    		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    		
    		Div currentKunReadingDiv = new Div("col-md-11");
    		row2Div.addHtmlElement(currentKunReadingDiv);
    		
    		H currentKunReadingH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
    		currentKunReadingH.addHtmlElement(new Text(kunReading.get(idx)));
    		
    		currentKunReadingDiv.addHtmlElement(currentKunReadingH);
    	}
		
		return kunReadingDiv;		
	}
	
	private Div generateOnYomiSection(Menu menu) {
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
		
		if (kanjiDic2Entry == null) {
			return null;
		}
		
		List<String> onReading = kanjiDic2Entry.getOnReading();
		
		Div onReadingDiv = new Div();
				
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div onReadingTitleDiv = new Div("col-md-10");
    	
    	H onReadingTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	onReadingTitleH4.setId("onReadingId");
    	
    	onReadingTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.onReading.title")));
    	menu.getChildMenu().add(new Menu(onReadingTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.onReading.title")));    	
    	
    	onReadingTitleDiv.addHtmlElement(onReadingTitleH4);
    	
    	row1Div.addHtmlElement(onReadingTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	onReadingDiv.addHtmlElement(row1Div);

		// wiersz z czytaniem on
    	Div row2Div = new Div("row");
    	onReadingDiv.addHtmlElement(row2Div);
    	
    	for (int idx = 0; idx < onReading.size(); ++idx) {
    		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    		
    		Div currentOnReadingDiv = new Div("col-md-11");
    		row2Div.addHtmlElement(currentOnReadingDiv);
    		
    		H currentOnReadingH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
    		currentOnReadingH.addHtmlElement(new Text(onReading.get(idx)));
    		
    		currentOnReadingDiv.addHtmlElement(currentOnReadingH);
    	}
		
		return onReadingDiv;		
	}
	
	private Div generateTranslateSection(Menu menu) {
				
		List<String> translate = kanjiEntry.getPolishTranslates();
		
		Div translateDiv = new Div();
				
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div translateTitleDiv = new Div("col-md-10");
    	
    	H translateTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	translateTitleH4.setId("translateId");
    	
    	translateTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.translate.title")));
    	menu.getChildMenu().add(new Menu(translateTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.translate.title")));    	
    	
    	translateTitleDiv.addHtmlElement(translateTitleH4);
    	
    	row1Div.addHtmlElement(translateTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	translateDiv.addHtmlElement(row1Div);

		// wiersz z tlumaczeniem
    	Div row2Div = new Div("row");
    	translateDiv.addHtmlElement(row2Div);
    	
    	for (int idx = 0; idx < translate.size(); ++idx) {
    		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    		
    		Div currentTranslateDiv = new Div("col-md-11");
    		row2Div.addHtmlElement(currentTranslateDiv);
    		
    		H currentTranslateH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
    		currentTranslateH.addHtmlElement(new Text(translate.get(idx)));
    		
    		currentTranslateDiv.addHtmlElement(currentTranslateH);
    	}
		
		return translateDiv;		
	}
	
	private Div generateInfoSection(Menu menu) {
		
		String info = kanjiEntry.getInfo();
		
		if (info == null || info.length() <= 0) {
			return null;
		}
						
		Div infoDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div infoTitleDiv = new Div("col-md-10");
    	
    	H infoTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	infoTitleH4.setId("infoId");
    	
    	infoTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.info.title")));
    	menu.getChildMenu().add(new Menu(infoTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.info.title")));
    	
    	infoTitleDiv.addHtmlElement(infoTitleH4);
    	
    	row1Div.addHtmlElement(infoTitleDiv);

    	// dodaj wiersz z tytulem
    	infoDiv.addHtmlElement(row1Div);

    	// wiersz z informacjami dodatkowymi
    	Div row2Div = new Div("row");
    	infoDiv.addHtmlElement(row2Div);

		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
		
		Div infoTextDiv = new Div("col-md-11");
		row2Div.addHtmlElement(infoTextDiv);

		H infoTextH4 = new H(4);
		infoTextDiv.addHtmlElement(infoTextH4);
		
		infoTextH4.addHtmlElement(new Text(String.valueOf(info)));
						
		return infoDiv;
	}
	
	private Div generateKanjiGroupsSection(Menu menu) {
		
		// kanji groups
		List<GroupEnum> groups = kanjiEntry.getGroups();
		
		if (groups == null || groups.size() == 0) {
			return null;
		}
		
		Div kanjiGroupsDiv = new Div();
		
    	// wiersz z tytulem
    	Div row1Div = new Div("row");
    	
    	// tytul
    	Div kanjiGroupsTitleDiv = new Div("col-md-10");
    	
    	H kanjiGroupsTitleH4 = new H(4, null, "margin-top: 0px; font-weight:bold;");
    	
    	kanjiGroupsTitleH4.setId("kanjiGroupsId");
    	
    	kanjiGroupsTitleH4.addHtmlElement(new Text(getMessage("kanjiDictionaryDetails.page.kanjiEntry.groups.title")));
    	menu.getChildMenu().add(new Menu(kanjiGroupsTitleH4.getId(), getMessage("kanjiDictionaryDetails.page.kanjiEntry.groups.title")));    	
    	
    	kanjiGroupsTitleDiv.addHtmlElement(kanjiGroupsTitleH4);
    	
    	row1Div.addHtmlElement(kanjiGroupsTitleDiv);
    	
    	// dodaj wiersz z tytulem
    	kanjiGroupsDiv.addHtmlElement(row1Div);

		// wiersz z grupami
    	Div row2Div = new Div("row");
    	kanjiGroupsDiv.addHtmlElement(row2Div);
    	
    	for (int idx = 0; idx < groups.size(); ++idx) {
    		row2Div.addHtmlElement(new Div("col-md-1")); // przerwa
    		
    		Div currentKanjiGroupDiv = new Div("col-md-11");
    		row2Div.addHtmlElement(currentKanjiGroupDiv);
    		
    		H currentKanjiGroupH = new H(4, null, "margin-top: 0px;margin-bottom: 5px");
    		currentKanjiGroupH.addHtmlElement(new Text(groups.get(idx).getValue()));
    		
    		currentKanjiGroupDiv.addHtmlElement(currentKanjiGroupH);
    	}
		
		return kanjiGroupsDiv;		
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
		
		int fixme = 1; // sprawdzic
		
		//kanjiEntry.setKanjiDic2Entry(null);
		//kanjiEntry.setKanjivgEntry(null);
				
		this.kanjiEntry = kanjiEntry;
	}
	
	/*

	private List<IScreenItem> generateDetails(final KanjiEntry kanjiEntry) {
		
		List<IScreenItem> report = new ArrayList<IScreenItem>();
		
		KanjiDic2Entry kanjiDic2Entry = kanjiEntry.getKanjiDic2Entry();
				
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
