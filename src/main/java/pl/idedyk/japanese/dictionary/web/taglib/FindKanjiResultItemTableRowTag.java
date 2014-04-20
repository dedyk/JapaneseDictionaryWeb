package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dto.KanjiDic2Entry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Button.ButtonType;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;

public class FindKanjiResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindKanjiRequest findKanjiRequest;
	
	private KanjiEntry resultItem;
	
	private String detailsLink;
	private String detailsLinkValue;
	
	@Override
	public int doStartTag() throws JspException {
		
		try {
            JspWriter out = pageContext.getOut();
            
            Tr tr = new Tr();    
            
            // pobranie danych
            //
            String findWord = findKanjiRequest.word;

            String kanji = resultItem.getKanji();
            
            // kanji
	    	Td kanjiTd = new Td();
	    	tr.addHtmlElement(kanjiTd);
            
	    	kanjiTd.addHtmlElement(new Text(getStringWithMark(kanji, findWord, true)));
	    	
	    	// elementy podstawowe
	    	Td radicalsTd = new Td();
	    	tr.addHtmlElement(radicalsTd);

	    	KanjiDic2Entry kanjiDic2Entry = resultItem.getKanjiDic2Entry();
	    	
	    	if (kanjiDic2Entry != null) {
	    		List<String> radicals = kanjiDic2Entry.getRadicals();
	    		
	    		if (radicals != null) {
	    			radicalsTd.addHtmlElement(new Text(getStringWithMark(toString(radicals, null, " "), findWord, true)));	    			
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
            
            String link = detailsLink.replaceAll("%ID%", String.valueOf(resultItem.getId())).
            		replaceAll("%KANJI%", kanji != null ? kanji : "-");
            
            Button linkButton = new Button();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setButtonType(ButtonType.BUTTON);
            linkButton.setClazz("btn btn-default");
            linkButton.setOnClick("window.location = '" + link + "'");
            
            linkButton.addHtmlElement(new Text(detailsLinkValue));
            
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

	public String getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(String detailsLink) {
		this.detailsLink = detailsLink;
	}

	public String getDetailsLinkValue() {
		return detailsLinkValue;
	}

	public void setDetailsLinkValue(String detailsLinkValue) {
		this.detailsLinkValue = detailsLinkValue;
	}
}
