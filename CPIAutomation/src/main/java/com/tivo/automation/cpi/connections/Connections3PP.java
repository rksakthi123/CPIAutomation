/**
 * 
 */
package com.tivo.automation.cpi.connections;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.wink.json4j.OrderedJSONObject;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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

/**
 * @author skaliyaperumal
 *
 */
public class Connections3PP {

	@Test
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, NoSuchFieldException, SecurityException, 
	IllegalArgumentException, IllegalAccessException, org.apache.wink.json4j.JSONException, InstantiationException, InvocationTargetException, NoSuchMethodException {
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
		for(String client:clients) {
			//if(index<5&&index!=2) {
			List<LinkedHashMap> listResult1=new ArrayList<LinkedHashMap>();
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
		Map<String, List<String>> listEntities=getPartialEntities(listSource,"entities","score");
		System.out.println(listEntities.size());
		Map<String, List<String>> listKeywords=getPartialKeywords(listSource,"keywords","weight");
		System.out.println(listKeywords);
		Map<String, List<String>> listExpected=connectionsUtil.combineKeywordandEntity(listKeywords, listEntities);
		System.out.println(listExpected.size());
		DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");
		inputDetails.setDbHost(dbHost);
		String queryInput=connectionsUtil.getPrimaryIdValues(listSource,"sk");
		//System.out.println(queryInput);
		String actual=dbModule.runQuery("select  objectidfrom as sk,objectidto,linkobjecttypeto,weight from DigitalFirst..webvideoobjecttoobjectrelevancy"
				+ " where provideridfrom=216 and "
				+ "objectidfrom in("+queryInput+") and LinkObjectTypeTo in(2,1,808)", inputDetails);

		JSONObject jsonactual=new JSONObject(actual);

		System.out.println(listExpected);
		//System.out.println(jsonactual);
		listResult1=connectionsUtil.compareConnectionFeedDB(listExpected, jsonactual,client);
		System.out.println(listResult1.size());
		cpiUtil.mergeAllResult(listResultFinal,listResult1);
		
			//}
			index++;
		}
		System.out.println("listResultFinal:"+listResultFinal.size());
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setResult(listResultFinal);
		cPITestResult.setReportFileName("CpiConnections3pp");
		cPITestResult.setInputFileName(prop.get("QAConnectionsPath").toString()+clients[0]+"_ent_shortlisted.json");
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
	}



