<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Stuck Deployments</title>
</head>
<body>
<h3>CMS Stuck Deployments List</h3><br/>

	<table border="1" cellpadding="3" cellspacing="0">
		<tr align="left">
			<th>Deployment_Id</th>
			<th>Stuck Mins Back</th>
			<th>Stuck At</th>
			<th>Environment</th>
		</tr>
		<c:forEach var="stkdpmt" items="${stuckDpmtColl.cmsStuckDpmts}">
		<tr align="left">
			<td>
				<a href="<%=System.getProperty("oneops.url")%>/r/d/${stkdpmt.deploymentId}">${stkdpmt.deploymentId}</a>
			</td>
			<td>${stkdpmt.stuckMinsBack}</td>
			<td>${stkdpmt.stuckAt}</td>
			<td>
			    <a href="<%=System.getProperty("oneops.url")%>/r/ns?path=${stkdpmt.path}">${stkdpmt.path}</a>
			</td>
		</tr>
		</c:forEach>
	</table>
	
<h3>In Progress Stuck Deployments List</h3><br/>

	<table border="1" cellpadding="3" cellspacing="0">
		<tr align="left">
			<th>Deployment_Id</th>
			<th>Stuck Mins Back</th>
			<th>Stuck At</th>
			<th>Environment</th>
		</tr>
		<c:forEach var="stkdpmt" items="${stuckDpmtColl.inProgressStuckDpmts}">
		<tr align="left">
			<td>
				<a href="<%=System.getProperty("oneops.url")%>/r/d/${stkdpmt.deploymentId}">${stkdpmt.deploymentId}</a>
			</td>
			<td>${stkdpmt.stuckMinsBack}</td>
			<td>${stkdpmt.stuckAt}</td>
			<td>
			    <a href="<%=System.getProperty("oneops.url")%>/r/ns?path=${stkdpmt.path}">${stkdpmt.path}</a>
			</td>
		</tr>
		</c:forEach>
	</table>	

<h3>Paused Stuck Deployments List</h3><br/>

	<table border="1" cellpadding="3" cellspacing="0">
		<tr align="left">
			<th>Deployment_Id</th>
			<th>Stuck Mins Back</th>
			<th>Stuck At</th>
			<th>Environment</th>
		</tr>
		<c:forEach var="stkdpmt" items="${stuckDpmtColl.pausedStuckDpmts}">
		<tr align="left">
			<td>
				<a href="<%=System.getProperty("oneops.url")%>/r/d/${stkdpmt.deploymentId}">${stkdpmt.deploymentId}</a>
			</td>
			<td>${stkdpmt.stuckMinsBack}</td>
			<td>${stkdpmt.stuckAt}</td>
			<td>
			    <a href="<%=System.getProperty("oneops.url")%>/r/ns?path=${stkdpmt.path}">${stkdpmt.path}</a>
			</td>
		</tr>
		</c:forEach>
	</table>	
</body>
</html>