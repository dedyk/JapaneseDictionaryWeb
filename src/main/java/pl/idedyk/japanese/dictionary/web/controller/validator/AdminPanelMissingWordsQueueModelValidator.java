package pl.idedyk.japanese.dictionary.web.controller.validator;

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
		
		if (size == null) {
			errors.rejectValue("size", "admin.panel.validation.adminPanelMissingWordsQueueModel.size.incorrect");
			
		} else {
			
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
		}

	}
}
