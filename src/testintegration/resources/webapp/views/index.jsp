<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Embedded Jetty: JSP Examples</title>
</head>
<body>

<c:forEach items="${messages}" var="message">
<h1>Message: ${message}</h1>
</c:forEach>

</body>
</html>