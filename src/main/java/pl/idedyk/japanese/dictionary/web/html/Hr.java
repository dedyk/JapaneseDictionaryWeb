package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Hr extends HtmlElementCommon {
	
	public Hr() { 
		super();
	}

	@Override
	protected String getTagName() {
		return "hr";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return false;
	}	
	
	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		return null;
	}	
}
