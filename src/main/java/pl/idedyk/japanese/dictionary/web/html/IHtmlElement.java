package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public interface IHtmlElement {

	public void render(JspWriter out) throws IOException;
}
