	/**
 * 
 */
package com.tivo.automation.cpi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class RichMediaImage {
	
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
		//	if(index<5&&index==3) {
			inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_richmedia.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");
			System.out.println(queryInput);
			inputDetails.setDbHost(dbHost);
			//String importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst_Replica..programs where provideruri in ("+queryInput+")", inputDetails);
			String importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInput+")", inputDetails);
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			JSONArray jsonResultArray=output.getJSONArray("Result");
			Map<String, String> mapAttributes=new HashMap();
			mapAttributes.put("image_url", "sf_ClipURL");
			mapAttributes.put("media_type", "RichMediaType");
			mapAttributes.put("plain_sk", "provideruri");
			JSONObject source=jsonUtil.getSpecificValues(jsonResultArray,"plain_sk,media.image_url,media.media_type",mapAttributes,jsonImportDeleteDate);
			//System.out.println(source.getJSONArray("Result").get(1));
			source=mergeFields(source);
			//System.out.println(source.getJSONArray("Result").get(1));
		//	queryInput=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"provideruri");
			System.out.println(queryInput);
			String actual=dbModule.runQuery("select provideruri+sf_ClipURL as provideruri,sf_ClipURL,Case RichMediaType when 'Image' then 'image' End as RichMediaType"
					+ " from DigitalFirst..Richmedias a join DigitalFirst..ProgramRichmedias b\n" + 
					"   on a.RichmediaID=b.RichmediaID join DigitalFirst..programs c\n" + 
					"   on b.programid=c.programid\n" + 
					"   where c.provideruri in("+queryInput+")\n" + 
					"   and a.richmediatype='Image'", inputDetails);
		
			JSONObject jsonActual=new JSONObject(actual);
			JsonComparator jsonComparator=new JsonComparator();
			MappingDetails mappingDetails=new MappingDetails();
			mappingDetails.setPrimaryAttribute("provideruri");
			mappingDetails.setTestingAttribute("RichMediaType,sf_ClipURL");
			mappingDetails.setPrimaryAttributeType(AttributeType.String);
			mappingDetails.setPartialCheck(true);
			List<LinkedHashMap> listResult1=jsonComparator.compareJson(source, jsonActual, mappingDetails);
			cpiUtil.mergeAllResult(listResultFinal,listResult1);
			expectedCount=expectedCount+source.getJSONArray("Result").length();
			actualCount=actualCount+jsonActual.getJSONArray("Result").length();
			
		//	}
			index++;
		}
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setReportFileName("CpiFeed3PPRichMediaImage");
		cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_richmedia.data");
		cPITestResult.setResult(listResultFinal);
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
		
	}
	
	public JSONObject mergeFields(JSONObject source) {
		JSONArray jsonResultArray=source.getJSONArray("Result");
		for(int i=0;i<jsonResultArray.length();i++) {
			JSONObject jsonObj = (JSONObject)jsonResultArray.get(i);
			jsonObj.put("provideruri", jsonObj.get("provideruri").toString()+jsonObj.get("sf_ClipURL").toString());
		}
		return source;
	}
	

}
