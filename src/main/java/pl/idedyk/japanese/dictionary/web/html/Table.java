package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;

public class Table extends HtmlElementCommon {
	
	public Table() { 
		super();
	}
	
	public Table(String clazz) {
		super(clazz);
	}

	public Table(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "table";
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
