<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="pageTitle"> <spring:message code="kanjiDictionary.page.title" /> </c:set>
<c:set var="pageDescription"> <spring:message code="kanjiDictionary.page.pageDescription"/> </c:set>

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

<jdwt:isMobile mobile="false">
	<c:set var="strokeCountMaxElements" value="30" />
	<c:set var="strokeNumberFontSizeValue" value="200" />
	<c:set var="strokeFontSizeValue" value="150" />
</jdwt:isMobile>

<jdwt:isMobile mobile="true">
	<c:set var="strokeCountMaxElements" value="8" />
	<c:set var="strokeNumberFontSizeValue" value="180" />
	<c:set var="strokeFontSizeValue" value="140" />
</jdwt:isMobile>

<t:template pageTitle="${pageTitle}" pageDescription="${pageDescription}">

	<jsp:body>

		<script>
			// rysowanie
			var canvas, stage;
			var drawingCanvas;
			var oldPt;
	        var oldMidPt;
	        var color;
	        var stroke;

	        var strokePaths = [];
	        var currentPath = [];
		
			$(document).ready(function() {

				// wyszukiwanie
				$( "#word" ).autocomplete({
				 	source: "${pageContext.request.contextPath}/kanjiDictionary/autocomplete",
				 	minLength: 1
				});				

				$( "#searchButton" ).button();

				$( "#wordPlaceId").selectpicker();

				// rysowanie				
				canvas = document.getElementById("detectCanvas");

	            stage = new createjs.Stage(canvas);
	            stage.autoClear = false;
	            stage.enableDOMEvents(true);

	            createjs.Touch.enable(stage);
	            createjs.Ticker.setFPS(24);

	            drawingCanvas = new createjs.Shape();

	            stage.addEventListener("stagemousedown", handleMouseDown);
	            stage.addEventListener("stagemouseup", handleMouseUp);

	            stage.addChild(drawingCanvas);
	            stage.update();	

	            // tabelki
				$('#kanjiDictionaryFindKanjiResult').dataTable({
					language: {
						url: '${pageContext.request.contextPath}/js/datatables/polish.json'
					},
					"bStateSave": true,
					"aaSorting": [],
					"sDom": "<'row'<'col-xs-12'f><'col-xs-6'l><'col-xs-6'p>r>t<'row'<'col-xs-6'i><'col-xs-6'p>>",
					"bLengthChange": false,
					"bPaginate": false
				});

				$('#kanjiDictionaryFindKanjiDetectResult').dataTable({
					language: {
						url: '${pageContext.request.contextPath}/js/datatables/polish.json'
					},
					"bStateSave": true,
					"aaSorting": [],
					"sDom": "<'row'<'col-xs-12'f><'col-xs-6'l><'col-xs-6'p>r>t<'row'<'col-xs-6'i><'col-xs-6'p>>",
					"bLengthChange": false,
					"bPaginate": false
				});

				// zaznaczenie wybranych elementow podstawowych
				<c:if test="${sessionScope.selectedRadicals != null}">
					selectedRadicals = [];
					
					<c:forEach items="${sessionScope.selectedRadicals}" var="currentRadical">
					selectedRadicals.push('<c:out value="${currentRadical}" />');

					$('#radicalTableId td').filter(function() { return getRadicalCellKanji($(this)) == '<c:out value="${currentRadical}" />'; }).css("background-color", "yellow");
										
					</c:forEach>			
				</c:if>
		
				updateSelectedRadicals(null);

	            // wybor zakladki
	            <c:if test="${selectTab != null}">
	            $('#<c:out value="${selectTab}" />').tab('show');
	            </c:if>

				// przesuniecie ekranu po wyszukiwaniu znakow kanji
				<c:if test="${findKanjiResult != null && runScrollAnim == true}">

				window.setTimeout(function() {

					$('html, body').animate({
			        	scrollTop: $("#findKanjiResultHrId").offset().top
			    	}, 1000);
			    	
				}, 300);
				
				</c:if>

				// przesuniecie ekranu po rozpoznawaniu znakow kanji
				<c:if test="${findKanjiDetectResult != null && runScrollAnim == true}">

				window.setTimeout(function() { 
					$('html, body').animate({
			        	scrollTop: $("#findKanjiDetectResultHrId").offset().top
			    	}, 1000);
				}, 300);
				
				</c:if>

				// narysowanie znaku
				<c:if test="${kanjiDictionaryDrawStroke != null}">

					strokePaths = [];
				
					<c:forEach items="${kanjiDictionaryDrawStroke.strokes}" var="currentStroke">

					currentPath = [];

						<c:forEach items="${currentStroke}" var="currentPoint">					
							currentPath.push([<c:out value="${currentPoint.x}" />, <c:out value="${currentPoint.y}" />]);									
						</c:forEach>

						strokePaths.push(currentPath);

						currentPath = [];
						
					</c:forEach>

					reDrawDetect();
					
				</c:if>		

				// zmiana zakladki
				$("#meaningTabLiId").on("click", function(e) {
					saveCurrectTab('meaningTabLiId');
					
					// automatyczne ustawienie focusu
					setFocusIfNeeded(true);
				});		

				$("#radicalsTabLiId").on("click", function(e) {
					saveCurrectTab('radicalsTabLiId');
				});		

				$("#detectTabLiId").on("click", function(e) {
					saveCurrectTab('detectTabLiId');
				});
				
				// automatyczne ustawienie focusu
				setFocusIfNeeded(false);
			});

	        function handleMouseDown(event) {

	        	currentPath = [];
	            
	            color = "#000000";
	            stroke = 10;

	            oldPt = new createjs.Point(stage.mouseX, stage.mouseY);
	            oldMidPt = oldPt;

	            currentPath.push([stage.mouseX, stage.mouseY]);
	            
	            stage.addEventListener("stagemousemove" , handleMouseMove);
	        }

	        function handleMouseMove(event) {
	            var midPt = new createjs.Point(oldPt.x + stage.mouseX>>1, oldPt.y+stage.mouseY>>1);
	            
	            drawingCanvas.graphics.clear().setStrokeStyle(stroke, 'round', 'round').beginStroke(color).moveTo(midPt.x, midPt.y).curveTo(oldPt.x, oldPt.y, oldMidPt.x, oldMidPt.y);

	            oldPt.x = stage.mouseX;
	            oldPt.y = stage.mouseY;

	            oldMidPt.x = midPt.x;
	            oldMidPt.y = midPt.y;

	            currentPath.push([stage.mouseX, stage.mouseY]);

	            stage.update();
	        }

	        function handleMouseUp(event) {
	            stage.removeEventListener("stagemousemove" , handleMouseMove);

				if (currentPath.length != 0) {
					strokePaths.push(currentPath);
					
					currentPath = [];
				}	            
	        }

	        function saveCurrectTab(tabId) {

	        	$.get( "${pageContext.request.contextPath}/kanjiDictionary/saveCurrectTab", { tabId: tabId } );
		    }
	        
	        function setFocusIfNeeded(fromTab) {
	        	if (fromTab == true) {
					<c:if test="${findKanjiResult == null}">
						setTimeout(function() { 
								$( "#word").focus();
							}, 200);						
					</c:if>
	        	} else {
					<c:if test="${selectTab == null && findKanjiResult == null}">
						$( "#word").focus();
					</c:if>				
					
					<c:if test="${selectTab != null && selectTab == 'meaningTabLiId' && findKanjiResult == null}">
						$( "#word").focus();
					</c:if>
	        	}
	        }

		</script>
			
		<div>		
    		<ul class="nav nav-tabs">
        		<li class="active"><a data-toggle="tab" href="#meaning" id='<c:out value="${tabs[0].id}" />'> <spring:message code="kanjiDictionary.page.tab.meaning" /> </a></li>
        		<li><a data-toggle="tab" href="#radicals" id='<c:out value="${tabs[1].id}" />'> <spring:message code="kanjiDictionary.page.tab.radicals" /> </a></li>
        		<li><a data-toggle="tab" href="#detect" id='<c:out value="${tabs[2].id}" />'> <spring:message code="kanjiDictionary.page.tab.detect" /> </a></li>
    		</ul>
    		
    		<div class="tab-content">
    		
        		<div id="meaning" class="tab-pane fade in active col-md-12" style="padding-top: 20px; padding-bottom: 20px">
            		
            		<form:form method="get" action="${pageContext.request.contextPath}/kanjiDictionarySearch">

						<fieldset>
							<legend><spring:message code="kanjiDictionary.page.tab.meaning.title" /></legend>			
						</fieldset>
            		
            			<form:errors cssClass="alert alert-danger" path="*" element="div" />
            			
						<c:if test="${kanjiAutocompleteInitialized == false}">
							<div class="alert alert-info"><spring:message code="kanjiDictionary.page.kanjiAutocompleteInitialized.false"/></div>
						</c:if>		
            			
						<table>				
							<tr>
								<td><form:label path="word"><spring:message code="kanjiDictionary.page.label.searchWord"/></form:label></td>
								<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="word" path="word"/></td>				
							</tr>
							
							<tr>
								<td><form:label path="wordPlace" cssStyle="margin: 0px 10px 0px 0px"><spring:message code="kanjiDictionary.page.label.wordPlace"/></form:label></td>
								<td>
									<form:select id="wordPlaceId" path="wordPlace">
										<form:option value="START_WITH" label="${wordPlaceStartWith}" />
										<form:option value="ANY_PLACE" label="${wordPlaceAnyPlace}" />
										<form:option value="EXACT" label="${wordPlaceExact}" />
									</form:select>
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
					
						<div class="col-md-12">
							<hr id="findKanjiResultHrId" style="margin-top: 10px; margin-bottom: 10px" />
						
							<p class="text-left"><h4><spring:message code="kanjiDictionary.page.search.table.caption" /></h4></p>
							
							<c:if test="${kanjiDictionaryEntrySpellCheckerSuggestionList != null}">				
								<jdwt:generateSpellCheckerSuggestionList id="kanjiDictionaryEntrySpellCheckerSuggestionListId" spellCheckerSuggestionList="${kanjiDictionaryEntrySpellCheckerSuggestionList}" type="kanjiDictionaryEntry" />
							</c:if>
														
							<table id="kanjiDictionaryFindKanjiResult" class="table table-striped" style="font-size: 120%;">
								<thead>
									<tr>
										<th><spring:message code="kanjiDictionary.page.search.table.column.kanji" /></th>
										<th><spring:message code="kanjiDictionary.page.search.table.column.radicals" /></th>
										<th><spring:message code="kanjiDictionary.page.search.table.column.strokeCount" /></th>
										<th><spring:message code="kanjiDictionary.page.search.table.column.translate" /></th>
										<!-- <jdwt:isMobile mobile="false"><th><spring:message code="kanjiDictionary.page.search.table.column.info" /></th></jdwt:isMobile> -->
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
						</div>
					</c:if>
            		         		          		           		
        		</div>
        
        		<div id="radicals" class="tab-pane fade col-md-12" style="padding-top: 20px; padding-bottom: 20px">
        			
        			<form>
						<fieldset>
							<legend><spring:message code="kanjiDictionary.page.tab.radicals.title" /></legend>			
						</fieldset>        			
        			</form>
            		
            		<script>
						var selectedRadicals = [];

						function getRadicalCellKanji(cell) {

							var cellKanji = cell.text().trim();

							if (cellKanji != '') {
								return cellKanji;	
							}

							var imgTag = cell.find("img");

							if (imgTag != null) {
								return imgTag.attr("alt");

							}
							
							return "";
						}

						function updateRadicalsTableState(json) {

							var allAvailableRadicals = json.allAvailableRadicals;
							
							$('#radicalTableId td').each(function() {
								
								var thisId = $(this).attr('id');

								if (thisId != null && thisId.indexOf("strokeCount_") == 0) {
									return;
								}

								var thisIdKanji = getRadicalCellKanji($(this));

								if (allAvailableRadicals.indexOf(thisIdKanji) == -1) {
									$(this).css("opacity", "0.4");
									
								} else {
									$(this).css("opacity", "1.0");
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
										$('#radicalTableFoundId tr:last').append('<td style="padding: 5px; font-size: ${strokeNumberFontSizeValue}%; text-align: center; border: 1px solid black; background-color: #CCCCCC">' + value.strokeCount + "</td>");

										currentStrokeCount = value.strokeCount;
										strokeCountCounter++;
									}
									
									if (strokeCountCounter == ${strokeCountMaxElements - 1}) {
										$('#radicalTableFoundId').append("<tr></tr>");

										strokeCountCounter = 0;
									}

									$('#radicalTableFoundId tr:last').append('<td style="padding: 5px; font-size: ${strokeFontSizeValue}%; text-align: center; border: 1px solid black;">' + 
											'<a href=\'<c:out value="${pageContext.request.contextPath}" />/kanjiDictionaryDetails/' + value.id + '/' + value.kanji + '\'>' + value.kanji + "</a></td>");								

									strokeCountCounter++;

									if (strokeCountCounter == ${strokeCountMaxElements - 1}) {
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
								var selectedRadicalKanji = getRadicalCellKanji(selectedRadicalTd);

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

									if (selectedRadicalIndexOf == -1) {

										$('html, body').animate({
								        	scrollTop: $("#radicalTableFoundDivId").offset().top
								    	}, 1000);
									}
								},

								error: function( xhr, status, errorThrown ) {
									alert('<spring:message code="kanjiDictionary.page.search.radicals.problem" />');
								},

								complete: function( xhr, status ) {
								}
								});
							
						}
            		</script>
            		
            		<div>
            		
            			<c:set var="currentStrokeCount" value="" />
            			<c:set var="strokeCountCounter" value="0" />
      		
	            		<table id="radicalTableId" align="center">
	            			<c:forEach items="${radicalList}" var="currentRadical">
	            				
	            			  	<c:if test="${strokeCountCounter == 0}">
	            					<tr>
	            				</c:if>
	            				
								<c:if test="${currentStrokeCount != currentRadical.strokeCount}">
	            					<td id="strokeCount_${currentRadical.strokeCount}" style="padding: 5px; font-size: ${strokeNumberFontSizeValue}%; text-align: center; border: 1px solid black; background-color: #CCCCCC">${currentRadical.strokeCount}</td>
	            					
	            					<c:set var="currentStrokeCount" value="${currentRadical.strokeCount}" />
	            					<c:set var="strokeCountCounter" value="${strokeCountCounter + 1}" />
	            				</c:if>
	            				
	            				<c:if test="${strokeCountCounter == strokeCountMaxElements}">
	            					<c:set var="strokeCountCounter" value="0" />
	            					</tr>
	            					<tr>
	            				</c:if>   
	            				
								<c:choose>
							      <c:when test="${currentRadical.image == null}">
							      	<td style="padding: 5px; font-size: ${strokeFontSizeValue}%; text-align: center; border: 1px solid black;" onclick="updateSelectedRadicals(this);">${currentRadical.radical}</td>
							      </c:when>
							
							      <c:otherwise>
							      	<td style="padding: 5px; font-size: ${strokeFontSizeValue}%; text-align: center; border: 1px solid black;" onclick="updateSelectedRadicals(this);">
							      	<img src="${staticFilePrefix}/${currentRadical.image}" alt="${currentRadical.radical}" /></td>
							      </c:otherwise>
								</c:choose>
	            				
	            				<c:set var="strokeCountCounter" value="${strokeCountCounter + 1}" />
	            				
	            				 <c:if test="${strokeCountCounter == strokeCountMaxElements}">
	            					<c:set var="strokeCountCounter" value="0" />
	            					</tr>
	            					<tr>
	            				</c:if>             				
	            				 				
	            			</c:forEach>
	            			
	            			</tr>
	            		</table>            		
					</div>
					
					<div id="radicalTableFoundDivId" style="padding-top: 10px;">
						<h4><spring:message code="kanjiDictionary.page.search.radicals.found" /></h4>
						
						<div>
							<table id="radicalTableFoundId" align="center">
							</table>	
						</div>					
					</div>		
        		</div>
        		
        		<div id="detect" class="tab-pane fade col-md-12" style="padding-top: 20px; padding-bottom: 20px">
        		        			
        		    <form id="detectForm" method="post" action="${pageContext.request.contextPath}/kanjiDictionaryDetectSearch">
        		    	<input id="detectFormStrokes" type="hidden" name="strokes" value="" />
        		    	<input id="detectFormWidth" type="hidden" name="width" value="" />
        		    	<input id="detectFormHeight" type="hidden" name="height" value="" />
        		    </form>
        		        			
        			<div>
        				<h4><spring:message code="kanjiDictionary.page.tab.detect.info1" /></h4>
        				<h5><spring:message code="kanjiDictionary.page.tab.detect.info2" /></h5>
        				<hr/>
        			</div>
        			
        			<c:if test="${kanjiDictionaryDetectErrorMessage != null}">
        			<div class="col-md-12 alert alert-danger">
        				<c:out value="${kanjiDictionaryDetectErrorMessage}" />
        			</div>        			
        			</c:if>
        			
        			<div>
        				<div>
							<button onclick="detect();" type="button" class="btn btn-default" style="margin-bottom: 10px; width: 160px;">
								<b><spring:message code="kanjiDictionary.page.tab.detect.detect" /></b>
							</button>
							
							<button onclick="undoStrokeDetect();" type="button" class="btn btn-default" style="margin-bottom: 10px; width: 160px;">
								<spring:message code="kanjiDictionary.page.tab.detect.undo" />
							</button>
							
							<button onclick="clearDetect();" type="button" class="btn btn-default" style="margin-bottom: 10px; width: 160px;">
								<spring:message code="kanjiDictionary.page.tab.detect.clear" />
							</button>        				
        				</div>
        			
        				<div>
        					<jdwt:isMobile mobile="false">
        						<canvas id="detectCanvas" width="500" height="500" style="border: 1px solid black;">        					
        						</canvas>
        					</jdwt:isMobile>
        					
        					<jdwt:isMobile mobile="true">        					
        						<canvas id="detectCanvas" width="10" height="10" style="border: 1px solid black;">        					
        						</canvas>
        					</jdwt:isMobile>        					
        				</div>
        			</div>

		            <c:if test="${findKanjiDetectResult != null}">
					
						<div class="col-md-12">
							<hr id="findKanjiDetectResultHrId" style="margin-top: 10px; margin-bottom: 10px" />
						
							<p class="text-left"><h4><spring:message code="kanjiDictionary.page.search.table.caption" /></h4></p>
						
							<table id="kanjiDictionaryFindKanjiDetectResult" class="table table-striped" style="font-size: 120%;">
								<thead>
									<tr>
										<th><spring:message code="kanjiDictionary.page.search.table.column.kanji" /></th>
										<th><spring:message code="kanjiDictionary.page.search.table.column.radicals" /></th>
										<th><spring:message code="kanjiDictionary.page.search.table.column.strokeCount" /></th>
										<th><spring:message code="kanjiDictionary.page.search.table.column.translate" /></th>
										<!-- <jdwt:isMobile mobile="false"><th><spring:message code="kanjiDictionary.page.search.table.column.info" /></th></jdwt:isMobile> -->
										<th></th>
									</tr>
								</thead>
								<tfood>
									<c:forEach items="${findKanjiDetectResult.result}" var="currentResult">
										<jdwt:findKanjiResultItemTableRow
											resultItem="${currentResult}" />
									</c:forEach>
								</tfood>
								
							</table>							
						</div>
					</c:if>
        			
        			<script>
						function detect() {								
								if (strokePaths.length == 0) {
									alert('<spring:message code="kanjiDictionary.page.tab.detect.pleaseDraw" />');
									
									return;
								}
								
								var detectStrokePathsString = "";
																
								for (var idx = 0; idx < strokePaths.length; ++idx) {
									var currentStrokePath = strokePaths[idx];

									var currentStringStrokePath = "";

									for (var ydx = 0; ydx < currentStrokePath.length; ++ydx) {
										currentStringStrokePath += currentStrokePath[ydx][0] + "," + currentStrokePath[ydx][1] + ";";
									}

									detectStrokePathsString += currentStringStrokePath + "\n";
								}

								$("#detectFormStrokes").val(detectStrokePathsString);
								$("#detectFormWidth").val($('#detectCanvas').prop("width"));
								$("#detectFormHeight").val($('#detectCanvas').prop("height"));
																
								$("#detectForm").submit();
							}

							function undoStrokeDetect() {

								if (strokePaths.length > 0) {
									strokePaths.pop();

									reDrawDetect();
								}
							}

							function reDrawDetect() {

								stage.clear();

					            color = "#000000";
					            stroke = 10;
								
								for (var idx = 0; idx < strokePaths.length; ++idx) {
									var currentStrokePath = strokePaths[idx];

									oldPt = new createjs.Point(currentStrokePath[0][0], currentStrokePath[0][1]);
						            oldMidPt = oldPt;

									for (var currentStrokePathIdx = 0; currentStrokePathIdx < currentStrokePath.length; ++currentStrokePathIdx) {

							            var midPt = new createjs.Point(oldPt.x + currentStrokePath[currentStrokePathIdx][0]>>1, oldPt.y + currentStrokePath[currentStrokePathIdx][1]>>1);

							            drawingCanvas.graphics.clear().setStrokeStyle(stroke, 'round', 'round').beginStroke(color).moveTo(midPt.x, midPt.y).curveTo(oldPt.x, oldPt.y, oldMidPt.x, oldMidPt.y);

							            oldPt.x = currentStrokePath[currentStrokePathIdx][0];
							            oldPt.y = currentStrokePath[currentStrokePathIdx][1];

							            oldMidPt.x = midPt.x;
							            oldMidPt.y = midPt.y;

							            stage.update();
									}
								}
							}

							function clearDetect() {

								strokePaths = [];

								stage.clear();
							}
							
							<jdwt:isMobile mobile="true">
								var detectCanvasSize = screen.width - 50;
								
								if (detectCanvasSize > screen.height - 50) {
									detectCanvasSize = screen.height - 50;
								}
								
								$('#detectCanvas').prop("width", detectCanvasSize)
								$('#detectCanvas').prop("height", detectCanvasSize)
							
							</jdwt:isMobile>							
        			</script>        			
        		</div>        		
    		</div>
		</div>	
			
	</jsp:body>
	
</t:template>