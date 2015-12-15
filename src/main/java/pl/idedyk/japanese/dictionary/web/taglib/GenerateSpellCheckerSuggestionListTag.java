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

import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Text;

public class GenerateSpellCheckerSuggestionListTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String id;
	
	private List<String> spellCheckerSuggestionList;
	
	private String type;
	
	private MessageSource messageSource;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		
		try {
            JspWriter out = pageContext.getOut();

            if (spellCheckerSuggestionList == null || spellCheckerSuggestionList.size() == 0) {
            	return SKIP_BODY;
            }
            
            Div mainDiv = new Div("alert", "font-size: 115%");
            
            mainDiv.setId(id);
            
            //
            
            mainDiv.addHtmlElement(new Text(messageSource.getMessage("wordDictionary.page.label.wordDictionaryEntrySpellCheckerSuggestionList.info", null, Locale.getDefault())));
            
            //
            
            for (int spellCheckerSuggestionListIdx = 0; spellCheckerSuggestionListIdx < spellCheckerSuggestionList.size(); ++spellCheckerSuggestionListIdx) {
            	
            	String currentSpellCheckerSuggestion = spellCheckerSuggestionList.get(spellCheckerSuggestionListIdx);
            	
            	A link = new A();
            	
            	if (type.equals("wordDictionaryEntry") == true) {
            		
            		WordDictionarySearchModel searchModel = new WordDictionarySearchModel();
            		
            		searchModel.setWord(currentSpellCheckerSuggestion);
            		searchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());
            		
            		List<String> searchIn = new ArrayList<String>();
            		
            		searchIn.add("KANJI");
            		searchIn.add("KANA");
            		searchIn.add("ROMAJI");
            		searchIn.add("TRANSLATE");
            		searchIn.add("INFO");
            		searchIn.add("GRAMMA_FORM_AND_EXAMPLES");
            		searchIn.add("NAMES");
            				
            		searchModel.setSearchIn(searchIn);
            		
            		List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();
            		
            		for (DictionaryEntryType dictionaryEntryType : addableDictionaryEntryList) {
            			searchModel.addDictionaryType(dictionaryEntryType);
            		}
            		
            		link.setHref(LinkGenerator.generateWordSearchLink(pageContext.getServletContext().getContextPath(), searchModel));
            	}
            	
            	link.addHtmlElement(new Text(currentSpellCheckerSuggestion));
            	            	
            	mainDiv.addHtmlElement(link);
            	
            	if (spellCheckerSuggestionListIdx != spellCheckerSuggestionList.size() - 1) {
            		mainDiv.addHtmlElement(new Text(", "));
            	}
            }            
            
            mainDiv.render(out);
            
            return SKIP_BODY;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getSpellCheckerSuggestionList() {
		return spellCheckerSuggestionList;
	}

	public void setSpellCheckerSuggestionList(List<String> spellCheckerSuggestionList) {
		this.spellCheckerSuggestionList = spellCheckerSuggestionList;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
