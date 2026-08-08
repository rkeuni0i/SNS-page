<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	session.invalidate();
	out.print("<p align=center><br>로그아웃을 완료하였습니다.<br><br>"
		+ "<a href='login.html'><button>로그인하기</button></a></p>");
%>
