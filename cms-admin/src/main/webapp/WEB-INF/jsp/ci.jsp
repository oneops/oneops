<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CI</title>
</head>
<body>
<h3>CI Detail</h3><br/>

	<table border="0" cellpadding="3">
		<tr align="left">
			<th>${ci.ciName}</th>
		</tr>
		<tr align="left">
			<td><i>Class Name :</i></td>
			<td><a href="md.do?type=clazz&name=${ci.ciClassName}">${ci.ciClassName}</a></td>
		</tr>
		<tr align="left">
			<td><i>Implementation:</i></td>
			<td>${ci.impl}</td>
		</tr>
		<tr align="left">
			<td><i>NS Path:</i></td>
			<td><a href="#" onclick="document.forms['search-form'].submit(); return false;">${ci.nsPath}</a> </td>
		</tr>
		<tr align="left">
			<td><i>State:</i></td>
			<td>${ci.ciState}</td>
		</tr>
		<tr align="left">
			<td><i>Comments:</i></td>
			<td>${ci.comments}</td>
		</tr>
		<tr align="left">
			<td><i>Created:</i></td>
			<td>${ci.created}</td>
		</tr>
	</table>
	<form action="ci.do" name="search-form">
		<input type="hidden"  name="nspath" value="${ci.nsPath}">
		 <input type="hidden" name="classname" value=""> 
		 <input type="hidden" name="Search" value="Search">
	</form>


	<table border="1" cellpadding="3" cellspacing="0">
		<tr align="left">
			<th>Attributes</th>
			<th>DF Value</th>
			<th>DJ Value</th>
			<th>Comments</th>
			<th>Owner</th>
			<th>Created</th>
			<th>Updated</th>
		</tr>
		<c:forEach var="attr" items="${attributes}">
		<tr align="left">
			<td>${attr.attributeName}</td>
			<td>${attr.dfValue}</td>
			<td>${attr.djValue}</td>
			<td>${attr.comments}</td>
			<td>${attr.owner}</td>
			<td>${attr.created}</td>
			<td>${attr.updated}</td>
		</tr>
		</c:forEach>
	</table>

    <br/>
    <b>From relations:</b><br/>
		<c:forEach var="fromRel" items="${fromRelations}">
		<a href="ci.do?id=${fromRel.fromCiId}">${fromRel.fromCi.ciName}&nbsp;</a>
		-->&nbsp;<a href="relation.do?id=${fromRel.ciRelationId}">${fromRel.relationName}</a>
		&nbsp;--><a href="ci.do?id=${fromRel.toCiId}">${fromRel.toCi.ciName}</a>&nbsp;&nbsp;&nbsp;<i>${fromRel.comments}</i>
		<br/>
		</c:forEach>
    
    <br/>
    <b>To relations:</b><br/>
		<c:forEach var="toRel" items="${toRelations}">
		<a href="ci.do?id=${toRel.fromCiId}">${toRel.fromCi.ciName}&nbsp;</a>
		-->&nbsp;<a href="relation.do?id=${toRel.ciRelationId}">${toRel.relationName}</a>
		&nbsp;--><a href="ci.do?id=${toRel.toCiId}">${toRel.toCi.ciName}</a>&nbsp;${toRel.comments}
		<br/>
		</c:forEach>

</body>
</html>
