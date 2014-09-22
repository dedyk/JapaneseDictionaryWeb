<%@ page contentType="text/html" isErrorPage="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>

<c:set var="pageTitle"> <spring:message code="admin.panel" /> </c:set>

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:admTemplate pageTitle="${pageTitle}">

	<jsp:body>
	
		<c:if test="${genericLogList != null}">
			
			<hr id="genericLogListHrId" style="margin-bottom: 10px" />
		
			<p class="text-left"><h4><spring:message code="admin.panel.genericlog.title" /></h4></p>
		
			<table id="genericLogListResult" class="table table-striped" style="font-size: 120%;">
				<thead>
					<tr>
						<th><spring:message code="admin.panel.genericlog.column.id" /></th>
						<th><spring:message code="admin.panel.genericlog.column.timestamp" /></th>
						<th><spring:message code="admin.panel.genericlog.column.operation" /></th>
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