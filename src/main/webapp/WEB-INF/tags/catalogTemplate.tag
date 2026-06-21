<%@tag description="Overall Page template" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@tag import="pl.idedyk.japanese.dictionary.web.common.LinkGenerator" %>
<%@tag import="pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.SectionEntryIndexEntry" %>

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
	<h4 style="margin-top: 15px"><spring:message code="wordDictionary.catalog.pageNo.title" /></h4>
	
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
<div class="menu-catalog-index-columns">
	<c:forEach items="${sectionIndex.sectionEntry}" var="currentSectionEntry">	
	    <div class="menu-catalog-index-letter-section">
	        <div class="menu-catalog-index-entry-group">
            	<c:choose>
            		<c:when test="${currentSectionEntry.polishWord != null}">
            			<div class="menu-catalog-index-main-word"><c:out value="${currentSectionEntry.polishWord}" /></div>
            		</c:when>
            		<c:otherwise>
            			<div class="menu-catalog-index-main-word"><c:out value="${currentSectionEntry.romaji}" /></div>
            		</c:otherwise>
            	</c:choose>
            	
            	<c:forEach items="${currentSectionEntry.entries}" var="currentSectionEntryEntry">	
            		<div class="menu-catalog-index-sub-entry">
            			<%
            				StringBuffer subEntryEntryValue = new StringBuffer();
            			
            				SectionEntryIndexEntry sectionEntryIndexEntryi = (SectionEntryIndexEntry) jspContext.getAttribute("currentSectionEntryEntry");
            				
            				String kanji = sectionEntryIndexEntryi.getKanji();
            				String kana = sectionEntryIndexEntryi.getKana();
            				String romaji = sectionEntryIndexEntryi.getRomaji();
            				
            				if (kanji != null) {
            					subEntryEntryValue.append(kanji);
            				}
            				
            				if (kana != null) {
            					if (subEntryEntryValue.length() > 0) {
            						subEntryEntryValue.append("  ");
            					}
            					
            					subEntryEntryValue.append(kana);
            				}

            				if (romaji != null) {
            					if (subEntryEntryValue.length() > 0) {
            						subEntryEntryValue.append("  ");
            					}
            					
            					subEntryEntryValue.append(romaji);
            				}			            			
            			%>           			            			
            			<span class="menu-catalog-index-sub-entry-entry"><p style="white-space: pre-wrap; margin: 0 0 0px"><a href="${currentSectionEntryEntry.url}"><%= subEntryEntryValue.toString() %></a> </p></span>
            		</div>
            	</c:forEach>
	    </div>
	   </div>
	
	</c:forEach>
</div>
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

