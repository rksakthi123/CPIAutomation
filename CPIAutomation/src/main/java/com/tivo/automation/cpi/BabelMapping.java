/**
 * 
 */
package com.tivo.automation.cpi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.wink.json4j.OrderedJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.tivo.automation.cpi.util.ConnectionsUtil;
import com.tivo.automation.cpi.util.CpiUtil;
import com.tivo.automation.cpi.util.JsonUtil;
import com.tivo.automation.dbutil.DBHost;
import com.tivo.automation.dbutil.DBModule;
import com.tivo.automation.input.InputDetails;

/**
 * @author skaliyaperumal
 *
 */
public class BabelMapping {
	@Test
	public void test() throws JSONException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
	IOException, org.apache.wink.json4j.JSONException, ClassNotFoundException, SQLException {
		List<String> listResult=new ArrayList();
		InputDetails inputDetails=new InputDetails();
		JsonUtil jsonUtil=new JsonUtil();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		DBModule dbModule=new DBModule();
		//String clients[]=prop.get("client").toString().split(",");
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		int index=0;
		for(String client:clients) {
			//if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_avail.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

			inputDetails.setDbHost(dbHost);
			String importDeleteDate=dbModule.runQuery("select programid,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInput+")"
					+ " and import_deletedate is null", inputDetails);
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			
			queryInput=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"programid");
			List<String> listExpectedSK=Arrays.asList(queryInput.split(","));
			//System.out.println(listExpectedSK);
			String actual=dbModule.runQuery("select  providervalue from Babel..Providermapping where providerid=216 and isactive=1 "
					+ "and objecttypeid=2 and providervalue in("+queryInput+")", inputDetails);
			List<String> listActualSK=new ArrayList();
			JSONObject json=new JSONObject(actual);
			JSONArray jsonArray=json.getJSONArray("Result");
			for(int i=0;i<jsonArray.length();i++) {
				JSONObject jsonObject=(JSONObject)jsonArray.get(i);
				listActualSK.add(jsonObject.get("providervalue").toString());
			}
			//System.out.println(listActualSK);
			for(String expected:listExpectedSK) {
				expected=expected.substring(1);
				expected=expected.substring(0,expected.length()-1);
				if(!listActualSK.contains(expected)) {
					listResult.add(client+":"+expected+" is missing");
				}
			}
		//	}
			index++;
		}
		System.out.println(listResult);
	//	}
		/*InputDetails inputDetails=new InputDetails();
		ConnectionsUtil connectionsUtil=new ConnectionsUtil();
		JsonUtil jsonUtil=new JsonUtil();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		DBModule dbModule=new DBModule();
		//String clients[]=prop.get("client").toString().split(",");
		String clients[]=cpiUtil.getFileNames().split(",");
		
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		//for(String client:clients) {
			List<LinkedHashMap> listResult1=new ArrayList<LinkedHashMap>();
		inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/03 April 2018/KG/kgconnection_20180401T164047/"+clients[0]+"_ent_shortlisted.json");
		List<OrderedJSONObject> listoutput=connectionsUtil.readJsonFile(inputDetails);
		Map<String, String> mapAttributes=new HashMap();
		mapAttributes.put("plain_sk", "provideruri");
		mapAttributes.put("url", "clip_url");
		//JSONArray jsonResultArray=output.getJSONArray("Result");
		//System.out.println(jsonResultArray.length());
	//	JSONObject source=jsonUtil.getSpecificInJson(jsonResultArray,"keywords,entities,sk,is_curated",mapAttributes);
		List<OrderedJSONObject> listSource=connectionsUtil.getSpecificInJson(listoutput,"keywords,entities,sk,is_curated",mapAttributes);
		List<String> listExpectedSK=new ArrayList();
		
		for(int i=0;i<listSource.size();i++) {
			listExpectedSK.add(listSource.get(i).get("sk").toString());
		}
		System.out.println(listExpectedSK);
		String queryInput=connectionsUtil.getPrimaryIdValues(listSource,"sk");
		DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");
		inputDetails.setDbHost(dbHost);
		

		String actual=dbModule.runQuery("select  providervalue from Babel..Providermapping where providerid=216 and isactive=1 "
				+ "and objecttypeid=2 and providervalue in("+queryInput+")", inputDetails);
		List<String> listActualSK=new ArrayList();
		JSONObject json=new JSONObject(actual);
		JSONArray jsonArray=json.getJSONArray("Result");
		for(int i=0;i<jsonArray.length();i++) {
			JSONObject jsonObject=(JSONObject)jsonArray.get(i);
			listActualSK.add(jsonObject.get("providervalue").toString());
		}
		System.out.println(listActualSK);
		for(String expected:listExpectedSK) {
			if(!listActualSK.contains(expected)) {
				System.out.println(expected+" is missing");
			}
		}*/
		//System.out.println(listActualSK.size());
	}

}
