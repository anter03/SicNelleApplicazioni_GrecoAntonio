<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Home</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <h1>Welcome, <c:out value="${sessionScope.username != null ? sessionScope.username : sessionScope.email}" />!</h1>

        <p><a href="${pageContext.request.contextPath}/logout">Logout</a></p>

        <c:if test="${not empty sessionScope.successMessage}">
            <p class="success-message"><c:out value="${sessionScope.successMessage}" /></p>
            <c:remove var="successMessage" scope="session" />
        </c:if>
        <c:if test="${not empty requestScope.errorMessage}">
            <p class="error-message"><c:out value="${requestScope.errorMessage}" /></p>
            <c:remove var="errorMessage" scope="request" />
        </c:if>

        <h2>Your Uploaded Files</h2>

        <c:choose>
            <c:when test="${not empty requestScope.contents}">
                <table class="content-table">
                    <thead>
                        <tr>
                            <th>File Name</th>
                            <th>Type</th>
                            <th>Upload Time</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="content" items="${requestScope.contents}">
                            <tr>
                                <td><c:out value="${content.originalName}" /></td>
                                <td><c:out value="${content.mimeType}" /></td>
                                <td><c:out value="${content.createdAt}" /></td>
                                <td>
                                    <!-- Action Links (Placeholders) -->
                                    <a href="#">View</a> |
                                    <a href="#">Download</a> |
                                    <a href="#">Delete</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p>You have not uploaded any files yet.</p>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
