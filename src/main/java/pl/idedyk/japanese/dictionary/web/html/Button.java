package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;

public class Button extends HtmlElementCommon {
	
	private ButtonType buttonType;
	
	private String onClick;
	
	public Button() { 
		super();
	}
	
	public Button(String clazz) {
		super(clazz);
	}

	public Button(String clazz, String style) {		
		super(clazz, style);
	}

	public ButtonType getButtonType() {
		return buttonType;
	}

	public void setButtonType(ButtonType buttonType) {
		this.buttonType = buttonType;
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}

	@Override
	protected String getTagName() {
		return "button";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}	
	
	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		
		List<String[]> additionalTagAttributes = new ArrayList<String[]>();
		
		if (buttonType != null) {
			additionalTagAttributes.add(new String[] { "type", buttonType.getType() });
		}
		
		if (onClick != null) {
			additionalTagAttributes.add(new String[] { "onclick", onClick });
		}
		
		return additionalTagAttributes;
	}	
	
	public static enum ButtonType {
		
		BUTTON("button");
		
		private String type;
		
		ButtonType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}
}
