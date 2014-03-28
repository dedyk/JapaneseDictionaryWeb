package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Td extends HtmlElementCommon {
	
	public Td() { 
		super();
	}
	
	public Td(String clazz) {
		super(clazz);
	}

	public Td(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "td";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}	
	
	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		return null;
	}	
}
