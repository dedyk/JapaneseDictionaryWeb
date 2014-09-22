package pl.idedyk.japanese.dictionary.web.controller.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pl.idedyk.japanese.dictionary.web.controller.model.AdminPanelModel;

@Component
public class AdminPanelModelValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return AdminPanelModel.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		AdminPanelModel adminPanelModel = (AdminPanelModel)target;
		
		String pageNo = adminPanelModel.getPageNo();
		
		if (pageNo == null) {
			errors.rejectValue("pageNo", "admin.panel.validation.adminPanelModel.pageNo.null");
			
		} else {
			
			Long pageNoLong = null;
			
			try {
				pageNoLong = Long.parseLong(pageNo);
				
			} catch (NumberFormatException e) {
				errors.rejectValue("pageNo", "admin.panel.validation.adminPanelModel.pageNo.null");
			}
			
			if (pageNoLong != null) {
				
				if (pageNoLong.longValue() <= 0) {
					errors.rejectValue("pageNo", "admin.panel.validation.adminPanelModel.pageNo.null");
				}				
			}
		}		
	}

}
