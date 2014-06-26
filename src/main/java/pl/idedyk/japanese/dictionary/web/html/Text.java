package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class Text implements IHtmlElement {
	
	private String text;

	public Text(String text) {
		super();
		this.text = text;
	}

	@Override
	public void render(Writer out) throws IOException {		
		
		PrintWriter printWriter = null;
		
		if (out instanceof PrintWriter == false) {
			printWriter = new PrintWriter(out);
			
		} else {
			printWriter = (PrintWriter)out;
		}
		
		printWriter.println(text);		
	}
}
