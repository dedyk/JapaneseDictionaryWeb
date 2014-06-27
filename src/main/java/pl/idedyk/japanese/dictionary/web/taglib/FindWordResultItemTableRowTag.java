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

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;

public class FindWordResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindWordRequest findWordRequest;
	
	private FindWordResult.ResultItem resultItem;
	
	private MessageSource messageSource;
		
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		
		try {
            JspWriter out = pageContext.getOut();
            
            Tr tr = new Tr();            
            
            // pobranie danych
            //
            String findWord = findWordRequest.word;
            
	    	String kanji = resultItem.getKanji();
	    	String prefixKana = resultItem.getPrefixKana();
	    	List<String> kanaList = resultItem.getKanaList();
	    	String prefixRomaji = resultItem.getPrefixRomaji();
	    	List<String> romajiList = resultItem.getRomajiList();
	    	List<String> translates = resultItem.getTranslates();
	    	String info = resultItem.getInfo();

	    	String tempPrefixKana = prefixKana != null && prefixKana.equals("") == false ? prefixKana : null;
	    	String tempPrefixRomaji = prefixRomaji != null && prefixRomaji.equals("") == false ? prefixRomaji : null;
            	    	
            // kanji
	    	Td kanjiTd = new Td();
	    	tr.addHtmlElement(kanjiTd);
            
	    	if (resultItem.isKanjiExists() == true) {

	    		if (tempPrefixKana != null) {
	    			kanjiTd.addHtmlElement(new Text("(" + getStringWithMark(tempPrefixKana, findWord, false) + ") "));
	    		}

	    		kanjiTd.addHtmlElement(new Text(getStringWithMark(kanji, findWord, findWordRequest.searchKanji)));
	    	}
            
	    	// kana
	    	Td kanaTd = new Td();
	    	tr.addHtmlElement(kanaTd);
	    	
	    	if (kanaList != null && kanaList.size() > 0) {
	    		kanaTd.addHtmlElement(new Text(getStringWithMark(toString(kanaList, tempPrefixKana), findWord, findWordRequest.searchKana)));
	    	}
	    	
	    	// romaji
	    	Td romajiTd = new Td();
	    	tr.addHtmlElement(romajiTd);
	    	
	    	if (romajiList != null && romajiList.size() > 0) {
	    		romajiTd.addHtmlElement(new Text(getStringWithMark(toString(romajiList, tempPrefixRomaji), findWord, findWordRequest.searchRomaji)));
	    	}
	    	
	    	// translates
	    	Td translateTd = new Td();
	    	tr.addHtmlElement(translateTd);
	    	
	    	if (translates != null && translates.size() > 0) {
	    		translateTd.addHtmlElement(new Text(getStringWithMark(toString(translates, null), findWord, findWordRequest.searchTranslate)));
	    	}
	    	
	    	// info
	    	Td infoTd = new Td();
	    	tr.addHtmlElement(infoTd);
	    	
	    	if (info != null && info.equals("") == false) {
	    		infoTd.addHtmlElement(new Text(getStringWithMark(info, findWord, findWordRequest.searchInfo)));
	    	}
            
            // details link
	    	Td detailsLinkTd = new Td();
	    	tr.addHtmlElement(detailsLinkTd);
            
            String link = LinkGenerator.generateDictionaryEntryDetailsLink(pageContext.getServletContext().getContextPath(), resultItem.getDictionaryEntry(), null);
            A linkButton = new A();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage(
            		"wordDictionary.page.search.table.column.details.value", null, Locale.getDefault())));
            
            tr.render(out);
            
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
    private String getStringWithMark(String text, String findWord, boolean mark) {
    	
    	if (mark == false || findWord == null) {
    		return text;
    	}
    	
    	String findWordLowerCase = findWord.toLowerCase(Locale.getDefault());
    	
		StringBuffer texStringBuffer = new StringBuffer(text);								
		StringBuffer textLowerCaseStringBuffer = new StringBuffer(text.toLowerCase(Locale.getDefault()));
										
		int idxStart = 0;
		
		final String fontBegin = "<font color='red'>";
		final String fontEnd = "</font>";
		
		while(true) {
			
			int idx1 = textLowerCaseStringBuffer.indexOf(findWordLowerCase, idxStart);
			
			if (idx1 == -1) {
				break;
			}
			
			texStringBuffer.insert(idx1, fontBegin);
			textLowerCaseStringBuffer.insert(idx1, fontBegin);
			
			texStringBuffer.insert(idx1 + findWordLowerCase.length() + fontBegin.length(), fontEnd);
			textLowerCaseStringBuffer.insert(idx1 + findWordLowerCase.length() + fontBegin.length(), fontEnd);

			idxStart = idx1 + findWordLowerCase.length() + fontBegin.length() + fontEnd.length();
		}
		
		return texStringBuffer.toString();
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
