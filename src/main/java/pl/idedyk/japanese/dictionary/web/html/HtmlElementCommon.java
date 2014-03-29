package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;


public abstract class HtmlElementCommon implements IHtmlElement {

	protected List<IHtmlElement> htmlElementList = new ArrayList<IHtmlElement>();

	protected String id;
	
	protected Integer width;
	protected Integer height;
	
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
		
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
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
		
		if (id != null) {
			out.print("id=\"" + id + "\" ");
		}
		
		if (width != null) {
			out.print("width=\"" + width + "\" ");
		}

		if (height != null) {
			out.print("height=\"" + height + "\" ");
		}
		
		if (clazz != null) {
			out.print("class=\"" + clazz + "\" ");
		}

		if (style != null) {
			out.print("style=\"" + style + "\" ");
		}
		
		List<String[]> additionalTagAttributes = getAdditionalTagAttributes();
		
		if (additionalTagAttributes != null) {
			
			for (String[] currentAdditionalTagAttribute : additionalTagAttributes) {
				out.print(currentAdditionalTagAttribute[0] + "=\"" + currentAdditionalTagAttribute[1] + "\" ");
			}
		}
		
		out.println(">");
		
		if (isSupportHtmlElementList() == true) {
			
			for (IHtmlElement currentHtmlElement : htmlElementList) {
				currentHtmlElement.render(out);
			}
		}
		
		out.println("</" + getTagName() + ">");
	}
}
