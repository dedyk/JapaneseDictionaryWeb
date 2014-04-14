<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="jdwt" uri="/WEB-INF/japaneseDictionaryWebTags.tld"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="pageTitle">
	<spring:message code="kanjiDictionary.page.title" />
</c:set>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
	
		<div>		
    		<ul class="nav nav-tabs">
        		<li class="active"><a data-toggle="tab" href="#meaning"> <spring:message code="kanjiDictionary.page.tab.meaning" /> </a></li>
        		<li><a data-toggle="tab" href="#radicals"> <spring:message code="kanjiDictionary.page.tab.radicals" /> </a></li>
        		<li><a data-toggle="tab" href="#strokeCount"> <spring:message code="kanjiDictionary.page.tab.strokeCount" /> </a></li>
    		</ul>
    		
    		<div class="tab-content">
    		
        		<div id="meaning" class="tab-pane fade in active">
            		<h3>Znaczenie</h3>
            		
        		</div>
        
        		<div id="radicals" class="tab-pane fade">
            		<h3>Elementy podstawowe</h3>
        		</div>

        		<div id="strokeCount" class="tab-pane fade">
            		<h3>Liczba kresek</h3>
        		</div>
    		</div>
		</div>	
	
	</jsp:body>
	
</t:template>