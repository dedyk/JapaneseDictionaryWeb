package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;


public class A extends HtmlElementCommon {
	
	private String href;
	
	private String onClick;
		
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
			additionalTagAttributes.add(new String[] { "href", href });
		}
		
		if (onClick != null) {
			additionalTagAttributes.add(new String[] { "onclick", onClick });
		}
		
		return additionalTagAttributes;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}
}
