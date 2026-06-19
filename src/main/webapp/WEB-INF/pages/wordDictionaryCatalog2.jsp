<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="firstPageButton"> <spring:message code="wordDictionary.catalog.page.firstPage"/> </c:set>
<c:set var="previousPageButton"> <spring:message code="wordDictionary.catalog.page.previousPage"/> </c:set>
<c:set var="nextPageButton"> <spring:message code="wordDictionary.catalog.page.nextPage"/> </c:set>
<c:set var="lastPageButton"> <spring:message code="wordDictionary.catalog.page.lastPage"/> </c:set>

<spring:eval var="useExternalStaticFiles" expression="@applicationProperties.getProperty('use.external.static.files')" />

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:template pageTitle="${pageTitle}" pageDescription="${pageDescription}">

	<jsp:body>
	
		<h3>${pageTitle}</h3>
		
		<%-- Sekcja z nazwami literek --%>
		<c:forEach items="${sectionNamesList}" var="currentSectionName">
			
			<%-- Wybor klasy dla linku --%>
			<c:choose>
				<c:when test="${selectedSectionName == currentSectionName}">
					<c:set var="menuCatalogSectionNameItemClass">menu-catalog-section-name-item menu-catalog-section-name-item-active</c:set>
				</c:when>
				
				<c:otherwise>
				<c:set var="menuCatalogSectionNameItemClass">menu-catalog-section-name-item menu-catalog-section-name-item-inactive</c:set>
				</c:otherwise>
			
			</c:choose>
						
			<a href="link" class="<c:out value='${menuCatalogSectionNameItemClass}'/>"><span><c:out value='${currentSectionName}' /></span></a>
		</c:forEach>					

		
		
	</jsp:body>
</t:template>
