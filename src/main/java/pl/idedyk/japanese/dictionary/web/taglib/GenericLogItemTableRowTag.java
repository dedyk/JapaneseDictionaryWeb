package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;

public class GenericLogItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private GenericLog genericLog;
	
	private MessageSource messageSource;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		
		try {
            JspWriter out = pageContext.getOut();
            
            Tr tr = new Tr();            
            
            // id
            addColumn(tr, String.valueOf(genericLog.getId()));
                        
            // timestamp
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            addColumn(tr, String.valueOf(simpleDateFormat.format(genericLog.getTimestamp())));

            // operation
            addColumn(tr, genericLog.getOperation().toString());

            
            // remote ip
            addColumn(tr, genericLog.getRemoteIp());
            
            // remote host
            addColumn(tr, genericLog.getRemoteHost());
            
            // pobranie danych
            //
            /*
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
            */
            
            tr.render(out);
            
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	private void addColumn(Tr tr, String value) {
		
		if (value == null) {
			value = "";
		}
		
        Td td = new Td();
        tr.addHtmlElement(td);
        
        td.addHtmlElement(new Text(value));		
	}

	public GenericLog getGenericLog() {
		return genericLog;
	}

	public void setGenericLog(GenericLog genericLog) {
		this.genericLog = genericLog;
	}
}
