<%@ page contentType="text/html" isErrorPage="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>

<c:set var="pageTitle"> <spring:message code="admin.panel" /> </c:set>

<c:set var="search"> <spring:message code="admin.panel.search.button"/> </c:set>

<c:set var="selectPickerNoneSelectedText"> <spring:message code="common.selectpicker.noneSelectedText"/> </c:set>
<c:set var="selectPickerCountSelectedText"> <spring:message code="common.selectpicker.countSelectedText"/> </c:set>

<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />

<t:admTemplate pageTitle="${pageTitle}">

	<jsp:body>

		<form:form method="get" id="panelSearchId" action="${pageContext.request.contextPath}/adm/panelSearch">
		
			<fieldset>
				<legend><spring:message code="admin.panel.search.title" /></legend>			
			</fieldset>
			
			<form:errors cssClass="alert alert-danger" path="*" element="div" />		
			
			<table>				
				<tr>
					<td><form:label path="pageNo" cssStyle="margin: 0px 10px 10px 0px"><spring:message code="admin.panel.search.pageNo"/></form:label></td>
					<td><form:input cssClass="form-control" cssStyle="margin: 0px 10px 10px 0px" id="pageNoId" path="pageNo"/></td>
					<%-- <td><label style="margin: 0px 0px 10px 10px"><spring:message code="admin.panel.search.pageNoFrom" arguments="${maxPageSize}"/></label> --%>					
				</tr>

				<tr>
					<td><form:label path="" cssStyle="margin: 0px 10px 0px 0px"><spring:message code="admin.panel.search.genericLogOperationStringList"/></form:label></td>
					<td>
						<form:select id="genericLogOperationStringListId" path="genericLogOperationStringList" multiple="true" data-selected-text-format="count">
							<c:forEach items="${genericLogOperationEnumList}" var="currentGenericLogOperationEnum">
								<form:option value="${currentGenericLogOperationEnum}" label="${currentGenericLogOperationEnum}" />
							</c:forEach>								
						</form:select>
					</td>
					<td>
						<a href="#" class="btn btn-default" style="margin: 0px 10px 10px 10px" onclick="selectAllGenericLogOperations(true)"><spring:message code="admin.panel.search.genericLogOperationStringList.selectAll" /></a>
					</td>
					<td>
						<a href="#" class="btn btn-default" style="margin: 0px 10px 10px 0px" onclick="selectAllGenericLogOperations(false)"><spring:message code="admin.panel.search.genericLogOperationStringList.deselectAll" /></a>
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
	
		<c:if test="${genericLogList != null}">
			
			<hr id="genericLogListHrId" style="margin-bottom: 10px" />
		
			<p class="text-left"><h4><spring:message code="admin.panel.genericlog.title" /></h4></p>

			<div class="col-md-12" style="text-align: right; margin: 0px 0px 20px 0px ">			
				<c:if test="${currentPage != null}">				
					<c:if test="${currentPage != 1}">
						<a href="#" class="btn btn-default" onclick="setPageNo(1)"><spring:message code="admin.panel.search.button.first" /></a>
					</c:if>

					<c:if test="${currentPage > 1}">
						<a href="#" class="btn btn-default" onclick="setPageNo(${currentPage - 1})"><spring:message code="admin.panel.search.button.previous" /></a>
					</c:if>
					
					<c:if test="${currentPage < maxPageSize}">
						<a href="#" class="btn btn-default" onclick="setPageNo(${currentPage + 1})"><spring:message code="admin.panel.search.button.next" /></a>
						<a href="#" class="btn btn-default" onclick="setPageNo(${maxPageSize})"><spring:message code="admin.panel.search.button.last" /></a>
					</c:if>										
				</c:if>							
			</div>
		
			<table id="genericLogListResult" class="table table-striped" style="font-size: 120%;">
				<thead>
					<tr>
						<th><spring:message code="admin.panel.genericlog.column.id" /></th>
						<th><spring:message code="admin.panel.genericlog.column.timestamp" /></th>
						<th><spring:message code="admin.panel.genericlog.column.operation" /></th>
						<th><spring:message code="admin.panel.genericlog.column.suboperation" /></th>
						<%-- <th><spring:message code="admin.panel.genericlog.column.requestUrl" /></th> --%>
						<th><spring:message code="admin.panel.genericlog.column.remoteIp" /></th>
						<th><spring:message code="admin.panel.genericlog.column.remoteHost" /></th>						
						<th></th>
					</tr>
				</thead>
				<tfood>
					<c:forEach items="${genericLogList}" var="currentResult">
 						<jdwt:genericLogItemTableRowTag
							genericLog="${currentResult}" />
					</c:forEach>
				</tfood>				
			</table>
			
			<div class="col-md-12" style="text-align: right">			
				<c:if test="${currentPage != null}">				
					<c:if test="${currentPage != 1}">
						<a href="#" class="btn btn-default" onclick="setPageNo(1)"><spring:message code="admin.panel.search.button.first" /></a>
					</c:if>

					<c:if test="${currentPage > 1}">
						<a href="#" class="btn btn-default" onclick="setPageNo(${currentPage - 1})"><spring:message code="admin.panel.search.button.previous" /></a>
					</c:if>
					
					<c:if test="${currentPage < maxPageSize}">
						<a href="#" class="btn btn-default" onclick="setPageNo(${currentPage + 1})"><spring:message code="admin.panel.search.button.next" /></a>
						<a href="#" class="btn btn-default" onclick="setPageNo(${maxPageSize})"><spring:message code="admin.panel.search.button.last" /></a>
					</c:if>										
				</c:if>							
			</div>
			
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

				function setPageNo(pageNo) {
					$( "#pageNoId" ).val(pageNo);

					$( "#panelSearchId").submit();				
				}
			</script>
		</c:if>
		
		<script>
			$(document).ready(function() {

				$( "#genericLogOperationStringListId").selectpicker({
					noneSelectedText: '${selectPickerNoneSelectedText}', 
					countSelectedText: '${selectPickerCountSelectedText}'
				});

				$( "#searchButton" ).button();				
			});

			function selectAllGenericLogOperations(status) {
				if (status == true) {
					$('#genericLogOperationStringListId').selectpicker('selectAll');
				} else {
					$('#genericLogOperationStringListId').selectpicker('deselectAll');
				}
			}			
		</script>		
		
			
	</jsp:body>
</t:admTemplate>