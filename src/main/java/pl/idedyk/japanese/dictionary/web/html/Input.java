package pl.idedyk.japanese.dictionary.web.html;

import java.util.ArrayList;
import java.util.List;

public class Input extends HtmlElementCommon {
	
	private String name;
	
	private String placeholder;
	
	private String required;
	
	private InputType type;
	
	public Input() { 
		super();
	}
	
	public Input(String clazz) {
		super(clazz);
	}

	public Input(String clazz, String style) {		
		super(clazz, style);
	}

	@Override
	protected String getTagName() {
		return "input";
	}

	@Override
	protected boolean isSupportHtmlElementList() {
		return true;
	}

	@Override
	protected List<String[]> getAdditionalTagAttributes() {
		
		List<String[]> additionalTagAttributes = new ArrayList<String[]>();
		
		if (name != null) {
			additionalTagAttributes.add(new String[] { "name", name });
		}
		
		if (placeholder != null) {
			additionalTagAttributes.add(new String[] { "placeholder", placeholder });
		}
		
		if (required != null) {
			additionalTagAttributes.add(new String[] { "required", required });
		}

		if (type != null) {
			additionalTagAttributes.add(new String[] { "type", type.getValue() });
		}

		return additionalTagAttributes;		
	}
	
	public static enum InputType {
		
		TEXT("text"),
		
		EMAIL("email");
		
		private String value;
		
		private InputType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
	}

	public InputType getType() {
		return type;
	}

	public void setType(InputType type) {
		this.type = type;
	}
}
