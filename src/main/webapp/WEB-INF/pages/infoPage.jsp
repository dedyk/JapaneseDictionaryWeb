<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<t:template pageTitle="${pageTitle}" pageDescription="${pageDescription}">

	<jsp:body>
		
		<div class="col-md-12">
		    <c:out escapeXml="false" value="${pageContent}" />		
		</div>
	</jsp:body>
</t:template>