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

	String message;
	try {
		new UserService().signUp(uid, upass, upass2, uname);
		message = "회원가입이 완료되었습니다.";
		out.print("<p align=center><br>" + message + "<br><br>"
			+ "<a href='login.html'><button>로그인하기</button></a></p>");
	} catch (DuplicateUserException e) {
		out.print("<p align=center><br>이미 가입한 회원입니다.<br><br>"
			+ "<a href='signup.html'><button>돌아가기</button></a></p>");
	} catch (ValidationException e) {
		out.print("<p align=center><br>" + HtmlEscaper.escape(e.getMessage()) + "<br><br>"
			+ "<a href='signup.html'><button>돌아가기</button></a></p>");
	}
%>
