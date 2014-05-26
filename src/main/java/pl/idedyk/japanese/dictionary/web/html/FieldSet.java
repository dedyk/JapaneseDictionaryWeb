package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;

public class FieldSet extends HtmlElementCommon {

	@Override
	protected String getTagName() {
		return "fieldset";
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
