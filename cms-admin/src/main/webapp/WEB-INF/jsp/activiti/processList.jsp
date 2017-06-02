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

<p><b>Process Instances</b></p>
<p>In progress</p>
<table border="0" cellpadding="3">
	<tr align="left">
		<th>Id</th>
		<th>ProcessDefinitionId</th>
		<th>StartActivityId</th>
		<th>StartTime</th>
        <th></th>
	</tr>
	<c:forEach var="item" items="${unfinishedProcesses}">
	<tr align="left">
		<td>${item.id}</td>
		<td>${item.processDefinitionId}</td>
		<td>${item.startActivityId}</td>
		<td><f:formatDate type="both" pattern="dd/mm/yy hh:mm:ss" value="${item.startTime}" /></td>
        <td><a href="activityList.do?processInstanceId=${item.id}">Activity</a></td>
	</tr>
	</c:forEach>
</table>
<p>Finished</p>
<table border="0" cellpadding="3">
	<tr align="left">
		<th>Id</th>
		<th>ProcessDefinitionId</th>
		<th>StartActivityId</th>
		<th>EndActivityId</th>
		<th>StartTime</th>
		<th>EndTime</th>
		<th>Duration ,s</th>
        <th></th>
	</tr>
	<c:forEach var="item" items="${finishedProcesses}">
	<tr align="left">
		<td>${item.id}</td>
		<td>${item.processDefinitionId}</td>
		<td>${item.startActivityId}</td>
		<td>${item.endActivityId}</td>
		<td><f:formatDate type="both" pattern="dd/mm/yy hh:mm:ss" value="${item.startTime}" /></td>
		<td><f:formatDate type="both" pattern="dd/mm/yy hh:mm:ss" value="${item.endTime}" /></td>
		<td><f:formatNumber type="number" value="${item.durationInMillis/1000}"/></td>
        <td><a href="activityList.do?processInstanceId=${item.id}">Activity</a></td>
	</tr>
	</c:forEach>
</table>

</body>
</html>