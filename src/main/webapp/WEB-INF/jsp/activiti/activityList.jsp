<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Controller Process</title>
</head>
<body>

<p><b>Activities for process: </b>${process.processDefinitionId} started <f:formatDate type="both" pattern="dd/mm/yy hh:mm:ss" value="${process.startTime}" /></p>
<p>In progress</p>
<table border="0" cellpadding="3">
	<tr align="left">
        <th>Id</th>
		<th>Name</th>
		<th>StartTime</th>
		<th>ExecutionId</th>
	</tr>
	<c:forEach var="item" items="${unfinishedActivities}">
	<tr align="left">
        <td>${item.processInstanceId}</td>
		<td>${item.activityName}</td>
		<td><f:formatDate type="both" pattern="dd/mm/yy hh:mm:ss" value="${item.startTime}" /></td>
		<td>${item.executionId}</td>
	</tr>
	</c:forEach>
</table>
<p>Finished</p>
<table border="0" cellpadding="3">
	<tr align="left">
        <th>ProcessInstanceId</th>
		<th>Name</th>
		<th>StartTime</th>
		<th>EndTime</th>
		<th>Duration ,s</th>
		<th>ExecutionId</th>
	</tr>
	<c:forEach var="item" items="${finishedActivities}">
	<tr align="left">
        <td>${item.processInstanceId}</td>
		<td>${item.activityName}</td>
		<td><f:formatDate type="both" pattern="dd/mm/yy hh:mm:ss" value="${item.startTime}" /></td>
		<td><f:formatDate type="both" pattern="dd/mm/yy hh:mm:ss" value="${item.endTime}" /></td>
		<td><f:formatNumber type="number" value="${item.durationInMillis/1000}"/></td>
		<td>${item.executionId}</td>
	</tr>
	</c:forEach>
</table>


</body>
</html>