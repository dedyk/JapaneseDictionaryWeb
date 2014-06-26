package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class P extends HtmlElementCommon {
	
	public P() { 
		super();
	}
	
	public P(String clazz) {
		super(clazz);
	}

	public P(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "p";
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
