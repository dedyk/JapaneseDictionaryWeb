package pl.idedyk.japanese.dictionary.web.dictionary.lucene;


public class LuceneDBGenerator {

	public static void main(String[] args) throws Exception {

		// parametry pliku
		final String dictionaryFilePath = "db/word.csv";
		final String kanjiFilePath = "db/kanji.csv";
		final String radicalFilePath = "db/radical.csv";
				
		final String dbOutDirFile = "db-lucene";
		
		new pl.idedyk.japanese.dictionary.lucene.LuceneDBGenerator().generate(
				dictionaryFilePath, kanjiFilePath, radicalFilePath, true, dbOutDirFile);		
	}		
}