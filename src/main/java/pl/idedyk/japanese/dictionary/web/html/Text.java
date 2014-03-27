package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public class Text implements IHtmlElement {
	
	private String text;

	public Text(String text) {
		super();
		this.text = text;
	}

	@Override
	public void render(JspWriter out) throws IOException {		
		out.println(text);		
	}
}
