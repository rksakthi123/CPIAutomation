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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.wink.json4j.OrderedJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.tivo.automation.input.InputDetails;

/**
 * @author skaliyaperumal
 *
 */
public class JsonUtil {
	public JSONObject readJsonFile(InputDetails inputDetails) throws JSONException, IOException
	{
		InputStream stream =new FileInputStream(new File(inputDetails.getFilePath()));
		LineNumberReader reader  = new LineNumberReader(new InputStreamReader(stream));
		String lineRead = "",output="";
		JSONObject jsonResult=new JSONObject();
		JSONArray jsonResultArray=new JSONArray();
		int i=0;
		while ((lineRead = reader.readLine()) != null) {

			JSONObject json1 = new JSONObject(lineRead);

			jsonResultArray.put(json1);
			i++;
		}
		jsonResult.put("Result", jsonResultArray); 
		return jsonResult;

	}

	public String getActiveRecords(JSONObject obj,String PrimaryId) {
		JSONArray jsonResultArray=obj.getJSONArray("Result");
		String output="";
		int count=0,count1=0;
		for(int i=0;i<jsonResultArray.length();i++) {
			count++;
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);

			String value=jsonObj.get("import_deletedate").toString();

			if(value.length()==0) {
				count1++;
				output=output+","+"'"+jsonObj.get(PrimaryId).toString()+"'";
			}




		}

		if(output.length()>3) {
			output=output.substring(1);
		}

		return output;
	}

	public String getPrimaryIdValues(JSONObject obj,String PrimaryId) throws org.apache.wink.json4j.JSONException {
		JSONArray jsonResultArray=obj.getJSONArray("Result");
		String output="";
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);
			for(String  key:jsonObj.keySet()) {

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


	public Map<String, String> readValueInJsonArray(String key,JSONObject object) {

		Map<String, String> mapJsonArrayValues=new HashMap();
		String output="";
		String[] nestedField=key.split("\\.");
		JSONArray resultArray=object.getJSONArray(nestedField[0]);
		JSONObject json=null;
		if(resultArray.length()==1) {
			json=(JSONObject)resultArray.get(0);
			if(json.has(nestedField[1])) {
				output=json.get(nestedField[1]).toString();
				mapJsonArrayValues.put(output,nestedField[1]);  //value in key and key in value
			}
			else {
				output="null";
			}
		}
		else {
			for(int i=0;i<resultArray.length();i++) {
				json=(JSONObject)resultArray.get(i);
				if(json.has(nestedField[1])) {
					output=json.get(nestedField[1]).toString();
					mapJsonArrayValues.put(output,nestedField[1]);  //value in key and key in value
				}
				else {
					output="null";
				}
			}
		}


		return mapJsonArrayValues;
	}

	public boolean checkImportDeleteDate(JSONObject importDeleteDate,String provideruri) {
		JSONArray resultArray=importDeleteDate.getJSONArray("Result");
		for(int i=0;i<resultArray.length();i++) {
			JSONObject jsonObj = (JSONObject)resultArray.get(i);
			if(jsonObj.get("provideruri").toString().equals(provideruri)&&jsonObj.get("import_deletedate").toString().length()>0) {
				return false;
			}
		}
		return true;
	}

	public JSONObject getSpecificInJson(JSONArray jsonResultArray,String requiredFields,Map<String, String> map) {
		//System.out.println(map);
		JSONObject output=new JSONObject();
		JSONArray resultArray=new JSONArray();
		List<String> fields=Arrays.asList(requiredFields.split(","));
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject json=new JSONObject();
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);


			for(String key:jsonObj.keySet()) {

				for(String field:fields) {
					if(field.contains(key)) {
						if(field.contains(".")) {
							String[] nestedField=field.split("\\.");
							if(nestedField.length==2) {
								//String value=readValueInJsonArray(field,jsonObj);
								Map<String, String> mapJsonArrayValues=readValueInJsonArray(field,jsonObj);
								for(String jsonArrayValues:mapJsonArrayValues.keySet()) {
									if(map.containsKey(nestedField[1])) {
										json.put(map.get(nestedField[1]), jsonArrayValues);
									}
									else {
										json.put(nestedField[1], jsonArrayValues);
									}
								}


							}
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

			resultArray.put(json);
		}
		output.put("Result", resultArray);
		return output;
	}

	public JSONObject getSpecificValues(JSONArray jsonResultArray,String requiredFields,Map<String, String> map,JSONObject importDeleteDate) {
		//System.out.println(map);
		JSONObject output=new JSONObject();
		JSONArray resultArrayFinal=new JSONArray();
	//	List<String> fields=Arrays.asList(requiredFields.split(","));
		List<String> fields = new LinkedList<String>(Arrays.asList(requiredFields.split(",")));
		
		int index=0;
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject json=new JSONObject();
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);
		
			
			
			if(checkImportDeleteDate(importDeleteDate,jsonObj.get("plain_sk").toString())) {
				Map<String, String> mapJsonArrayValues=new HashMap();
				List<Map<String, String>> listJsonArrayValues=new ArrayList();
				for(String key:jsonObj.keySet()) {

					for(String field:fields) {
						if(field.contains(key)) {
							
							if(field.contains(".")) {
								String[] nestedField=field.split("\\.");
								if(nestedField.length==2) {
									String value="";

									mapJsonArrayValues=readValueInJsonArray(field,jsonObj);
									if(mapJsonArrayValues.keySet().size()>1) {
										listJsonArrayValues.add(mapJsonArrayValues);
									}
									else {
										
										for(String jsonArrayValues:mapJsonArrayValues.keySet()) {
											if(map.containsKey(nestedField[1])) {
												json.put(map.get(nestedField[1]), jsonArrayValues);
											}
											else {
												json.put(nestedField[1], jsonArrayValues);
											}
										}
										
									}
								}
							}
							else {
								if(map.containsKey(key)) {
									json.put(map.get(key), jsonObj.get(key));
								}
								else {
									json.put(key, jsonObj.get(key));
								}
							}
						}
						
					}
				}
			
				
				JSONObject jsonTemp=new JSONObject();
				jsonTemp=json;
				//Adding JsonArray values into json Object
				if(listJsonArrayValues.size()>0) {
					
					for(Map<String, String> mapValues:listJsonArrayValues) {
					
						for(String jsonArrayValues:mapValues.keySet()) {
							JSONObject jsonTemp1=new JSONObject();
							for(String jsonKey:json.keySet()) {
								jsonTemp1.put(jsonKey, json.get(jsonKey));
							}
							
							
							
							
							if(map.containsKey(mapValues.get(jsonArrayValues))) {
								jsonTemp1.put(map.get(mapValues.get(jsonArrayValues)), jsonArrayValues);
							}
							else {
								jsonTemp1.put((mapValues.get(jsonArrayValues)), jsonArrayValues);
							}
							

							resultArrayFinal.put(jsonTemp1);
							
							
						}

					}

				}
				else {
					resultArrayFinal.put(json);
				}
				

			}
			
		}
		output.put("Result", resultArrayFinal);

		return output;
	}

}
