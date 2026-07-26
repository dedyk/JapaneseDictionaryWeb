<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@page import="pl.idedyk.japanese.dictionary.web.common.LinkGenerator" %>

<c:set var="pageTitle"> <spring:message code="captcha.page.title"/> </c:set>
<c:set var="pageDescription"> <spring:message code="captcha.page.pageDescription"/> </c:set>

<c:set var="verifyButtonValue"> <spring:message code="captcha.page.label.verifyButtonValue"/> </c:set>

<spring:eval var="useExternalStaticFiles" expression="@applicationProperties.getProperty('use.external.static.files')" />

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:template pageTitle="${pageTitle}" pageDescription="${pageDescription}" disableNavmenu="true">

	<jsp:body>
			
		<form:form method="get" action="${pageContext.request.contextPath}/catpcha/verify">
		
			<fieldset>
				<legend><spring:message code="captcha.page.title2" /></legend>			
			</fieldset>
			
			<form:errors cssClass="alert alert-danger" path="*" element="div" />
						
			<table>				
				<tr>
					<td><form:label path="userCaptcha" cssStyle="margin: 0px 10px 10px 0px"><spring:message code="captcha.page.label.userCaptcha"/></form:label></td>
					<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="userCaptcha" path="userCaptcha"/></td>				
				</tr>
										
				<tr>
					<td></td>
					<td>
						<input class="btn btn-default btn-lg" id="verifyButton" type="submit" value="${verifyButtonValue}" />					
					</td>				
				</tr>			
			</table>
		</form:form>
						
		<script>
			$(document).ready(function() {
					
				$( "#verifyButton" ).button();
				$( "#userCaptcha").focus();
			});
			
		</script>		
	</jsp:body>
</t:template>
