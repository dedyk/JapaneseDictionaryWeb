<%@ page contentType="text/html" isErrorPage="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>

<c:set var="pageTitle"> <spring:message code="admin.panel" /> </c:set>

<c:set var="search"> <spring:message code="admin.panel.search.button"/> </c:set>

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:admTemplate pageTitle="${pageTitle}">

	<jsp:body>

		<form:form method="get" action="${pageContext.request.contextPath}/adm/panelSearch">
		
			<fieldset>
				<legend><spring:message code="admin.panel.search.title" /></legend>			
			</fieldset>
			
			<form:errors cssClass="alert alert-danger" path="*" element="div" />		
			
			<table>				
				<tr>
					<td><form:label path="pageNo" cssStyle="margin: 0px 10px 10px 0px"><spring:message code="admin.panel.search.pageNo"/></form:label></td>
					<td><form:input cssClass="form-control" cssStyle="margin: 0px 10px 10px 0px" id="word" path="pageNo"/></td>
					<td><label style="margin: 0px 0px 10px 10px"><spring:message code="admin.panel.search.pageNoFrom" arguments="${maxPageSize}"/></label>					
				</tr>

				<tr>
					<td></td>
					<td>
						<input class="btn btn-default btn-lg" id="searchButton" type="submit" value="${search}" />					
					</td>				
				</tr>
				
<%-- 				<tr>
					<td><form:label path="wordPlace"><spring:message code="wordDictionary.page.label.wordPlace"/></form:label></td>
					<td>
						<form:select id="wordPlaceId" path="wordPlace">
							<form:option value="START_WITH" label="${wordPlaceStartWith}" />
							<form:option value="ANY_PLACE" label="${wordPlaceAnyPlace}" />
							<form:option value="EXACT" label="${wordPlaceExact}" />
						</form:select>						
					</td>
				</tr>
				
				<tr>
					<td><form:label path=""><spring:message code="wordDictionary.page.label.searchIn"/></form:label></td>
					<td>
						<form:select id="searchInId" path="searchIn" multiple="true" data-selected-text-format="count">
							<form:option value="COMMON_WORDS" label="${searchInOnlyCommonWords}" />
							<form:option value="KANJI" label="${searchInKanji}" />
							<form:option value="KANA" label="${searchInKana}" />
							<form:option value="ROMAJI" label="${searchInRomaji}" />
							<form:option value="TRANSLATE" label="${searchInTranslate}" />
							<form:option value="INFO" label="${searchInInfo}" />
						</form:select>
					</td>				
				</tr>
								
				<tr>
					<td><form:label path="" cssStyle="margin: 0px 10px 0px 0px"><spring:message code="wordDictionary.page.label.dictionaryType"/></form:label></td>
					<td>
						<form:select id="dictionaryTypeListId" path="dictionaryTypeStringList" multiple="true" data-selected-text-format="count">
							<c:forEach items="${addableDictionaryEntryList}" var="currentAddableDictionaryEntry">
								<form:option value="${currentAddableDictionaryEntry}" label="${currentAddableDictionaryEntry.name}" />
							</c:forEach>								
						</form:select>
					</td>				
				</tr>				
						
				--%>			
			</table>
		</form:form>	
	
		<c:if test="${genericLogList != null}">
			
			<hr id="genericLogListHrId" style="margin-bottom: 10px" />
		
			<p class="text-left"><h4><spring:message code="admin.panel.genericlog.title" /></h4></p>
		
			<table id="genericLogListResult" class="table table-striped" style="font-size: 120%;">
				<thead>
					<tr>
						<th><spring:message code="admin.panel.genericlog.column.id" /></th>
						<th><spring:message code="admin.panel.genericlog.column.timestamp" /></th>
						<th><spring:message code="admin.panel.genericlog.column.operation" /></th>
						<th><spring:message code="admin.panel.genericlog.column.requestUrl" /></th>
						<th><spring:message code="admin.panel.genericlog.column.remoteIp" /></th>
						<th><spring:message code="admin.panel.genericlog.column.remoteHost" /></th>						
						
<%--						<th></th> --%>
					</tr>
				</thead>
				<tfood>
					<c:forEach items="${genericLogList}" var="currentResult">
 						<jdwt:genericLogItemTableRowTag
							genericLog="${currentResult}" />
					</c:forEach>
				</tfood>
				
			</table>
			
			<script>
				$(document).ready(function() {
					$('#genericLogListResult').dataTable({
						language: {
							url: '${staticFilePrefix}/js/datatables/polish.json'
						},
						"bStateSave": true,
						"aaSorting": [],
						"sDom": "<'row'<'col-xs-12'f><'col-xs-6'l><'col-xs-6'p>r>t<'row'<'col-xs-6'i><'col-xs-6'p>>",
						"bLengthChange": false,
						"bPaginate": false
					});
				});
			</script>
		</c:if>
		
		<script>
			$(document).ready(function() {
				
<%--				$( "#word" ).autocomplete({
				 	source: "${pageContext.request.contextPath}/wordDictionary/autocomplete",
				 	minLength: 2
				});
	
				$( "#searchButton" ).button();

				$( "#wordPlaceId").selectpicker();
				
				$( "#searchInId").selectpicker({
					noneSelectedText: '${selectPickerNoneSelectedText}', 
					countSelectedText: '${selectPickerCountSelectedText}'
				});

				$( "#dictionaryTypeListId").selectpicker({
					noneSelectedText: '${selectPickerNoneSelectedText}', 
					countSelectedText: '${selectPickerCountSelectedText}'
				});

				<c:if test="${findWordResult != null && runScrollAnim == true}">
					$('html, body').animate({
			        	scrollTop: $("#findWordResultHrId").offset().top
			    	}, 1000);
				</c:if>		
				--%>		
			});
		</script>		
		
			
	</jsp:body>
</t:admTemplate>