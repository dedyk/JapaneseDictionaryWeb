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

	<link href="css/excite-bike/jquery-ui-1.10.4.custom.css" rel="stylesheet">
	
	<script src="js/jquery-1.10.2.js"></script>
	<script src="js/jquery-ui-1.10.4.custom.js"></script>

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
							<img src="http://www.idedyk.pl/index_html_m4c6a7bce.png" align="middle">
						</td>
						
						<td style="font-size:150%;" >
							小さくて奥ゆかしい日本語ヘルパー <br/>
							Mały, skromny japoński pomocnik
						</td>
					</tr>				
				</table>
				
							
				
			</div>
			
			<div style="margin-bottom:50px;" >
				<button id="button1">A button1 element</button>
				<button id="button2">A button2 element</button>
				<button id="button3">A button3 element</button>
			</div>			
						
			<div id="content">
				<jsp:doBody />
			</div>
			
			<div class="row footer">
				FOOTER
			</div>
		</div>
	</div>
	
	<script>
		$( "#nav" ).menu( {position: {at: "left bottom"}});

		$(document).ready(function() {
			$( "#button1" ).button();
			$( "#button2" ).button();
			$( "#button3" ).button();
		});

		
	</script>
</body>
</html>
