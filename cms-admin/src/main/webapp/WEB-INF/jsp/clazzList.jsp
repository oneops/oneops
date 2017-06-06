<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CMS Classes</title>
</head>
<body>

	<table border="0" cellpadding="3">
		<tr align="left">
			<th>Classes</th>
		</tr>
		<c:forEach var="clazz" items="${clazzes}">
		<tr align="left">
			<td><a href="md.do?type=clazz&name=${clazz.className}">${clazz.className}</a></td>
			<td>${clazz.description}</td>
		</tr>
		</c:forEach>
	</table>
	
</body>
</html>