package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;


public abstract class HtmlElementCommon implements IHtmlElement {

	protected List<IHtmlElement> htmlElementList = new ArrayList<IHtmlElement>();

	protected String clazz;
	
	protected String style;
	
	public HtmlElementCommon() { }
	
	public HtmlElementCommon(String clazz) {
		this.clazz = clazz;
	}

	public HtmlElementCommon(String clazz, String style) {
		this.clazz = clazz;
		this.style = style;
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
		
	public void addHtmlElement(IHtmlElement htmlElement) {
		
		if (isSupportHtmlElementList() == false) {
			throw new RuntimeException();
		}
		
		htmlElementList.add(htmlElement);
	}

	
	protected abstract String getTagName();
	
	protected abstract boolean isSupportHtmlElementList();
	
	protected abstract List<String[]> getAdditionalTagAttributes();
	
	@Override
	public void render(JspWriter out) throws IOException {
		
		out.print("<" + getTagName() + " ");
		
		if (clazz != null) {
			out.print("class=\"" + clazz + "\" ");
		}

		if (style != null) {
			out.print("style=\"" + style + "\" ");
		}
		
		List<String[]> additionalTagAttributes = getAdditionalTagAttributes();
		
		if (additionalTagAttributes != null) {
			
			for (String[] currentAdditionalTagAttribute : additionalTagAttributes) {
				out.println(currentAdditionalTagAttribute[0] + "=\"" + currentAdditionalTagAttribute[1] + "\"");
			}
		}
		
		out.println("/>");
		
		if (isSupportHtmlElementList() == true) {
			
			for (IHtmlElement currentHtmlElement : htmlElementList) {
				currentHtmlElement.render(out);
			}
		}
		
		out.println("</" + getTagName() + ">");
	}
}
