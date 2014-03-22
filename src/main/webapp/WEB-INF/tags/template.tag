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
	
	<link href="<c:out value='${pageContext.request.contextPath}' />/css/excite-bike/jquery-ui-1.10.4.custom.css" rel="stylesheet">
	
	<link href="<c:out value='${pageContext.request.contextPath}' />/css/bootstrap/bootstrap.css" rel="stylesheet">
	<link href="<c:out value='${pageContext.request.contextPath}' />/css/bootstrap-select/bootstrap-select.css" rel="stylesheet">
	
	<link href="<c:out value='${pageContext.request.contextPath}' />/css/datatables/jquery.dataTables.css" rel="stylesheet">
	<link href="<c:out value='${pageContext.request.contextPath}' />/css/datatables/dataTables.bootstrap.css" rel="stylesheet">
	
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/jquery/jquery-1.11.0.js"></script>
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/jquery/jquery-ui-1.10.4.custom.js"></script>
	
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/bootstrap/bootstrap.js"></script>
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/bootstrap-select/bootstrap-select.js"></script>
	
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/datatables/jquery.dataTables.js"></script>
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/datatables/dataTables.bootstrap.js"></script>
	
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/lazy-line-painter/jquery.lazylinepainter-1.4.1-fm.js"></script>
	<script src="<c:out value='${pageContext.request.contextPath}' />/js/raphael/raphael.js"></script>

<style>
	body {
		margin: 50px;
	}
</style>

</head>

<body>
	<div class="container">

		<div class="row">

			<div class="row header" style="margin-bottom: 30px;">

				<table>
					<tr>
						<td><img src="${pageContext.request.contextPath}/img/japan-flag.png" align="middle" style="margin: 0px 10px 0px 0px"></td>

						<td style="font-size: 150%;"><spring:message code="template.title.full.japanese"/><br /> <spring:message code="template.title.full.polish"/> </td>
					</tr>
				</table>
			</div>

			<nav role="navigation" class="navbar navbar-default">
				<div class="navbar-header">
					<a href="#" class="navbar-brand" onclick="goTo('')"><spring:message code="template.title.short.polish"/></a>
				</div>
				<div id="navbarCollapse" class="collapse navbar-collapse">
					<ul class="nav navbar-nav">
						<c:choose>
							<c:when test="${selectedMenu == 'wordDictionary'}">
								<li class="active"><a href="#" onclick="goTo('wordDictionary')"><spring:message code="template.menu.dictionary"/></a></li>
							</c:when>
							<c:otherwise>
								<li><a href="#" onclick="goTo('wordDictionary')"><spring:message code="template.menu.dictionary"/></a></li>
							</c:otherwise>
						</c:choose>

						<c:choose>
							<c:when test="${selectedMenu == 'kanjiDictionary'}">
								<li class="active"><a href="#" onclick="goTo('kanjiDictionary')"><spring:message code="template.menu.kanji"/></a></li>
							</c:when>
							<c:otherwise>
								<li><a href="#" onclick="goTo('kanjiDictionary')"><spring:message code="template.menu.kanji"/></a></li>
							</c:otherwise>
						</c:choose>

						<c:choose>
							<c:when test="${selectedMenu == 'suggetion'}">
								<li class="active"><a href="#" onclick="goTo('suggetion')"><spring:message code="template.menu.suggestion"/></a></li>
							</c:when>
							<c:otherwise>
								<li><a href="#" onclick="goTo('suggetion')"><spring:message code="template.menu.suggestion"/></a></li>
							</c:otherwise>
						</c:choose>

						<c:choose>
							<c:when test="${selectedMenu == 'info'}">
								<li class="active"><a href="#" onclick="goTo('info')"><spring:message code="template.menu.information"/></a></li>
							</c:when>
							<c:otherwise>
								<li><a href="#" onclick="goTo('info')"><spring:message code="template.menu.information"/></a></li>
							</c:otherwise>
						</c:choose>
					</ul>
				</div>
			</nav>

			<div id="content">
				<jsp:doBody />
			</div>

			<div class="row footer" style="margin-top: 50px;">
				<b>FIXME: FOOTER</b>
			</div>
		</div>
	</div>

	<script>
		$( "#nav" ).menu( {position: {at: "left bottom"}});

		function goTo(component) {
			window.location = "<c:out value='${pageContext.request.contextPath}' />" + "/" + component;
		}
		
	</script>
</body>
</html>
