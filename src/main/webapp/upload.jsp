<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Carica File</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <h1>Carica File di Testo</h1>
        <a href="${pageContext.request.contextPath}/home" class="back-link">&larr; Torna alla Home</a>
        <form action="${pageContext.request.contextPath}/upload" method="post" enctype="multipart/form-data">
            <input type="file" name="file" accept=".txt"><br><br>
            <input type="submit" value="Carica">
        </form>

        <c:if test="${not empty requestScope.errorMessage}">
            <p class="error-message feedback-message"><c:out value="${requestScope.errorMessage}" /></p>
        </c:if>
        <c:if test="${not empty requestScope.successMessage}">
            <p class="success-message feedback-message"><c:out value="${requestScope.successMessage}" /></p>
        </c:if>
    </div>
</body>
</html>