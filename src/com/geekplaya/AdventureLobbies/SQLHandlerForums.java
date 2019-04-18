package com.geekplaya.AdventureLobbies;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;

public class SQLHandlerForums {

	/**
	 * The most annoying thing in the world...
	 */	
	private AdventureLobbies server;
	public void fetchClasses() {
		server = AdventureLobbies.plugin;
	}
	
	int bronzeID = 33;
	int silverID = 34;
	int goldID = 35;
	int donorID = 36;
	
	public int getBronzeID() {
		return bronzeID;
	}

	public int getSilverID() {
		return silverID;
	}
	
	public int getGoldID() {
		return goldID;
	}
	
	public int getDonorID() {
		return donorID;
	}
	
	public void connect() {
		close();
		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName);
			String url = "jdbc:mysql:///forums"; 
			server.sqlConnectionForums = DriverManager.getConnection(url, "", """);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void close() {
		try {
			DbUtils.close(server.sqlConnectionForums);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void update(String query){
		connect();
		Statement st = null;
		try {
			st = server.sqlConnectionForums.createStatement();
			String sql = query;
			st.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnectionForums);
		}
	}

	public ResultSet query(String query){
		connect();
		ResultSet rs = null;
		Statement st = null;
		try {
			st = server.sqlConnectionForums.createStatement();
			String sql = query;
			rs = st.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnectionForums);
		}
		return rs;
	}
	
	public int numRows(String query) {
		connect();
		Statement st = null;
		ResultSet rs = null;
		int num = 0;
		try {
			st = server.sqlConnectionForums.createStatement();
		    rs = st.executeQuery(query);
		    while (rs.next()) {}	
		    rs.last();
		    num = rs.getRow();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    DbUtils.closeQuietly(rs);
		    DbUtils.closeQuietly(st);
		    DbUtils.closeQuietly(server.sqlConnectionForums);
		}
		return num;
	}


	
}