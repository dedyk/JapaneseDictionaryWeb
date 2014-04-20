<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="pageTitle">
	<spring:message code="kanjiDictionary.page.title" />
</c:set>

<c:set var="wordPlaceStartWith"> <spring:message code="kanjiDictionary.page.label.wordPlace.startWith"/> </c:set>
<c:set var="wordPlaceAnyPlace"> <spring:message code="kanjiDictionary.page.label.wordPlace.anyPlace"/> </c:set>
<c:set var="wordPlaceExact"> <spring:message code="kanjiDictionary.page.label.wordPlace.exact"/> </c:set>

<c:set var="search"> <spring:message code="kanjiDictionary.page.label.search"/> </c:set>

<c:set var="kanjiDictionaryDetailsLinkValue"> <spring:message code="kanjiDictionary.page.search.table.column.details.value" /> </c:set>

<spring:eval var="useExternalStaticFiles" expression="@applicationProperties.getProperty('use.external.static.files')" />

<c:choose>
     <c:when test="${useExternalStaticFiles == true}">
     	<spring:eval var="staticFilePrefix" expression="@applicationProperties.getProperty('use.external.static.path')" />
     </c:when>

     <c:otherwise>
     	<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />
     </c:otherwise>
</c:choose>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
	
		<div>		
    		<ul class="nav nav-tabs">
        		<li class="active"><a data-toggle="tab" href="#meaning"> <spring:message code="kanjiDictionary.page.tab.meaning" /> </a></li>
        		<li><a data-toggle="tab" href="#radicals"> <spring:message code="kanjiDictionary.page.tab.radicals" /> </a></li>
    		</ul>
    		
    		<div class="tab-content">
    		
        		<div id="meaning" class="tab-pane fade in active col-md-12" style="padding-top: 20px; padding-bottom: 20px">
            		
            		<form:form method="get" action="${pageContext.request.contextPath}/kanjiDictionarySearch">
            		
            			<form:errors cssClass="alert alert-danger" path="*" element="div" />
            		
						<table>				
							<tr>
								<td><form:label path="word"><spring:message code="kanjiDictionary.page.label.searchWord"/></form:label></td>
								<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="word" path="word"/></td>				
							</tr>
							
							<tr>
								<td><form:label path="wordPlace" cssStyle="margin: 0px 10px 0px 0px"><spring:message code="kanjiDictionary.page.label.wordPlace"/></form:label></td>
								<td>
									<table>
										<form:select id="wordPlaceId" path="wordPlace">
											<form:option value="START_WITH" label="${wordPlaceStartWith}" />
											<form:option value="ANY_PLACE" label="${wordPlaceAnyPlace}" />
											<form:option value="EXACT" label="${wordPlaceExact}" />
										</form:select>
									</table>
								</td>
							</tr>
							
							<tr>
								<td><form:label path="strokeCountFrom" cssStyle="margin: 0px 10px 0px 0px"><spring:message code="kanjiDictionary.page.label.strokeCountFrom"/></form:label></td>
								<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="strokeCountFrom" path="strokeCountFrom"/></td>
							</tr>

							<tr>
								<td><form:label path="strokeCountTo" cssStyle="margin: 0px 10px 0px 0px"><spring:message code="kanjiDictionary.page.label.strokeCountTo"/></form:label></td>
								<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="strokeCountTo" path="strokeCountTo"/></td>
							</tr>
																
							<tr>
								<td></td>
								<td>
									<input class="btn btn-default btn-lg" id="searchButton" type="submit" value="${search}" />					
								</td>				
							</tr>			
						</table>
            		</form:form>            		          		           		
        		</div>
        
        		<div id="radicals" class="tab-pane fade col-md-12" style="padding-top: 20px; padding-bottom: 20px">
            		<h3>FIXME - Elementy podstawowe</h3>
        		</div>
    		</div>
    		
			<c:if test="${findKanjiResult != null}">
			
				<div>
					<hr id="findKanjiResultHrId" style="margin-top: 10px; margin-bottom: 10px" />
				
					<p class="text-left"><h4><spring:message code="kanjiDictionary.page.search.table.caption" /></h4></p>
				
					<table id="kanjiDictionaryFindKanjiResult" class="table table-striped" style="font-size: 120%;">
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
									findKanjiRequest="${findKanjiRequest}"
									resultItem="${currentResult}"
									detailsLink="${pageContext.request.contextPath}/kanjiDictionaryDetails/%ID%/%KANJI%"
									detailsLinkValue="${kanjiDictionaryDetailsLinkValue}" />
							</c:forEach>
						</tfood>
						
					</table>
					
					<script>
						$(document).ready(function() {
							$('#kanjiDictionaryFindWordResult').dataTable({
								language: {
									url: '${staticFilePrefix}/js/datatables/polish.json'
								},
								"aaSorting": [],
								"sDom": "<'row'<'col-xs-12'f><'col-xs-6'l><'col-xs-6'p>r>t<'row'<'col-xs-6'i><'col-xs-6'p>>",
								"bLengthChange": false
							});
						});
					</script>
				</div>
			</c:if>

		</div>	
		
		<script>
			$(document).ready(function() {

				$( "#word" ).autocomplete({
				 	source: "${pageContext.request.contextPath}/kanjiDictionary/autocomplete",
				 	minLength: 1
				});				

				$( "#searchButton" ).button();

				$( "#wordPlaceId").selectpicker();

				<c:if test="${findKanjiResult != null}">
				$('html, body').animate({
		        	scrollTop: $("#findKanjiResultHrId").offset().top
		    	}, 1000);
				</c:if>

			});
		</script>
	
	</jsp:body>
	
</t:template>