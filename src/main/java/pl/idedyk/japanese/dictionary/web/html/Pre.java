package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;

public class Pre extends HtmlElementCommon {
	
	public Pre() { 
		super();
	}
	
	@Override
	protected String getTagName() {
		return "pre";
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
