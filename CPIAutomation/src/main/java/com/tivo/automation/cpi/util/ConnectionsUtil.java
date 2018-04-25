/**
 * 
 */
package com.tivo.automation.cpi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wink.json4j.OrderedJSONObject;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tivo.automation.input.InputDetails;

/**
 * @author skaliyaperumal
 *
 */
public class ConnectionsUtil {


	public 	List<LinkedHashMap> compareConnectionFeedDB(Map<String, List<String>> feed,JSONObject db,String client) throws JsonParseException, JsonMappingException, IOException {

		JSONArray jsonResultArrayDB=db.getJSONArray("Result");
		
		Map<String, List<String>> mapDB=getDbValues(jsonResultArrayDB);
		
		List<LinkedHashMap> listResult=new ArrayList();
		for(String sk:feed.keySet()) {
			List<String> expectedList=feed.get(sk);
			//System.out.println(sk+" "+expectedList.size()+" "+expectedList);
			List<String> actualList=mapDB.get(sk);
			
			if(actualList!=null&&expectedList.size()>0) {
				listResult=compareTwoArrayLists(listResult,expectedList, actualList,sk,client);
			}
			else if(actualList==null&&expectedList.size()==0) {
				//System.out.println(sk+" "+actualList.size()+" "+actualList);
			}
			else {
				System.out.println(client+":"+sk+" error.Actual:"+actualList);
				System.out.println("Expected:"+expectedList);
			}
		}

		return listResult;


	}
	
	  public  Map<String, Integer> toMap(JSONObject jsonobj)  throws JSONException, JsonParseException, JsonMappingException, IOException {
			boolean DESC = false;
		  LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		  System.out.println(jsonobj.toString());
		  for(String key:jsonobj.keySet()) {
			  sortedMap.put(key, jsonobj.toString().indexOf(key));
		  }
		  sortedMap=sortByComparator1(sortedMap,DESC);
		  System.out.println(sortedMap);
		  return sortedMap;
	    }
	  
	  public List<OrderedJSONObject> getSpecificInJson(List<OrderedJSONObject> listoutput,String requiredFields,Map<String, String> map) 
			  throws JSONException, org.apache.wink.json4j.JSONException {
			//System.out.println(map);
			JSONObject output=new JSONObject();
			//JSONArray resultArray=new JSONArray();
			List<OrderedJSONObject> listOutput=new LinkedList();
			List<String> fields=Arrays.asList(requiredFields.split(","));
			for(int i=0;i<listoutput.size();i++) {
				OrderedJSONObject json=new OrderedJSONObject();
				OrderedJSONObject jsonObj =listoutput.get(i);
				
				
					for(Object key1:jsonObj.keySet()) {
						String key=key1.toString();
						for(String field:fields) {
							if(field.contains(key)) {
								if(field.contains(".")) {
									String[] nestedField=field.split("\\.");
									
								}
								else {
									if(map.containsKey(key)) {
										json.put(map.get(key), jsonObj.get(key));
									}
									else {
										json.put(key, jsonObj.get(key));
									}
								}
								break;
							}

						
					}
						
					

				}
					
					listOutput.add(json);
			}
		
			return listOutput;
		}
	  public String getPrimaryIdValues(List<OrderedJSONObject> listoutput,String PrimaryId) throws org.apache.wink.json4j.JSONException {
			//JSONArray jsonResultArray=obj.getJSONArray("Result");
			String output="";
			for(int i=0;i<listoutput.size();i++) {
				OrderedJSONObject jsonObj = listoutput.get(i);
				for(Object  key1:jsonObj.keySet()) {
					String key=key1.toString();
					if(key.equals(PrimaryId)) {
						String value=jsonObj.get(key).toString();
						if(value.contains("'")) {
							value=value.replace("'", "''");
						}
						output=output+","+"'"+value+"'";
						break;
					}
				}
			}
			if(output.length()>3) {
			output=output.substring(1);
			}
			
			return output;
		}
		public List<OrderedJSONObject> readJsonFile(InputDetails inputDetails) throws JSONException, IOException, NoSuchFieldException, 
		SecurityException, IllegalArgumentException, IllegalAccessException, org.apache.wink.json4j.JSONException {
			InputStream stream =new FileInputStream(new File(inputDetails.getFilePath()));
			LineNumberReader reader  = new LineNumberReader(new InputStreamReader(stream));
			String lineRead = "",output="";
			JSONObject jsonResult=new JSONObject();
			JSONArray jsonResultArray=new JSONArray();
			List<OrderedJSONObject> listOutput=new LinkedList();
			int i=0;
			while ((lineRead = reader.readLine()) != null) {
				
				OrderedJSONObject json1 = new OrderedJSONObject(lineRead);
				listOutput.add(json1);
				i++;
			}
			
			return listOutput;
			
		}
	  
