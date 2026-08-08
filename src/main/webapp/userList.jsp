<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.time.format.DateTimeFormatter"%>
<%@ page import="domain.User"%>
<%@ page import="service.UserService"%>
<%@ page import="util.HtmlEscaper"%>
<%
	// 가입자 목록은 개인정보에 해당하므로 로그인한 사용자에게만 노출한다.
	if (session.getAttribute("id") == null) {
		response.sendRedirect("login.html");
		return;
	}

	List<User> users = new UserService().getAllUsers();
	DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
%>
<html>
<head>
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/core.css">
	<title>MySNS 가입자</title>
</head>
<body>
	<div class="page-hdr">MySNS</div>
	<div class="page-body">
		<table align="center">
			<tr><th colspan="3">가입자 리스트</th></tr>
<%
	for (User user : users) {
		String when = (user.getCreatedAt() != null) ? user.getCreatedAt().format(fmt) : "";
%>
			<tr><td colspan="3"><hr></td></tr>
			<tr>
				<td><%= HtmlEscaper.escape(user.getId()) %></td>
				<td><%= HtmlEscaper.escape(user.getName()) %></td>
				<td>&nbsp;<small>(<%= when %>)</small></td>
			</tr>
<%
	}
%>
		</table>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
