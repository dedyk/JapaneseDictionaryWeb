package pl.idedyk.japanese.dictionary.web.dictionary;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;

@Component
public class WordDictionarySearchModelValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
				
		return WordDictionarySearchModel.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		
	}
}
