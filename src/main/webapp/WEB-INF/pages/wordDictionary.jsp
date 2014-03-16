<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="pageTitle"> <spring:message code="wordDictionary.page.title"/> </c:set>

<c:set var="workPlaceStartWith"> <spring:message code="wordDictionary.page.label.wordPlace.startWith"/> </c:set>
<c:set var="workPlaceAnyPlace"> <spring:message code="wordDictionary.page.label.wordPlace.anyPlace"/> </c:set>
<c:set var="workPlaceExact"> <spring:message code="wordDictionary.page.label.wordPlace.exact"/> </c:set>

<c:set var="searchInKanji"> <spring:message code="wordDictionary.page.label.searchIn.kanji"/> </c:set>
<c:set var="searchInKana"> <spring:message code="wordDictionary.page.label.searchIn.kana"/> </c:set>
<c:set var="searchInRomaji"> <spring:message code="wordDictionary.page.label.searchIn.romaji"/> </c:set>
<c:set var="searchInTranslate"> <spring:message code="wordDictionary.page.label.searchIn.translate"/> </c:set>
<c:set var="searchInInfo"> <spring:message code="wordDictionary.page.label.searchIn.info"/> </c:set>

<c:set var="search"> <spring:message code="wordDictionary.page.label.search"/> </c:set>

<c:set var="selectPickerNoneSelectedText"> <spring:message code="common.selectpicker.noneSelectedText"/> </c:set>
<c:set var="selectPickerCountSelectedText"> <spring:message code="common.selectpicker.countSelectedText"/> </c:set>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
			
		<form:form method="post" action="${pageContext.request.contextPath}/wordDictionary">
			
			<form:errors cssClass="alert alert-danger" path="*" element="div" />		
			
			<table>				
				<tr>
					<td><form:label path="word"><spring:message code="wordDictionary.page.label.searchWord"/></form:label></td>
					<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="word" path="word"/></td>				
				</tr>
				
				<tr>
					<td><form:label path="wordPlace"><spring:message code="wordDictionary.page.label.wordPlace"/></form:label></td>
					<td>
						<table>
							<form:select id="workPlaceId" path="wordPlace">
								<form:option value="START_WITH" label="${workPlaceStartWith}" />
								<form:option value="ANY_PLACE" label="${workPlaceAnyPlace}" />
								<form:option value="EXACT" label="${workPlaceExact}" />
							</form:select>
						</table>
					</td>
				</tr>
				
				<tr>
					<td><form:label path=""><spring:message code="wordDictionary.page.label.searchIn"/></form:label></td>
					<td>
						<table>
							<form:select id="searchInId" path="searchIn" multiple="true" data-selected-text-format="count">
								<form:option value="KANJI" label="${searchInKanji}" />
								<form:option value="KANA" label="${searchInKana}" />
								<form:option value="ROMAJI" label="${searchInRomaji}" />
								<form:option value="TRANSLATE" label="${searchInTranslate}" />
								<form:option value="INFO" label="${searchInInfo}" />
							</form:select>
						</table>
					</td>				
				</tr>
				
				<tr>
					<td><form:label path="" cssStyle="margin: 0px 10px 0px 0px"><spring:message code="wordDictionary.page.label.dictionaryType"/></form:label></td>
					<td>
						<table>
							<form:select id="dictionaryTypeListId" path="dictionaryTypeStringList" multiple="true" data-selected-text-format="count">
								<c:forEach items="${addableDictionaryEntryList}" var="currentAddableDictionaryEntry">
									<form:option value="${currentAddableDictionaryEntry}" label="${currentAddableDictionaryEntry.name}" />
								</c:forEach>								
							</form:select>
						</table>
					</td>				
				</tr>				
						
				<tr>
					<td></td>
					<td>
						<input class="btn btn-default btn-lg" id="searchButton" type="submit" value="${search}" />					
					</td>				
				</tr>			
			</table>
		</form:form>
		
		<!--  FIXME: Nie znaleziono wynikow -->
		<!-- Zaznaczenie ciagu -->
		<!-- Przeskakiwanie do wynikow # -->
		<!-- Pageowanie, sortowanie -->
		<c:if test="${findWordResult != null}">
			
			<hr style="margin-bottom: 10px" />
		
			<table class="table table-striped" style="font-size: 120%;">
				<caption class="text-left"><h4><spring:message code="wordDictionary.page.search.table.caption" /></h4></caption> 
				<thead>
					<tr>
						<th><spring:message code="wordDictionary.page.search.table.column.kanji" /></th>
						<th><spring:message code="wordDictionary.page.search.table.column.kana" /></th>
						<th><spring:message code="wordDictionary.page.search.table.column.romaji" /></th>
						<th><spring:message code="wordDictionary.page.search.table.column.translate" /></th>
						<th><spring:message code="wordDictionary.page.search.table.column.info" /></th>
					</tr>			
				</thead>
				<tfood>
					<c:forEach items="${findWordResult.result}" var="currentResult">
						<jdwt:findWordResultItemTableRow findWordRequest="${findWordRequest}" resultItem="${currentResult}"></jdwt:findWordResultItemTableRow>
					</c:forEach>
				</tfood>
				
			</table>
		</c:if>
		
		<script>
			$(document).ready(function() {
				
				$( "#word" ).autocomplete({
				 	source: "${pageContext.request.contextPath}/wordDictionary/autocomplete",
				 	minLength: 2
				});
	
				$( "#searchButton" ).button();

				$( "#workPlaceId").selectpicker();
				
				$( "#searchInId").selectpicker({
					noneSelectedText: '${selectPickerNoneSelectedText}', 
					countSelectedText: '${selectPickerCountSelectedText}'
				});

				$( "#dictionaryTypeListId").selectpicker({
					noneSelectedText: '${selectPickerNoneSelectedText}', 
					countSelectedText: '${selectPickerCountSelectedText}'
				});
				
			});
		</script>		
	</jsp:body>
</t:template>
