package pl.idedyk.japanese.dictionary.web.mysql.model;

public class WordKanjiSearchNoFoundStat {

	private String word;
	
	private long stat;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public long getStat() {
		return stat;
	}

	public void setStat(long stat) {
		this.stat = stat;
	}
}
