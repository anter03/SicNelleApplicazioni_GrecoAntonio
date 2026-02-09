<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Register</title>
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>
<body>
    <h1>Register</h1>
    <form action="${pageContext.request.contextPath}/register" method="post">
        <label for="username">Username:</label><br>
        <input type="text" id="username" name="username" required><br><br>
        <label for="password">Password:</label><br>
        <input type="password" id="password" name="password" required><br><br>
        <div class="g-recaptcha" data-sitekey="YOUR_RECAPTCHA_SITE_KEY"></div><br>
        <input type="submit" value="Register">
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
