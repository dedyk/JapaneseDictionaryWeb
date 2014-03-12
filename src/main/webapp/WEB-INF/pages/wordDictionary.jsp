<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<t:template pageTitle="Mały, skromny japoński pomocnik | 小さくて奥ゆかしい日本語ヘルパー">
	<jsp:body>
			
		<form:form method="post" action="${pageContext.request.contextPath}/wordDictionary/search">
			
			<table>
				<tr>
					<td><form:label path="word">Szukane słowo</form:label></td>
					<td><form:input cssClass="form-control" cssStyle="margin: 0px 0px 10px 0px" id="word" path="word"/></td>				
				</tr>
				
				<tr>
					<td><form:label path="wordPlace">Szukane ciągu</form:label></td>
					<td>
						<table>
							<form:select id="workPlaceId" path="wordPlace">
								<form:option value="START_WITH" label="od pierwszego znaku w słowie" />
								<form:option value="ANY_PLACE" label="w dowolnym miejscu w słowie" />
								<form:option value="EXACT" label="dokładnego" />
							</form:select>
						</table>
					</td>
				</tr>
				
				<tr>
					<td><form:label path="">Szukaj w</form:label></td>
					<td>
						<table>
							<form:select id="searchInId" path="searchIn" multiple="true" data-selected-text-format="count">
								<form:option value="KANJI" label="kanji" />
								<form:option value="KANA" label="czytanie japońskie" />
								<form:option value="ROMAJI" label="czytanie romaji" />
								<form:option value="TRANSLATE" label="tłumaczenia" />
								<form:option value="INFO" label="informacje dodatkowe" />
							</form:select>
						</table>
					</td>				
				</tr>
				
				<tr>
					<td><form:label path="" cssStyle="margin: 0px 10px 0px 0px">Typy szukanych słów</form:label></td>
					<td>
						<table>
							<form:select id="dictionaryTypeListId" path="dictionaryTypeStringList" multiple="true" data-selected-text-format="count">
								<c:forEach items="${addableDictionaryEntryList}" var="currentAddableDictionaryEntry">
									<form:option value="${currentAddableDictionaryEntry}" label="${currentAddableDictionaryEntry.name}" />
								</c:forEach>								
							</form:select>
<%--  
							<c:forEach items="${addableDictionaryEntryList}" var="currentAddableDictionaryEntry">
								<tr><td><form:checkbox path="dictionaryTypeStringList" label="${currentAddableDictionaryEntry.name}" value="${currentAddableDictionaryEntry}"/></td></tr>							
							</c:forEach>
--%>
						</table>
					</td>				
				</tr>				
						
				<tr>
					<td></td>
					<td>
						<input class="btn btn-default btn-lg" id="searchButton" type="submit" value="Szukaj" />					
					</td>				
				</tr>			
			</table>
			
		</form:form>		
		
		<script>
			$(document).ready(function() {
				
				$( "#word" ).autocomplete({
				 	source: "${pageContext.request.contextPath}/wordDictionary/autocomplete",
				 	minLength: 2
				});
	
				$( "#searchButton" ).button();

				$( "#workPlaceId").selectpicker();
				
				$( "#searchInId").selectpicker({
					noneSelectedText: 'proszę wybrać', 
					countSelectedText: 'wybrano {0} z {1}'
				});

				$( "#dictionaryTypeListId").selectpicker({
					noneSelectedText: 'proszę wybrać', 
					countSelectedText: 'wybrano {0} z {1}'
				});
				
			});
		</script>		
	</jsp:body>
</t:template>