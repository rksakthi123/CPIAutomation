package com.tivo.automation.cpi.sync;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import com.tivo.automation.cpi.util.CPITestResult;
import com.tivo.automation.cpi.util.CpiUtil;
import com.tivo.automation.cpi.util.CreateHtml;
import com.tivo.automation.cpi.util.DBUtil;
import com.tivo.automation.cpi.util.JsonUtil;
import com.tivo.automation.dbutil.DBHost;
import com.tivo.automation.dbutil.DBModule;
import com.tivo.automation.input.InputDetails;
import com.tivo.automation.jsonutil.AttributeType;
import com.tivo.automation.jsonutil.JsonComparator;
import com.tivo.automation.jsonutil.MappingDetails;

public class Programs {
	@Test(groups = { "QA"})	
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, ParseException, org.apache.wink.json4j.JSONException {
		InputDetails inputDetails=new InputDetails();
		JsonUtil jsonUtil=new JsonUtil();
		DBModule dbModule=new DBModule();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		String queryInputAll="";
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		int index=0;
		for(String client:clients) {
		//	if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_release.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			queryInputAll=queryInputAll+","+queryInput;
			index++;
		//	}
		}
		queryInputAll=queryInputAll.substring(1);
		JSONObject list3ppData=getDBData(queryInputAll,"QA","3PP");
		list3ppData=changeDateTimeFormat(list3ppData, "earliest_asset_availability");
		expectedCount=list3ppData.getJSONArray("Result").length();
		JSONObject listMosaicData=getDBData(queryInputAll,"QA","Mosaic");
		actualCount=listMosaicData.getJSONArray("Result").length();
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("program_id");
		mappingDetails.setTestingAttribute("program_type_id,release_year,master_title,earliest_asset_availability,language_id");
		mappingDetails.setPrimaryAttributeType(AttributeType.String);
		mappingDetails.setPartialCheck(true);
		List<LinkedHashMap> listResult1=jsonComparator.compareJson(list3ppData, listMosaicData, mappingDetails);
		
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setResult(listResult1);
		cPITestResult.setReportFileName("CpiSyncPrograms");
		cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_release.data");
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
	}
	public String getTestingAttribute(String nonTestingAttribute,JSONObject dbData) {
		String testingAttributes="";
		List<String> listNonTestingAttributes=Arrays.asList(nonTestingAttribute.split(","));
		JSONArray jsonResultArray=dbData.getJSONArray("Result");

		JSONObject jsonObj = (JSONObject)jsonResultArray.get(1);
		for(String keys:jsonObj.keySet()) {
			if(!listNonTestingAttributes.contains(keys)) {
				testingAttributes=testingAttributes+","+keys;
			}
		}

		testingAttributes=testingAttributes.substring(1);
		return testingAttributes;

	}
	
	public JSONObject changeDateTimeFormat(JSONObject source,String field) {
		JSONArray jsonResultArray=source.getJSONArray("Result");
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject json=(JSONObject)jsonResultArray.get(i);
			for(String key:json.keySet()) {

				if(key.equals(field)) {
					String updated=json.get(key).toString()+" 00:00:00.0";

					json.putOpt(key, updated);
				}
			}
		}
		return source;
	}
	
	public JSONObject getDBData(String queryInputAll,String environment,String table) throws ClassNotFoundException, SQLException, IOException, JSONException, ParseException {
		InputDetails inputDetails=new InputDetails();
		DBModule dbModule=new DBModule();
		JsonUtil jsonUtil=new JsonUtil();
		//List<WebVideoProgramContentAvailability> listData=new ArrayList();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		DBHost dbHost=cpiUtil.getHostInfo(prop,environment);
		inputDetails.setDbHost(dbHost);
		String importDeleteDate="";
		DBUtil dbUtil=new DBUtil();
		
		

		String actual="";
		if(table.equals("3PP")) {
			actual=dbModule.runQuery("select programid as program_id,programtype as program_type_id,releaseyear as release_year,"
					+ "Programdescriptionlanguage as language_id,title as master_title,earliestassetavailability as earliest_asset_availability  from DigitalFirst..programs "
					+ "where provideruri in ("+queryInputAll+") and import_deletedate is null", inputDetails);
			
		}
		else {
			importDeleteDate=dbModule.runQuery("select programid,provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInputAll+")", inputDetails);
			
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			queryInputAll=jsonUtil.getActiveRecords(jsonImportDeleteDate,"provideruri");
		actual=dbModule.runQuery("select program_id,CASE WHEN program_type_id = 8 THEN 'episode' WHEN program_type_id = 9 THEN 'other' "
				+ "END as program_type_id,release_year,master_title,"
				+ "earliest_asset_availability,CASE WHEN language_id = 1 THEN 'ENG' END as language_id"
				+ " from Mosaic..programbase where provider_id=216 and provideruri in ("+queryInputAll+")", inputDetails);
		}

		JSONObject jsonActual=new JSONObject(actual);

		return jsonActual;
	}

}
