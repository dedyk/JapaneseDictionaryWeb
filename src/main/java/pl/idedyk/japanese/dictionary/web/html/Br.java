package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class Br extends HtmlElementCommon {
	
	public Br() { 
		super();
	}

	@Override
	protected String getTagName() {
		return "br";
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
