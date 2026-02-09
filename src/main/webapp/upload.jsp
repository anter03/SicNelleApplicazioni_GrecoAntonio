<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>File Upload</title>
</head>
<body>
    <h1>Upload Text File</h1>
    <form action="${pageContext.request.contextPath}/upload" method="post" enctype="multipart/form-data">
        <input type="file" name="file" accept=".txt"><br><br>
        <input type="submit" value="Upload">
    </form>

    <c:if test="${not empty requestScope.message}">
        <p><c:out value="${requestScope.message}" /></p>
    </c:if>
</body>
</html>
