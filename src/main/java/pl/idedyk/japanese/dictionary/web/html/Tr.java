package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class Tr implements IHtmlElement {
	
	private List<Td> tdList = new ArrayList<Td>();

	private String clazz;
	
	private String style;
	
	public Tr() { }
	
	public Tr(String clazz) {
		this.clazz = clazz;
	}

	public Tr(String clazz, String style) {
		this.clazz = clazz;
		this.style = style;
	}
	
	public void addTd(Td td) {
		tdList.add(td);
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	@Override
	public void render(JspWriter out) throws IOException {
		
		out.print("<tr ");
		
		if (clazz != null) {
			out.print("class=\"" + clazz + "\" ");
		}

		if (style != null) {
			out.print("style=\"" + style + "\" ");
		}
		
		out.println("/>");
		
		for (Td currentTd : tdList) {
			currentTd.render(out);
		}		
		
		out.println("</tr>");		
	}
}
