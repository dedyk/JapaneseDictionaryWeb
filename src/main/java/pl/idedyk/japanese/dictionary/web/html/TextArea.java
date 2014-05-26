package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;

public class TextArea extends HtmlElementCommon {
	
	private String name;
	
	private Integer rows;

	public TextArea() { 
		super();
	}
	
	public TextArea(String clazz) {
		super(clazz);
	}

	public TextArea(String clazz, String style) {		
		super(clazz, style);
	}
	
	@Override
	protected String getTagName() {
		return "textarea";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return false;
	}

	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		
		List<String[]> additionalTagAttributes = new ArrayList<String[]>();
		
		if (name != null) {
			additionalTagAttributes.add(new String[] { "name", name });
		}

		if (rows != null) {
			additionalTagAttributes.add(new String[] { "rows", rows.toString() });
		}

		return additionalTagAttributes;		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}
}
