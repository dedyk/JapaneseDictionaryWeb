package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Li extends HtmlElementCommon {
		
	public Li() { 
		super();
	}
	
	public Li(String clazz) {
		super(clazz);
	}

	public Li(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "li";
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
