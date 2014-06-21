package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class B extends HtmlElementCommon {
	
	public B() { 
		super();
	}
	
	public B(String clazz) {
		super(clazz);
	}

	public B(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "b";
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
