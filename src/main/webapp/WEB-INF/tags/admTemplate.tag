<%@tag description="Overall Page template" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@attribute name="header" fragment="true"%>
<%@attribute name="footer" fragment="true"%>
<%@attribute name="pageTitle"%>
<%@attribute name="pageDescription" required="false"%>

<!doctype html>
<html lang="pl">
<head>
	<meta charset="utf-8" />
	
	<meta name="keywords" content="<spring:message code="template.keywords"/>" />
	
	<c:if test="${pageDescription != null}">
	<meta name="description" content="${pageDescription}" />
	</c:if>
	
	<c:if test="${metaRobots != null}">
	<meta name="robots" content="${metaRobots}" />
	</c:if>	
	
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
	
	<link rel="icon" type="image/png" href="${staticFilePrefix}/img/favicon.png"/>
	
	<link href="<c:out value='${staticFilePrefix}' />/css/excite-bike/jquery-ui-1.10.4.custom.min.css" rel="stylesheet" />
	
	<link href="<c:out value='${staticFilePrefix}' />/css/bootstrap/bootstrap.min.css" rel="stylesheet" />
	<link href="<c:out value='${staticFilePrefix}' />/css/bootstrap-select/bootstrap-select.min.css" rel="stylesheet" />
	
	<link href="<c:out value='${staticFilePrefix}' />/css/datatables/jquery.dataTables.min.css" rel="stylesheet" />
	<link href="<c:out value='${staticFilePrefix}' />/css/datatables/dataTables.bootstrap.css" rel="stylesheet" />
	
	<link href="<c:out value='${staticFilePrefix}' />/css/japanese-dictionary/japanese-dictionary.css" rel="stylesheet" />
	
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery/jquery-1.11.1.min.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery/jquery-ui-1.10.4.custom.min.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery-validate/jquery.validate.min.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/jquery-validate/messages_pl.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/bootstrap/bootstrap.min.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/bootstrap-select/bootstrap-select.min.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/datatables/jquery.dataTables.min.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/datatables/dataTables.bootstrap.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/lazy-line-painter/jquery.lazylinepainter-1.4.1-fm.js"></script>
	<script src="<c:out value='${staticFilePrefix}' />/js/raphael/raphael-min.js"></script>
	
	<script src="<c:out value='${staticFilePrefix}' />/js/easeljs/easeljs-0.7.1.min.js"></script>

	<style>
		body {
			margin-top: 50px;
			margin-bottom: 50px;
		}
	</style>
</head>

<body>
	
	<div class="container">

		<div class="row">
			
			<div class="row header" style="margin-bottom: 30px;">

				<table style="width: 100%">
					<tr>
						<td style="width: 5%"><img src="${staticFilePrefix}/img/japan-flag.png" align="middle" style="margin: 0px 10px 0px 0px" /></td>

						<td style="font-size: 150%; width: 80%"><spring:message code="template.title.full.japanese"/><br /> <spring:message code="template.title.full.polish"/> </td>						
					</tr>				
				</table>

			</div>
			
 			<nav class="navbar navbar-default">
				<div class="navbar-header">
					<a href="<c:out value='${pageContext.request.contextPath}' />/" class="navbar-brand"><spring:message code="template.title.short.polish"/></a>
				</div>
				<div id="navbarCollapse" class="collapse navbar-collapse">
					<ul class="nav navbar-nav">

						<c:choose>
							<c:when test="${selectedMenu == 'panel'}">
								<li class="active"><a href="<c:out value='${pageContext.request.contextPath}' />/adm/panel"><spring:message code="admTemplate.menu.panel"/></a></li>
							</c:when>
							<c:otherwise>
								<li><a href="<c:out value='${pageContext.request.contextPath}' />/adm/panel"><spring:message code="admTemplate.menu.panel"/></a></li>
							</c:otherwise>
						</c:choose>

						<li><a href="<c:out value='${pageContext.request.contextPath}' />/adm/generateDailyReport"><spring:message code="admTemplate.menu.generateDailyReport"/></a></li>
						
						<li><a href="<c:out value='${pageContext.request.contextPath}' />/adm/showCurrentDailyReport"><spring:message code="admTemplate.menu.showCurrentDailyReport"/></a></li>
						
						<li><a href="<c:url value="j_spring_security_logout" />"><spring:message code="admTemplate.menu.logout"/></a></li>
					</ul>
				</div>
			</nav>
			
			<div id="content" class="col-md-12">			
				<jsp:doBody />
			</div>

			<div class="row footer col-md-12" style="margin-top: 50px; font-size: 90%">
				<hr style="margin-bottom: 5px" />
				
				<spring:eval var="appVersion" expression="@applicationProperties.getProperty('app.version')" />
				<spring:message code="template.footer.app.version"/>&nbsp;${appVersion} <br/>
				
				<spring:eval var="googlePlusAuthorId" expression="@applicationProperties.getProperty('google.plus.author.id')" />
				
				<spring:message code="template.footer.author"/>&nbsp;<a style="color: #777777" href="https://plus.google.com/${googlePlusAuthorId}?rel=author"><spring:message code="template.footer.author.name"/></a> <br/>
								
				<spring:message code="template.footer.android.version1"/>&nbsp;<a style="color: #777777" href="<spring:message code='template.footer.android.version.link'/>"><spring:message code="template.footer.android.version2"/></a> <br/> <br/>				
				
				<c:set var="wordDictionaryUrl"><c:out value='${pageContext.request.contextPath}' />/wordDictionaryCatalog/1</c:set>
				<c:set var="wordDictionaryNameUrl"><c:out value='${pageContext.request.contextPath}' />/wordDictionaryNameCatalog/1</c:set>
				<c:set var="kanjiDictionaryUrl"><c:out value='${pageContext.request.contextPath}' />/kanjiDictionaryCatalog/1</c:set>
				
				<a style="color: #777777" href="${wordDictionaryUrl}"><spring:message code="template.footer.catalog.word"/></a> -
				<a style="color: #777777" href="${wordDictionaryNameUrl}"><spring:message code="template.footer.catalog.wordname"/></a> -
				<a style="color: #777777" href="${kanjiDictionaryUrl}"><spring:message code="template.footer.catalog.kanji"/></a>
			</div>
		</div>
	</div>
		
	<script>
		$( "#nav" ).menu( {position: {at: "left bottom"}});		

		$().ready(function() {
			
		});			
	</script>
</body>
</html>
