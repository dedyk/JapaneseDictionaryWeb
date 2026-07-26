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
<c:set var="reloadButtonValue"> <spring:message code="captcha.page.label.reloadButtonValue"/> </c:set>

<spring:eval var="useExternalStaticFiles" expression="@applicationProperties.getProperty('use.external.static.files')" />

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:template pageTitle="${pageTitle}" pageDescription="${pageDescription}" disableNavmenu="true">

	<jsp:body>
			
		<form:form method="get" action="${pageContext.request.contextPath}/captcha/verify">
		
			<fieldset>
				<legend><spring:message code="captcha.page.title2" /></legend>			
			</fieldset>
			
			<form:errors cssClass="alert alert-danger" path="*" element="div" />
						
			<table>
				<tr>
					<td></td>
					<td><img style="margin: 0px 0px 15px 0px" src="<c:url value='${command.captchaBase64Image}'/>"  /> </td>				
				</tr>
							
				<tr>
					<td><form:label path="userCaptcha" cssStyle="margin: 0px 10px 10px 0px"><spring:message code="captcha.page.label.userCaptcha"/></form:label></td>
					<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="userCaptcha" path="userCaptcha"/></td>				
				</tr>
										
				<tr>
					<td></td>
					<td>
						<input class="btn btn-default btn-lg" id="verifyButton" type="submit" value="${verifyButtonValue}" />
						<input class="btn btn-default btn-lg" id="reloadButton" type="button" value="${reloadButtonValue}" onclick="window.location.href=window.location.href;" style="margin: 0px 0px 0px 10px"/>					
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
