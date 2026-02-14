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

        <%--
            GESTIONE FEEDBACK (RF8):
            Usiamo i tag c:if senza specificare lo scope (es. sessionScope).
            In questo modo JSTL cercherà il messaggio sia nella Request che nella Session.
        --%>

        <%-- Messaggio di Errore (tipicamente da req.setAttribute via forward) --%>
        <c:if test="${not empty errorMessage}">
            <p class="error-message">
                <c:out value="${errorMessage}" />
            </p>
        </c:if>

        <%-- Messaggio di Successo (tipicamente da session.setAttribute via redirect) --%>
        <c:if test="${not empty successMessage}">
            <p class="success-message">
                <c:out value="${successMessage}" />
            </p>
            <%-- Fondamentale rimuoverlo dalla sessione per non farlo riapparire al refresh --%>
            <c:remove var="successMessage" scope="session" />
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <label for="username">Username:</label><br>
            <input type="text" id="username" name="username" required><br><br>

            <label for="email">Email:</label><br>
            <input type="email" id="email" name="email" required><br><br>

            <label for="password">Password:</label><br>
            <input type="password" id="password" name="password" required><br><br>

            <label for="fullName">Full Name:</label><br>
            <input type="text" id="fullName" name="fullName" required><br><br>

            <input type="submit" value="Register">
        </form>

        <p>Hai già un account? <a href="${pageContext.request.contextPath}/login">Accedi</a></p>
    </div>
</body>
</html>