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

	try {
		new UserService().withdraw(uid, upass);
		session.invalidate();
		out.print("<p align=center><br>회원 탈퇴가 완료되었습니다.<br><br>"
			+ "<a href='login.html'><button>처음으로</button></a></p>");
	} catch (InvalidCredentialsException e) {
		out.print("<p align=center><br>비밀번호가 일치하지 않습니다.<br><br>"
			+ "<a href='withdraw.html'><button>돌아가기</button></a></p>");
	} catch (UserNotFoundException e) {
		session.invalidate();
		out.print("<p align=center><br>존재하지 않는 회원입니다.<br><br>"
			+ "<a href='login.html'><button>처음으로</button></a></p>");
	}
%>
