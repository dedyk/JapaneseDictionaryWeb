package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;


public class Td extends HtmlElementCommon {
	
	private String colspan;
	
	private String rowspan;
	
	public Td() { 
		super();
	}
	
	public Td(String clazz) {
		super(clazz);
	}

	public Td(String clazz, String style) {		
		super(clazz, style);
	}

	public String getColspan() {
		return colspan;
	}

	public void setColspan(String colspan) {
		this.colspan = colspan;
	}

	public String getRowspan() {
		return rowspan;
	}

	public void setRowspan(String rowspan) {
		this.rowspan = rowspan;
	}

	@Override
	protected String getTagName() {
		return "td";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}	
	
	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		
		List<String[]> additionalTagAttributes = new ArrayList<String[]>();

		if (colspan != null) {
			additionalTagAttributes.add(new String[] { "colspan", colspan });
		}
		
		if (rowspan != null) {
			additionalTagAttributes.add(new String[] { "rowspan", rowspan });
		}

		return additionalTagAttributes;
	}	
}
