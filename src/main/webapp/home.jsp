<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  In a real application, you would use the JSTL tag library to prevent XSS.
  For example, to display a user's name, you would use:
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  ...
  <h1>Welcome, <c:out value="${user.name}" />!</h1>
--%>
<html>
<head>
    <title>Home</title>
</head>
<body>
    <h1>Welcome!</h1>
</body>
</html>
