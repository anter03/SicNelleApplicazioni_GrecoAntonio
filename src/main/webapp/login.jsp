<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Login</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="login-container">
        <h1>Login</h1>
        <form action="${pageContext.request.contextPath}/login" method="post">
            <label for="email">Email:</label><br>
            <input type="text" id="email" name="email" required><br><br>
            <label for="password">Password:</label><br>
            <input type="password" id="password" name="password" required><br><br>
            <input type="submit" value="Login">
        </form>
        <c:if test="${not empty sessionScope.errorMessage}">
            <p class="error-message"><c:out value="${sessionScope.errorMessage}" /></p>
            <c:remove var="errorMessage" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.successMessage}">
            <p class="success-message"><c:out value="${sessionScope.successMessage}" /></p>
            <c:remove var="successMessage" scope="session" />
        </c:if>
    </div>
</body>
</html>
