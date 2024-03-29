package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.List;

import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;

public class KanjiDictionaryDetectLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private String strokes;
	
	private List<KanjiRecognizerResultItem> detectKanjiResult;

	public KanjiDictionaryDetectLoggerModel(LoggerModelCommon loggerModelCommon, String strokes, List<KanjiRecognizerResultItem> detectKanjiResult) {
		
		super(loggerModelCommon);
		
		this.strokes = strokes;
		this.detectKanjiResult = detectKanjiResult;
	}

	public String getStrokes() {
		return strokes;
	}

	public void setStrokes(String strokes) {
		this.strokes = strokes;
	}

	public List<KanjiRecognizerResultItem> getDetectKanjiResult() {
		return detectKanjiResult;
	}

	public void setDetectKanjiResult(List<KanjiRecognizerResultItem> detectKanjiResult) {
		this.detectKanjiResult = detectKanjiResult;
	}

	@Override
	public String toString() {
		return "KanjiDictionaryDetectLoggerModel [strokes=" + strokes + ", detectKanjiResult=" + detectKanjiResult
				+ "]";
	}
}
