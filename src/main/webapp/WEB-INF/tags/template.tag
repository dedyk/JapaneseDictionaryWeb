<%@tag description="Overall Page template" pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@attribute name="header" fragment="true"%>
<%@attribute name="footer" fragment="true"%>
<%@attribute name="pageTitle"%>

<!doctype html>
<html lang="pl">
<head>
	<meta charset="utf-8" />
	
	<title>${pageTitle}</title>
	
	<spring:eval var="useExternalStaticFiles" expression="@applicationProperties.getProperty('use.external.static.files')" />
	
	<c:choose>
      <c:when test="${useExternalStaticFiles == true}">
      	<spring:eval var="staticFilePrefix" expression="@applicationProperties.getProperty('use.external.static.path')" />
      </c:when>

      <c:otherwise>
      	<c:set var="staticFilePrefix" value="${pageContext.request.contextPath}" />
      </c:otherwise>
	</c:choose>
	
	<link href="<c:out value='${staticFilePrefix}' />/css/excite-bike/jquery-ui-1.10.4.custom.css" rel="stylesheet" />
	
	<link href="<c:out value='${staticFilePrefix}' />/css/bootstrap/bootstrap.css" rel="stylesheet" />
	<link href="<c:out value='${staticFilePrefix}' />/css/bootstrap-select/bootstrap-select.css" rel="stylesheet" />
	
	<link href="<c:out value='${staticFilePrefix}' />/css/datatables/jquery.dataTables.css" rel="stylesheet" />
	<link href="<c:out value='${staticFilePrefix}' />/css/datatables/dataTables.bootstrap.css" rel="stylesheet" />
	
	<link href="<c:out value='${staticFilePrefix}' />/css/japanese-dictionary/japanese-dictionary.css" rel="stylesheet" />
	
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery/jquery-1.11.1.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery/jquery-ui-1.10.4.custom.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery-validate/jquery.validate.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery-validate/messages_pl.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/bootstrap/bootstrap.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/bootstrap-select/bootstrap-select.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/datatables/jquery.dataTables.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/datatables/dataTables.bootstrap.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/lazy-line-painter/jquery.lazylinepainter-1.4.1-fm.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/raphael/raphael.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/easeljs/easeljs-0.7.1.min.js"></script>

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
						<td><img src="${staticFilePrefix}/img/japan-flag.png" align="middle" style="margin: 0px 10px 0px 0px" /></td>

						<td style="font-size: 150%;"><spring:message code="template.title.full.japanese"/><br /> <spring:message code="template.title.full.polish"/> </td>
					</tr>
				</table>
			</div>

 			<nav class="navbar navbar-default">
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
							<c:when test="${selectedMenu == 'suggestion'}">
								<li class="active"><a href="#" onclick="goTo('suggestion')"><spring:message code="template.menu.suggestion"/></a></li>
							</c:when>
							<c:otherwise>
								<li><a href="#" onclick="goTo('suggestion')"><spring:message code="template.menu.suggestion"/></a></li>
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
			
			<jdwt:startInfo />

			<div id="content" class="col-md-12">
				<jsp:doBody />
			</div>

			<div class="row footer col-md-12" style="margin-top: 50px; font-size: 90%">
				<hr style="margin-bottom: 5px" />
				
				<spring:eval var="authorEmail" expression="@applicationProperties.getProperty('mail.smtp.to')" />
				
				<spring:message code="template.footer.author"/>&nbsp;<a style="color: #777777" href="mailto:${authorEmail}"><spring:message code="template.footer.author.name"/></a> </br>
								
				<spring:message code="template.footer.android.version1"/>&nbsp;<a style="color: #777777" href="<spring:message code='template.footer.android.version.link'/>"><spring:message code="template.footer.android.version2"/></a>
							
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
