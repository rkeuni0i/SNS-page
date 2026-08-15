<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ page import="exceptions.DataAccessException"%>
<%
	// 스택 트레이스를 화면에 그대로 노출하면 내부 구조/쿼리/경로가 새어나간다.
	// 상세 원인은 서버 로그에만 남기고, 사용자에게는 이해 가능한 문구만 보여준다.
	if (exception != null) {
		application.log("[mysns] Unhandled exception at " + request.getRequestURI(), exception);
	}

	String userMessage = (exception instanceof DataAccessException)
			? "일시적인 서버 오류로 요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요."
			: "요청을 처리하는 중 오류가 발생했습니다.";
%>
<html>
<head>
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/core.css">
	<title>MySNS - 오류</title>
</head>
<body>
	<div class="page-hdr"><a href="main.jsp">MySNS</a></div>
	<div class="page-body">
		<div class="message-card is-error">
			<p><%= userMessage %></p>
			<a href="main.jsp"><button type="button">메인으로</button></a>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
