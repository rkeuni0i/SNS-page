<%@ page contentType="text/html" pageEncoding="UTF-8"%>

<%@ page import ="dao.*"%>
<%
	request.setCharacterEncoding("utf-8");
	
	String uid = request.getParameter("id");
	 
	// Class.forName("com.mysql.jdbc.Driver");
	// Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysns?serverTimezone=UTC", "root", "1111");
	
	/*Connection conn = ConnectionPool.get();
	String sql = "DELETE FROM user WHERE id = ?";
	PreparedStatement stmt = conn.prepareStatement(sql);
	stmt.setString(1, uid);
	
	int count = stmt.executeUpdate();
	*/
	if(new UserDAO().delete(uid)){
		out.print("회원 탈퇴가 완료되었습니다.");
	}
	else{
		out.print("회원 탈퇴 처리 중 오류가 발생하였습니다.");
	}
	%>