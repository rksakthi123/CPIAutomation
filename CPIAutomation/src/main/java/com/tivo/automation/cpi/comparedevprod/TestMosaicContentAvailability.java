package com.tivo.automation.cpi.comparedevprod;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class TestMosaicContentAvailability {
	
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
			if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_avail.data");


			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			if(queryInput.length()>1) {
				queryInputAll=queryInputAll+","+queryInput;
				}
			}
			index++;
		}
		queryInputAll=queryInputAll.substring(1);

		JSONObject listDevData=getDBData(queryInputAll,"QA");
		JSONObject listProdData=getDBData(queryInputAll,"PROD");
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("programId");
		mappingDetails.setTestingAttribute("providerId,videoContentLinkTypeId,platformId,distributionOrganizationId,videoContentLink,"
				+ "sourceOrganizationId,programQuality,startAvailabilityBegin,startAvailabilityEnd,isActive");
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
		
		importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInputAll+") and import_deletedate is null", inputDetails);
		
		JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
		queryInputAll=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"provideruri");
		
		String actual="";
		
			actual=dbModule.runQuery("select a.programId,a.providerId,a.videoContentLinkTypeId,a.programId,a.videoContentLink,a.platformId,"
					+ "a.sourceOrganizationId,a.distributionOrganizationId,a.startAvailabilityBegin,a.startAvailabilityEnd,a.lastRefreshTimeStamp,a.programQuality,"
					+ "a.isActive from Mosaic..webvideoprogramcontentavailability a join Mosaic..Programbase b on a.programid=b.program_id "
					+ "and b.provideruri in ("+queryInputAll+") where b.provider_id=216 and b.isactive=1 and a.providerID=216", inputDetails);
		
		JSONObject jsonActual=new JSONObject(actual);
		jsonActual=concatenateMultiColumns(jsonActual);
		/*
		JSONArray jsonResultArray=jsonActual.getJSONArray("Result");
		JSONArray jsonResult=new JSONArray();
		JSONObject result = new JSONObject();
		for(int i=0;i<jsonResultArray.length();i++) {
			WebVideoProgramContentAvailability webVideoProgramContentAvailability=new WebVideoProgramContentAvailability();
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);
			JSONObject jsonObjOut = new JSONObject();
			jsonObjOut.put("providerId", Integer.parseInt(jsonObj.get("providerId").toString()));
			jsonObjOut.put("videoContentLinkTypeId", Integer.parseInt(jsonObj.get("videoContentLinkTypeId").toString()));
			jsonObjOut.put("platformId", Integer.parseInt(jsonObj.get("platformId").toString()));
			jsonObjOut.put("distributionOrganizationId", Integer.parseInt(jsonObj.get("distributionOrganizationId").toString()));
			jsonObjOut.put("programId", jsonObj.get("programId").toString());
			jsonObjOut.put("videoContentLink", jsonObj.get("videoContentLink").toString());
			jsonObjOut.put("sourceOrganizationId", jsonObj.get("sourceOrganizationId").toString());
			jsonObjOut.put("programQuality", jsonObj.get("programQuality").toString());
			jsonObjOut.put("startAvailabilityBegin", jsonObj.get("startAvailabilityBegin").toString());
			jsonObjOut.put("startAvailabilityEnd",jsonObj.get("startAvailabilityEnd").toString());
			jsonObjOut.put("lastRefreshTimeStamp", jsonObj.get("lastRefreshTimeStamp").toString());
			jsonObjOut.put("isActive",jsonObj.get("isActive").toString());
			jsonResult.put(jsonObjOut);
		
		}
		result.put("Result", jsonResult);*/
		return jsonActual;
	}
	
	public JSONObject concatenateMultiColumns(JSONObject jsonActual) {
		JSONArray jsonResultArray=jsonActual.getJSONArray("Result");
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);
			jsonObj.put("programId", jsonObj.get("programId").toString()+jsonObj.get("videoContentLink").toString()+jsonObj.get("programQuality").toString()+getIsActive(jsonObj.get("platformId").toString()));
		}
		return jsonActual;
	}
	
	public int getIsActive(String StrStatus) {
		
		if(StrStatus.equals("true")||StrStatus.equals("1")) {
			return 1;
		}
		return 0;
	}
	public Date convertStringToDate(String date) throws ParseException {
		Date dateOutput=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		if(date.length()>1) {
		java.util.Date dateModified = sdf.parse(date);
		dateOutput = new Date(dateModified.getTime());
		}
		return dateOutput;
		
	}
	
	

}
