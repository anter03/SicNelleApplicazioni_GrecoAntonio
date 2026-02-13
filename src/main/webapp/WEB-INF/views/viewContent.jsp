<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Visualizza Contenuto</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <a href="${pageContext.request.contextPath}/home" class="back-link">&larr; Torna alla Lista</a>
        <h1>Visualizzazione File: <c:out value="${content.originalName}"/></h1>
        <p>Caricato da: <c:out value="${content.authorUsername}"/></p>
        
        <div class="content-view">
            <pre><c:out value="${contentText}"/></pre>
        </div>
    </div>
</body>
</html>
