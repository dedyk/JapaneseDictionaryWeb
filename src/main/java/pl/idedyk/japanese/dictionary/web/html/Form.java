package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;

public class Form extends HtmlElementCommon {
	
	public Form() { 
		super();
	}
	
	public Form(String clazz) {
		super(clazz);
	}

	@Override
	protected String getTagName() {
		return "form";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}

	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
}
