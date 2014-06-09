<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="pageTitle"> <spring:message code="start.page.title"/> </c:set>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
		
		<spring:message code="start.page.welcome1"/>&nbsp;<b><spring:message code="start.page.welcome2"/></b><spring:message code="start.page.welcome3"/>
			
	</jsp:body>
</t:template>