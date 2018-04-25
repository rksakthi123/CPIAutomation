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
public class OtherLinks {
	@Test
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, org.apache.wink.json4j.JSONException {
		InputDetails inputDetails=new InputDetails();
		JsonUtil jsonUtil=new JsonUtil();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		DBModule dbModule=new DBModule();
		//String clients[]=prop.get("client").toString().split(",");
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		int index=0;
		for(String client:clients) {
			//if(index<2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_otherlinks.data");
			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

			inputDetails.setDbHost(dbHost);
			String importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInput+")", inputDetails);
			/*String importDeleteDate=dbModule.runQuery("select c.provideruri,a.programid,a.import_deletedate from DigitalFirst..programs c join "
					+ " DigitalFirst..programrichmedias a on c.programid=a.programid" + 
					" join DigitalFirst..richmedias b on a.richmediaid=b.richmediaid" + 
					
					" where c. provideruri in ("+queryInput+") and b.richmediatype='Clip'", inputDetails);*/
			
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			JSONArray jsonResultArray=output.getJSONArray("Result");
			Map<String, String> mapAttributes=new HashMap();
			mapAttributes.put("plain_sk", "provideruri");
			mapAttributes.put("url", "clip_url");
			//System.out.println(jsonResultArray);
			JSONObject source=jsonUtil.getSpecificValues(jsonResultArray,"plain_sk,links.url",mapAttributes,jsonImportDeleteDate);
			System.out.println("source:"+source);
			queryInput=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"provideruri");
		//	queryInput=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"programid");
			
			inputDetails.setDbHost(dbHost);
			String actual=dbModule.runQuery("select rc.clip_url,p.provideruri from  Mosaic..rmclip rc  join Mosaic..rmcliprelevancylink rcl on rc.clip_id=rcl.clip_id  \n" + 
					"join Mosaic..programbase p on rcl.link_object_id=p.program_id where\n" + 
					"p.provider_id=216 and p.isactive=1 and p.provideruri in ("+queryInput+") and rc.provider_id=216", inputDetails);
			/*String actual=dbModule.runQuery("select rc.clip_url,rcl.programid from  Mosaic..rmclip rc  join Mosaic..rmcliprelevancylink rcl on rc.clip_id=rcl.clip_id  \n" + 
					"where rcl.link_object_id in("+queryInput+") and " + 
					"rcl.provider_id=216  and rc.provider_id=216", inputDetails);*/
			JSONObject jsonActual=new JSONObject(actual);
			//System.out.println(jsonActual);
			JsonComparator jsonComparator=new JsonComparator();
			MappingDetails mappingDetails=new MappingDetails();
			mappingDetails.setPrimaryAttribute("provideruri");
			mappingDetails.setTestingAttribute("clip_url");
			mappingDetails.setPrimaryAttributeType(AttributeType.String);
			mappingDetails.setPartialCheck(true);
			List<LinkedHashMap> listResult1=jsonComparator.compareJson(source, jsonActual, mappingDetails);
			cpiUtil.mergeAllResult(listResultFinal,listResult1);
			expectedCount=expectedCount+source.getJSONArray("Result").length();
			actualCount=actualCount+jsonActual.getJSONArray("Result").length();
			index++;
			//}
		}
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(actualCount);
		cPITestResult.setExpectedNumberOfRecords(expectedCount);
		cPITestResult.setResult(listResultFinal);
		cPITestResult.setReportFileName("CpiFeed3PPOtherLinks");
		cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_otherlinks.data");
		//System.out.println(listResultFinal);
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
	}
	
	

}
