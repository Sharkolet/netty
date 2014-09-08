package com.shark.netty;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class DBConnection {
	 static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; 
	 
	 private static String DB_URL;
	 private static String USER;
	 private static String PASS;
	 
	 private Connection conn;
	 private Statement stmt;
 
	 DBConnection() {
		 try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(new File("src/main/resources/db.properties"));
			prop.load(in);
			in.close();
			DB_URL = "jdbc:mysql://localhost/" + prop.getProperty("db_name");
			USER = prop.getProperty("user");
			PASS = prop.getProperty("password");
		    Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL,USER,PASS);
		    stmt = conn.createStatement();
		 } catch(SQLException se) {
			 se.printStackTrace();
		 } catch(Exception e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public void insertData(String ip, String url, String redirect) {
		 String sql = "INSERT INTO connections VALUES ('" + ip + "', '" + url + "', '" + redirect + "', NOW())";
		 try {
			 stmt.executeUpdate(sql);
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
		 close();
	 }
	 
	 public void insertData(String ip, String url) {
		 String sql = "INSERT INTO connections VALUES ('" + ip + "', '" + url + "', NULL, NOW())";
		 try {
			 stmt.executeUpdate(sql);
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
		 close();
	 }
	 
	 public String getStatusOutput() {
		 StringBuilder res = new StringBuilder();
		 String sql;
		 ResultSet rs;
		 try {
			 res.append("Общее количество запросов: ");
			 sql = "SELECT COUNT(*) FROM connections";
			 rs = stmt.executeQuery(sql);
			 if (rs.next()) {
				 res.append(rs.getInt(1));
			 }
			 
			 res.append("\nКоличество уникальных запросов: ");
			 sql = "SELECT COUNT(*) FROM (SELECT * FROM connections GROUP BY ip, url) AS temp";
			 rs = stmt.executeQuery(sql);
			 if (rs.next()) {
				 res.append(rs.getInt(1));
			 }
			 
			 res.append("\n\nСчетчик запросов на каждый IP:\n");
			 sql = "SELECT ip, COUNT(*) as count, MAX(time) AS last FROM connections GROUP BY ip";
			 rs = stmt.executeQuery(sql);
			 while (rs.next()) {
				 res.append("IP: " + rs.getString(1) + " Количество: " + rs.getString(2) + " Последнее: " + rs.getString(3) + "\n");
			 }
			 
			 res.append("\nКоличество переадресаций:\n");
			 sql = "SELECT redirect, COUNT(*) FROM connections WHERE redirect IS NOT NULL GROUP BY redirect";
			 rs = stmt.executeQuery(sql);
			 while (rs.next()) {
				 res.append("Ссылка: " + rs.getString(1) + " - " + rs.getString(2) + " раз\n");
			 }
		} catch (SQLException e) {
			 e.printStackTrace();
			 return "Can't get output!";
		}
		 
		 return res.toString();
	 }
	 
	 public void close() {
		 try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	 }
	 
}