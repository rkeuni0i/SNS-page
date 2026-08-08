package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;

import util.ConnectionPool;

public class FeedDAO {
	public boolean insert(String uid, String ucon, String ufile) throws NamingException, SQLException {
		Connection conn = ConnectionPool.get(); // 커넥션 풀 사용해서 사용자 접속 시 가용 메모리 줌.
		PreparedStatement stmt = null;
		try {			
			String sql = "INSERT INTO feed(id, content, images) VALUES(?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, uid);
			stmt.setString(2, ucon);
			stmt.setString(3, ufile);
			
			int count = stmt.executeUpdate();
			return (count ==1) ? true: false; 
			
		} finally {
			if ( stmt!= null) stmt.close(); 
			if (conn!= null) conn.close();
		}
	}
 public ArrayList<FeedObj> getList() throws SQLException, NamingException {
	 	Connection conn = ConnectionPool.get(); 
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM feed ORDER BY ts DESC;"; 
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			ArrayList<FeedObj> feeds = new ArrayList<FeedObj>();
			while(rs.next()) {
				feeds.add(new FeedObj(rs.getString("id"), rs.getString("content"), rs.getString("ts"), rs.getString("images")));// 여기서 images는 DB에서 저장된 레코드 이름이랑 같아야함.  
			}
			return feeds;			
		} finally {
			if (rs!= null) rs.close();
			if ( stmt!= null) stmt.close(); 
			if (conn!= null) conn.close();
		}
 }
}
