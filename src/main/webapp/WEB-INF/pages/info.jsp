<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="pageTitle"> <spring:message code="info.page.title"/> </c:set>

<t:template pageTitle="${pageTitle}">

	<jsp:body>
		
		<div class="col-md-12">
		    <div class="col-md-2">
        	</div>
		
			<div class="col-md-8">
		
				<center>
					<p><b></b><spring:message code="info.page.body.text1"/></b></p>
					<p><b><spring:message code="info.page.body.text2"/></b></p>
				</center>
				
				<div align="justify">
					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.text3"/></p>
				
					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.text4"/></p>
					
					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.text5"/></p>
					
					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention1"/></p>
					
					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention2"/>					
						<ul>
	 						<li><a style="color: #777777" href="<spring:message code='info.page.body.attention2.link1'/>"><spring:message code="info.page.body.attention2.link1"/></a></li>
							<li><a style="color: #777777" href="<spring:message code='info.page.body.attention2.link2'/>"><spring:message code="info.page.body.attention2.link2"/></a></li>
							<li><a style="color: #777777" href="<spring:message code='info.page.body.attention2.link3'/>"><spring:message code="info.page.body.attention2.link3"/></a></li>
							<li><a style="color: #777777" href="<spring:message code='info.page.body.attention2.link4'/>"><spring:message code="info.page.body.attention2.link4"/></a></li>
						</ul>
					</p>

					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention3"/>					
						<ul>
	 						<li><a style="color: #777777" href="<spring:message code='info.page.body.attention3.link1'/>"><spring:message code="info.page.body.attention3.link1"/></a></li>
							<li><a style="color: #777777" href="<spring:message code='info.page.body.attention3.link2'/>"><spring:message code="info.page.body.attention3.link2"/></a></li>
						</ul>
					</p>

					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention4"/>					
						<ul>
	 						<li><a style="color: #777777" href="<spring:message code='info.page.body.attention4.link1'/>"><spring:message code="info.page.body.attention4.link1"/></a></li>
							<li><a style="color: #777777" href="<spring:message code='info.page.body.attention4.link2'/>"><spring:message code="info.page.body.attention4.link2"/></a></li>
						</ul>
					</p>

					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention5"/>					
						<ul>
	 						<li><a style="color: #777777" href="<spring:message code='info.page.body.attention5.link1'/>"><spring:message code="info.page.body.attention5.link1"/></a></li>
						</ul>
					</p>

					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention6"/>					
						<ul>
	 						<li><a style="color: #777777" href="<spring:message code='info.page.body.attention6.link1'/>"><spring:message code="info.page.body.attention6.link1"/></a></li>
							<li><a style="color: #777777" href="<spring:message code='info.page.body.attention6.link2'/>"><spring:message code="info.page.body.attention6.link2"/></a></li>
						</ul>
					</p>

					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention7"/>					
						<ul>
	 						<li><a style="color: #777777" href="<spring:message code='info.page.body.attention7.link1'/>"><spring:message code="info.page.body.attention7.link1"/></a></li>
							<li><a style="color: #777777" href="<spring:message code='info.page.body.attention7.link2'/>"><spring:message code="info.page.body.attention7.link2"/></a></li>
						</ul>
					</p>

					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention8"/>					
						<ul>
	 						<li><spring:message code='info.page.body.attention8.source1'/></li>
	 						<li><spring:message code='info.page.body.attention8.source2'/></li>
	 						<li><spring:message code='info.page.body.attention8.source3'/></li>
	 						<li><spring:message code='info.page.body.attention8.source4'/></li>
	 						<li><spring:message code='info.page.body.attention8.source5'/></li>
						</ul>
					</p>

					<p>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="info.page.body.attention9"/>					
						<ul>
	 						<li><a style="color: #777777" href="<spring:message code='info.page.body.attention9.link1'/>"><spring:message code="info.page.body.attention9.link1"/></a></li>
						</ul>
					</p>
				
				</div>
		
				
		
			</div>		
		
			<div class="col-md-2">
        	</div>		
		</div>
	</jsp:body>
</t:template>