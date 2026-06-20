<%@tag description="Overall Page template" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@tag import="pl.idedyk.japanese.dictionary.web.common.LinkGenerator" %>

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
	
	<a href="${LinkGenerator.createCatalogLink(pageContext.request.contextPath, requestScope.catalogPageName, requestScope.selectedSectionType, currentSectionName, 1)}" 
		onclick="saveScrollPos()" class="<c:out value='${menuCatalogSectionNameItemClass}'/>"><span><c:out value='${currentSectionName}' /></span></a>
</c:forEach>

<%--Numery stron --%>
<c:if test="${sectionNamePageNoList.size() > 1 }">
	<h4><spring:message code="wordDictionary.catalog.pageNo.title" /></h4>
	
	<c:forEach items="${sectionNamePageNoList}" var="currentSectionNamePageNo">
		
		<%-- Wybor klasy dla linku --%>
		<c:choose>
			<c:when test="${selectedSectionPageNo == currentSectionNamePageNo}">
				<c:set var="menuCatalogSectionNameItemClass">menu-catalog-section-name-item menu-catalog-section-name-item-active</c:set>
			</c:when>
			
			<c:otherwise>
			<c:set var="menuCatalogSectionNameItemClass">menu-catalog-section-name-item menu-catalog-section-name-item-inactive</c:set>
			</c:otherwise>
		
		</c:choose>
						
		<a href="${LinkGenerator.createCatalogLink(pageContext.request.contextPath, requestScope.catalogPageName, requestScope.selectedSectionType, requestScope.selectedSectionName, currentSectionNamePageNo)}"
			onclick="saveScrollPos()" class="<c:out value='${menuCatalogSectionNameItemClass}'/>"><span><c:out value='${currentSectionNamePageNo}' /></span></a>
	</c:forEach>			
</c:if>

<%--Zawartosc spisu --%>
<div id="sectionIndexContentStart"></div>

<c:forEach items="${sectionIndex.sectionEntry}" var="currentSectionEntry">
	<h5>${currentSectionEntry.romaji}</h5>

</c:forEach>

<%-- Przewijanie --%>
<script>

function saveScrollPos() {
	localStorage.setItem("scrollPos", $(window).scrollTop());
	
	return true;
};

$().ready(function() {
	var scrollPos = localStorage.getItem("scrollPos");
	
	if (scrollPos != null) {
		$('html, body').animate({
	    	scrollTop: scrollPos
		}, 0);
		
		localStorage.removeItem("scrollPos");
	}
});
</script>

