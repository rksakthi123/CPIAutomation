/**
 * 
 */
package com.tivo.automation.cpi.connections;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

import com.tivo.automation.cpi.util.CPITestResult;
import com.tivo.automation.cpi.util.ConnectionsUtil;
import com.tivo.automation.cpi.util.CpiUtil;
import com.tivo.automation.cpi.util.CreateHtml;
import com.tivo.automation.cpi.util.JsonUtil;
import com.tivo.automation.dbutil.DBHost;
import com.tivo.automation.dbutil.DBModule;
import com.tivo.automation.input.InputDetails;
import com.tivo.automation.jsonutil.AttributeType;
import com.tivo.automation.jsonutil.JsonComparator;
import com.tivo.automation.jsonutil.MappingDetails;

/**
 * @author skaliyaperumal
 *
 */
public class ConnectionsDBSync2 {
	
	@Test
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, org.apache.wink.json4j.JSONException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		InputDetails inputDetails=new InputDetails();
		ConnectionsUtil connectionsUtil=new ConnectionsUtil();
		JsonUtil jsonUtil=new JsonUtil();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		DBModule dbModule=new DBModule();
		//String clients[]=prop.get("client").toString().split(",");
		String clients[]=cpiUtil.getFileNames().split(",");
		
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		int index=0;
		String objectIdFrom="",objectIdTo="",keywords="",entities="";
		List<LinkedHashMap> listResult1=new ArrayList<LinkedHashMap>();
		for(String client:clients) {
			if(index<6) {
			
			System.out.println(client);
			inputDetails.setFilePath(prop.get("QAConnectionsPath").toString()+client+"_ent_shortlisted.json");
		//inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/QA/17April2018/KG/kgconnection_20180412T165806/"+client+"_ent_shortlisted.json");
		List<OrderedJSONObject> listoutput=connectionsUtil.readJsonFile(inputDetails);
		Map<String, String> mapAttributes=new HashMap();
		mapAttributes.put("plain_sk", "provideruri");
		mapAttributes.put("url", "clip_url");
		//JSONArray jsonResultArray=output.getJSONArray("Result");
		//System.out.println(jsonResultArray.length());
	//	JSONObject source=jsonUtil.getSpecificInJson(jsonResultArray,"keywords,entities,sk,is_curated",mapAttributes);
		List<OrderedJSONObject> listSource=connectionsUtil.getSpecificInJson(listoutput,"keywords,entities,sk,is_curated",mapAttributes);
		
		//	source=getPartialKeywords(source);
		//System.out.println(source.getJSONArray("Result").length());
		Connections3PP connections3PP=new Connections3PP();
		Map<String, List<String>> listEntities=connections3PP.getPartialEntities(listSource,"entities","score");
		//System.out.println(listEntities.size());
		Map<String, List<String>> listKeywords=connections3PP.getPartialKeywords(listSource,"keywords","weight");
		//System.out.println(listKeywords.size());
		Map<String, List<String>> listExpected=connectionsUtil.combineKeywordandEntity(listKeywords, listEntities);
		//System.out.println(listExpected.size());
		
		 objectIdFrom=readObjectIdFrom(listExpected);
		
		 objectIdTo=readObjectIdTo(listExpected);
		
		 keywords=readKeywords(listKeywords);
		
		 entities=readEntities(listEntities);
		
		
		DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");
		inputDetails.setDbHost(dbHost);
		
		String expected=dbModule.runQuery("select RTRIM(LOWER(objectidto))+objectidfrom as sk,RTRIM(LOWER(objectidto)) as objectidto,weight from " + 
				" DigitalFirst..webvideoobjecttoobjectrelevancy " + 
				"where provideridfrom=216 and  " + 
				" objectidfrom in("+objectIdFrom+")  and objectidto in("+keywords+") and LinkObjectTypeTo in(808) and import_deleteDate is null" + 
				" union  " + 
				"select objectidto+objectidfrom as sk,objectidto,weight from  " + 
				" DigitalFirst..webvideoobjecttoobjectrelevancy  " + 
				"where provideridfrom=216 and  " + 
				" objectidfrom in("+objectIdFrom+") and objectidto in("+entities+") and LinkObjectTypeTo in(1,2) and import_deleteDate is null", inputDetails);
		JSONObject jsonexpected=new JSONObject(expected);
		//System.out.println(jsonexpected.getJSONArray("Result"));
		
		
		String actual=dbModule.runQuery("select RTRIM(LOWER(b.webvideodescriptorname))+objectidfrom as sk,RTRIM(LOWER(b.webvideodescriptorname)) as objectidto,weight from  " + 
				" Mosaic..webvideoobjecttoobjectrelevancy a join Mosaic..webvideodescriptor b  on a.objectidto=b.webvideodescriptorid  " + 
				"where provideridfrom=216 and   " + 
				"objectidfrom in("+objectIdFrom+") and objectidto in(select webvideodescriptorid from Mosaic..webvideodescriptor where " + 
						"webvideodescriptorname in("+keywords+"))  and LinkObjectTypeidTo in(808) " + 
				" union  " + 
				"select objectidto+objectidfrom as sk,objectidto,weight from  " + 
				" Mosaic..webvideoobjecttoobjectrelevancy  " + 
				"where provideridfrom=216 and   " + 
				"objectidfrom in("+objectIdFrom+") and objectidto in("+entities+")  and LinkObjectTypeidTo in(1,2)", inputDetails);
		JSONObject jsonActual=new JSONObject(actual);
		
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("sk");
		mappingDetails.setTestingAttribute("objectidto,weight");
		mappingDetails.setPrimaryAttributeType(AttributeType.String);
		mappingDetails.setPartialCheck(true);
	listResult1=jsonComparator.compareJson(jsonexpected, jsonActual, mappingDetails);
		
		cpiUtil.mergeAllResult(listResultFinal,listResult1);
			}
			index++;
		}
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setResult(listResultFinal);
		cPITestResult.setReportFileName("CpiConnectionsSync");
		cPITestResult.setInputFileName(prop.get("QAConnectionsPath").toString()+clients[0]+"_ent_shortlisted.json");
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
	}
	
	public String readObjectIdFrom(Map<String, List<String>> mapExpected) {
		String output="";
		for(String objectIdFrom:mapExpected.keySet()) {
			
			output=output+","+"'"+objectIdFrom+"'";
					
		}
		if(output.replace("'", "").replace(",", "").length()>5) {
			output=output.substring(1);
			}
			else {
				output="'null'";
			}
		return output;
		
	}
	public String readObjectIdTo(Map<String, List<String>> mapKeywords) {
		String objectIdTo="";
		for(String objectIdFrom:mapKeywords.keySet()) {
			List<String> listKeywords=mapKeywords.get(objectIdFrom);
			for(String keyword:listKeywords) {
				keyword=keyword.split("<#>")[0];
				if(keyword.contains("'")) {
					keyword=keyword.replace("'", "''");
				}
				objectIdTo=objectIdTo+","+"'"+keyword+"'";
			}
					
		}
		if(objectIdTo.replace("'", "").replace(",", "").length()>5) {
			objectIdTo=objectIdTo.substring(1);
			}
			else {
				objectIdTo="'null'";
			}
		return objectIdTo;
		
	}
	
	public String readKeywords(Map<String, List<String>> mapKeywords) {
		String keywords="";
		for(String objectIdFrom:mapKeywords.keySet()) {
			List<String> listKeywords=mapKeywords.get(objectIdFrom);
			for(String keyword:listKeywords) {
				keyword=keyword.split("<#>")[0];
				if(keyword.contains("'")) {
					keyword=keyword.replace("'", "''");
				}
				keywords=keywords+","+"'"+keyword+"'";
			}
					
		}
		if(keywords.replace("'", "").replace(",", "").length()>5) {
		keywords=keywords.substring(1);
		}
		else {
			keywords="'null'";
		}
		return keywords;
		
	}
	public String readEntities(Map<String, List<String>> mapEntities) {
		String entities="";
		for(String objectIdFrom:mapEntities.keySet()) {
			List<String> listKeywords=mapEntities.get(objectIdFrom);
			for(String keyword:listKeywords) {
				keyword=keyword.split("<#>")[0];
				if(keyword.contains("'")) {
					keyword=keyword.replace("'", "''");
				}
				entities=entities+","+"'"+keyword+"'";
			}
					
		}
		if(entities.replace("'", "").replace(",", "").length()>5) {
			entities=entities.substring(1);
			}
			else {
				entities="'null'";
			}
		return entities;
		
	}


}
