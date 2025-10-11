package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;


public class A extends HtmlElementCommon {
	
	private String href;
	private String onClick;
	private boolean escapeHref;
	private String dataToggle;
		
	public A() { 
		super();
	}
	
	public A(String clazz) {
		super(clazz);
	}

	public A(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "a";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}	
	
	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		
		List<String[]> additionalTagAttributes = new ArrayList<String[]>();

		if (href != null) {
			additionalTagAttributes.add(new String[] { "href", escapeHref == false ? href : StringEscapeUtils.escapeHtml4(href) });
		}
		
		if (onClick != null) {
			additionalTagAttributes.add(new String[] { "onclick", onClick });
		}
		
		if (dataToggle != null) {
			additionalTagAttributes.add(new String[] { "data-toggle", dataToggle });
		}
		
		return additionalTagAttributes;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	public void setEscapeHref(boolean escapeHref) {
		this.escapeHref = escapeHref;
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}

	public String getDataToggle() {
		return dataToggle;
	}

	public void setDataToggle(String dataToggle) {
		this.dataToggle = dataToggle;
	}
}
