package pl.idedyk.japanese.dictionary.web.controller.validator;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionarySearchModel;

@Component
public class KanjiDictionarySearchModelValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
				
		return KanjiDictionarySearchModel.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		KanjiDictionarySearchModel kanjiDictionarySearchModel = (KanjiDictionarySearchModel)target;
		
		boolean addStrokeCountFromError = false;
		
		String strokeCountFrom = kanjiDictionarySearchModel.getStrokeCountFrom();
		Integer strokeCountFromInteger = null;
		
		if (strokeCountFrom != null && strokeCountFrom.trim().equals("") == false) {
			strokeCountFromInteger = Utils.parseInteger(strokeCountFrom);
			
			if (strokeCountFromInteger == null) {
				addStrokeCountFromError = true;
				
			} else {
				
				if (strokeCountFromInteger.intValue() <= 0) {
					addStrokeCountFromError = true;
					
					strokeCountFromInteger = null;
				}			
			}
		}
		
		boolean addStrokeCountToError = false;
		
		String strokeCountTo = kanjiDictionarySearchModel.getStrokeCountTo();
		Integer strokeCountToInteger = null;
		
		if (strokeCountTo != null && strokeCountTo.trim().equals("") == false) {
			strokeCountToInteger = Utils.parseInteger(strokeCountTo);
			
			if (strokeCountToInteger == null) {
				addStrokeCountToError = true;
				
			} else {
				
				if (strokeCountToInteger.intValue() <= 0) {
					addStrokeCountToError = true;
					
					strokeCountToInteger = null;
				}			
			}
		}		
	
		String word = kanjiDictionarySearchModel.getWord();
		
		if ((word == null || word.trim().equals("") == true) && strokeCountFromInteger == null && strokeCountToInteger == null) {
			errors.rejectValue("word", "kanjiDictionary.validation.kanjiDictionarySearchModel.word.null");
			
		} else if (strokeCountFromInteger == null && strokeCountToInteger == null) {		
			List<String> tokenWord = Utils.tokenWord(word);
			
			if (tokenWord == null || tokenWord.size() == 0) {
				errors.rejectValue("word", "kanjiDictionary.validation.kanjiDictionarySearchModel.word.null");
			}
		}
		
		String wordPlace = kanjiDictionarySearchModel.getWordPlace();
		
		if (wordPlace == null) {
			errors.rejectValue("wordPlace", "kanjiDictionary.validation.kanjiDictionarySearchModel.wordPlace.null");
			
		} else {
			
			try {
				WordPlaceSearch.valueOf(wordPlace);
				
			} catch (IllegalArgumentException e) {
				errors.rejectValue("wordPlace", "kanjiDictionary.validation.kanjiDictionarySearchModel.wordPlace.illegalArgument");				
			}			
		}
		
		if (addStrokeCountFromError == true) {
			errors.rejectValue("strokeCountFrom", "kanjiDictionary.validation.kanjiDictionarySearchModel.strokeCountFrom.illegalArgument");
		}

		if (addStrokeCountToError == true) {
			errors.rejectValue("strokeCountTo", "kanjiDictionary.validation.kanjiDictionarySearchModel.strokeCountTo.illegalArgument");
		}
		
		if (strokeCountFromInteger != null && strokeCountToInteger != null && strokeCountFromInteger.intValue() > strokeCountToInteger.intValue()) {
			errors.rejectValue("strokeCountFrom", "kanjiDictionary.validation.kanjiDictionarySearchModel.strokeCountFrom.bigger.than.strokeCountTo");
		}
	}
}
