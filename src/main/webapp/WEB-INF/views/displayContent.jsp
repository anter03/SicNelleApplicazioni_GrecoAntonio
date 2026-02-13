<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Uploaded Content</title>
</head>
<body>
    <h1>Uploaded Content</h1>

    <c:if test="${not empty requestScope.errorMessage}">
        <p style="color: red;"><c:out value="${requestScope.errorMessage}" /></p>
    </c:if>

    <c:choose>
        <c:when test="${not empty requestScope.contents}">
            <ul>
                <c:forEach var="content" items="${requestScope.contents}">
                    <li>
                        <strong>Filename:</strong> <c:out value="${content.filename}" /><br>
                        <strong>Upload Time:</strong> <fmt:formatDateTime value="${content.uploadTime}" pattern="yyyy-MM-dd HH:mm:ss" /><br>
                        <strong>Content:</strong><br>
                        <pre><c:out value="${content.contentText}" /></pre>
                        <hr>
                    </li>
                </c:forEach>
            </ul>
        </c:when>
        <c:otherwise>
            <p>No content uploaded yet.</p>
        </c:otherwise>
    </c:choose>

    <p><a href="${pageContext.request.contextPath}/upload.jsp">Upload more content</a></p>
</body>
</html>
