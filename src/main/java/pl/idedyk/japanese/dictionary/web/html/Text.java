package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.text.StringEscapeUtils;

public class Text implements IHtmlElement {
	
	private String text;
	private boolean addEscape;

	public Text(String text) {
		this(text, false);
	}
		
	public Text(String text, boolean addEscape) {
		super();
		this.text = text;
		this.addEscape = addEscape;
	}

	@Override
	public void render(Writer out) throws IOException {		
		
		PrintWriter printWriter = null;
		
		if (out instanceof PrintWriter == false) {
			printWriter = new PrintWriter(out);
			
		} else {
			printWriter = (PrintWriter)out;
		}
		
		String resultValue = addEscape == false ? text : StringEscapeUtils.escapeHtml4(text);
		
		printWriter.println(resultValue);
	}
}
