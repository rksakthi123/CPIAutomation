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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
public class Release {

	@Test
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, org.apache.wink.json4j.JSONException {
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
		//	if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_release.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

			inputDetails.setDbHost(dbHost);
			String importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInput+")", inputDetails);
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			JSONArray jsonResultArray=output.getJSONArray("Result");
			Map<String, String> mapAttributes=new HashMap();
			mapAttributes.put("plain_sk", "ProviderURI");
			mapAttributes.put("date", "earliestassetavailability");
			mapAttributes.put("release_year", "releaseyear");
			JSONObject source=jsonUtil.getSpecificValues(jsonResultArray,"plain_sk,release_year,date_info.date",mapAttributes,jsonImportDeleteDate);
			//source=changeDateTimeFormat(source, "earliestassetavailability");
			queryInput=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"provideruri");
			System.out.println((queryInput));
			String actual=dbModule.runQuery("select ProviderURI,earliestassetavailability,releaseyear from DigitalFirst..programs where "
					+ " providerUri in ("+queryInput+")", inputDetails);
			JSONObject jsonActual=new JSONObject(actual);
			JsonComparator jsonComparator=new JsonComparator();
			MappingDetails mappingDetails=new MappingDetails();
			mappingDetails.setPrimaryAttribute("ProviderURI");
			mappingDetails.setTestingAttribute("releaseyear,earliestassetavailability");
			mappingDetails.setPrimaryAttributeType(AttributeType.String);
			mappingDetails.setPartialCheck(true);
			List<LinkedHashMap> listResult1=jsonComparator.compareJson(source, jsonActual, mappingDetails);
			cpiUtil.mergeAllResult(listResultFinal,listResult1);
			expectedCount=expectedCount+source.getJSONArray("Result").length();
			actualCount=actualCount+jsonActual.getJSONArray("Result").length();
			
			//}
			index++;
		}
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setReportFileName("CpiFeed3PPRelease");
		cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_release.data");
		cPITestResult.setResult(listResultFinal);
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
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


}
