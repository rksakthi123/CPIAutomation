/**
 * 
 */
package com.tivo.automation.cpi.sync;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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
public class WebVideoSocialAttributes {
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
		int index=0;
		for(String client:clients) {
			//if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_pop.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			queryInputAll=queryInputAll+","+queryInput;
		//	}
			index++;
		}
		queryInputAll=queryInputAll.substring(1);
		JSONObject list3ppData=getDBData(queryInputAll,"QA","3PP");
		expectedCount=list3ppData.getJSONArray("Result").length();
		JSONObject listMosaicData=getDBData(queryInputAll,"QA","Mosaic");
		actualCount=listMosaicData.getJSONArray("Result").length();
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("programid");
		mappingDetails.setTestingAttribute("hashtags,viewcount,shares,likes,dislikes");
		mappingDetails.setPrimaryAttributeType(AttributeType.String);
		mappingDetails.setPartialCheck(true);
		List<LinkedHashMap> listResult1=jsonComparator.compareJson(list3ppData, listMosaicData, mappingDetails);
		
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setReportFileName("CpiSyncSocialAttributes");
		cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_pop.data");
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
	
	public JSONObject getDBData(String queryInputAll,String environment,String table) throws ClassNotFoundException, SQLException, IOException, JSONException, ParseException {
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
		
		
		String actual="";
		if(table.equals("3PP")) {
			actual=dbModule.runQuery("select a.viewcount,a.likes,a.dislikes,a.shares,a.programid,a.hashtags from DigitalFirst..webvideovideosocialattributes a"
					+ " join DigitalFirst..programs b on a.programid=b.programid "
					+ " where  b.provideruri in ("+queryInputAll+") and a.import_deletedate is null", inputDetails);
		}
		else {
			importDeleteDate=dbModule.runQuery("select a.programid,provideruri,a.import_deletedate from DigitalFirst..webvideovideosocialattributes a "
					+ "join  DigitalFirst..programs b on a.programid=b.programid and  b.provideruri in ("+queryInputAll+")", inputDetails);
			
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			queryInputAll=jsonUtil.getActiveRecords(jsonImportDeleteDate,"programid");

		actual=dbModule.runQuery("select a.viewcount,a.likes,a.dislikes,a.shares,b.provideruri,a.programid,a.hashtags" + 
				" from Mosaic..webvideovideosocialattributes a join Mosaic..Programbase b on a.programid=b.program_id "+ 
				" where b.provider_id=216 and b.isactive=1 and a.providerID=216 and a.programid in ("+queryInputAll+")", inputDetails);
		}

		JSONObject jsonActual=new JSONObject(actual);

		return jsonActual;
	}

}
