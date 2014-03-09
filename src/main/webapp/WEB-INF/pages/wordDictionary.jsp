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
					<td>
						<input type="submit" value="Szukaj" />					
					</td>				
				</tr>			
			</table>
			
			
			
		</form:form>
		
		Słowniczek
		
		<p>Szukaza fraza</p>
		<p>Pole do wpisywania</p>
		<p>Podpowiadacz</p>
		<p>Szukaj ciagu</p>
		<p>od pierwszego znaku w słowie</p>
		<p>w dowolnym miejscu w słowie</p>
		<p>dokładnego</p>
		<p>Szukaj w</p>
		<p>kanji</p>
		<p>czytanie japońskie</p>
		<p>czytanie romaji</p>
		<p>tlumaczenia</p>
		<p>informacje dodatkowe</p>
		<p>typy szukanych słów</p>
		<p>generowane</p>
		
		
		<script>
			$( "#word" ).autocomplete({
			 	source: "${pageContext.request.contextPath}/wordDictionary/autocomplete",
			 	minLength: 2
			});

		</script>		
	</jsp:body>
</t:template>