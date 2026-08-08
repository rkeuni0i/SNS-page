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
		out.print("<p align=center><br>아이디가 존재하지 않습니다.<br><br>"
			+ "<a href='login.html'><button>돌아가기</button></a></p>");
	} catch (InvalidCredentialsException e) {
		out.print("<p align=center><br>패스워드가 일치하지 않습니다.<br><br>"
			+ "<a href='login.html'><button>돌아가기</button></a></p>");
	} catch (ValidationException e) {
		out.print("<p align=center><br>" + util.HtmlEscaper.escape(e.getMessage()) + "<br><br>"
			+ "<a href='login.html'><button>돌아가기</button></a></p>");
	}
%>
