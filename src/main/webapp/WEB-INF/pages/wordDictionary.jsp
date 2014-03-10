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
					<td><form:input id="word" path="word"/></td>				
				</tr>
				
				<tr>
					<td><form:label path="wordPlace">Szukane ciągu</form:label></td>
					<td>
						<table>
							<tr><td><form:radiobutton path="wordPlace" label="od pierwszego znaku w słowie" value="START_WITH" /></td></tr>
							<tr><td><form:radiobutton path="wordPlace" label="w dowolnym miejscu w słowie" value="ANY_PLACE" /></td></tr>
							<tr><td><form:radiobutton path="wordPlace" label="dokładnego" value="EXACT" /></td></tr>
						</table>
					</td>
				</tr>
				
				<tr>
					<td><form:label path="">Szukaj w</form:label></td>
					<td>
						<table>
							<form:select id="searchInId" path="searchIn" multiple="true">
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
					<td><form label path="">Typy szukanych słów</form></td>
					<td>
						<table>
							<form:select id="dictionaryTypeListId" path="dictionaryTypeStringList" multiple="true">
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
						<input id="searchButton" type="submit" value="Szukaj" />					
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

				$( "#searchInId").multiselect();

				$( "#dictionaryTypeListId").multiselect();
				
			});
		</script>		
	</jsp:body>
</t:template>