package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;

public class Tr extends HtmlElementCommon {
	
	public Tr() { 
		super();
	}
	
	public Tr(String clazz) {
		super(clazz);
	}

	public Tr(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "tr";
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
