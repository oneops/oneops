<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CMS Class</title>
</head>
<body>
<h3>Class Definition Detail</h3><br/>

	<table border="0" cellpadding="3">
		<tr align="left">
			<th>${clazz.className}</th>
		</tr>
		<tr align="left">
			<td><i>Short Class Name:</i></td>
			<td>${clazz.shortClassName}</td>
		</tr>
		<tr align="left">
			<td><i>Access Level:</i></td>
			<td>${clazz.accessLevel}</td>
		</tr>
		<tr align="left">
			<td><i>Implementation:</i></td>
			<td>${clazz.impl}</td>
		</tr>
		<tr align="left">
			<td><i>Is Namespace:</i></td>
			<td>${clazz.isNamespace}</td>
		</tr>
		<tr align="left">
			<td><i>Use class name in ns:</i></td>
			<td>${clazz.useClassNameNS}</td>
		</tr>
		<tr align="left">
			<td><i>Description:</i></td>
			<td>${clazz.description}</td>
		</tr>
		<tr align="left">
			<td><i>Created:</i></td>
			<td>${clazz.created}</td>
		</tr>
	</table>


	<table border="1" cellpadding="3" cellspacing="0">
		<tr align="left">
			<th>Attributes</th>
			<th>Data Type</th>
			<th>Inherited from</th>
			<th>Is Mandatory</th>
			<th>Is Inheritable</th>
			<th>Default Value</th>
			<th>Value Format</th>
			<th>Description</th>
		</tr>
		<c:forEach var="attr" items="${clazz.mdAttributes}">
		<tr align="left">
			<td>${attr.attributeName}</td>
			<td>${attr.dataType}</td>
			<td><a href="md.do?type=clazz&name=${attr.inheritedFrom}">${attr.inheritedFrom}</a></td>
			<td>${attr.isMandatory}</td>
			<td>${attr.isInheritable}</td>
			<td>${attr.defaultValue}</td>
			<td>${attr.valueFormat}</td>
			<td>${attr.description}</td>
		</tr>
		</c:forEach>
	</table>
    <br/>
    <b>From relations:</b><br/>
		<c:forEach var="fromRel" items="${clazz.fromRelations}">
		<a href="md.do?type=clazz&name=${fromRel.fromClassName}">${fromRel.fromClassName}&nbsp;</a>
		-->&nbsp;<a href="md.do?type=relation&name=${fromRel.relationName}">${fromRel.relationName}</a>&nbsp;(${fromRel.linkType})
		<c:if test="${fromRel.isStrong=='true'}">(strong)</c:if>
		&nbsp;--><a href="md.do?type=clazz&name=${fromRel.toClassName}">${fromRel.toClassName}</a>&nbsp;&nbsp;&nbsp;<i>${fromRel.description}</i>
		<br/>
		</c:forEach>
    
    <br/>
    <b>To relations:</b><br/>
		<c:forEach var="toRel" items="${clazz.toRelations}">
		<a href="md.do?type=clazz&name=${toRel.fromClassName}">${toRel.fromClassName}&nbsp;</a>
		-->&nbsp;<a href="md.do?type=relation&name=${toRel.relationName}">${toRel.relationName}</a>&nbsp;(${toRel.linkType})
		<c:if test="${toRel.isStrong=='true'}">(strong)</c:if>
		&nbsp;--><a href="md.do?type=clazz&name=${toRel.toClassName}">${toRel.toClassName}</a>&nbsp;${toRel.description}
		<br/>
		</c:forEach>
   
</body>
</html>