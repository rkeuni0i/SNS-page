<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	// 마이페이지 = 자기 자신의 프로필. 화면을 따로 두지 않고 profile.jsp를 재사용해
	// 프로필 렌더링/팔로우 로직이 두 곳에서 따로 관리되는 것을 막는다.
	String myId = (String) session.getAttribute("id");
	if (myId == null) {
		response.sendRedirect("login.html");
		return;
	}
	response.sendRedirect("profile.jsp?id=" + java.net.URLEncoder.encode(myId, "UTF-8"));
%>
