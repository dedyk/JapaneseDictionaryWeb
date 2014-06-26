package pl.idedyk.japanese.dictionary.web.html;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
	public void render(Writer out) throws IOException {
		
		PrintWriter printWriter = null;
		
		if (out instanceof PrintWriter == false) {
			printWriter = new PrintWriter(out);
			
		} else {
			printWriter = (PrintWriter)out;
		}
		
		printWriter.print("<" + getTagName() + " ");
		
		if (id != null) {
			printWriter.print("id=\"" + id + "\" ");
		}
		
		if (width != null) {
			printWriter.print("width=\"" + width + "\" ");
		}

		if (height != null) {
			printWriter.print("height=\"" + height + "\" ");
		}
		
		if (clazz != null) {
			printWriter.print("class=\"" + clazz + "\" ");
		}

		if (style != null) {
			printWriter.print("style=\"" + style + "\" ");
		}
		
		List<String[]> additionalTagAttributes = getAdditionalTagAttributes();
		
		if (additionalTagAttributes != null) {
			
			for (String[] currentAdditionalTagAttribute : additionalTagAttributes) {
				printWriter.print(currentAdditionalTagAttribute[0] + "=\"" + currentAdditionalTagAttribute[1] + "\" ");
			}
		}
		
		printWriter.println(">");
		
		if (isSupportHtmlElementList() == true) {
			
			for (IHtmlElement currentHtmlElement : htmlElementList) {
				currentHtmlElement.render(out);
			}
		}
		
		printWriter.println("</" + getTagName() + ">");
	}
}
