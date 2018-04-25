/**
 * 
 */
package com.tivo.automation.cpi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import com.tivo.automation.dbutil.DBHost;

/**
 * @author skaliyaperumal
 *
 */
public class CpiUtil {


	public Properties loadProperty() {
		Properties prop = new Properties();
		InputStream input = null;
		try {

			input = getClass().getResourceAsStream("config.properties");
			prop.load(input);
		}
		catch (IOException io) {
			io.printStackTrace();
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return prop;
	}
	
	public DBHost getHostInfo(Properties prop,String environment) {
		DBHost dbHost=new DBHost();
		if(environment.equals("DEV")) {
			dbHost.setUrl("jdbc:jtds:sqlserver://"+prop.get("DevDBServer").toString());
			dbHost.setUserName(prop.get("DevUsername").toString());
			dbHost.setPassword(prop.get("DevPassword").toString());
			dbHost.setDataBase("SqlServerJtds");
		}
		else if(environment.equals("PROD")) {
			dbHost.setUrl("jdbc:jtds:sqlserver://"+prop.get("ProdDBServer").toString()+";domain=corporate");
			dbHost.setUserName(prop.get("ProdUsername").toString());
			dbHost.setPassword(prop.get("ProdPassword").toString());
			dbHost.setDataBase("SqlServerJtds");
		}
		else if(environment.equals("QA")) {
			dbHost.setUrl("jdbc:jtds:sqlserver://"+prop.get("QADBServer").toString());
			dbHost.setUserName(prop.get("QAUsername").toString());
			dbHost.setPassword(prop.get("QAPassword").toString());
			dbHost.setDataBase("SqlServerJtds");
		}
	return dbHost;
	}
	
	public List<LinkedHashMap> mergeAllResult(List<LinkedHashMap> listResultFinal,List<LinkedHashMap> listResult){
		for(LinkedHashMap map:listResult) {
			listResultFinal.add(map);
		}
		return listResultFinal;
	}

	public String getFileNames() {
		List<String> results = new ArrayList<String>();
		List<String> listOutput = new ArrayList<String>();

		File[] files = new File("/Users/skaliyaperumal/Projects/CPI/07 Mar 2018/Crawl/all_data_20180304T164339").listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 

		for (File file : files) {
			if (file.isFile()) {
				results.add(file.getName());
			}
		}


		String output=results.toString().replace("[", "").replace("]", "").replace(", ", ",").replace(".data", "");

		for(String file:results) {
			if(file.contains(".data")) {
				file=file.replace(", ", ",").replace(".data", "");
				file=removeClient(file);
				if(!listOutput.contains(file)) {
					listOutput.add(file);
				}
			}

		}
		
		return listOutput.toString().replace("[", "").replace("]", "").replace(", ", ",");
	}
	
	public String removeClient(String file) {
		file=file.replace("_avail", "").replace("_award", "").replace("_cast", "").replace("_channel", "").replace("_chart", "").replace("_company", "").replace("_news", "").replace("_otherlinks", "")
				.replace("_pop", "").replace("_programcrew", "").replace("_rank", "").replace("_release", "").replace("_richmedia", "").replace("_role", "").replace("_schedule", "");
		return file;
	}
}
