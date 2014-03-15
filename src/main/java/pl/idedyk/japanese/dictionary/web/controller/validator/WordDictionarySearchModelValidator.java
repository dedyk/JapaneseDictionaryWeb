package pl.idedyk.japanese.dictionary.web.controller.validator;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;

@Component
public class WordDictionarySearchModelValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
				
		return WordDictionarySearchModel.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		WordDictionarySearchModel wordDictionarySearchModel = (WordDictionarySearchModel)target;
				
		String word = wordDictionarySearchModel.getWord();
		
		if (word == null) {
			errors.rejectValue("word", "wordDictionary.validation.wordDictionarySearchModel.word.null");
			
		} else {			
			List<String> tokenWord = Utils.tokenWord(word);
			
			if (tokenWord == null || tokenWord.size() == 0) {
				errors.rejectValue("word", "wordDictionary.validation.wordDictionarySearchModel.word.null");
			}
		}
		
		String wordPlace = wordDictionarySearchModel.getWordPlace();
		
		if (wordPlace == null) {
			errors.rejectValue("wordPlace", "wordDictionary.validation.wordDictionarySearchModel.wordPlace.null");
			
		} else {
			
			try {
				FindWordRequest.WordPlaceSearch.valueOf(wordPlace);
				
			} catch (IllegalArgumentException e) {
				errors.rejectValue("wordPlace", "wordDictionary.validation.wordDictionarySearchModel.wordPlace.illegalArgument");				
			}			
		}
		
		List<String> searchIn = wordDictionarySearchModel.getSearchIn();
		
		if (searchIn == null) {
			errors.rejectValue("searchIn", "wordDictionary.validation.wordDictionarySearchModel.searchIn.null");
			
		} else {
			
			if (searchIn.size() == 0) {
				errors.rejectValue("searchIn", "wordDictionary.validation.wordDictionarySearchModel.searchIn.null");
				
			} else {
				
				for (String currentSearchIn : searchIn) {
					
					if (currentSearchIn == null) {
						errors.rejectValue("searchIn", "wordDictionary.validation.wordDictionarySearchModel.searchIn.illegalArgument");
						
						break;
					}
					
					if (	Utils.isKanjiSearchIn(currentSearchIn) == false &&
							Utils.isKanaSearchIn(currentSearchIn) == false &&
							Utils.isRomajiSearchIn(currentSearchIn) == false &&
							Utils.isTranslateSearchIn(currentSearchIn) == false &&
							Utils.isInfoSearchIn(currentSearchIn) == false) {
						
						errors.rejectValue("searchIn", "wordDictionary.validation.wordDictionarySearchModel.searchIn.illegalArgument");
						
						break;
					}
				}
			}			
		}
		
		List<String> dictionaryTypeStringList = wordDictionarySearchModel.getDictionaryTypeStringList();
		
		if (dictionaryTypeStringList == null) {
			errors.rejectValue("dictionaryTypeStringList", "wordDictionary.validation.wordDictionarySearchModel.dictionaryTypeStringList.null");
			
		} else {
			
			if (dictionaryTypeStringList.size() == 0) {
				errors.rejectValue("dictionaryTypeStringList", "wordDictionary.validation.wordDictionarySearchModel.dictionaryTypeStringList.null");
				
			} else {
				
				List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();

				for (String currentDictionaryTypeString : dictionaryTypeStringList) {
					
					if (currentDictionaryTypeString == null) {
						errors.rejectValue("dictionaryTypeStringList", "wordDictionary.validation.wordDictionarySearchModel.dictionaryTypeStringList.illegalArgument");
						
						break;
					}
					
					DictionaryEntryType dictionaryEntryType = null;
					
					try {
						dictionaryEntryType = DictionaryEntryType.valueOf(currentDictionaryTypeString);
						
					} catch (IllegalArgumentException e) {
						errors.rejectValue("dictionaryTypeStringList", "wordDictionary.validation.wordDictionarySearchModel.dictionaryTypeStringList.illegalArgument");
						
						break;				
					}			

					if (addableDictionaryEntryList.contains(dictionaryEntryType) == false) {
						errors.rejectValue("dictionaryTypeStringList", "wordDictionary.validation.wordDictionarySearchModel.dictionaryTypeStringList.illegalArgument");
						
						break;						
					}					
				}				
			}			
		}
		
	}
}
