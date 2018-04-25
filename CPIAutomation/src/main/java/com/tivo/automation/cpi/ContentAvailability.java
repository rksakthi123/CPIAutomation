/**
 * 
 */
package com.tivo.automation.cpi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.SQLException;
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
public class ContentAvailability {
	@Test
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, org.apache.wink.json4j.JSONException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		InputDetails inputDetails=new InputDetails();
		JsonUtil jsonUtil=new JsonUtil();
		DBModule dbModule=new DBModule();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		int index=0;
		for(String client:clients) {
			System.out.println(client);
			//if(index==3) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_avail.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			expectedCount=expectedCount+output.getJSONArray("Result").length();
			String queryInput=jsonUtil.getPrimaryIdValues(output,"sk");
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

			inputDetails.setDbHost(dbHost);
			//String importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst_Replica..programs where provideruri in ("+queryInput+")", inputDetails);
			String importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where programid in ("+queryInput+")", inputDetails);
			
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			JSONArray jsonResultArray=output.getJSONArray("Result");
			Map<String, String> mapAttributes=new HashMap();
			mapAttributes.put("platform_id", "PlatformId");
			mapAttributes.put("last_refreshed_timestamp", "LastRefreshTimeStamp");
			mapAttributes.put("distribution_id", "DistributionOrganization");
			mapAttributes.put("source_id", "SourceOrganization");
			mapAttributes.put("quality", "ProgramQuality");	
			mapAttributes.put("reference_url", "VideoContentLink");
			mapAttributes.put("sk", "programid");

			JSONObject source=jsonUtil.getSpecificValues(jsonResultArray,"source_id,source_availabilities.platform_id,"
					+ "source_availabilities.last_refreshed_timestamp,source_availabilities.reference_url,"
					+ "source_availabilities.quality,source_availabilities.distribution_id,"
					+ "sk",mapAttributes,jsonImportDeleteDate);
			
			source=changeDateTimeFormat(source, "LastRefreshTimeStamp");
			source=checkQuality(source, "ProgramQuality");


/*
			String actual=dbModule.runQuery("select CASE WHEN a.PlatformId = 1 THEN 'pc' WHEN a.PlatformId = 2 THEN 'ios' END AS PlatformId,b.provideruri,a.LastRefreshTimeStamp,a.DistributionOrganizationId,"
					+ "a.SourceOrganizationId,a.ProgramQuality,a.VideoContentLink\n" + 
					" from Mosaic..webvideoprogramcontentavailability a join Mosaic..Programbase b on a.programid=b.program_id and b.provideruri in ("+queryInput+")\n" + 
					"where b.provider_id=216 and b.isactive=1 and a.providerID=216", inputDetails);*/
			String actual=dbModule.runQuery("select platformName AS PlatformId,programid,LastRefreshTimeStamp,DistributionOrganization,"
					+ "SourceOrganization,ProgramQuality,VideoContentLink\n" + 
					" from DigitalFirst..webvideoprogramcontentavailability "
					+ "where programid in ("+queryInput+")\n" + 
					"", inputDetails);

			JSONObject jsonActual=new JSONObject(actual);
			JsonComparator jsonComparator=new JsonComparator();
			MappingDetails mappingDetails=new MappingDetails();
			mappingDetails.setPrimaryAttribute("programid");
			mappingDetails.setTestingAttribute("ProgramQuality,PlatformId,DistributionOrganization,SourceOrganization");
			mappingDetails.setPrimaryAttributeType(AttributeType.String);
			mappingDetails.setPartialCheck(true);
			System.out.println(source);
			List<LinkedHashMap> listResult1=jsonComparator.compareJson(source, jsonActual, mappingDetails);
			cpiUtil.mergeAllResult(listResultFinal,listResult1);
			//expectedCount=expectedCount+source.getJSONArray("Result").length();
			actualCount=actualCount+jsonActual.getJSONArray("Result").length();
			
		//}
			index++;
		}
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setReportFileName("CpiFeed3PPContentAvailability");
		cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_avail.data");
		cPITestResult.setResult(listResultFinal);
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
	}
	public JSONObject checkQuality(JSONObject source,String field) {
		JSONArray jsonResultArray=source.getJSONArray("Result");
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject json=(JSONObject)jsonResultArray.get(i);
			if(!json.has(field)) {
				json.put(field, "SD");
			}
			for(String key:json.keySet()) {

				if(key.equals(field)) {
					if(json.get(key).toString().equals("null")) {
						String updated="SD";
						json.putOpt(key, updated);
					}
					

					
				}
			}
		}
		return source;
	}

	public JSONObject changeDateTimeFormat(JSONObject source,String field) {
		JSONArray jsonResultArray=source.getJSONArray("Result");
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject json=(JSONObject)jsonResultArray.get(i);
			for(String key:json.keySet()) {

				if(key.equals(field)) {
					String updated=json.get(key).toString().replace("T", " ").replace("Z", ".0");

					json.putOpt(key, updated);
				}
			}
		}
		return source;
	}
}
