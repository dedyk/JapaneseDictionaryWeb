package pl.idedyk.japanese.dictionary.web.dictionary.dto;

import pl.idedyk.japanese.dictionary.api.dto.RadicalInfo;

public class WebRadicalInfo extends RadicalInfo {

	private static final long serialVersionUID = 1L;
	
	private String image;

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
}
