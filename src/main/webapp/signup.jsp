<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="exceptions.DuplicateUserException"%>
<%@ page import="exceptions.ValidationException"%>
<%@ page import="service.UserService"%>
<%@ page import="util.HtmlEscaper"%>
<%
	request.setCharacterEncoding("utf-8");

	String uid = request.getParameter("id");
	String upass = request.getParameter("ps");
	String upass2 = request.getParameter("ps2");   // 예전에는 폼에만 있고 서버에서 검사하지 않았다
	String uname = request.getParameter("name");

	boolean success;
	String message;
	String backHref;
	try {
		new UserService().signUp(uid, upass, upass2, uname);
		success = true;
		message = "회원가입이 완료되었습니다.";
		backHref = "login.html";
	} catch (DuplicateUserException e) {
		success = false;
		message = "이미 가입한 회원입니다.";
		backHref = "signup.html";
	} catch (ValidationException e) {
		success = false;
		message = HtmlEscaper.escape(e.getMessage());
		backHref = "signup.html";
	}
%>
<html>
<head>
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/core.css">
	<title>MySNS 회원가입</title>
</head>
<body>
	<div class="page-hdr"><a href="main.jsp">MySNS</a></div>
	<div class="page-body">
		<div class="message-card<%= success ? "" : " is-error" %>">
			<p><%= message %></p>
			<a href="<%= backHref %>"><button type="button"><%= success ? "로그인하기" : "돌아가기" %></button></a>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
