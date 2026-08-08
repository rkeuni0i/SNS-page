<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.time.format.DateTimeFormatter"%>
<%@ page import="domain.Feed"%>
<%@ page import="service.FeedService"%>
<%@ page import="util.HtmlEscaper"%>
<%
	String uid = (String) session.getAttribute("id");
	if (uid == null) {
		response.sendRedirect("login.html");
		return;
	}

	// [실제로 겪었던 문제 — repository.JdbcFeedRepository 주석 참고]
	// 예전에는 이 자리에서 Connection -> PreparedStatement -> ResultSet 을 직접 열어
	// 화면을 그리려 했는데, stmt/conn 이 먼저 닫히는 바람에 rs.next() 에서
	// "ResultSet is closed" 오류가 났다. 지금은 FeedService 가 커넥션이 살아있는
	// 스코프 안에서 결과를 List 로 완전히 옮겨 담아 반환하므로 구조적으로 재발할 수 없다.
	List<Feed> feeds = new FeedService().getRecentFeeds();

	DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=0">
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/core.css">
	<title>MySNS</title>
</head>
<body>
	<div class="page-hdr">MySNS</div>
	<div class="page-body">
		<table align="center">
			<tr height="40">
				<td><b><%= HtmlEscaper.escape(uid) %>님의 피드</b></td>
				<td align="right">
					<a href="feedAdd.html"><button type="button">글쓰기</button></a>
					<a href="logout.jsp"><button type="button">로그아웃</button></a>
				</td>
			</tr>
<%
	for (Feed feed : feeds) {
		// 사용자 입력은 반드시 이스케이프한다. 예전에는 본문을 그대로 이어붙여
		// 저장형 XSS(<script> 삽입) 가 가능했다.
		String imgstr = feed.getImageFileName()
				.map(img -> "<img src=\"images/" + HtmlEscaper.escape(img) + "\" width=\"240\" alt=\"\">")
				.orElse("");
		String when = (feed.getCreatedAt() != null) ? feed.getCreatedAt().format(fmt) : "";
%>
			<tr><td colspan="2"><hr></td></tr>
			<tr>
				<td><small><%= HtmlEscaper.escape(feed.getAuthorId()) %></small></td>
				<td align="right"><small><%= when %></small></td>
			</tr>
			<tr><td colspan="2"><%= imgstr %></td></tr>
			<tr><td colspan="2"><%= HtmlEscaper.escapeWithLineBreaks(feed.getContent()) %></td></tr>
<%
	}
%>
		</table>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
