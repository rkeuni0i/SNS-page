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
	<div class="page-hdr"><a href="main.jsp">MySNS</a></div>
	<div class="page-body">
		<div class="container">
			<h1 class="user-list-title">가입자 리스트</h1>
<%
	for (User user : users) {
		String when = (user.getCreatedAt() != null) ? user.getCreatedAt().format(fmt) : "";
		String initial = (user.getName() != null && !user.getName().isEmpty())
				? HtmlEscaper.escape(user.getName().substring(0, 1))
				: "?";
%>
			<a class="user-row" href="profile.jsp?id=<%= java.net.URLEncoder.encode(user.getId(), "UTF-8") %>">
				<div class="avatar"><%= initial %></div>
				<div class="user-info">
					<div class="user-name"><%= HtmlEscaper.escape(user.getName()) %></div>
					<div class="user-id"><%= HtmlEscaper.escape(user.getId()) %></div>
				</div>
				<div class="user-joined"><%= when %></div>
			</a>
<%
	}
%>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
