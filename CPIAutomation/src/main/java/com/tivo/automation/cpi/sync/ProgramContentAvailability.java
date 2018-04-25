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
public class ProgramContentAvailability {
	
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
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_avail.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			queryInputAll=queryInputAll+","+queryInput;
			index++;
			//}
		}
		queryInputAll=queryInputAll.substring(1);
		JSONObject list3ppData=getDBData(queryInputAll,"QA","3PP");
		
		list3ppData=AddVideoLinkInProgramId(list3ppData);
		
		JSONObject listMosaicData=getDBData(queryInputAll,"QA","Mosaic");
		listMosaicData=AddVideoLinkInProgramId(listMosaicData);
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("programid");
		mappingDetails.setTestingAttribute("videocontentlink,videocontentlinktypeid,platformid,sourceorganizationid,distributionorganizationid,programquality,"
				+ "startavailabilitybegin,startavailabilityend");
		mappingDetails.setPrimaryAttributeType(AttributeType.String);
		mappingDetails.setPartialCheck(true);
		List<LinkedHashMap> listResult1=jsonComparator.compareJson(list3ppData, listMosaicData, mappingDetails);
		
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setReportFileName("CpiSyncContentAvailability");
		cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_avail.data");
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
	
	public JSONObject AddVideoLinkInProgramId(JSONObject jsonObject) {
		JSONArray jsonResultArray=jsonObject.getJSONArray("Result");
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);
			String modifiedValue=jsonObj.getString("programid").toString()+jsonObj.getString("videocontentlink").toString();
			jsonObj.put("programid", modifiedValue);
		}
		return jsonObject;
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
		
		//importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInputAll+")", inputDetails);
		
		String actual="";
		if(table.equals("3PP")) {
			actual=dbModule.runQuery("select a.programid+a.videocontentlinktype+a.platformname+a.programquality as programid,a.videocontentlink,a.videocontentlinktype as videocontentlinktypeid,a.platformname as platformid,"
					+ "a.sourceorganization as sourceorganizationid,a.distributionorganization as distributionorganizationid,"
					+ "a.programquality,a.availabilitybegin as startavailabilitybegin,a.availabilityend as startavailabilityend from DigitalFirst..webvideoprogramcontentavailability a"
					+ " join DigitalFirst..programs b on a.programid=b.programid where b.provideruri in ("+queryInputAll+") and "
							+ "a.import_deletedate is null", inputDetails);
		}
		else {
			importDeleteDate=dbModule.runQuery("select b.provideruri,b.programid,a.import_deletedate from DigitalFirst..webvideoprogramcontentavailability a "
					+ "join  DigitalFirst..programs b on a.programid=b.programid where  b.provideruri in ("+queryInputAll+")", inputDetails);
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			queryInputAll=jsonUtil.getActiveRecords(jsonImportDeleteDate,"programid");

		actual=dbModule.runQuery("select a.programid+CASE WHEN a.videocontentlinktypeid = 1 THEN 'URL' END+"
				+ "CASE WHEN a.PlatformId = 1 THEN 'pc' WHEN a.PlatformId = 2 THEN 'ios' END+a.programquality as programid,"
				+ "CASE WHEN a.videocontentlinktypeid = 1 THEN 'URL' END AS videocontentlinktypeid,a.videocontentlink,"
				+ "CASE WHEN a.PlatformId = 1 THEN 'pc' WHEN a.PlatformId = 2 THEN 'ios' END AS platformid,"
				+ "a.sourceorganizationid,a.distributionorganizationid,a.programquality,"
				+ "a.startavailabilitybegin,a.startavailabilityend"
				+ " from Mosaic..webvideoprogramcontentavailability a where a.programid in("+queryInputAll+") and a.providerid=216", inputDetails);
		}

		JSONObject jsonActual=new JSONObject(actual);

		return jsonActual;
	}

}
