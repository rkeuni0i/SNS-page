<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="util.*"%>
<%@ page import="dao.*"%>
<%

	ArrayList<UserObj> users = (new UserDAO()).getList();
	String str = "<table align = center>";
	str += "<tr><th colspan=3>가입자 리스트</th><td>";
	
	for(UserObj user : users){
		str += "<tr><td colspan=3><hr></td></tr>";
		str +="<tr>";
		str += "<td>" + user.getId() + "</td>";
		str += "<td>"+ user.getName() + "</td>";
		str += "<td>&nbsp;<small>(" + user.getTs() + ")</small></td>";
		str += "</tr>";
	}
	str += "</table>";
	out.print(str);
%>