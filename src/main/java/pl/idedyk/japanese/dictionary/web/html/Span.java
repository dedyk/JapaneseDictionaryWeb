package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Span extends HtmlElementCommon {
	
	public Span() { 
		super();
	}
	
	public Span(String clazz) {
		super(clazz);
	}

	public Span(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "span";
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
