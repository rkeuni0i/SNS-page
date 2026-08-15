<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	session.invalidate();
%>
<html>
<head>
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/core.css">
	<title>MySNS</title>
</head>
<body>
	<div class="page-hdr"><a href="main.jsp">MySNS</a></div>
	<div class="page-body">
		<div class="message-card">
			<p>로그아웃을 완료하였습니다.</p>
			<a href="login.html"><button type="button">로그인하기</button></a>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
