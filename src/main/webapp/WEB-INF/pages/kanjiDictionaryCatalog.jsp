<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="pageTitle"> <spring:message code="kanjiDictionary.catalog.page.title"/> </c:set>
<c:set var="pageDescription"> <spring:message code="kanjiDictionary.catalog.page.pageDescription"/> </c:set>

<c:set var="previousPageButton"> <spring:message code="kanjiDictionary.catalog.page.previousPage"/> </c:set>
<c:set var="nextPageButton"> <spring:message code="kanjiDictionary.catalog.page.nextPage"/> </c:set>

<spring:eval var="useExternalStaticFiles" expression="@applicationProperties.getProperty('use.external.static.files')" />

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:template pageTitle="${pageTitle}" pageDescription="${pageDescription}">

	<jsp:body>

         <c:if test="${findKanjiResult != null}">

			<hr id="findKanjiResultHrId" style="margin-top: 10px; margin-bottom: 10px" />
		
			<p class="text-left"><h4><spring:message code="kanjiDictionary.catalog.page.table.caption" /></h4></p>
		
			<table id="kanjiDictionaryResult" class="table table-striped" style="font-size: 120%;">
				<thead>
					<tr>
						<th><spring:message code="kanjiDictionary.page.search.table.column.kanji" /></th>
						<th><spring:message code="kanjiDictionary.page.search.table.column.radicals" /></th>
						<th><spring:message code="kanjiDictionary.page.search.table.column.strokeCount" /></th>
						<th><spring:message code="kanjiDictionary.page.search.table.column.translate" /></th>
						<th><spring:message code="kanjiDictionary.page.search.table.column.info" /></th>
						<th></th>
					</tr>
				</thead>
				<tfood>
					<c:forEach items="${findKanjiResult.result}" var="currentResult">
						<jdwt:findKanjiResultItemTableRow
							resultItem="${currentResult}" />
					</c:forEach>
				</tfood>					
			</table>	

			<div class="col-md-12" style="text-align: right">
			
				<c:if test="${pageNo != 0}">
					<a href="/kanjiDictionaryCatalog?page=${pageNo - 1}" class="btn btn-default">${previousPageButton}</a>
				</c:if>
			
				<c:if test="${findKanjiResult.moreElemetsExists == true}">
					<a href="/kanjiDictionaryCatalog?page=${pageNo + 1}" class="btn btn-default">${nextPageButton}</a>
				</c:if>			
			
			</div>
			
			<script>
				$('#kanjiDictionaryResult').dataTable({
					language: {
						url: '${staticFilePrefix}/js/datatables/polish.json'
					},
					"bStateSave": true,
					"aaSorting": [],
					"sDom": "<'row'<'col-xs-12'f><'col-xs-6'l><'col-xs-6'p>r>t<'row'<'col-xs-6'i><'col-xs-6'p>>",
					"bLengthChange": false,
					"bPaginate": false
				});
			</script>	
		</c:if>

	</jsp:body>
	
</t:template>