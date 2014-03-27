package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class Td implements IHtmlElement {
	
	private List<IHtmlElement> htmlElementList = new ArrayList<IHtmlElement>();

	private String clazz;
	
	private String style;
	
	public Td() { }
	
	public Td(String clazz) {
		this.clazz = clazz;
	}

	public Td(String clazz, String style) {
		this.clazz = clazz;
		this.style = style;
	}
	
	public void addHtmlElement(IHtmlElement htmlElement) {
		htmlElementList.add(htmlElement);
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
		
		out.print("<td ");
		
		if (clazz != null) {
			out.print("class=\"" + clazz + "\" ");
		}

		if (style != null) {
			out.print("style=\"" + style + "\" ");
		}
		
		out.println("/>");
		
		for (IHtmlElement currentHtmlElement : htmlElementList) {
			currentHtmlElement.render(out);
		}		
		
		out.println("</td>");		
	}
}
