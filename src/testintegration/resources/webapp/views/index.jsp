<%@ page session="false" contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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