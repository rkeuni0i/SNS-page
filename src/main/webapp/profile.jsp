<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Optional"%>
<%@ page import="java.time.format.DateTimeFormatter"%>
<%@ page import="domain.User"%>
<%@ page import="domain.Feed"%>
<%@ page import="service.UserService"%>
<%@ page import="service.FollowService"%>
<%@ page import="service.FeedService"%>
<%@ page import="util.HtmlEscaper"%>
<%
	String myId = (String) session.getAttribute("id");
	if (myId == null) {
		response.sendRedirect("login.html");
		return;
	}

	FollowService followService = new FollowService();

	// 팔로우/언팔로우 버튼은 이 페이지 자신에게 POST하는 폼으로 구현했다(PRG 패턴).
	// 처리 후 같은 프로필로 리다이렉트해, 새로고침해도 요청이 중복 전송되지 않는다.
	if ("POST".equalsIgnoreCase(request.getMethod())) {
		String targetId = request.getParameter("targetId");
		String action = request.getParameter("action");
		if (targetId != null) {
			try {
				if ("follow".equals(action)) {
					followService.follow(myId, targetId);
				} else if ("unfollow".equals(action)) {
					followService.unfollow(myId, targetId);
				}
			} catch (exceptions.ValidationException | exceptions.UserNotFoundException e) {
				// 정상 UI로는 도달할 수 없는 경로(자기 자신 팔로우, 삭제된 회원 등)라
				// 화면에 에러를 보여주기보다 조용히 무시하고 프로필로 되돌아간다.
			}
		}
		response.sendRedirect("profile.jsp?id=" + java.net.URLEncoder.encode(targetId, "UTF-8"));
		return;
	}

	String profileId = request.getParameter("id");
	if (profileId == null || profileId.isBlank()) {
		profileId = myId;
	}

	Optional<User> userOpt = new UserService().findById(profileId);
%>
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=0">
	<meta charset="utf-8">
	<link rel="stylesheet" href="css/core.css">
	<title>MySNS 프로필</title>
</head>
<body>
	<div class="page-hdr"><a href="main.jsp">MySNS</a></div>
	<div class="page-body">
		<div class="container">
<%
	if (!userOpt.isPresent()) {
%>
			<div class="message-card is-error">
				<p>존재하지 않는 회원입니다.</p>
				<a href="userList.jsp"><button type="button">가입자 목록으로</button></a>
			</div>
<%
	} else {
		User user = userOpt.get();
		boolean isSelf = myId.equals(user.getId());
		boolean following = !isSelf && followService.isFollowing(myId, user.getId());
		int followerCount = followService.getFollowerCount(user.getId());
		int followingCount = followService.getFollowingCount(user.getId());
		String initial = (user.getName() != null && !user.getName().isEmpty())
				? HtmlEscaper.escape(user.getName().substring(0, 1))
				: "?";
		DateTimeFormatter dfmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String joined = (user.getCreatedAt() != null) ? user.getCreatedAt().format(dfmt) : "";
%>
			<div class="profile-card">
				<div class="avatar profile-avatar"><%= initial %></div>
				<div class="profile-name"><%= HtmlEscaper.escape(user.getName()) %></div>
				<div class="profile-id"><%= HtmlEscaper.escape(user.getId()) %></div>
				<div class="profile-joined">가입일 <%= joined %></div>
				<div class="profile-stats">
					<div class="profile-stat"><span class="stat-num"><%= followerCount %></span><span class="stat-label">팔로워</span></div>
					<div class="profile-stat"><span class="stat-num"><%= followingCount %></span><span class="stat-label">팔로잉</span></div>
				</div>
<%
		if (isSelf) {
%>
				<div class="profile-actions">
					<a href="logout.jsp"><button type="button">로그아웃</button></a>
				</div>
<%
		} else {
%>
				<form class="profile-actions" method="post" action="profile.jsp">
					<input type="hidden" name="targetId" value="<%= HtmlEscaper.escape(user.getId()) %>">
					<input type="hidden" name="action" value="<%= following ? "unfollow" : "follow" %>">
					<button type="submit" class="<%= following ? "btn-outline" : "" %>">
						<%= following ? "팔로잉 취소" : "팔로우" %>
					</button>
				</form>
<%
		}

		List<Feed> feeds = new FeedService().getFeedsOf(user.getId());
%>
			</div>

			<h2 class="user-list-title"><%= isSelf ? "내 피드" : HtmlEscaper.escape(user.getName()) + "님의 피드" %></h2>
<%
		if (feeds.isEmpty()) {
%>
			<div class="empty-state">아직 작성한 피드가 없습니다.</div>
<%
		}
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		for (Feed feed : feeds) {
			String imgstr = feed.getImageFileName()
					.map(img -> "<img class=\"feed-image\" src=\"images/" + HtmlEscaper.escape(img) + "\" alt=\"\">")
					.orElse("");
			String when = (feed.getCreatedAt() != null) ? feed.getCreatedAt().format(fmt) : "";
%>
			<div class="feed-card">
				<div class="feed-meta">
					<span class="feed-author"><%= HtmlEscaper.escape(feed.getAuthorId()) %></span>
					<span class="feed-time"><%= when %></span>
				</div>
				<%= imgstr %>
				<div class="feed-content"><%= HtmlEscaper.escapeWithLineBreaks(feed.getContent()) %></div>
			</div>
<%
		}
	}
%>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
