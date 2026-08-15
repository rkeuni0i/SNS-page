<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="domain.User"%>
<%@ page import="exceptions.InvalidCredentialsException"%>
<%@ page import="exceptions.UserNotFoundException"%>
<%@ page import="exceptions.ValidationException"%>
<%@ page import="service.UserService"%>
<%
	request.setCharacterEncoding("utf-8");
	String uid = request.getParameter("id");
	String upass = request.getParameter("ps");
	String errorMessage = null;

	try {
		User user = new UserService().login(uid, upass);

		// 세션 고정(Session Fixation) 공격 방어:
		// 로그인 "전"에 공격자가 심어둔 세션 ID를 그대로 쓰면, 로그인 성공 후에도
		// 공격자가 같은 세션 ID로 피해자 계정에 접근할 수 있다.
		// 인증 성공 시점에 기존 세션을 버리고 새 세션을 발급한다.
		session.invalidate();
		session = request.getSession(true);
		session.setAttribute("id", user.getId());
		session.setAttribute("name", user.getName());

		response.sendRedirect("main.jsp");
		return;
	} catch (UserNotFoundException e) {
		errorMessage = "아이디가 존재하지 않습니다.";
	} catch (InvalidCredentialsException e) {
		errorMessage = "패스워드가 일치하지 않습니다.";
	} catch (ValidationException e) {
		errorMessage = util.HtmlEscaper.escape(e.getMessage());
	}
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
		<div class="message-card is-error">
			<p><%= errorMessage %></p>
			<a href="login.html"><button type="button">돌아가기</button></a>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
