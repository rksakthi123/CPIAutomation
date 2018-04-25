/**
 * 
 */
package com.tivo.automation.cpi.comparedevprod;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
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

/**
 * @author skaliyaperumal
 *
 */
public class TestWebVideoSocialAttributes {
	@Test
	public void test() throws JsonParseException, JsonMappingException, org.apache.wink.json4j.JSONException, IOException, ClassNotFoundException, JSONException, SQLException, ParseException {
		InputDetails inputDetails=new InputDetails();
		JsonUtil jsonUtil=new JsonUtil();
		DBModule dbModule=new DBModule();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		String queryInputAll="";
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		for(String client:clients) {
			inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/13 Mar 2018/CRAWL/all_data_20180313T090030/"+client+"_pop.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			if(queryInput.length()>1) {
				queryInputAll=queryInputAll+","+queryInput;
				}
		}
		queryInputAll=queryInputAll.substring(1);

		JSONObject listDevData=getDBData(queryInputAll,"QA");
		JSONObject listProdData=getDBData(queryInputAll,"PROD");
		String nonTestingAttribute="WebVideoVideoSocialAttributeId";
		String testingAttributes=getTestingAttribute(nonTestingAttribute,listDevData);
		System.out.println(testingAttributes);
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("ProgramId");
		mappingDetails.setTestingAttribute(testingAttributes);
		mappingDetails.setPrimaryAttributeType(AttributeType.String);
		mappingDetails.setPartialCheck(true);
		List<LinkedHashMap> listResult1=jsonComparator.compareJson(listDevData, listProdData, mappingDetails);
		
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setResult(listResult1);
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
	public JSONObject getDBData(String queryInputAll,String environment) throws ClassNotFoundException, SQLException, IOException, JSONException, ParseException, org.apache.wink.json4j.JSONException {
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
		System.out.println("select provideruri,import_deletedate from DigitalFirst_Replica..programs where provideruri in ("+queryInputAll+")");
		if(environment.equals("QA")) {
			importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst_Replica..programs where provideruri in ("+queryInputAll+")", inputDetails);
		}
		else {
			importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInputAll+")", inputDetails);
		}

		JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
		queryInputAll=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"provideruri");

		String actual="";
		System.out.println("select a.* from Mosaic..webvideovideosocialattributes a join Mosaic..Programbase b on a.programid=b.program_id "
				+ "and b.provideruri in ("+queryInputAll+") " + 
				"where b.provider_id=216 and b.isactive=1 and a.providerID=216");
		actual=dbModule.runQuery("select a.* from Mosaic..webvideovideosocialattributes a join Mosaic..Programbase b on a.programid=b.program_id "
				+ "and b.provideruri in ("+queryInputAll+") " + 
				"where b.provider_id=216 and b.isactive=1 and a.providerID=216", inputDetails);

		JSONObject jsonActual=new JSONObject(actual);

		return jsonActual;
	}

}
