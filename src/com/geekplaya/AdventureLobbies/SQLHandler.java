package com.geekplaya.AdventureLobbies;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;

public class SQLHandler {

	/**
	 * The most annoying thing in the world...
	 */	
	private AdventureLobbies server;
	public void fetchClasses() {
		server = AdventureLobbies.plugin;
	}
	
	public void connect() {
		close();
		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName);
			String url = "jdbc:mysql://"; 
			server.sqlConnection = DriverManager.getConnection(url, "", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			DbUtils.close(server.sqlConnection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void update(String query){
		connect();
		Statement st = null;
		try {
			st = server.sqlConnection.createStatement();
			String sql = query;
			st.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
	}

	public ResultSet query(String query){
		connect();
		ResultSet rs = null;
		Statement st = null;
		try {
			st = server.sqlConnection.createStatement();
			String sql = query;
			rs = st.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
		return rs;
	}
	
	public int numRows(String query) {
		connect();
		Statement st = null;
		ResultSet rs = null;
		int num = 0;
		try {
			st = server.sqlConnection.createStatement();
		    rs = st.executeQuery(query);
		    while (rs.next()) {}	
		    rs.last();
		    num = rs.getRow();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnection);
		}
		return num;
	}

	
}