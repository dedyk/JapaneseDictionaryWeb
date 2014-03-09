<%@tag description="Overall Page template" pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@attribute name="header" fragment="true"%>
<%@attribute name="footer" fragment="true"%>
<%@attribute name="pageTitle"%>

<!doctype html>
<html lang="pl">
<head>
	<meta charset="utf-8">

	<title>${pageTitle}</title>

	<link href="http://code.jquery.com/ui/1.10.4/themes/excite-bike/jquery-ui.css" rel="stylesheet">
	
	<script src="http://code.jquery.com/jquery-1.11.0.min.js"></script>
	<script src="http://code.jquery.com/ui/1.10.4/jquery-ui.min.js"></script>

	<style>
	
	body {
		font: 80% "Trebuchet MS", sans-serif;
		margin: 50px;
	}
	.demoHeaders {
		margin-top: 2em;
	}
	#dialog-link {
		padding: .4em 1em .4em 20px;
		text-decoration: none;
		position: relative;
	}
	#dialog-link span.ui-icon {
		margin: 0 5px 0 0;
		position: absolute;
		left: .2em;
		top: 50%;
		margin-top: -8px;
	}
	#icons {
		margin: 0;
		padding: 0;
	}
	#icons li {
		margin: 2px;
		position: relative;
		padding: 4px 0;
		cursor: pointer;
		float: left;
		list-style: none;
	}
	#icons span.ui-icon {
		float: left;
		margin: 0 4px;
	}
	.fakewindowcontain .ui-widget-overlay {
		position: absolute;
	}
		
	</style>

</head>

<body>
	<div class="container">
		
		<div class="row">

			<div class="row header" style="margin-bottom:30px;" >
				
				<table>
					<tr>
						<td>
							<img src="${pageContext.request.contextPath}/img/japan-flag.png" align="middle">
						</td>
						
						<td style="font-size:150%;" >
							小さくて奥ゆかしい日本語ヘルパー <br/>
							Mały, skromny japoński pomocnik
						</td>
					</tr>				
				</table>
			</div>
			
			<div style="margin-bottom:50px;" >
				<button id="startButton">Start</button>
				<button id="wordDictionaryButton" data-href="wordDictionary">Słowniczek</button>
				<button id="kanjiDictionaryButton" data-href="kanjiDictionary">Kanji</button>
				<button id="suggestionButton" data-href="suggetion">Zgłoś sugestię</button>
				<button id="infoButton" data-href="info">Informacje</button>
			</div>			
						
			<div id="content">
				<jsp:doBody />
			</div>
			
			<div class="row footer" style="margin-top:50px;">
				<b>FIXME: FOOTER</b>
			</div>
		</div>
	</div>
	
	<script>
		$( "#nav" ).menu( {position: {at: "left bottom"}});

		$(document).ready(function() {
			$( "#startButton" ).button();
			$( "#startButton" ).click(function( event ) {
				var link = this;
				
				window.location = "<c:out value='${pageContext.request.contextPath}' />";
			});
			
			$( "#wordDictionaryButton" ).button();
			$( "#wordDictionaryButton" ).click(function( event ) {
				var link = this;
				
				window.location = "<c:out value='${pageContext.request.contextPath}' />" + "/" + $(link).attr("data-href");
			});
						
			$( "#kanjiDictionaryButton" ).button();
			$( "#kanjiDictionaryButton" ).click(function( event ) {
				var link = this;
				
				window.location = "<c:out value='${pageContext.request.contextPath}' />" + "/" + $(link).attr("data-href");
			});
			
			$( "#suggestionButton" ).button();
			$( "#suggestionButton" ).click(function( event ) {
				var link = this;
				
				window.location = "<c:out value='${pageContext.request.contextPath}' />" + "/" + $(link).attr("data-href");
			});

			$( "#infoButton" ).button();
			$( "#infoButton" ).click(function( event ) {
				var link = this;
				
				window.location = "<c:out value='${pageContext.request.contextPath}' />" + "/" + $(link).attr("data-href");
			});
		});
		
	</script>
</body>
</html>
