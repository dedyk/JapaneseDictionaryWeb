package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.io.Writer;

public interface IHtmlElement {

	public void render(Writer out) throws IOException;
}
