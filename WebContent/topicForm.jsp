<%@ page language="java" contentType="text/html; charset=GB18030"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GB18030">
<title>Insert title here</title>
</head>
<body>
<form method="post" action="/molo/topic/">
<input type="text" name="name" />
<br/><select name="type">
<%
	for(com.modofo.molo.model.SampleType type:com.modofo.molo.model.SampleType.values()){
%>
<option value="<%=type%>"><%=type.description() %></option>
<% } %>
</select>
<br/>
<textarea rows="10" cols="100" name="sampleText"></textarea>
<br/>
<input type="submit"/>
</form>
</body>
</html>