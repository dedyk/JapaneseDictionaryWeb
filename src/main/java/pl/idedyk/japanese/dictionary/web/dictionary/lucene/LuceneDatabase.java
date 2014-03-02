package pl.idedyk.japanese.dictionary.web.dictionary.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import pl.idedyk.japanese.dictionary.api.dictionary.IDatabaseConnector;
import pl.idedyk.japanese.dictionary.api.dictionary.Utils;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;

public class LuceneDatabase implements IDatabaseConnector {
	
	private String dbDir;
	
	private Directory index;
	//private SimpleAnalyzer analyzer;
	private IndexReader reader;
	private IndexSearcher searcher;
	
	public LuceneDatabase(String dbDir) {
		this.dbDir = dbDir;		
	}
	
	public void open() throws IOException {
		
		index = FSDirectory.open(new File(dbDir));
		//analyzer = new SimpleAnalyzer(Version.LUCENE_47);
		reader = DirectoryReader.open(index);
		searcher = new IndexSearcher(reader);
	}
	
	public void close() throws IOException {		
		reader.close();
		
		index.close();
	}
	
	@Override
	public FindWordResult findDictionaryEntries(FindWordRequest findWordRequest) throws DictionaryException {
		
		FindWordResult findWordResult = new FindWordResult();
		findWordResult.result = new ArrayList<FindWordResult.ResultItem>();
		
		BooleanQuery query = new BooleanQuery();
		
		// object type
		PhraseQuery phraseQuery = new PhraseQuery();
		phraseQuery.add(new Term(LuceneStatic.objectType, LuceneStatic.dictionaryEntry_objectType));
		
		query.add(phraseQuery, Occur.MUST);
		
		int maxDocumentsInQuery = 0;
		final int maxResult = 50;
		
		if (findWordRequest.wordPlaceSearch == WordPlaceSearch.ANY_PLACE) {
			
			maxDocumentsInQuery = Integer.MAX_VALUE;
			
			BooleanQuery wordBooleanQuery = new BooleanQuery();
					
			wordBooleanQuery.add(new MatchAllDocsQuery(), Occur.MUST);			
			
			createDictionaryEntryListFilter(wordBooleanQuery, findWordRequest.dictionaryEntryList);
			
			query.add(wordBooleanQuery, Occur.MUST);			
			
		} else {
			maxDocumentsInQuery = maxResult + 1;
			
			BooleanQuery wordBooleanQuery = new BooleanQuery();
			
			if (findWordRequest.searchKanji == true) {
				wordBooleanQuery.add(createQuery(findWordRequest.word, LuceneStatic.dictionaryEntry_kanji, findWordRequest.wordPlaceSearch), Occur.SHOULD);				
			}
			
			if (findWordRequest.searchKana == true) {
				wordBooleanQuery.add(createQuery(findWordRequest.word, LuceneStatic.dictionaryEntry_kanaList, findWordRequest.wordPlaceSearch), Occur.SHOULD);				
			}
			
			if (findWordRequest.searchRomaji == true) {
				wordBooleanQuery.add(createQuery(findWordRequest.word, LuceneStatic.dictionaryEntry_romajiList, findWordRequest.wordPlaceSearch), Occur.SHOULD);				
			}
			
			if (findWordRequest.searchTranslate == true) {
				wordBooleanQuery.add(createQuery(findWordRequest.word, LuceneStatic.dictionaryEntry_translatesList, findWordRequest.wordPlaceSearch), Occur.SHOULD);
				
				String wordWithoutPolishChars = Utils.removePolishChars(findWordRequest.word);
				
				wordBooleanQuery.add(createQuery(wordWithoutPolishChars, LuceneStatic.dictionaryEntry_translatesListWithoutPolishChars, 
						findWordRequest.wordPlaceSearch), Occur.SHOULD);
			}

			if (findWordRequest.searchInfo == true) {
				wordBooleanQuery.add(createQuery(findWordRequest.word, LuceneStatic.dictionaryEntry_info, findWordRequest.wordPlaceSearch), Occur.SHOULD);
				
				String wordWithoutPolishChars = Utils.removePolishChars(findWordRequest.word);
				
				wordBooleanQuery.add(createQuery(wordWithoutPolishChars, LuceneStatic.dictionaryEntry_infoWithoutPolishChars, 
						findWordRequest.wordPlaceSearch), Occur.SHOULD);
			}

			createDictionaryEntryListFilter(wordBooleanQuery, findWordRequest.dictionaryEntryList);			
			
			query.add(wordBooleanQuery, Occur.MUST);
		}
		
		try {
			ScoreDoc[] scoreDocs = searcher.search(query, null, maxDocumentsInQuery).scoreDocs;
			
			for (ScoreDoc scoreDoc : scoreDocs) {
								
				Document foundDocument = searcher.doc(scoreDoc.doc);
				
				String idString = foundDocument.get(LuceneStatic.dictionaryEntry_id);
				
				List<String> dictionaryEntryTypeList = Arrays.asList(foundDocument.getValues(LuceneStatic.dictionaryEntry_dictionaryEntryTypeList));
				List<String> attributeList = Arrays.asList(foundDocument.getValues(LuceneStatic.dictionaryEntry_attributeList));
				List<String> groupsList = Arrays.asList(foundDocument.getValues(LuceneStatic.dictionaryEntry_groupsList));
				
				String prefixKanaString = foundDocument.get(LuceneStatic.dictionaryEntry_prefixKana);
				
				String kanjiString = foundDocument.get(LuceneStatic.dictionaryEntry_kanji);
				List<String> kanaList = Arrays.asList(foundDocument.getValues(LuceneStatic.dictionaryEntry_kanaList));
								
				String prefixRomajiString = foundDocument.get(LuceneStatic.dictionaryEntry_prefixRomaji);
				
				List<String> romajiList = Arrays.asList(foundDocument.getValues(LuceneStatic.dictionaryEntry_romajiList));				
				List<String> translateList = Arrays.asList(foundDocument.getValues(LuceneStatic.dictionaryEntry_translatesList));
								
				String infoString = foundDocument.get(LuceneStatic.dictionaryEntry_info);
				
				boolean addDictionaryEntry = false;
				
				if (findWordRequest.wordPlaceSearch == WordPlaceSearch.ANY_PLACE) {
										
					if (findWordRequest.searchKanji == true) {
						
						if (kanjiString.indexOf(findWordRequest.word) != -1) {
							addDictionaryEntry = true;
						}
					}

					if (addDictionaryEntry == false && findWordRequest.searchKana == true) {
						
						for (String currentKana : kanaList) {							
							if (currentKana.indexOf(findWordRequest.word) != -1) {
								addDictionaryEntry = true;
								
								break;
							}							
						}						
					}
					
					if (addDictionaryEntry == false && findWordRequest.searchRomaji == true) {
						
						for (String currentRomaji : romajiList) {
							if (currentRomaji.indexOf(findWordRequest.word) != -1) {
								addDictionaryEntry = true;
								
								break;
							}							
						}
					}
					
					if (addDictionaryEntry == false && findWordRequest.searchTranslate == true) {
						
						String findWordWordLowerCase = findWordRequest.word.toLowerCase();
						
						for (String currentTranslate : translateList) {
							
							if (currentTranslate.toLowerCase().indexOf(findWordWordLowerCase) != -1) {
								addDictionaryEntry = true;
								
								break;
							}							
						}						
						
						List<String> translateListWithoutPolishChars = Arrays.asList(foundDocument.getValues(LuceneStatic.dictionaryEntry_translatesListWithoutPolishChars));
						
						if (translateListWithoutPolishChars != null && translateListWithoutPolishChars.size() > 0) {
							
							String findWordWordLowerCaseWithoutPolishChars = Utils.removePolishChars(findWordWordLowerCase);
							
							for (String currentTranslateListWithoutPolishChars : translateListWithoutPolishChars) {
								
								if (currentTranslateListWithoutPolishChars.toLowerCase().indexOf(findWordWordLowerCaseWithoutPolishChars) != -1) {
									addDictionaryEntry = true;
									
									break;
								}
							}
						}						
					}

					if (addDictionaryEntry == false && findWordRequest.searchInfo == true) {
						
						String findWordWordLowerCase = findWordRequest.word.toLowerCase();
						
						if (infoString != null) {
							
							if (infoString.toLowerCase().indexOf(findWordWordLowerCase) != -1) {
								addDictionaryEntry = true;
							}
							
							if (addDictionaryEntry == false) {								
								String infoStringWithoutPolishChars = foundDocument.get(LuceneStatic.dictionaryEntry_infoWithoutPolishChars);
								
								if (infoStringWithoutPolishChars != null) {
									String findWordWordLowerCaseWithoutPolishChars = Utils.removePolishChars(findWordWordLowerCase);
									
									if (infoStringWithoutPolishChars.toLowerCase().indexOf(findWordWordLowerCaseWithoutPolishChars) != -1) {
										addDictionaryEntry = true;
									}									
								}
							}
						}
					}
					
				} else {					
					addDictionaryEntry = true;
				}
				
				if (addDictionaryEntry == true && findWordResult.result.size() >= maxResult) {
					
					findWordResult.moreElemetsExists = true;
					
					addDictionaryEntry = false;
				}
				
				if (addDictionaryEntry == true) {		
					
					DictionaryEntry entry = Utils.parseDictionaryEntry(idString, dictionaryEntryTypeList, attributeList,
							groupsList, prefixKanaString, kanjiString, kanaList, prefixRomajiString, romajiList,
							translateList, infoString);
	
					findWordResult.result.add(new FindWordResult.ResultItem(entry));
				}
			}
			
		} catch (IOException e) {
			throw new DictionaryException("Błąd podczas wyszukiwania słówek: " + e);
		}
						
		return findWordResult;
	}
	