	  public  LinkedHashMap<String, Integer> sortByComparator1(LinkedHashMap<String, Integer> unsortMap, final boolean order)
		{

			List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

			// Sorting the list based on values
			Collections.sort(list, new Comparator<Entry<String, Integer>>()
			{
				public int compare(Entry<String, Integer> o1,
						Entry<String, Integer> o2)
				{
					if (order)
					{
						return o1.getValue().compareTo(o2.getValue());
					}
					else
					{
						return o2.getValue().compareTo(o1.getValue());

					}
				}
			});

			// Maintaining insertion order with the help of LinkedList
			LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
			for (Entry<String, Integer> entry : list)
			{
				sortedMap.put(entry.getKey(), entry.getValue());
			}

			return sortedMap;
		}


	   

	public  LinkedHashMap<String, Double> sortByComparator(LinkedHashMap<String, Double> unsortMap, final boolean order)
	{

		List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Double>>()
		{
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2)
			{
				if (order)
				{
					return o1.getValue().compareTo(o2.getValue());
				}
				else
				{
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Entry<String, Double> entry : list)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	
	public List<LinkedHashMap>  compareTwoArrayLists(List<LinkedHashMap> listResult,List<String> listExpected,List<String> listActual,String primaryKey,String client) {
		Map<String,String> mapExpected=new HashMap();
		Map<String,String> mapActual=new HashMap();
		
		
		List<String> listExpectedId=new ArrayList();
		List<String> listActualId=new ArrayList();
		for(String expected:listExpected) {
			String[] expectedData=expected.split("\\<\\#\\>");
			mapExpected.put(expectedData[0], expectedData[1]);
			listExpectedId.add(expectedData[0]);
		}
		for(String actual:listActual) {
			String[] actualData=actual.split("\\<\\#\\>");
			if(actualData.length==2) {
				if(!mapActual.containsKey(actualData[0])) {    //Checking if same entity id has more entry in db
			mapActual.put(actualData[0], actualData[1]);
			}
				else {
					mapActual.put(actualData[0]+"a", actualData[1]);
				}
				
			listActualId.add(actualData[0]);
			}
			else {
				mapActual.put(actualData[0], "");
				listActualId.add(actualData[0]);
			}
		}

		for(String expectdId:listExpectedId) {
			LinkedHashMap<String,Object> mapResult=new LinkedHashMap();
			if(listActualId.contains(expectdId)) {
				
				if(!mapExpected.get(expectdId).toString().equals(mapActual.get(expectdId).toString())) {
					if(Integer.parseInt(mapExpected.get(expectdId).toString())-Integer.parseInt(mapActual.get(expectdId).toString())>1) {
						
					//System.out.println(primaryKey+" "+expectdId+" score mismatch.Expected:"+mapExpected.get(expectdId).toString()+" But actual:"+mapActual.get(expectdId).toString());
					mapResult.put(primaryKey+":"+client, primaryKey+expectdId);
					mapResult.put("Expected", expectdId+" score mismatch.Expected:"+mapExpected.get(expectdId).toString());
					mapResult.put("Actual", mapActual.get(expectdId).toString());
					mapResult.put("Status", false);
					}
				
					
				}
			}
			else {
				
				
				mapResult.put(primaryKey+":"+client, primaryKey+expectdId);
				mapResult.put("Expected"+expectdId, expectdId+" is missing"+listExpected);
				mapResult.put("Actual"+expectdId, listActual);
				mapResult.put("Status", false);
				
			}
			if(mapResult.size()>0) {
			listResult.add(mapResult);
			}
			
		}
		return listResult;
		
	}

	public Map<String, List<String>> combineKeywordandEntity(Map<String, List<String>> keywords,Map<String, List<String>> entity){
		List<String> listOutput;
		Map<String, List<String>> mapCombinedKeywordEntity=new HashMap();
		for(String keyword:keywords.keySet()) {
			listOutput=new ArrayList();
			List<String> listKeywords=keywords.get(keyword);
			List<String> listEntities=entity.get(keyword);
			for(String key:listKeywords) {

				listOutput.add(key);
			}
			for(String ent:listEntities) {
				listOutput.add(ent);
			}

			mapCombinedKeywordEntity.put(keyword, listOutput);
		}

		return mapCombinedKeywordEntity;
	}
	public List<String> getKeywordDetails(Object obj) {
		String output="";
		List<String> listOutput=new ArrayList();
		Map<String, Map> map=(Map<String, Map>)obj;
		for(String keywordName:map.keySet()) {
			Map<String, Map> mapKeyword=map.get(keywordName);
			listOutput.add(keywordName+" "+mapKeyword.get("weight"));
		}
		return listOutput;
	}
	public List<String> getEntityDetails(Object obj) {
		String output="";
		List<String> listOutput=new ArrayList();
		List<String> listOutput2=new ArrayList();
		Map<String, Map> map=(Map<String, Map>)obj;
		for(String entityName:map.keySet()) {
			Map<String, List> mapKeyword=map.get(entityName);
			if(mapKeyword.get("rovi_1.1_id").size()>0) {
				listOutput.add(mapKeyword.get("rovi_1.1_id").toString().replace("[", "").replace("]", "")+"<#>"+mapKeyword.get("score"));    //read entity id and its score
			}
		}

		for(String entityDetails:listOutput) {     //Combine all ids and its score into one array list

			String[] entityDetail=entityDetails.split("\\<\\#\\>");
			String score=entityDetail[1];
			String[] entityIds=entityDetail[0].split("\\,");
			for(String entityId:entityIds) {
				listOutput2.add(entityId+" "+(int) Math.round(Double.parseDouble(score)));
			}
		}
		return listOutput2;
	}

	public Map<String,List<String>> getDbValues(JSONArray jsonArray){
		Map<String,List<String>> dbValues=new HashMap();
		List<String> listIds=new ArrayList();
		String key="",oldKey="";
		for(int i=0;i<jsonArray.length();i++) {

			JSONObject jsonObj = jsonArray.getJSONObject(i);
			key=jsonObj.get("sk").toString();
			
			if((!key.equals(oldKey)&&oldKey.length()>0)) {
				dbValues.put(oldKey, listIds);
				listIds=new ArrayList();
			}
			oldKey=key;
			listIds.add(jsonObj.get("objectidto").toString()+"<#>"+jsonObj.get("weight").toString());
			if(i==jsonArray.length()-1) {
				dbValues.put(oldKey, listIds);
				break;
			}
		}

		return dbValues;
	}
	public Map jsonToMap(JSONObject jsonObj) throws JsonParseException, JsonMappingException, IOException {

		Map map = new HashMap();
		ObjectMapper mapper = new ObjectMapper();

		map = mapper.readValue(jsonObj.toString(), new TypeReference<HashMap>(){});
		return map;
	}



	public Map<String, Map> readAllJsonValues(JSONArray jsonArray,String primaryAttr) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Map> mappingAttributes=new HashMap<String,Map>();
		String key1="",key2="";

		for(int i=0;i<jsonArray.length();i++) {
			List<Map<String,String>> testingAttrs=new ArrayList<Map<String,String>>();
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			//System.out.println(jsonObj);
			String[] mainAttr=primaryAttr.split("\\.");
			if(mainAttr.length==1) {

				key1=jsonObj.get(mainAttr[0]).toString();

			}

			else if(mainAttr.length==2) {
				JSONObject json=jsonObj.getJSONObject(mainAttr[0]);
				key1=json.get(mainAttr[1]).toString();
			}



			mappingAttributes.put(key1,jsonToMap(jsonObj));


			//System.out.println(readAllTestingAttributes(jsonObj,null));


		}
		return mappingAttributes;
	}

}
