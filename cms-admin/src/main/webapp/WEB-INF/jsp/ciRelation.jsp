<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CMS CI Relation</title>
</head>
<body>
<h3>CI Relation Definition Detail</h3><br/>
	<table border="0" cellpadding="3">
		<tr align="left">
			<th>${relation.relationName}</th>
		</tr>
		<tr align="left">
			<td><i>Relation state:</i></td>
			<td>${relation.relationState}</td>
		</tr>
		<tr align="left">
			<td><i>NS Path:</i></td>
			<td><a href="#" onclick="document.forms['search-form'].submit(); return false;">${relation.nsPath}</a> </td>
		</tr>
		<tr align="left">
			<td><i>Relation GoId:</i></td>
			<td>${relation.relationGoid}</td>
		</tr>
		<tr align="left">
			<td><i>Comments:</i></td>
			<td>${relation.comments}</td>
		</tr>
		<tr align="left">
			<td><i>Created:</i></td>
			<td>${relation.created}</td>
		</tr>
		<tr align="left">
			<td><i>Updated:</i></td>
			<td>${relation.updated}</td>
		</tr>
	</table>
	<form action="ci.do" name="search-form">
		<input type="hidden"  name="nspath" value="${relation.nsPath}">
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
    <br/>

	
</body>
</html>