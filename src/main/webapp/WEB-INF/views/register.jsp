<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Register</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="login-container">
        <h1>Register</h1>
        <form action="${pageContext.request.contextPath}/register" method="post">
            <label for="username">Username:</label><br>
            <input type="text" id="username" name="username" required><br><br>

            <label for="email">Email:</label><br>
            <input type="email" id="email" name="email" required><br><br> <!-- Type changed to email for better UX -->

            <label for="password">Password:</label><br>
            <input type="password" id="password" name="password" required><br><br>

            <label for="fullName">Full Name:</label><br>
            <input type="text" id="fullName" name="fullName" required><br><br>

            <input type="submit" value="Register">
        </form>
        <p>Hai già un account? <a href="${pageContext.request.contextPath}/login">Accedi</a></p>
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
