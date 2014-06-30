package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;


public class Img extends HtmlElementCommon {
	
	private String src;
	
	private String alt;
	
	private String widthImg;
	private String heightImg;
		
	public Img() { 
		super();
	}
	
	public Img(String clazz) {
		super(clazz);
	}

	public Img(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "img";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}	
	
	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		
		List<String[]> additionalTagAttributes = new ArrayList<String[]>();

		if (src != null) {
			additionalTagAttributes.add(new String[] { "src", src });
		}
		
		if (alt != null) {
			additionalTagAttributes.add(new String[] { "alt", alt });
		}

		if (widthImg != null) {
			additionalTagAttributes.add(new String[] { "width", widthImg });
		}

		if (heightImg != null) {
			additionalTagAttributes.add(new String[] { "height", heightImg });
		}
		
		return additionalTagAttributes;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public String getWidthImg() {
		return widthImg;
	}

	public void setWidthImg(String widthImg) {
		this.widthImg = widthImg;
	}

	public String getHeightImg() {
		return heightImg;
	}

	public void setHeightImg(String heightImg) {
		this.heightImg = heightImg;
	}
}
