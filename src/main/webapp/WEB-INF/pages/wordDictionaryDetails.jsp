<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
			
		<jdwt:generateWordDictionaryDetails
			dictionaryEntry="${dictionaryEntry}"
			forceDictionaryEntryType="${forceDictionaryEntryType}"
			detailsLink="${pageContext.request.contextPath}/wordDictionaryDetails/%ID%/%KANJI%/%KANA%?forceDictionaryEntryType=%FORCEDICTIONARYENTRYTYPE%" />
				
	</jsp:body>
</t:template>
