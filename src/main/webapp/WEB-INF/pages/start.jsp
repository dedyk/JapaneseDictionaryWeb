<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="pageTitle"> <spring:message code="start.page.title"/> </c:set>
<c:set var="pageDescription"> <spring:message code="start.page.pageDescription"/> </c:set>

<t:template pageTitle="${pageTitle}" pageDescription="${pageDescription}">

	<jsp:body>
		
		<p style="margin-bottom: 20px"><spring:message code="start.page.welcome1"/>&nbsp;<b><spring:message code="start.page.welcome2"/></b><spring:message code="start.page.welcome3"/></p>
		<p style="margin-bottom: 20px"><spring:message code="start.page.welcome4"/></p>
		<p style="margin-bottom: 20px"><spring:message code="start.page.welcome5"/></p>
		<p style="margin-bottom: 20px"><spring:message code="start.page.welcome6"/></p>
		<p><spring:message code="start.page.welcome7"/></p>
			
	</jsp:body>
</t:template>