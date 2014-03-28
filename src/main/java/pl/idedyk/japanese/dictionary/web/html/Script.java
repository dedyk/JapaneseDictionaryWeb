package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;

public class Script extends HtmlElementCommon {
	
	public Script() { 
		super();
	}
	
	@Override
	protected String getTagName() {
		return "script";
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
