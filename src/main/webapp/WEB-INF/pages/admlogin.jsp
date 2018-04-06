<%@ page contentType="text/html" isErrorPage="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="pageTitle"> <spring:message code="admin.login.page" /> </c:set>
<c:set var="loginButton"> <spring:message code="admin.login.page.login.button" /> </c:set>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
	
		<c:if test="${errorMessage != null}">
			<div class="alert alert-danger" id="errorMessageId"> ${errorMessage} </div>
		</c:if>
					
		<form name="loginForm" action="<c:url value='/adm/j_spring_security_check' />" method='POST'>
		
			<fieldset>
				<legend>
					${pageTitle}
				</legend>			
			</fieldset>

		  <table>

			<tr>
				<td><label style="margin: 0px 10px 0px 0px"><spring:message code="admin.login.page.user.label"/></label>
				<td><input class="form-control" style="margin: 0px 0px 10px 0px" type="text" name="username" value=""></td>
			</tr>
			
			<tr>
				<td><label><spring:message code="admin.login.page.password.label"/></label></td>
				<td><input class="form-control" style="margin: 0px 0px 10px 0px" type="password" name="password" /></td>
			</tr>
			
			<tr>			
				<td></td>
				<td>
					<input class="btn btn-default btn-lg" id="submit" type="submit" value="${loginButton}" />					
				</td>	
			</tr>
		  </table>
 
		</form>
			
	</jsp:body>
</t:template>