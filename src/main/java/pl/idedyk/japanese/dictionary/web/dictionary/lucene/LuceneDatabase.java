package pl.idedyk.japanese.dictionary.web.dictionary.lucene;

import java.util.List;
import java.util.Set;

import pl.idedyk.japanese.dictionary.api.dictionary.IDatabaseConnector;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;

public class LuceneDatabase implements IDatabaseConnector {

	@Override
	public Set<String> findAllAvailableRadicals(String[] arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FindWordResult findDictionaryEntries(FindWordRequest arg0) throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void findDictionaryEntriesInGrammaFormAndExamples(FindWordRequest arg0, FindWordResult arg1)
			throws DictionaryException {
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
