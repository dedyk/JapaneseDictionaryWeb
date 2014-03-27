package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;

public class Table implements IHtmlElement {
	
	private List<Tr> trList = new ArrayList<Tr>();

	private String clazz;
	
	private String style;
	
	public Table() { }
	
	public Table(String clazz) {
		this.clazz = clazz;
	}

	public Table(String clazz, String style) {
		this.clazz = clazz;
		this.style = style;
	}
	
	public void addTr(Tr tr) {
		trList.add(tr);
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
		
		out.print("<table ");
		
		if (clazz != null) {
			out.print("class=\"" + clazz + "\" ");
		}

		if (style != null) {
			out.print("style=\"" + style + "\" ");
		}
		
		out.println("/>");
		
		for (Tr currentTr : trList) {
			currentTr.render(out);
		}		
		
		out.println("</table>");		
	}
}
