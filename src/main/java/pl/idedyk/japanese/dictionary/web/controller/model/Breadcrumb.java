package pl.idedyk.japanese.dictionary.web.controller.model;

import java.io.Serializable;

public class Breadcrumb implements Serializable {

	private static final long serialVersionUID = 1L;

	private String text;
	
	private String url;
	
	public Breadcrumb() {
	}

	public Breadcrumb(String text, String url) {
		this.text = text;
		this.url = url;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
