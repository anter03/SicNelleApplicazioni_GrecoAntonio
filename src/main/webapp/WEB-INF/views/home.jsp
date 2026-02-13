<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Home</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <h1>Benvenuto, <c:out value="${sessionScope.username != null ? sessionScope.username : sessionScope.email}" />!</h1>

        <div class="nav-links">
            <p>
                <a href="${pageContext.request.contextPath}/upload" class="upload-link button">Carica i tuoi file</a>
                | 
                <a href="${pageContext.request.contextPath}/logout">Esci</a>
            </p>
        </div>

        <c:if test="${not empty sessionScope.successMessage}">
            <p class="success-message"><c:out value="${sessionScope.successMessage}" /></p>
            <c:remove var="successMessage" scope="session" />
        </c:if>
        <c:if test="${not empty requestScope.errorMessage}">
            <p class="error-message"><c:out value="${requestScope.errorMessage}" /></p>
            <c:remove var="errorMessage" scope="request" />
        </c:if>

        <h2>I tuoi file caricati</h2>

        <c:choose>
            <c:when test="${not empty requestScope.userContents}">
                <table class="content-table">
                    <thead>
                        <tr>
                            <th>Nome File</th>
                            <th>Autore</th>
                            <th>Tipo</th>
                            <th>Data di Caricamento</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="content" items="${requestScope.userContents}">
                            <tr>
                                <td><c:out value="${content.originalName}" /></td>
                                <td><c:out value="${content.authorUsername}" /></td>
                                <td><c:out value="${content.mimeType}" /></td>
                                <td><c:out value="${content.createdAt}" /></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/viewContent?id=${content.id}">Visualizza</a> |
                                    <a href="${pageContext.request.contextPath}/download?id=${content.id}">Scarica</a> |
                                    <form action="${pageContext.request.contextPath}/delete" method="post" style="display:inline;" onsubmit="return confirm('Sei sicuro di voler eliminare questo file?');">
                                        <input type="hidden" name="id" value="${content.id}">
                                        <button type="submit" class="link-button">Elimina</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p>Non hai ancora caricato alcun file.</p>
            </c:otherwise>
        </c:choose>

        <h2>File caricati da altri utenti</h2>

        <c:choose>
            <c:when test="${not empty requestScope.otherUsersContents}">
                <table class="content-table">
                    <thead>
                        <tr>
                            <th>Nome File</th>
                            <th>Autore</th>
                            <th>Tipo</th>
                            <th>Data di Caricamento</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="content" items="${requestScope.otherUsersContents}">
                            <tr>
                                <td><c:out value="${content.originalName}" /></td>
                                <td><c:out value="${content.authorUsername}" /></td>
                                <td><c:out value="${content.mimeType}" /></td>
                                <td><c:out value="${content.createdAt}" /></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/viewContent?id=${content.id}">Visualizza</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p>Nessun file caricato da altri utenti.</p>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>