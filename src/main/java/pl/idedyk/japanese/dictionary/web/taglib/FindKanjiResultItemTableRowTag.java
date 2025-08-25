package pl.idedyk.japanese.dictionary.web.taglib;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.api.dictionary.Utils;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.dictionary.dto.WebRadicalInfo;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Img;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class FindKanjiResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindKanjiRequest findKanjiRequest;
	
	private KanjiCharacterInfo resultItem;
	private int resultItemIndex;

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
		
		//
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");		
		this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);		
		this.applicationProperties = (Properties)webApplicationContext.getBean("applicationProperties");
		
		if (servletRequest instanceof HttpServletRequest) {			
			HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
			
			userAgent = httpServletRequest.getHeader("User-Agent");			
		}
    	
    	boolean mobile = pl.idedyk.japanese.dictionary.web.common.Utils.isMobile(userAgent);
	
		try {
            JspWriter out = pageContext.getOut();
            
            // nowy wiersz
            Tr tr = new Tr();
            tr.setStyle("padding-bottom: 50px; " + (resultItemIndex % 2 == 0 ? "background-color: #f9f9f9" : ""));
            
            // pobranie danych
            String findWord = null;
            
            if (findKanjiRequest != null) {
            	findWord = findKanjiRequest.word;
            }
            
	    	// guzik do szczegolow - przygotowanie
	    	Td detailsLinkTd = new Td();
	    	detailsLinkTd.setStyle("width: 50%");
	    	            
            String link = LinkGenerator.generateKanjiDetailsLink(pageContext.getServletContext().getContextPath(), resultItem);
            
            A linkButton = new A();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage("kanjiDictionary.page.search.table.column.details.value", null, Locale.getDefault())));

            //
            
            // kanji
            {
                String kanji = resultItem.getKanji();
                
    	    	Td kanjiTd = new Td(null, "font-size: 300%; padding-right: 30px; vertical-align: top; width: 10%");
                
    	    	kanjiTd.addHtmlElement(new Text(getStringWithMark(kanji, findWord, true)));
    	    	tr.addHtmlElement(kanjiTd);
            }

	    	// szczegoly
            {
    	    	Td kanjiDetailsTd = new Td();
    	    	kanjiDetailsTd.setStyle("vertical-align: top; width: 40%");    	    	
    	    	
    	    	Table kanjiDetailsTdTable = new Table();
    	    	kanjiDetailsTdTable.setStyle("width: 100%");
    	    	
    	    	{    	    		
    	    		// liczba kresek
    	    		{
    	    			Tr kanjiDetailsTdTableTr = new Tr();
    	    			
    	    			// tytul
    	    			Td strokeCountTitleTd = new Td();    	    			
    	    			strokeCountTitleTd.setStyle("padding-right: 15px; font-weight:bold; font-size: 120%; vertical-align: top; width: 50%");
    	    	    	     	    	    	
    	    			strokeCountTitleTd.addHtmlElement(new Text(messageSource.getMessage("kanjiDictionary.page.search.table.column.strokeCount", new Object[] { }, Locale.getDefault())));
    	    	    	
    	    	    	// wartosc    	    	    	
    	    			Td strokeCountValueTd = new Td();
    	    			strokeCountValueTd.setStyle("font-size: 120%;");
    	    	    	
    	    	    	if (resultItem.getMisc().getStrokeCountList() != null && resultItem.getMisc().getStrokeCountList().size() > 0) {
    	    	    		strokeCountValueTd.addHtmlElement(new Text(String.valueOf(resultItem.getMisc().getStrokeCountList().get(0))));	    		
    	    	    	}
    	    	    	
    	    	    	kanjiDetailsTdTableTr.addHtmlElement(strokeCountTitleTd);
    	    	    	kanjiDetailsTdTableTr.addHtmlElement(strokeCountValueTd);
    	    	    	
    	    	    	kanjiDetailsTdTable.addHtmlElement(kanjiDetailsTdTableTr);
    	    		}
    	    		
    	    		// elementy podstawowe
    	    		{    	    			
        	    		Tr kanjiDetailsTdTableTr = new Tr();
    	    			
    	    	    	// elementy podstawowe    	    	    	
    	    			Td radicalsTitleTd = new Td();
    	    			
    	    			// tytul
    	    			radicalsTitleTd.setStyle("padding-right: 15px; font-weight:bold; font-size: 120%; vertical-align: top; width: 50%");    	    	    	
    	    			radicalsTitleTd.addHtmlElement(new Text(messageSource.getMessage("kanjiDictionary.page.search.table.column.radicals", new Object[] { }, Locale.getDefault())));
    	    			
    	    			// wartosc
    	    			Td radicalsValueTd = new Td();
    	    			radicalsValueTd.setStyle("font-size: 120%;");
    	    			
	    	    		List<String> radicals = resultItem.getMisc2().getRadicals();
	    	    		
	    	    		if (radicals != null) {
	    	    			
	    	    			for (String currentRadical : radicals) {
	    	    				
	    	    				WebRadicalInfo webRadicalInfo = dictionaryManager.getWebRadicalInfo(currentRadical);
	    	    				
	    	    				String webRadicalInfoImage = null;
	    	    				
	    	    				if (webRadicalInfo != null) {
	    	    					webRadicalInfoImage = webRadicalInfo.getImage();
	    	    					
	    	    				}
	    	    				
	    	    				if (webRadicalInfoImage == null) {
	    	    					radicalsValueTd.addHtmlElement(new Text(currentRadical + " "));
	    	    					
	    	    				} else {	    					
	    	    					String staticPrefix = LinkGenerator.getStaticPrefix(pageContext.getServletContext().getContextPath(), applicationProperties);
	    	    					
	    	    					Img currentRadicalImg = new Img();
	    	    					
	    	    					currentRadicalImg.setSrc(staticPrefix + "/" + webRadicalInfoImage);
	    	    					currentRadicalImg.setAlt(currentRadical);
	    	    					
	    	    					radicalsValueTd.addHtmlElement(currentRadicalImg);
	    	    				}
	    					}
	    	    		}
    	    			
    	    			kanjiDetailsTdTableTr.addHtmlElement(radicalsTitleTd);    	    			
    	    			kanjiDetailsTdTableTr.addHtmlElement(radicalsValueTd);
    	    	    	
    	    			kanjiDetailsTdTable.addHtmlElement(kanjiDetailsTdTableTr);
    	    		}
    	    		
    	    		// znaczenie
    	    		{
    	    			Tr kanjiDetailsTdTableTr = new Tr();
    	    			
    	    			// tytul
    	    			Td translateTitleTd = new Td();    	    			
    	    			translateTitleTd.setStyle("padding-right: 15px; font-weight:bold; font-size: 120%; vertical-align: top; width: 50%");
    	    	    	     	    	    	
    	    			translateTitleTd.addHtmlElement(new Text(messageSource.getMessage("kanjiDictionary.page.search.table.column.translate", new Object[] { }, Locale.getDefault())));
    	    	    	
    	    	    	// wartosc    	    	    	
    	    			Td translateValueTd = new Td();
    	    			translateValueTd.setStyle("font-size: 120%; padding-bottom: 20px");
    	    	    	    	    			
    	    	    	List<String> polishTranslates = Utils.getPolishTranslates(resultItem);
    	    	    	    	    	    	
    	    	    	if (polishTranslates != null && polishTranslates.size() > 0) {
    	    	    		translateValueTd.addHtmlElement(new Text(getStringWithMark(toString(polishTranslates, null, "<br/>"), findWord, true)));
    	    	    		
    	    	    		String info = Utils.getPolishAdditionalInfo(resultItem);
    	    	    		
    	    	    		// informcje dodatkowe
    	    	    		if (info != null && info.equals("") == false) {
    	    	    			
    	    	    			Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify");
    	    	    			
    	    	    			infoDiv.addHtmlElement(new Text(getStringWithMark(info, findWord, true)));
    	    	    			
    	    	    			translateValueTd.addHtmlElement(infoDiv);		    			
    	    	    		}
    	    	    	}
    	    	    	
    	    	    	kanjiDetailsTdTableTr.addHtmlElement(translateTitleTd);
    	    	    	kanjiDetailsTdTableTr.addHtmlElement(translateValueTd);
    	    	    	
    	    	    	kanjiDetailsTdTable.addHtmlElement(kanjiDetailsTdTableTr);
    	    		}
    	    		
    	    		// guzik do szczegolow - tylko dla wersji mobilnej
    	    		if (mobile == true) {
    	    			detailsLinkTd.setStyle(detailsLinkTd.getStyle() + "; padding-bottom: 20px");
    	    			
    	    			kanjiDetailsTdTable.addHtmlElement(detailsLinkTd);	    		
    		    	} 
    	    	}    	    	
    	    	
    	    	kanjiDetailsTd.addHtmlElement(kanjiDetailsTdTable);
    	    	tr.addHtmlElement(kanjiDetailsTd);            	
            }
            
            // guzik do szczegolow
            
	    	// gdy jest to normalna strona to daj guzik po lewej stronie
	    	if (mobile == false) {
	    		detailsLinkTd.setStyle(detailsLinkTd.getStyle() + "; padding-left: 20px");
	    		
	    		tr.addHtmlElement(detailsLinkTd);	    		
	    	}            

            //            
	    	
	    	tr.render(out);
            	    	
	    	/* stary kod
	    	// informacje dodatkowe
	    	if (userAgent == null || Utils.isMobile(userAgent) == false) {	    	

		    	String info = resultItem.getInfo();
		    	
		    	Td infoTd = new Td();
		    	tr.addHtmlElement(infoTd);
		    	
		    	if (info != null && info.equals("") == false) {
		    		infoTd.addHtmlElement(new Text(getStringWithMark(info, findWord, true)));
		    	}
	    	}
	    	*/
            
            return SKIP_BODY;
 
        } catch (Exception e) {
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

	public KanjiCharacterInfo getResultItem() {
		return resultItem;
	}

	public void setResultItem(KanjiCharacterInfo resultItem) {
		this.resultItem = resultItem;
	}

	public void setResultItemIndex(int resultItemIndex) {
		this.resultItemIndex = resultItemIndex;
	}
}