	private Query createQuery(String word, String fieldName, WordPlaceSearch wordPlaceSearch) {
		
		Query query = null;
		
		if (wordPlaceSearch == WordPlaceSearch.START_WITH) {
			query = new PrefixQuery(new Term(fieldName, word));
			
		} else if (wordPlaceSearch == WordPlaceSearch.EXACT) {
			query = new TermQuery(new Term(fieldName, word));
			
		} else {
			throw new RuntimeException();
		}
		
		return query;
	}
	
	private void createDictionaryEntryListFilter(BooleanQuery wordBooleanQuery, List<DictionaryEntryType> dictionaryEntryList) {
		
		if (dictionaryEntryList != null && dictionaryEntryList.size() > 0) {
			
			DictionaryEntryType[] allDictionaryEntryTypes = DictionaryEntryType.values();
			
			List<DictionaryEntryType> mustNotDictionaryEntryList = new ArrayList<DictionaryEntryType>();
			
			for (DictionaryEntryType currentDictionaryEntry : allDictionaryEntryTypes) {
				if (dictionaryEntryList.contains(currentDictionaryEntry) == false) {
					mustNotDictionaryEntryList.add(currentDictionaryEntry);
				}
			}				
			
			List<String> mustNotDictionaryEntryStringList = DictionaryEntryType.convertToValues(mustNotDictionaryEntryList);
			
			for (String currentMustNotDictionaryEntryStringList : mustNotDictionaryEntryStringList) {
				wordBooleanQuery.add(createQuery(currentMustNotDictionaryEntryStringList, LuceneStatic.dictionaryEntry_dictionaryEntryTypeList, WordPlaceSearch.EXACT), Occur.MUST_NOT);
			}
		}	
	}
	
	@Override
	public Set<String> findAllAvailableRadicals(String[] arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void findDictionaryEntriesInGrammaFormAndExamples(FindWordRequest findWordRequest, FindWordResult findWordResult)
			throws DictionaryException {
		
		if (findWordRequest.searchGrammaFormAndExamples == false) {
			return;
		}

		if (findWordResult.moreElemetsExists == true) {
			return;
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public FindKanjiResult findKanji(FindKanjiRequest arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<KanjiEntry> findKanjiFromRadicals(String[] arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FindKanjiResult findKanjisFromStrokeCount(int arg0, int arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<KanjiEntry> getAllKanjis(boolean arg0, boolean arg1) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getDictionaryEntriesSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DictionaryEntry getDictionaryEntryById(String arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<GroupEnum> getDictionaryEntryGroupTypes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<DictionaryEntry> getGroupDictionaryEntries(GroupEnum arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public KanjiEntry getKanjiEntry(String arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DictionaryEntry getNthDictionaryEntry(int arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}
}
