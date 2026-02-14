<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Home</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .nav-container {
            margin: 20px 0;
            padding: 10px;
            border-bottom: 1px solid #eee;
        }

        .nav-links {
            display: flex;
            gap: 15px;
            justify-content: flex-start;
            align-items: center;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            padding: 10px 20px;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 600;
            font-size: 14px;
            transition: all 0.3s ease;
            gap: 8px; /* Spazio tra icona e testo */
        }

        /* Bottone Carica */
        .btn-upload {
            background-color: #3498db;
            color: white;
            border: 1px solid #2980b9;
        }

        .btn-upload:hover {
            background-color: #2980b9;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            transform: translateY(-1px);
        }

        /* Bottone Esci */
        .btn-logout {
            background-color: #f8f9fa;
            color: #e74c3c;
            border: 1px solid #e74c3c;
        }

        .btn-logout:hover {
            background-color: #e74c3c;
            color: white;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            transform: translateY(-1px);
        }

        /* Icone */
        .btn i {
            font-size: 1.1em;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Benvenuto, <c:out value="${sessionScope.username != null ? sessionScope.username : sessionScope.email}" />!</h1>

     <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

     <div class="nav-container">
         <div class="nav-links">
             <a href="${pageContext.request.contextPath}/upload" class="btn btn-upload">
                 <i class="fas fa-cloud-upload-alt"></i> Carica i tuoi file
             </a>
             <a href="${pageContext.request.contextPath}/logout" class="btn btn-logout">
                 <i class="fas fa-sign-out-alt"></i> Logout
             </a>
         </div>
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