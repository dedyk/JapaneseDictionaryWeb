package pl.idedyk.japanese.dictionary.web.html;

import java.util.List;

public class Legend extends HtmlElementCommon {

	@Override
	protected String getTagName() {
		return "legend";
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
