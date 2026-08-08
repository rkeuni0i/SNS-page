<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="util.*"%>
<%@ page import="dao.*"%>

<%
	String uid = (String) session.getAttribute("id");
if(uid == null){
	response.sendRedirect("login.html");
	return;
}
session.setAttribute("id", uid); // session 로그인 지속시간(디폴트 30분) 다시 늘리려고 session 한번 더 호출
	/* rs를 사용하게 되면 rs.next()를 하기 전에 stmt와 conn이 close()하게 되어서 rs.next()할 수 가 없어 에러남. 
	String sql = "SELECT * FROM feed ORDER BY ts DESC;"; 
	
	Connection conn = ConnectionPool.get();
	PreparedStatement stmt = conn.prepareStatement(sql);
	ResultSet rs = stmt.executeQuery();
	*/
	ArrayList<FeedObj> feeds = (new FeedDAO()).getList();
	
	String str = "<table align = center>";
	//str += "<tr><th colspan=2>작성글 리스트</th></tr>"; 
	str += "<tr height=40><td><b>작성글 리스트</b></td>";
	str += "<td align =right>";
	str += "<a href='feedAdd.html'><button>글쓰기</button></a>";
	str += "</td></tr>";
	
	/*for (int i=0; i<feeds.size(); i++){
		FeedObj feed = feeds.get(i);*/
	for (FeedObj feed : feeds){ // 리스트 순회하는 for문 간단하게 작성하는 방법 
		String img = feed.getImages(), imgstr = "";
		if(img!= null){
			imgstr = "<img src=images/" + img + " width =240>"; // **이 부분 src에서 ''으로 묶으면 사진 안뜸(이유 알아보기)**
		} 
		str += "<tr><td colspan=2><hr></td></tr>";
		str += "<tr>";
		str += "<td><small>" + feed.getId() + "</small></td>";
		str += "<td><small>&nbsp;"+ feed.getTs() + "</small></td>";
		str += "</tr>";
		str += "<tr><td colspan=2>" + imgstr + "</td></tr>"; //여기서 imgstr이 가지고 있는 이미지 태그 제대로 나옴. 
		str += "<tr><td colspan=2>" + feed.getContent() + "</td></tr>";
	}
	str += "</table>";
	out.print(str);
	
%>