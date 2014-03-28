package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;


public class H extends HtmlElementCommon {
	
	private int number;
	
	public H(int number) { 
		super();
		
		this.number = number;		
	}
	
	public H(int number, String clazz) {
		super(clazz);
		
		this.number = number;
	}

	public H(int number, String clazz, String style) {
		super(clazz, style);
		
		this.number = number;
	}
		
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	protected String getTagName() {
		return "h" + number;
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
