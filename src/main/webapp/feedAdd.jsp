<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.apache.commons.fileupload.FileItem"%>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@ page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@ page import="exceptions.ValidationException"%>
<%@ page import="service.FeedService"%>
<%@ page import="util.HtmlEscaper"%>
<%
	// [보안 수정] 예전에는 작성자 아이디를 폼의 텍스트 입력(name="id")에서 받았다.
	// 그 값을 바꾸면 아무나 타인을 사칭해 글을 쓸 수 있었다.
	// 작성자는 반드시 서버가 세션에서 판단한다.
	String uid = (String) session.getAttribute("id");
	if (uid == null) {
		response.sendRedirect("login.html");
		return;
	}

	String ucon = null, ufname = null;
	byte[] ufile = null;
	String errorMessage = null;

	DiskFileItemFactory factory = new DiskFileItemFactory();
	ServletFileUpload sfu = new ServletFileUpload(factory);
	sfu.setHeaderEncoding("utf-8");
	sfu.setFileSizeMax(5L * 1024 * 1024);      // 파일 1개 최대 5MB
	sfu.setSizeMax(10L * 1024 * 1024);         // 요청 전체 최대 10MB

	try {
		List<FileItem> items = sfu.parseRequest(request);
		for (Iterator<FileItem> iter = items.iterator(); iter.hasNext(); ) {
			FileItem item = iter.next();
			String name = item.getFieldName();
			if (item.isFormField()) {
				if ("content".equals(name)) ucon = item.getString("utf-8");
			} else if ("image".equals(name) && item.getSize() > 0) {
				ufname = item.getName();
				ufile = item.get();
			}
		}

		String webAppRoot = application.getRealPath(java.io.File.separator);
		new FeedService().publish(uid, ucon, webAppRoot, ufname, ufile);

		response.sendRedirect("main.jsp");
		return;

	} catch (ValidationException e) {
		errorMessage = HtmlEscaper.escape(e.getMessage());
	} catch (java.io.IOException e) {
		errorMessage = HtmlEscaper.escape(e.getMessage());
	} catch (org.apache.commons.fileupload.FileUploadException e) {
		errorMessage = "업로드 용량 제한을 초과했습니다.";
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
			<a href="feedAdd.html"><button type="button">돌아가기</button></a>
		</div>
	</div>
	<div class="page-footer">Copyright: mysns.com, 202x</div>
</body>
</html>
