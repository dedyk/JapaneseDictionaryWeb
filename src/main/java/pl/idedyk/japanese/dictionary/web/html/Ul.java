package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Ul extends HtmlElementCommon {
		
	public Ul() { 
		super();
	}
	
	public Ul(String clazz) {
		super(clazz);
	}

	public Ul(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "ul";
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
