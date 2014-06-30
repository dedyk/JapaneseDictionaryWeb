package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dto.KanjiDic2Entry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.dictionary.dto.WebRadicalInfo;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Img;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;

public class FindKanjiResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindKanjiRequest findKanjiRequest;
	
	private KanjiEntry resultItem;

	private MessageSource messageSource;
	
	private DictionaryManager dictionaryManager;
	
	private Properties applicationProperties;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		
		this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
		
		this.applicationProperties = (Properties)webApplicationContext.getBean("applicationProperties");
		
		try {
            JspWriter out = pageContext.getOut();
            
            Tr tr = new Tr();    
            
            // pobranie danych
            
            String findWord = null;
            
            if (findKanjiRequest != null) {
            	findWord = findKanjiRequest.word;
            }            

            String kanji = resultItem.getKanji();
            
            // kanji
	    	Td kanjiTd = new Td(null, "font-size: 150%");
	    	tr.addHtmlElement(kanjiTd);
            
	    	kanjiTd.addHtmlElement(new Text(getStringWithMark(kanji, findWord, true)));
	    	
	    	// elementy podstawowe
	    	Td radicalsTd = new Td();
	    	tr.addHtmlElement(radicalsTd);

	    	KanjiDic2Entry kanjiDic2Entry = resultItem.getKanjiDic2Entry();
	    	
	    	if (kanjiDic2Entry != null) {
	    		List<String> radicals = kanjiDic2Entry.getRadicals();
	    		
	    		if (radicals != null) {
	    			
	    			for (String currentRadical : radicals) {
	    				
	    				WebRadicalInfo webRadicalInfo = dictionaryManager.getWebRadicalInfo(currentRadical);
	    				
	    				String webRadicalInfoImage = webRadicalInfo.getImage();
	    				
	    				if (webRadicalInfoImage == null) {
	    					radicalsTd.addHtmlElement(new Text(currentRadical + " "));
	    					
	    				} else {	    					
	    					String staticPrefix = LinkGenerator.getStaticPrefix(pageContext.getServletContext().getContextPath(), applicationProperties);
	    					
	    					Img currentRadicalImg = new Img();
	    					
	    					currentRadicalImg.setSrc(staticPrefix + "/" + webRadicalInfoImage);
	    					currentRadicalImg.setAlt(currentRadical);
	    					
	    					radicalsTd.addHtmlElement(currentRadicalImg);
	    				}
					}
	    		}
	    	}
	    	
	    	// liczba kresek
	    	Td strokeCountTd = new Td();
	    	tr.addHtmlElement(strokeCountTd);
	    	
	    	if (kanjiDic2Entry != null) {
	    		strokeCountTd.addHtmlElement(new Text(String.valueOf(kanjiDic2Entry.getStrokeCount())));	    		
	    	}
	    	
	    	// tlumaczenie
	    	List<String> polishTranslates = resultItem.getPolishTranslates();
	    	
	    	Td translateTd = new Td();
	    	tr.addHtmlElement(translateTd);
	    	
	    	if (polishTranslates != null && polishTranslates.size() > 0) {
	    		translateTd.addHtmlElement(new Text(getStringWithMark(toString(polishTranslates, null, "<br/>"), findWord, true)));
	    	}
	    	
	    	// informacje dodatkowe
	    	String info = resultItem.getInfo();
	    	
	    	Td infoTd = new Td();
	    	tr.addHtmlElement(infoTd);
	    	
	    	if (info != null && info.equals("") == false) {
	    		infoTd.addHtmlElement(new Text(getStringWithMark(info, findWord, true)));
	    	}
	    	// szczegoly
	    	Td detailsLinkTd = new Td();
	    	tr.addHtmlElement(detailsLinkTd);
            
            String link = LinkGenerator.generateKanjiDetailsLink(pageContext.getServletContext().getContextPath(), resultItem);
            
            A linkButton = new A();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage("kanjiDictionary.page.search.table.column.details.value", null, Locale.getDefault())));
            
            tr.render(out);
            
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
    private String getStringWithMark(String text, String findWord, boolean mark) {
    	
    	if (mark == false || findWord == null || findWord.trim().equals("") == true) {
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
    
	private String toString(List<String> listString, String prefix, String separator) {
		
		StringBuffer sb = new StringBuffer();
				
		for (int idx = 0; idx < listString.size(); ++idx) {
			if (prefix != null) {
				sb.append("(").append(prefix).append(") ");
			}
			
			sb.append(listString.get(idx));
			
			if (idx != listString.size() - 1) {
				sb.append(separator);
			}
		}
				
		return sb.toString();
	}

	public FindKanjiRequest getFindKanjiRequest() {
		return findKanjiRequest;
	}

	public void setFindKanjiRequest(FindKanjiRequest findKanjiRequest) {
		this.findKanjiRequest = findKanjiRequest;
	}

	public KanjiEntry getResultItem() {
		return resultItem;
	}

	public void setResultItem(KanjiEntry resultItem) {
		this.resultItem = resultItem;
	}
}
