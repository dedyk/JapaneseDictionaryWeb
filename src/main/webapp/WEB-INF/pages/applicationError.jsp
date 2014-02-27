<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h4>Błąd aplikacji</h4>
<strong>Exception: ${exception}: ${exception.message}</strong>
<pre>
	<c:forEach items="${exception.stackTrace}" var="stackTrace"> 
		${stackTrace} 
	</c:forEach>
</pre>    
