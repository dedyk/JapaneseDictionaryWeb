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
import pl.idedyk.japanese.dictionary.api.dto.Attribute;
import pl.idedyk.japanese.dictionary.api.dto.AttributeType;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.KanjiKanaPair;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSense;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSenseEntry;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon.PrintableSenseEntryGloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;

public class FindWordResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindWordRequest findWordRequest;
	
	private FindWordResult.ResultItem resultItem;
	
	private MessageSource messageSource;
	
	private DictionaryManager dictionaryManager;
		
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		/*
		ServletRequest servletRequest = pageContext.getRequest();
		
		String userAgent = null;
		
		if (servletRequest instanceof HttpServletRequest) {			
			HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
			
			userAgent = httpServletRequest.getHeader("User-Agent");			
		}
		*/
				
		//
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.dictionaryManager = webApplicationContext.getBean(DictionaryManager.class);
		
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
	    	
	    	// translates i informacje dodatkowe
	    	Td translateTd = new Td();
	    	tr.addHtmlElement(translateTd);
	    	
	    	// sprawdzenie czy wystepuja dane w nowym formacie
	    	JMdict.Entry dictionaryEntry2 = null;
	    	
	    	if (resultItem.getDictionaryEntry() != null && resultItem.getDictionaryEntry().isName() == false) {
	    			    		
				List<Attribute> jmdictEntryIdAttributeList = resultItem.getDictionaryEntry().getAttributeList().getAttributeList(AttributeType.JMDICT_ENTRY_ID);
				
				if (jmdictEntryIdAttributeList != null && jmdictEntryIdAttributeList.size() > 0) { // cos jest
					
					// pobieramy entry id
					Integer entryId = Integer.parseInt(jmdictEntryIdAttributeList.get(0).getAttributeValue().get(0));
					
					// pobieramy z bazy danych
					dictionaryEntry2 = dictionaryManager.getDictionaryEntry2ById(entryId);				
				}
	    	}
	    	
	    	// nie ma danych w nowym formacie, generujemy po staremu
	    	if (dictionaryEntry2 == null) { 
	    		
		    	if (translates != null && translates.size() > 0) {
		    		translateTd.addHtmlElement(new Text(getStringWithMark(toString(translates, null), findWord, findWordRequest.searchTranslate)));
					
		    		// informcje dodatkowe
		    		if (info != null && info.equals("") == false) {
		    			
		    			Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify");
		    			
		    			infoDiv.addHtmlElement(new Text(getStringWithMark(info, findWord, findWordRequest.searchInfo)));
		    			
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

	    	} else { // sa dane w nowym formacie
	    		
				List<KanjiKanaPair> kanjiKanaPairList = Dictionary2HelperCommon.getKanjiKanaPairListStatic(dictionaryEntry2);
				
				// szukamy konkretnego znaczenia dla naszego slowa
				KanjiKanaPair dictionaryEntry2KanjiKanaPair = Dictionary2HelperCommon.findKanjiKanaPair(kanjiKanaPairList, resultItem.getDictionaryEntry());

				//
				
		    	PrintableSense printableSense = Dictionary2HelperCommon.getPrintableSense(dictionaryEntry2KanjiKanaPair);
				
				// mamy znaczenia
				for (int senseIdx = 0; senseIdx < printableSense.getSenseEntryList().size(); ++senseIdx) {
					
					PrintableSenseEntry printableSenseEntry = printableSense.getSenseEntryList().get(senseIdx);
										
					// czesci mowy
					/*
					if (printableSenseEntry.getPolishPartOfSpeechValue() != null) {					
						translateTd.addHtmlElement(new Text(printableSenseEntry.getPolishPartOfSpeechValue() + "<br/>"));
					}
					*/				
					
					for (int currentGlossIdx = 0; currentGlossIdx < printableSenseEntry.getGlossList().size(); ++currentGlossIdx) {
						
						PrintableSenseEntryGloss printableSenseEntryGloss = printableSenseEntry.getGlossList().get(currentGlossIdx);
												
						translateTd.addHtmlElement(new Text(getStringWithMark(
								printableSenseEntryGloss.getGlossValue(), findWord, findWordRequest.searchTranslate) + 
								(printableSenseEntryGloss.getGlossValueGType() != null ? " (" + printableSenseEntryGloss.getGlossValueGType() + ")" : "") + 
								(currentGlossIdx != printableSenseEntry.getGlossList().size() - 1 ? "<br/>" : "")));						
					}					
					
					// informacje dodatkowe												
					if (printableSenseEntry.getAdditionalInfoValue() != null) {					
						Div infoDiv = new Div(null, "margin-left: 40px; margin-top: 3px; text-align: justify");
						
		    			infoDiv.addHtmlElement(new Text(getStringWithMark(printableSenseEntry.getAdditionalInfoValue(), findWord, findWordRequest.searchInfo)));
		    			
		    			translateTd.addHtmlElement(infoDiv);						
					}
					
					// przerwa
					if (senseIdx != printableSense.getSenseEntryList().size() - 1) {
						
						Div marginDiv = new Div(null, "margin-bottom: 12px;");
						
						translateTd.addHtmlElement(marginDiv);						
					}
				}
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
 
        } catch (Exception e) {
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
