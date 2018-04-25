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
public class Popularity {

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
		//	if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_pop.data");
				//inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/16 Mar 2018/CRAWL/all_data_20180315T164232/"+client+"_pop.data");
				JSONObject output=jsonUtil.readJsonFile(inputDetails);
				String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
				DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

				inputDetails.setDbHost(dbHost);
				String importDeleteDate=dbModule.runQuery("select provideruri,import_deletedate from DigitalFirst..programs where provideruri in ("+queryInput+")", inputDetails);
				JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
				JSONArray jsonResultArray=output.getJSONArray("Result");
				Map<String, String> mapAttributes=new HashMap();
				mapAttributes.put("num_views", "ViewCount");
				mapAttributes.put("num_likes", "Likes");
				mapAttributes.put("num_dislikes", "Dislikes");
				mapAttributes.put("num_shares", "Shares");
				mapAttributes.put("plain_sk", "provideruri");

				JSONObject source=jsonUtil.getSpecificValues(jsonResultArray,"num_views,num_likes,"
						+ "num_dislikes,num_shares,"
						+ "plain_sk",mapAttributes,jsonImportDeleteDate);
				//System.out.println("source:"+source);
				queryInput=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"provideruri");

				String actual=dbModule.runQuery("select a.ViewCount,a.Likes,a.Dislikes,a.Shares,b.provideruri \n" + 
						" from Mosaic..webvideovideosocialattributes a join Mosaic..Programbase b on a.programid=b.program_id  \n" + 
						"where b.provider_id=216 and b.provideruri in ("+queryInput+") and b.isactive=1 and a.providerID=216", inputDetails);

				JSONObject jsonActual=new JSONObject(actual);
				//System.out.println(jsonActual);
				JsonComparator jsonComparator=new JsonComparator();
				MappingDetails mappingDetails=new MappingDetails();
				mappingDetails.setPrimaryAttribute("provideruri");
				mappingDetails.setTestingAttribute("Likes,ViewCount,Dislikes,Shares");
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
	cPITestResult.setReportFileName("CpiFeed3PPPopularity");
	cPITestResult.setInputFileName(prop.get("QAFilePath").toString()+clients[0]+"_pop.data");
	cPITestResult.setResult(listResultFinal);
	//System.out.println(listResultFinal);
	CreateHtml obj=new CreateHtml();
	obj.createReport(cPITestResult);
}

}
