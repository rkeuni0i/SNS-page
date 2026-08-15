<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="exceptions.InvalidCredentialsException"%>
<%@ page import="exceptions.UserNotFoundException"%>
<%@ page import="service.UserService"%>
<%
	request.setCharacterEncoding("utf-8");

	// [보안 수정] 예전에는 요청 파라미터로 받은 아이디를 그대로 삭제했다.
	// 로그인하지 않은 사람도 아이디만 알면 남의 계정을 지울 수 있었다.
	// 이제 세션의 로그인 사용자만, 자기 비밀번호를 확인한 뒤에만 탈퇴할 수 있다.
	String uid = (String) session.getAttribute("id");
	if (uid == null) {
		response.sendRedirect("login.html");
		return;
	}
	String upass = request.getParameter("ps");

	boolean success;
	String message;
	String backHref;
	try {
		new UserService().withdraw(uid, upass);
		session.invalidate();
		success = true;
		message = "회원 탈퇴가 완료되었습니다.";
		backHref = "login.html";
	} catch (InvalidCredentialsException e) {
		success = false;
		message = "비밀번호가 일치하지 않습니다.";
		backHref = "withdraw.html";
	} catch (UserNotFoundException e) {
		session.invalidate();
		success = false;
		message = "존재하지 않는 회원입니다.";
		backHref = "login.html";
	}
%>
<html>
<head>
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/core.css">
	<title>MySNS 회원탈퇴</title>
</head>
<body>
	<div class="page-hdr"><a href="main.jsp">MySNS</a></div>
	<div class="page-body">
		<div class="message-card<%= success ? "" : " is-error" %>">
			<p><%= message %></p>
			<a href="<%= backHref %>"><button type="button"><%= success ? "처음으로" : "돌아가기" %></button></a>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
