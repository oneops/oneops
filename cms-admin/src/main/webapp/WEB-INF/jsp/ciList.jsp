<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Ci list</title>
</head>
<body>
	<form>
	<table border="0" cellpadding="1">
		<tr>
		<td>NS path:</td><td><input type="text" name="nspath" value="${nspath}"></td>
		<tr>
		</tr>
		<tr>
		<td>Class:</td><td><input type="text" name="classname" value="${classname}"></td>
		</tr>
		<tr>
		<td>CI name:</td><td><input type="text" name="ciname" value="${ciname}"></td>
		</tr>
		<tr>
		<td><input type="submit" name="Search" value="Search"></td>
		</tr>
	</table>
	</form>

	<table border="0" cellpadding="3">
		<tr align="left">
			<th>Ci list</th>
		</tr>
		<c:forEach var="ci" items="${cilist}">
		<tr align="left">
			<td><a href="ci.do?id=${ci.ciId}">${ci.ciName}</a></td>
			<td>${ci.comments}</td>
		</tr>
		</c:forEach>
	</table>
	
</body>
</html>