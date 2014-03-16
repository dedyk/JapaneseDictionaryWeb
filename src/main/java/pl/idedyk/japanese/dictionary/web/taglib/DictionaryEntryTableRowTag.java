package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;

public class DictionaryEntryTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private DictionaryEntry dictionaryEntry;
	
	@Override
	public int doStartTag() throws JspException {
		
		try {
            JspWriter out = pageContext.getOut();
 
            out.println("FFFFFFF: " + dictionaryEntry.toString());            
            
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	public DictionaryEntry getDictionaryEntry() {
		return dictionaryEntry;
	}

	public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}
}
