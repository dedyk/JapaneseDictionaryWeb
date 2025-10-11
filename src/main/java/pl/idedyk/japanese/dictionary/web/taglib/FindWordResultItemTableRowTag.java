package pl.idedyk.japanese.dictionary.web.taglib;

import java.util.List;
import java.util.Locale;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Br;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Span;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.taglib.utils.WordDictionary2SenseUtils;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.KanjiKanaPair;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict.Entry;

public class FindWordResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindWordRequest findWordRequest;
	
	private FindWordResult.ResultItem resultItem;
	
	private MessageSource messageSource;
		
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
		
		String link;
		
		try {
            JspWriter out = pageContext.getOut();
            
            Tr tr = new Tr();            
            
            // pobranie danych
            //
            String findWord = findWordRequest.word;
                        
            // tylko jeden z nich bedzie wypelniony
            Entry entry = resultItem.getEntry();
            DictionaryEntry dictionaryEntry = resultItem.getDictionaryEntry();

            Td translateTd;
            
            if (entry != null) {
            	// wygenerowanie wszystkich kombinacji
            	List<KanjiKanaPair> kanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(entry, true);
            	            	
            	// slowo
    	    	Td wordTd = new Td();   
    	    	tr.addHtmlElement(wordTd);
    	    	
    	    	Div wordDiv = new Div(null, "width: 100%");
    	    	wordTd.addHtmlElement(wordDiv);
    	    	
    	    	for (int kanjiKanaPairIdx = 0; kanjiKanaPairIdx < kanjiKanaPairList.size(); ++kanjiKanaPairIdx) {
    	    		
    	    		KanjiKanaPair kanjiKanaPair = kanjiKanaPairList.get(kanjiKanaPairIdx);
                	    	    		    	    		   	    		
    	    		// pobieramy wszystkie skladniki slowa
    	    		String kanji = kanjiKanaPair.getKanji();
    	    		String kana = kanjiKanaPair.getKana();
    	        	String romaji = kanjiKanaPair.getRomaji();
    	    		    	        	
            		Div singleWordDiv = createWordColumn(findWordRequest, findWord, kanji, kana, romaji, mobile);
            		                	
                	wordDiv.addHtmlElement(singleWordDiv);
				}
            	            	
            	// znaczenie
    	    	translateTd = new Td(null, "padding-top: 10px");
    	    	tr.addHtmlElement(translateTd);
    	    	
    	    	WordDictionary2SenseUtils.createSenseHtmlElements(messageSource, pageContext.getServletContext().getContextPath(), entry, translateTd, findWord, false);
    	    	                
                // link
                link = LinkGenerator.generateDictionaryEntryDetailsLink(pageContext.getServletContext().getContextPath(), entry);
                                
            } else if (dictionaryEntry != null) { // obsluga starego formatu
            	
            	// slowo
    	    	Td wordTd = new Td();
    	    	tr.addHtmlElement(wordTd);
    	    	
    	    	Div wordDiv = new Div(null, "width: 100%");
    	    	wordTd.addHtmlElement(wordDiv);
    	    	
    	    	String kanji = dictionaryEntry.getKanji();
    	    	String kana = dictionaryEntry.getKana();
	        	String romaji = dictionaryEntry.getRomaji();
	        	
        		Div singleWordDiv = createWordColumn(findWordRequest, findWord, kanji, kana, romaji, mobile);
        		                	
            	wordDiv.addHtmlElement(singleWordDiv);
            	
            	// znaczenie
    	    	translateTd = new Td(null, "padding-top: 10px");
    	    	tr.addHtmlElement(translateTd);
            	
            	List<String> translates = dictionaryEntry.getTranslates();
            	String info = dictionaryEntry.getInfo();
            	
		    	if (translates != null && translates.size() > 0) {
		    		translateTd.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(toString(translates, null), findWord, findWordRequest.searchTranslate)));
					
		    		// informcje dodatkowe
		    		if (info != null && info.equals("") == false) {
		    			
		    			Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify");
		    			
		    			infoDiv.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(info, findWord, findWordRequest.searchInfo)));
		    			
		    			translateTd.addHtmlElement(infoDiv);		    			
		    		}
		    	}
		    	
		    	// info
		    	/*
		    	if (userAgent == null || Utils.isMobile(userAgent) == false) {	    	
		    		
			    	Td infoTd = new Td();
			    	tr.addHtmlElement(infoTd);
			    	
			    	if (info != null && info.equals("") == false) {
			    		infoTd.addHtmlElement(new Text(getStringWithMark(info, findWord, findWordRequest.searchInfo)));
			    	}
		    	}
		    	*/
		    	
		    	link = LinkGenerator.generateDictionaryEntryDetailsLink(pageContext.getServletContext().getContextPath(), dictionaryEntry, null);
		    	
            } else { // to nigdy nie powinno wydarzyc sie
            	throw new RuntimeException();
            }


            // details link
	    	A linkButton = new A();
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage(
            		"wordDictionary.page.search.table.column.details.value", null, Locale.getDefault())));
            
            if (mobile == false) {
    	    	Td detailsLinkTd = new Td();
                detailsLinkTd.addHtmlElement(linkButton);

		    	tr.addHtmlElement(detailsLinkTd);
		    	
            } else {
            	Div divForLinkButton = new Div(null, "padding: 30px 0px 0px 0px;");
            	divForLinkButton.addHtmlElement(linkButton);
            	
            	translateTd.addHtmlElement(divForLinkButton);
            }
            
            tr.render(out);
                        
            return SKIP_BODY;
 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
		
	private Div createWordColumn(FindWordRequest findWordRequest, String findWord, String kanji, String kana, String romaji, boolean mobile) {
			
		if (mobile == false) {
			Div singleWordDiv = new Div(null, "display: flex; width: 100%; ");
			
	    	// kanji
	    	Span singleWordKanjiSpan = new Span(null, "width: 33%; padding: 5px 15px 10px 0px; overflow-wrap: break-word");
	    	
	    	if (kanji != null) {
	    		singleWordKanjiSpan.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(kanji, findWord, findWordRequest.searchKanji)));
	    	}
	    	
	    	// kana
	    	Span singleWordKanaSpan = new Span(null, "width: 33%; padding: 5px 15px 10px 0px; overflow-wrap: break-word;");
	    	singleWordKanaSpan.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(kana, findWord, findWordRequest.searchKana)));
	    	
	    	// romaji
	    	Span singleWordRomajiSpan = new Span(null, "width: 33%; padding: 5px 0px 10px 0px; overflow-wrap: break-word;");
	    	singleWordRomajiSpan.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(romaji, findWord, findWordRequest.searchRomaji)));
	    	
	    	// dodanie elementow                    	
	    	singleWordDiv.addHtmlElement(singleWordKanjiSpan);
	    	singleWordDiv.addHtmlElement(singleWordKanaSpan);
	    	singleWordDiv.addHtmlElement(singleWordRomajiSpan);
	    	
	    	return singleWordDiv;
			
		} else {
			Div singleWordDiv = new Div(null, "display: flex; width: 100%; padding: 0px 0px 30px 0px;");
			
			Span singleWordSpan = new Span(null, "width: 100%; padding: 5px 15px 10px 0px; overflow-wrap: break-word; line-height: 1.5;");
			
			if (kanji != null) {
				singleWordSpan.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(kanji, findWord, findWordRequest.searchKanji)));
				singleWordSpan.addHtmlElement(new Br());
			}
			
			singleWordSpan.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(kana, findWord, findWordRequest.searchKana)));
			singleWordSpan.addHtmlElement(new Br());
			singleWordSpan.addHtmlElement(new Text(WordDictionary2SenseUtils.getStringWithMark(romaji, findWord, findWordRequest.searchRomaji)));
			
			singleWordDiv.addHtmlElement(singleWordSpan);
			
			return singleWordDiv;
		}		    	                		
	}
	    
	private String toString(List<String> listString, String prefix) {
		
		StringBuffer sb = new StringBuffer();
				
		for (int idx = 0; idx < listString.size(); ++idx) {
			if (prefix != null) {
				sb.append("(").append(prefix).append(") ");
			}
			
			sb.append(listString.get(idx));
			
			if (idx != listString.size() - 1) {
				sb.append("<br/>");
			}
		}
				
		return sb.toString();
	}

	public FindWordResult.ResultItem getResultItem() {
		return resultItem;
	}

	public void setResultItem(FindWordResult.ResultItem resultItem) {
		this.resultItem = resultItem;
	}

	public FindWordRequest getFindWordRequest() {
		return findWordRequest;
	}

	public void setFindWordRequest(FindWordRequest findWordRequest) {
		this.findWordRequest = findWordRequest;
	}
}
