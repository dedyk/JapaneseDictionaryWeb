<%@ page contentType="text/html" isErrorPage="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>

<c:set var="pageTitle"> <spring:message code="admin.missing.words.queue.panel" /> </c:set>

<c:set var="getButton"> <spring:message code="admin.missing.words.queue.panel.get.button"/> </c:set>

<c:set var="selectPickerNoneSelectedText"> <spring:message code="common.selectpicker.noneSelectedText"/> </c:set>
<c:set var="selectPickerCountSelectedText"> <spring:message code="common.selectpicker.countSelectedText"/> </c:set>

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:admTemplate pageTitle="${pageTitle}">

	<jsp:body>
	
		<form:form commandName="command2" method="get" id="panelMissingWordsQueueId" action="${pageContext.request.contextPath}/adm/getMissingWordsQueue">
		
			<fieldset>
				<legend><spring:message code="admin.missing.words.queue.panel.form.title" /></legend>			
			</fieldset>
			
			<form:errors cssClass="alert alert-danger" path="*" element="div" />		
			
			<table>				
				<tr>
					<td><form:label path="size" cssStyle="margin: 0px 10px 10px 0px"><spring:message code="admin.missing.words.queue.panel.get.size"/></form:label></td>
					<td><form:input cssClass="form-control" cssStyle="margin: 0px 10px 10px 0px" id="sizeId" path="size"/></td>					
				</tr>
				
				<tr>
					<td><form:label path="lock" cssStyle="margin: 0px 10px 10px 0px"><spring:message code="admin.missing.words.queue.panel.get.lock"/></form:label></td>
					<td><form:checkbox cssClass="checkbox" cssStyle="margin: 0px 10px 10px 0px" id="lockId" path="lock"/>
				</tr>

				<tr>
					<td></td>
					<td>
						<input class="btn btn-default btn-lg" id="getButtonId" type="submit" value="${getButton}" />					
					</td>				
				</tr>
								
			</table>
		</form:form>	
					
	</jsp:body>
</t:admTemplate>