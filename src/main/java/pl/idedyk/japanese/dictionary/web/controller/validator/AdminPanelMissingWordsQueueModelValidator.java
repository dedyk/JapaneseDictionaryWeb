package pl.idedyk.japanese.dictionary.web.controller.validator;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pl.idedyk.japanese.dictionary.web.controller.model.AdminPanelMissingWordsQueueModel;

@Component
public class AdminPanelMissingWordsQueueModelValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return AdminPanelMissingWordsQueueModel.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		
		AdminPanelMissingWordsQueueModel adminPanelMissingWordsQueueModel = (AdminPanelMissingWordsQueueModel)target;

		String size = adminPanelMissingWordsQueueModel.getSize();
		List<String> wordListAsList = adminPanelMissingWordsQueueModel.getWordListAsList();
		
		if ((size == null || size.trim().equals("") == true) && wordListAsList.size() == 0) {
			errors.rejectValue("size", "admin.panel.validation.adminPanelMissingWordsQueueModel.size.incorrect.and.wordList.empty");
			
		} else {
			
			if (size != null && size.trim().equals("") == false && wordListAsList.size() > 0) {				
				errors.rejectValue("size", "admin.panel.validation.adminPanelMissingWordsQueueModel.size.notEmpty.and.wordList.notEmpty");
								
			} else if (wordListAsList.size() == 0) {
				
				Long sizeLong = null;
				
				try {
					sizeLong = Long.parseLong(size);
					
				} catch (NumberFormatException e) {
					errors.rejectValue("size", "admin.panel.validation.adminPanelMissingWordsQueueModel.size.incorrect");
				}
				
				if (sizeLong != null) {
					
					if (sizeLong.longValue() <= 0) {
						errors.rejectValue("size", "admin.panel.validation.adminPanelMissingWordsQueueModel.size.incorrect");
					}				
				}
				
			} else { // wypelniona lista
				
				// noop
								
			}
		}
	}
}
