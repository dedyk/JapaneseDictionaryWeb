package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Canvas extends HtmlElementCommon {
	
	public Canvas() { 
		super();
	}
	
	public Canvas(String clazz) {
		super(clazz);
	}

	public Canvas(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "canvas";
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
