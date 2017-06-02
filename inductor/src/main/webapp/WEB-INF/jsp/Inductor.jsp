<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Oneops Inductor</title>
</head>
<body>
<h1>Oneops - Inductor</h1>
<br/>
	<table border="0" cellpadding="3" cellspacing="0">
		<tr align="left">
			<td>Is Running</td>
			<td>${status.isRunning}</td>
		</tr>
		<tr align="left">
			<td>Queue Backlog</td>
			<td>${status.queueBacklog}</td>
		</tr>
		<tr align="left">
			<td>Last Run</td>
			<td>${status.lastRun}</td>
		</tr>
	</table>

<br/>
<c:if test="${status.isRunning=='true'}"><a href="pub.do?action=stop">Stop the publisher</a></c:if>
<c:if test="${status.isRunning!='true'}"><a href="pub.do?action=start">Start the publisher</a></c:if>
<br/>
</body>
</html>