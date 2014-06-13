<%@ page contentType="text/html" isErrorPage="true" pageEncoding="UTF-8"%>
<%@page import="java.io.PrintWriter"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>

<c:set var="pageTitle"> <spring:message code="error.page.title"/> </c:set>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
		
		<h2><spring:message code="error.page.body.title"/></h2>
		<hr/>
		<h4><spring:message code="error.page.body.text"/></h4>

		<jdwt:handleException />
			
	</jsp:body>
</t:template>