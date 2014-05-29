package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.util.ArrayList;
import java.util.List;

import pl.idedyk.japanese.dictionary.web.html.IHtmlElement;

public class Menu {

	private String id;

	private String title;
	
	private String customOnClick;
		
	private List<Menu> childMenu = new ArrayList<Menu>();
	
	private IHtmlElement beforeHtmlElement;
	
	public Menu(String id, String title) {
		this.id = id;
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Menu> getChildMenu() {
		return childMenu;
	}

	public void setChildMenu(List<Menu> childMenu) {
		this.childMenu = childMenu;
	}

	public String getCustomOnClick() {
		return customOnClick;
	}

	public void setCustomOnClick(String customOnClick) {
		this.customOnClick = customOnClick;
	}

	public IHtmlElement getBeforeHtmlElement() {
		return beforeHtmlElement;
	}

	public void setBeforeHtmlElement(IHtmlElement beforeHtmlElement) {
		this.beforeHtmlElement = beforeHtmlElement;
	}
}
