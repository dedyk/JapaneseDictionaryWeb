package pl.idedyk.japanese.dictionary.web.mysql.model;

public class GenericTextStat {

	private String text;
	
	private long stat;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getStat() {
		return stat;
	}

	public void setStat(long stat) {
		this.stat = stat;
	}
}
