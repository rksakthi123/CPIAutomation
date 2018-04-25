package com.tivo.automation.cpi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

import com.tivo.automation.input.InputDetails;

public class DBUtil {
	public String runQuery(String query,InputDetails inputDetails) throws ClassNotFoundException, SQLException {
		System.out.println(getDbDriver(inputDetails.getDbHost().getDataBase()));
		Class.forName(getDbDriver(inputDetails.getDbHost().getDataBase()));
		System.out.println(inputDetails.getDbHost().getUrl());
		System.out.println(inputDetails.getDbHost().getUserName());
		System.out.println(inputDetails.getDbHost().getPassword());
		//Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://TUL1CIPCNPDB1;databaseName=DigitalFirst;domain=corporate","skaliyaperumal","Yamuna@234");
		Connection conn = DriverManager.getConnection(inputDetails.getDbHost().getUrl(),inputDetails.getDbHost().getUserName(),inputDetails.getDbHost().getPassword());
        System.out.println("connection created");
        Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		JSONObject jsonResult = new JSONObject();
		jsonResult=resultInJson(rs);
		return jsonResult.toString();
		
	}
	
	public void getConnection() {
		
	}
	public String getDbDriver(String dbName) {
		String driver = null;
		switch (dbName) {
		case "MySQL":
			driver = "com.mysql.jdbc.Driver";
			break;
		case "SqlServer":
			driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			break;
		case "SqlServerJtds":
			driver = "net.sourceforge.jtds.jdbc.Driver";
			break;
		case "oracle":
			driver = "";
			break;
		}
		return driver;
	}
	public JSONObject resultInJson(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsCount = rsmd.getColumnCount();
		String columnNames[] = new String[columnsCount];
		JSONObject jsonResult = new JSONObject();
		JSONArray jsonResultArray = new JSONArray();
		for (int i = 1; i <= columnsCount; i++) {
			columnNames[i - 1] = rsmd.getColumnName(i);
		}

		while (rs.next()) {
			JSONObject json2 = new JSONObject();
			for (String column : columnNames) {
				if(rs.getString(column)!=null) {
				json2.put(column, rs.getString(column));
				}
				else {
					json2.put(column, "");
				}
			}

			jsonResultArray.put(json2);

		}
		jsonResult.put("Result", jsonResultArray);
		return jsonResult;
	}



}
