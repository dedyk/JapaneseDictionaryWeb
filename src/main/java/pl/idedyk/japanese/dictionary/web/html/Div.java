package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Div extends HtmlElementCommon {
	
	public Div() { 
		super();
	}
	
	public Div(String clazz) {
		super(clazz);
	}

	public Div(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "div";
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
