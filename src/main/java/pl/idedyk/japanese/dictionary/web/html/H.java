package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class H implements IHtmlElement {
	
	private int number;

	private List<IHtmlElement> htmlElementList = new ArrayList<IHtmlElement>();
	
	private String clazz;
	
	private String style;
	
	public H(int number) { 
		this.number = number;		
	}
	
	public H(int number, String clazz) {
		this.number = number;
		
		this.clazz = clazz;
	}

	public H(int number, String clazz, String style) {
		this.number = number;
		
		this.clazz = clazz;
		this.style = style;
	}
	
	public void addHtmlElement(IHtmlElement htmlElement) {
		htmlElementList.add(htmlElement);
	}
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public void render(JspWriter out) throws IOException {
		
		out.print("<h" + number + " ");
		
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
		
		out.println("</h" + number + ">");
	}
}
