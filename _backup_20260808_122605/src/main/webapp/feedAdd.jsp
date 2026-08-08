<%@page contentType="text/html" pageEncoding="utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.commons.fileupload.disk.*" %>
<%@ page import="org.apache.commons.fileupload.servlet.*" %>
<%@ page import="util.*" %>
<%@ page import="dao.*" %>
<%
	String uid = null, ucon = null, ufname = null;
	byte [] ufile = null;
	request.setCharacterEncoding("utf-8");
	
	ServletFileUpload sfu = new ServletFileUpload(new DiskFileItemFactory());
	List items = sfu.parseRequest(request);
	Iterator iter = items.iterator();
	while(iter.hasNext()){
		FileItem item = (FileItem) iter.next();
		String name = item.getFieldName();
		if(item.isFormField()){
			String value = item.getString("utf-8");
			if(name.equals("id")) uid = value;
			else if (name.equals("content")) ucon = value;
		}
		else {
			if(name.equals("image")){
				ufname = item.getName(); // 이미지 파일의 이름을 가져옴.
				ufile = item.get(); // 바디 부분(특수문자로 나왔던 그 부분)을 ufile에 저장
				String root = application.getRealPath(java.io.File.separator);
				System.out.println(root); // 이부분은 그냥 내가 추가함. root에는 사진파일이 들어있는 경로가 들어있기 때문에 어디에 있는지 알아보려고 추가.
				FileUtil.saveImage(root, ufname, ufile);
			}
		}
	}
	FeedDAO dao = new FeedDAO();
	if(dao.insert(uid,ucon,ufname) == true){
		response.sendRedirect("main.jsp");
		return;
	}
	else {
		out.print("작성 글의 업로드 중 오류가 발생하였습니다.");
	}
	
%>