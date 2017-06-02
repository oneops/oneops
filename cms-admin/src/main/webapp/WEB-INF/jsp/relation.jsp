<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CMS Relation</title>
</head>
<body>
<h3>Relation Definition Detail</h3><br/>
	<table border="0" cellpadding="3">
		<tr align="left">
			<th>${relation.relationName}</th>
		</tr>
		<tr align="left">
			<td><i>Short Name:</i></td>
			<td>${relation.shortRelationName}</td>
		</tr>
		<tr align="left">
			<td><i>Description:</i></td>
			<td>${relation.description}</td>
		</tr>
		<tr align="left">
			<td><i>Created:</i></td>
			<td>${relation.created}</td>
		</tr>
	</table>

	<table border="1" cellpadding="3" cellspacing="0">
		<tr align="left">
			<th>Attributes</th>
			<th>Data Type</th>
			<th>Is Mandatory</th>
			<th>Default Value</th>
			<th>Value Format</th>
			<th>Description</th>
		</tr>
		<c:forEach var="attr" items="${relation.mdAttributes}">
		<tr align="left">
			<td>${attr.attributeName}</td>
			<td>${attr.dataType}</td>
			<td>${attr.isMandatory}</td>
			<td>${attr.defaultValue}</td>
			<td>${attr.valueFormat}</td>
			<td>${attr.description}</td>
		</tr>
		</c:forEach>
	</table>

    <br/>
    <b>Targets:</b><br/>
		<c:forEach var="target" items="${relation.targets}">
		<a href="md.do?type=clazz&name=${target.fromClassName}">${target.fromClassName}&nbsp;</a>
		-->&nbsp;(${target.linkType})
		<c:if test="${target.isStrong=='true'}">(strong)</c:if>
		&nbsp;--><a href="md.do?type=clazz&name=${target.toClassName}">${target.toClassName}</a>&nbsp;&nbsp;&nbsp;<i>${target.description}</i>
		<br/>
		</c:forEach>
    
    <br/>

	
</body>
</html>