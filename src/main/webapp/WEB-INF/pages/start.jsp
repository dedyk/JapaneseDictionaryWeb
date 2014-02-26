<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<t:template pageTitle="START - TEST">
	<jsp:body>
	
		<script>

			$(function() {
							
				var availableTags = [
					"ActionScript",
					"AppleScript",
					"Asp",
					"BASIC",
					"C",
					"C++",
					"Clojure",
					"COBOL",
					"ColdFusion",
					"Erlang",
					"Fortran",
					"Groovy",
					"Haskell",
					"Java",
					"JavaScript",
					"Lisp",
					"Perl",
					"PHP",
					"Python",
					"Ruby",
					"Scala",
					"Scheme"
				];

				$( "#autocomplete" ).autocomplete({
					source: availableTags
				});

				
			});
		</script>
	
		START
		
		<h1>Welcome to jQuery UI!</h1>

		<div class="ui-widget">
			<p>This page demonstrates the widgets you downloaded using the theme you selected in the download builder. We've included and linked to minified versions of <a href="js/jquery-1.10.2.js">jQuery</a>, your personalized copy of <a href="js/jquery-ui-.custom.min.js">jQuery UI (js/jquery-ui-.custom.min.js)</a>, and <a href="css//jquery-ui-.custom.min.css">css//jquery-ui-.custom.min.css</a> which imports the entire jQuery UI CSS Framework. You can choose to link a subset of the CSS Framework depending on your needs. </p>
			<p>You've downloaded components and a theme that are compatible with jQuery 1.6+. Please make sure you are using jQuery 1.6+ in your production environment.</p>
		</div>
		
		<h1>YOUR COMPONENTS:</h1>
		
		<!-- Accordion -->
		<h2 class="demoHeaders">Accordion</h2>
		<div id="accordion">
			<h3>First</h3>
			<div>Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.</div>
			<h3>Second</h3>
			<div>Phasellus mattis tincidunt nibh.</div>
			<h3>Third</h3>
			<div>Nam dui erat, auctor a, dignissim quis.</div>
		</div>
		
		<!-- Autocomplete -->
		<h2 class="demoHeaders">Autocomplete</h2>
		<div>
			<input id="autocomplete" title="type &quot;a&quot;">
		</div>

		
	</jsp:body>
</t:template>