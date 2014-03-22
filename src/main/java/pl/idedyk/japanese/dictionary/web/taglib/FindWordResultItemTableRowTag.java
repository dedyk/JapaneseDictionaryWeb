package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;

public class FindWordResultItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private FindWordRequest findWordRequest;
	
	private FindWordResult.ResultItem resultItem;
	
	private String detailsLink;
	private String detailsLinkValue;
	
	@Override
	public int doStartTag() throws JspException {
		
		try {
            JspWriter out = pageContext.getOut();
 
            out.println("<tr>");
            
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
	    	out.println("<td>");
            
	    	if (resultItem.isKanjiExists() == true) {

	    		if (tempPrefixKana != null) {
	    			out.println("(" + getStringWithMark(tempPrefixKana, findWord, false) + ") ");
	    		}

	    		out.println(getStringWithMark(kanji, findWord, findWordRequest.searchKanji));
	    	}
	    	out.println("</td>");
            
	    	// kana
	    	out.println("<td>");
	    	
	    	if (kanaList != null && kanaList.size() > 0) {
	    		out.println(getStringWithMark(toString(kanaList, tempPrefixKana), findWord, findWordRequest.searchKana));
	    	}
	    	out.println("</td>");
	    	
	    	// romaji
	    	out.println("<td>");
	    	
	    	if (romajiList != null && romajiList.size() > 0) {
	    		out.println(getStringWithMark(toString(romajiList, tempPrefixRomaji), findWord, findWordRequest.searchRomaji));
	    	}
	    	
	    	out.println("</td>");
	    	
	    	// translates
	    	out.println("<td>");
	    	if (translates != null && translates.size() > 0) {
	    		out.println(getStringWithMark(toString(translates, null), findWord, findWordRequest.searchTranslate));
	    	}
	    	
	    	out.println("</td>");
	    	
	    	// info
	    	out.println("<td>");
	    	
	    	if (info != null && info.equals("") == false) {
	    		out.println(getStringWithMark(info, findWord, findWordRequest.searchInfo));
	    	}
	    	
	    	out.println("</td>");
            
            // details link
            out.println("<td>");
            
            String link = detailsLink.replaceAll("%ID%", String.valueOf(resultItem.getDictionaryEntry().getId())).
            		replaceAll("%KANJI%", kanji != null ? kanji : "-").
            		replaceAll("%KANA%", kanaList != null && kanaList.size() > 0 ? kanaList.get(0) : "-");
            
            //out.println("<a href=\"" + link + "\">" + detailsLinkValue + "</a>");
            out.println("<button type=\"button\" class=\"btn btn-default\" onclick=\"window.location = '" + link + "'\">" + detailsLinkValue + "</button>");
            
            out.println("</td>");
            
            out.println("</tr>");
            
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
    private String getStringWithMark(String text, String findWord, boolean mark) {
    	
    	if (mark == false) {
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
