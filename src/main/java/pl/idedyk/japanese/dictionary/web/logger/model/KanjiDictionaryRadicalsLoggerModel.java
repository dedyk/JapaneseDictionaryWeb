package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.Arrays;

public class KanjiDictionaryRadicalsLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;

	private String[] radicals;
	
	private int foundElemets;
		
	public KanjiDictionaryRadicalsLoggerModel(LoggerModelCommon loggerModelCommon, String[] radicals, int foundElemets) {
		
		super(loggerModelCommon);
		
		this.radicals = radicals;
		this.foundElemets = foundElemets;
	}

	public String[] getRadicals() {
		return radicals;
	}

	public void setRadicals(String[] radicals) {
		this.radicals = radicals;
	}

	public int getFoundElemets() {
		return foundElemets;
	}

	public void setFoundElemets(int foundElemets) {
		this.foundElemets = foundElemets;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryRadicalsLoggerModel [radicals=" + Arrays.toString(radicals) + ", foundElemets="
				+ foundElemets + "]";
	}
}
