package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;


public class Label extends HtmlElementCommon {
	
	private String for_;
	
	public Label() { 
		super();
	}
	
	public Label(String clazz) {
		super(clazz);
	}

	public Label(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "label";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}

	public String getFor() {
		return for_;
	}

	public void setFor(String for_) {
		this.for_ = for_;
	}

	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		
		List<String[]> additionalTagAttributes = new ArrayList<String[]>();
		
		if (for_ != null) {
			additionalTagAttributes.add(new String[] { "for", for_ });
		}

		return additionalTagAttributes;
	}	
}
