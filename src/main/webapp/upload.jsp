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

    <c:if test="${not empty sessionScope.errorMessage}">
        <p style="color: red;"><c:out value="${sessionScope.errorMessage}" /></p>
        <c:remove var="errorMessage" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.successMessage}">
        <p style="color: green;"><c:out value="${sessionScope.successMessage}" /></p>
        <c:remove var="successMessage" scope="session" />
    </c:if>
</body>
</html>