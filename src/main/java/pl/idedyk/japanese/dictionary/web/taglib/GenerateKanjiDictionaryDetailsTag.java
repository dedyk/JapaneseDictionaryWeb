package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Li;
import pl.idedyk.japanese.dictionary.web.html.Script;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Ul;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;

public class GenerateKanjiDictionaryDetailsTag extends TagSupport {
	
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
	
	private Div generateMenu(Menu mainMenu) {
		
		Div menuDiv = new Div("col-md-2");
		
        Ul ul = new Ul(null, "width: 300px");
		menuDiv.addHtmlElement(ul);
		
		ul.setId("sidebar");
		
		generateMenuSubMenu(ul, mainMenu.getChildMenu());
		
		Script script = new Script();
		
		StringBuffer scriptBody = new StringBuffer();
		
		scriptBody.append("$(function() {\n");
		scriptBody.append("$( \"#sidebar\" ).menu(); \n");
		scriptBody.append("});\n\n");
				
		scriptBody.append("$(function() {\n");
		scriptBody.append("\n");
		scriptBody.append("    var $sidebar   = $(\"#sidebar\"), \n");
		scriptBody.append("        $window    = $(window),\n");
		scriptBody.append("        offset     = $sidebar.offset(),\n");
		scriptBody.append("        topPadding = 25;\n");
		scriptBody.append("\n");
		scriptBody.append("    $window.scroll(function() {\n");
		scriptBody.append("        if ($window.scrollTop() > offset.top) {\n");
		scriptBody.append("            $sidebar.stop().animate({\n");
		scriptBody.append("                marginTop: $window.scrollTop() - offset.top + topPadding\n");
		scriptBody.append("            });\n");
		scriptBody.append("        } else {\n");
		scriptBody.append("            $sidebar.stop().animate({\n");
		scriptBody.append("                marginTop: 0\n");
		scriptBody.append("            });\n");
		scriptBody.append("        }\n");
		scriptBody.append("    });\n");
		scriptBody.append("    \n");
		scriptBody.append("})\n");
		
		script.addHtmlElement(new Text(scriptBody.toString()));
		
		menuDiv.addHtmlElement(script);
		        
        return menuDiv;
	}
	
	private Ul generateMenuSubMenu(Ul parentUl, List<Menu> menuList) {
		
		Ul ul = null;
		
		if (parentUl != null) {
			ul = parentUl;
			
		} else {
			ul = new Ul(null, "width: 370px");
		}
				
		for (Menu currentMenuList : menuList) {
			
			Li li = new Li();
			ul.addHtmlElement(li);
			
			A link = new A(null, "padding-bottom: 0px; padding-top: 0px");
			li.addHtmlElement(link);
			
			link.setHref("#");
			
			link.setOnClick("$('html, body').animate({ " 
					+ "scrollTop: $('#" + currentMenuList.getId() + "').offset().top - 15 " 
					+ "}, 1000); return false; ");
					
			link.addHtmlElement(new Text(currentMenuList.getTitle()));
			
			if (currentMenuList.getChildMenu().size() > 0) {
				li.addHtmlElement(generateMenuSubMenu(null, currentMenuList.getChildMenu()));
			}
		}		
		
		return ul;		
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
