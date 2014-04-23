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
											resultItem="${currentResult}" />
									</c:forEach>
								</tfood>
								
							</table>
							
							<script>
								$(document).ready(function() {
									$('#kanjiDictionaryFindKanjiResult').dataTable({
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
        
        		<div id="radicals" class="tab-pane fade col-md-12" style="padding-top: 20px; padding-bottom: 20px">
            		
            		<script>
						var selectedRadicals = [];

						function updateRadicalsTableState(json) {

							var allAvailableRadicals = json.allAvailableRadicals;
							
							$('#radicalTableId td').each(function() {
								
								var thisId = $(this).attr('id');

								if (thisId != null && thisId.indexOf("strokeCount_") == 0) {
									return;
								}

								var thisIdKanji = $(this).text();

								if (allAvailableRadicals.indexOf(thisIdKanji) == -1) {
									$(this).css("color", "silver");
									
								} else {
									$(this).css("color", "black");
								}
							});

							var kanjiFromRadicals = json.kanjiFromRadicals;
							var kanjiFromRadicalsMore = json.kanjiFromRadicalsMore;

							if (kanjiFromRadicals != null && kanjiFromRadicals.length > 0) {
								$('#radicalTableFoundId').empty();
								$('#radicalTableFoundId').css("width", "");

								var currentStrokeCount = null;
								var strokeCountCounter = 0;

								$.each(kanjiFromRadicals, function(key, value) {
									
									if (strokeCountCounter == 0) {
										$('#radicalTableFoundId').append("<tr></tr>");
									}

									if (currentStrokeCount != value.strokeCount) {
										$('#radicalTableFoundId tr:last').append('<td style="padding: 5px; font-size: 200%; text-align: center; border: 1px solid black; background-color: #CCCCCC">' + value.strokeCount + "</td>");

										currentStrokeCount = value.strokeCount;
										strokeCountCounter++;
									}

									if (strokeCountCounter == 28) {
										$('#radicalTableFoundId').append("<tr></tr>");

										strokeCountCounter = 0;
									}

									$('#radicalTableFoundId tr:last').append('<td style="padding: 5px; font-size: 150%; text-align: center; border: 1px solid black;">' + value.kanji + "</td>");								

									strokeCountCounter++;

									if (strokeCountCounter == 28) {
										strokeCountCounter = 0;
									}									
							    });
							    
							} else {
								$('#radicalTableFoundId').empty();

								$('#radicalTableFoundId').css("width", "100%");
								$('#radicalTableFoundId').append('<tr style="width: 100%"><td style="padding: 5px; text-align: left"><spring:message code="kanjiDictionary.page.search.radicals.noFound" /></td></tr>');								
							}
						}

						function updateSelectedRadicals(radicalTd) {


							if (radicalTd != null) {

								var selectedRadicalTd = $(radicalTd);
								var selectedRadicalKanji = selectedRadicalTd.text();

								var selectedRadicalIndexOf = selectedRadicals.indexOf(selectedRadicalKanji);
								
								if (selectedRadicalIndexOf == -1) {
									selectedRadicals.push(selectedRadicalKanji);

									selectedRadicalTd.css("background-color", "yellow");
									
								} else {
									selectedRadicals.splice(selectedRadicalIndexOf, 1);

									selectedRadicalTd.css("background-color", "");
								}
							}

							$.ajax({
								
								url: "${pageContext.request.contextPath}/kanjiDictionary/showAvailableRadicals",
									
								data: {
									selectedRadicals: selectedRadicals
								},
								
								type: "POST",

								dataType : "json",

								success: function( json ) {
									updateRadicalsTableState(json);
								},

								error: function( xhr, status, errorThrown ) {
									alert('<spring:message code="kanjiDictionary.page.search.radicals.problem" />');
								},

								complete: function( xhr, status ) {
								}
								});
							
						}
            		</script>
            		
            		<center>
            		
            			<c:set var="currentStrokeCount" value="" />
            			<c:set var="strokeCountCounter" value="0" />
      		
	            		<table id="radicalTableId">
	            			<c:forEach items="${radicalList}" var="currentRadical">
	            				
	            			  	<c:if test="${strokeCountCounter == 0}">
	            					<tr>
	            				</c:if>
	            				
								<c:if test="${currentStrokeCount != currentRadical.strokeCount}">
	            					<td id="strokeCount_${currentRadical.strokeCount}" style="padding: 5px; font-size: 200%; text-align: center; border: 1px solid black; background-color: #CCCCCC">${currentRadical.strokeCount}</td>
	            					
	            					<c:set var="currentStrokeCount" value="${currentRadical.strokeCount}" />
	            					<c:set var="strokeCountCounter" value="${strokeCountCounter + 1}" />
	            				</c:if>
	            				
	            				<c:if test="${strokeCountCounter == 30}">
	            					<c:set var="strokeCountCounter" value="0" />
	            					</tr>
	            					<tr>
	            				</c:if>             				
	
	            				<td style="padding: 5px; font-size: 150%; text-align: center; border: 1px solid black;" onclick="updateSelectedRadicals(this);">${currentRadical.radical}</td>
	            				<c:set var="strokeCountCounter" value="${strokeCountCounter + 1}" />
	            				
	            				 <c:if test="${strokeCountCounter == 30}">
	            					<c:set var="strokeCountCounter" value="0" />
	            					</tr>
	            					<tr>
	            				</c:if>             				
	            				 				
	            			</c:forEach>
	            			
	            			</tr>
	            		</table>            		
					</center>
					
					<div id="radicalTablePreviewDivId" style="padding-top: 10px;">
						<h4><spring:message code="kanjiDictionary.page.search.radicals.found" /></h4>
						
						<center>
							<table id="radicalTableFoundId">
							</table>						
						</center>					
					</div>
			
        		</div>
    		</div>
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

				updateSelectedRadicals(null);

			});
		</script>
	
	</jsp:body>
	
</t:template>