package pl.idedyk.japanese.dictionary.web.controller.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;

public class AdminPanelModel implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String pageNo = "1";
	
	private List<String> genericLogOperationStringList;

	public String getPageNo() {
		return pageNo;
	}

	public void setPageNo(String pageNo) {
		this.pageNo = pageNo;
	}

	public List<String> getGenericLogOperationStringList() {
		return genericLogOperationStringList;
	}

	public void setGenericLogOperationStringList(List<String> genericLogOperationStringList) {
		this.genericLogOperationStringList = genericLogOperationStringList;
	}
	
	public void addGenericLogOperationEnum(GenericLogOperationEnum genericLogOperationEnum) {
		
		if (genericLogOperationStringList == null) {
			genericLogOperationStringList = new ArrayList<String>();
		}
		
		genericLogOperationStringList.add(genericLogOperationEnum.toString());
	}
}
