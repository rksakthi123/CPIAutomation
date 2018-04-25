/**
 * 
 */
package com.tivo.automation.cpi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.wink.json4j.OrderedJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;


/**
 * @author skaliyaperumal
 *
 */
public class ParseJsonFile {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchFieldException 
	 */
	
	public ParseJsonFile() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		JSONObject json1 = new JSONObject();
		Field map = json1.getClass().getDeclaredField("map");
		map.setAccessible(true);//because the field is private final...
		map.set(json1, new LinkedHashMap<>());
		map.setAccessible(false);//return flag
	}
	
	public JSONObject createObject(String line) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		JSONObject json1 = new JSONObject(line);
		Field map = json1.getClass().getDeclaredField("map");
		map.setAccessible(true);//because the field is private final...
		map.set(json1, new LinkedHashMap<>());
		map.setAccessible(false);//return flag
		return json1;
	}
	public static void main(String[] args) throws JSONException, IOException, InstantiationException, IllegalAccessException, 
	IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, org.apache.wink.json4j.JSONException {
	        InputStream stream =new FileInputStream("/Users/skaliyaperumal/Projects/CPI/16 Mar 2018/KG/kgconnection_20180315T164235/bart_baker_ent_shortlisted.json");
			LineNumberReader reader  = new LineNumberReader(new InputStreamReader(stream));
			String lineRead = "",output="";
			OrderedJSONObject jsonResult=new OrderedJSONObject();
			List<OrderedJSONObject> list=new LinkedList();
			JSONArray jsonResultArray=new JSONArray();
			int i=0;
			while ((lineRead = reader.readLine()) != null&&i<=0) {
				System.out.println(lineRead);
				OrderedJSONObject json1 = new OrderedJSONObject(lineRead);
				System.out.println(json1);
				/*Field map = json1.getClass().getDeclaredField("map");
				map.setAccessible(true);//because the field is private final...
				map.set(json1, new LinkedHashMap<>());
				map.setAccessible(false);//return flag
				json1.put(i+"", lineRead);*/
				list.add(json1);
				i++;
			}
			System.out.println(list);
			jsonResult.put("Result", jsonResultArray); 
			System.out.println(jsonResult);
			
		
	}

}
