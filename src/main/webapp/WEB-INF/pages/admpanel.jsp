<%@ page contentType="text/html" isErrorPage="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="pageTitle"> <spring:message code="admin.panel" /> </c:set>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
	
		ADMIN PANEL
			
	</jsp:body>
</t:template>