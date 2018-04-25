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
import java.util.ListIterator;
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

/**
 * @author skaliyaperumal
 *
 */
public class TestProgramBase {
	@Test
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
		for(String client:clients) {
			inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/03 April 2018/CRAWL/all_data_20180401T164045/"+client+"_release.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			queryInputAll=queryInputAll+","+queryInput;
		}
		queryInputAll=queryInputAll.substring(1);

		JSONObject listDevData=getDBData(queryInputAll,"QA");
		JSONObject listProdData=getDBData(queryInputAll,"PROD");
		String nonTestingAttribute="last_update_date,last_update_user,creation_date,creation_user";
		String testingAttributes=getTestingAttribute(nonTestingAttribute,listDevData);
		System.out.println(testingAttributes);
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("program_id");
		mappingDetails.setTestingAttribute(testingAttributes);
		mappingDetails.setPrimaryAttributeType(AttributeType.String);
		mappingDetails.setPartialCheck(true);
		List<LinkedHashMap> listResult1=jsonComparator.compareJson(listDevData, listProdData, mappingDetails);
		System.out.println("Size:"+listResult1.size());
		listResult1=afterComparison(listResult1);
		System.out.println(listResult1.size());
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setResult(listResult1);
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
	}

	public List<LinkedHashMap> afterComparison(List<LinkedHashMap> listResult1) {
		List<LinkedHashMap> listResult=new ArrayList<LinkedHashMap>();
int i=0;
	ListIterator<LinkedHashMap> iter = listResult1.listIterator();
		//for(LinkedHashMap map:listResult1) {
			while(iter.hasNext()) {
				LinkedHashMap map=iter.next();
			if(map.get("Status").toString().equals("false")) {
			
				for(Object keys:map.keySet()) {

					if(keys.toString().contains("Expected"))
					{
						String testingAttribute=keys.toString();
						testingAttribute=testingAttribute.substring(8);
						String expected=map.get(keys).toString();
						String actual=map.get("Actual"+testingAttribute).toString();
						if(expected.equals("0")&&actual.length()==0) {
							iter.remove();
							break;
						}
					}
				}
			}
		}
		//listResult1.removeAll(listResult);
		return listResult1;
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
	public JSONObject getDBData(String queryInputAll,String environment) throws ClassNotFoundException, 
	SQLException, IOException, JSONException, ParseException, org.apache.wink.json4j.JSONException {
		InputDetails inputDetails=new InputDetails();
		DBModule dbModule=new DBModule();
		JsonUtil jsonUtil=new JsonUtil();
	//	List<WebVideoProgramContentAvailability> listData=new ArrayList();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		DBHost dbHost=cpiUtil.getHostInfo(prop,environment);
		inputDetails.setDbHost(dbHost);
		String importDeleteDate="";
		DBUtil dbUtil=new DBUtil();
		if(environment.equals("QA")) {
			importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst_Replica..programs where provideruri in ("+queryInputAll+")", inputDetails);
		}
		else {
			importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where  provideruri in ("+queryInputAll+")", inputDetails);
		}

		JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
		queryInputAll=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"provideruri");

		String actual="";

		actual=dbModule.runQuery("select * from Mosaic..programbase where provider_id=216 and isactive=1 and provideruri in ("+queryInputAll+")", inputDetails);

		JSONObject jsonActual=new JSONObject(actual);

		return jsonActual;
	}
}