	public Map<String, List<String>>  getPartialEntities(List<OrderedJSONObject> listSource,String type,String subType) throws org.apache.wink.json4j.JSONException {
		boolean DESC = false;
		ConnectionsUtil connectionsUtil=new ConnectionsUtil();
		
		Map<String, List<String>> mapExpectedData=new HashMap();
		//JSONArray jsonResultArray=source.getJSONArray("Result");
		for(int i=0;i<listSource.size();i++) {
			List<String> listOutput2=new ArrayList();
			JSONObject json=new JSONObject();
			OrderedJSONObject jsonObj =listSource.get(i);
			OrderedJSONObject jsonObjEntities=(OrderedJSONObject)jsonObj.get(type);  // Read keywords and modify if required
			String sk=jsonObj.get("sk").toString();
			String is_curated=jsonObj.get("is_curated").toString();
			LinkedHashMap<String,Double> map=new LinkedHashMap<String,Double>();
				List<String> listEntities=new ArrayList();

				Iterator<Object> it=jsonObjEntities.getOrder();
				 while(it.hasNext()){
					String key=it.next().toString();
					map.put(key, Math.floor(Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get(subType).toString())));
				}
				if(sk.equals("4d8dcfdf24b1c1772719ff6f9b582aab")) {
					System.out.println(map);
				}
				map=connectionsUtil.sortByComparator(map,DESC);
				if(sk.equals("4d8dcfdf24b1c1772719ff6f9b582aab")) {
					System.out.println(map);
				}
				int count=0;
				for(String key:map.keySet()) {
					// sortedEntity.put(key,entity.get(key));
					
					if(((OrderedJSONObject)jsonObjEntities.get(key)).has("rovi_1.1_id")) {
						if(is_curated.equals("false")) {
							
					if(((OrderedJSONObject)jsonObjEntities.get(key)).getJSONArray("rovi_1.1_id").length()>0&&count<5&&Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get("score").toString())>=40) {
						listEntities.add(((OrderedJSONObject)jsonObjEntities.get(key)).getJSONArray("rovi_1.1_id").toString().replace("\"", "").replace("[", "").replace("]", "").trim()+
								"<#>"+Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get("score").toString()));
						count++;
					}
						}
						else {
							if(((OrderedJSONObject)jsonObjEntities.get(key)).getJSONArray("rovi_1.1_id").length()>0
									&&Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get("score").toString())>=40) {
								
							listEntities.add(((OrderedJSONObject)jsonObjEntities.get(key)).
									getJSONArray("rovi_1.1_id").toString().replace("\"", "").replace("[", "").
									replace("]", "")+"<#>"+Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get("score").toString()));
							}
						}
					}
				}
				for(String entityDetails:listEntities) {     //Combine all ids and its score into one array list

					String[] entityDetail=entityDetails.split("\\<\\#\\>");
					String score=entityDetail[1];
					String[] entityIds=entityDetail[0].split("\\,");
					for(String entityId:entityIds) {
						listOutput2.add(entityId+"<#>"+(int) Math.round(Double.parseDouble(score)));
					}
				}


				//System.out.println(listOutput2);
			
				mapExpectedData.put(sk, listOutput2);
		}
		
		return mapExpectedData;

	}

	public Map<String, List<String>> getPartialKeywords(List<OrderedJSONObject> listSource,String type,String subType) throws 
	NoSuchFieldException, IllegalAccessException, JSONException, JsonParseException, JsonMappingException, IOException, org.apache.wink.json4j.JSONException {
		boolean DESC = false;
		
		
		ConnectionsUtil connectionsUtil=new ConnectionsUtil();
		Map<String, List<String>> mapExpectedData=new HashMap();
		//JSONArray jsonResultArray=source.getJSONArray("Result");
		for(int i=0;i<listSource.size();i++) {
			List<String> listOutput2=new ArrayList();
			JSONObject json=new JSONObject();
			OrderedJSONObject jsonObj =listSource.get(i);
			OrderedJSONObject jsonObjEntities=(OrderedJSONObject)jsonObj.get(type);  // Read keywords and modify if required
			String sk=jsonObj.get("sk").toString();
			String is_curated=jsonObj.get("is_curated").toString();
						
			LinkedHashMap<String,Double> map=new LinkedHashMap<String,Double>();
				List<String> listEntities=new ArrayList();
				
				Iterator<Object> it=jsonObjEntities.getOrder();
				 while(it.hasNext()){
					String key=it.next().toString();
					//key=key.replace("\t", "");
					map.put(key.toString(), Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get(subType).toString()));
				}
				
				map=connectionsUtil.sortByComparator(map,DESC);
				
				int count=0;
				
				for(String key:map.keySet()) {
					// sortedEntity.put(key,entity.get(key));
					 if(is_curated.equals("false")) {
					if(count<5&&Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get(subType).toString())>=20&&key.length()<26) {
						listEntities.add(key+"<#>"+Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get(subType).toString()));
						count++;
					}
					 }
					 else {
						 if(key.length()<26&&Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get(subType).toString())>=20) {
							listEntities.add(key+"<#>"+Double.parseDouble(((OrderedJSONObject)jsonObjEntities.get(key)).get(subType).toString())); 
						 }
					 }
				}
				
				for(String entityDetails:listEntities) {     //Combine all ids and its score into one array list

					String[] entityDetail=entityDetails.split("\\<\\#\\>");
					String score=entityDetail[1];
					
						listOutput2.add(entityDetail[0]+"<#>"+(int) Math.round(Double.parseDouble(score)));
					
				}


				mapExpectedData.put(sk, listOutput2);
			}

	
		return mapExpectedData;
	}
}
