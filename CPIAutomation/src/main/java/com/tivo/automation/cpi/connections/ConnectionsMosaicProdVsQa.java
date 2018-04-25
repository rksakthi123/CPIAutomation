/**
 * 
 */
package com.tivo.automation.cpi.connections;

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
import com.tivo.automation.cpi.util.ConnectionsUtil;
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
public class ConnectionsMosaicProdVsQa {
	
	@Test
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, org.apache.wink.json4j.JSONException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		InputDetails inputDetails=new InputDetails();
		ConnectionsUtil connectionsUtil=new ConnectionsUtil();
		JsonUtil jsonUtil=new JsonUtil();
		DBModule dbModule=new DBModule();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		int index=0;
		for(String client:clients) {
			//if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAConnectionsPath").toString()+client+"_ent_shortlisted.json");
			//inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/QA/17April2018/KG/kgconnection_20180412T165806/"+client+"_ent_shortlisted.json");
		JSONObject output=jsonUtil.readJsonFile(inputDetails);
		JSONArray jsonResultArray=output.getJSONArray("Result");
		Map<String, String> mapAttributes=new HashMap();
		mapAttributes.put("plain_sk", "provideruri");
		mapAttributes.put("url", "clip_url");
		JSONObject source=jsonUtil.getSpecificInJson(jsonResultArray,"keywords,entities,sk,is_curated",mapAttributes);
		String queryInput=jsonUtil.getPrimaryIdValues(source,"sk");
		
		DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");
		inputDetails.setDbHost(dbHost);
		String expected=dbModule.runQuery("select objectidto+objectidfrom as sk,objectidto,weight from"
				+ " Mosaic..webvideoobjecttoobjectrelevancy "
				+ " where provideridfrom=216 and "
				+ "objectidfrom in("+queryInput+") and LinkObjectTypeidTo in(1,2,808)", inputDetails);
		JSONObject jsonexpected=new JSONObject(expected);
		System.out.println(jsonexpected.getJSONArray("Result").length());
		dbHost=cpiUtil.getHostInfo(prop,"PROD");
		inputDetails.setDbHost(dbHost);
		
		String actual=dbModule.runQuery("select objectidto+objectidfrom as sk,objectidto,weight from"
				+ " Mosaic..webvideoobjecttoobjectrelevancy"
				+ " where provideridfrom=216 and "
				+ "objectidfrom in("+queryInput+") and LinkObjectTypeidTo in(1,2,808)", inputDetails);
		JSONObject jsonActual=new JSONObject(actual);
		
		JsonComparator jsonComparator=new JsonComparator();
		MappingDetails mappingDetails=new MappingDetails();
		mappingDetails.setPrimaryAttribute("sk");
		mappingDetails.setTestingAttribute("objectidto,weight");
		mappingDetails.setPrimaryAttributeType(AttributeType.String);
		mappingDetails.setPartialCheck(true);
		List<LinkedHashMap> listResult1=jsonComparator.compareJson(jsonexpected, jsonActual, mappingDetails);
		
		cpiUtil.mergeAllResult(listResultFinal,listResult1);
			//}
			index++;
		}
		CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setResult(listResultFinal);
		//System.out.println(listResultFinal);
		cPITestResult.setReportFileName("CpiConnectionsMosaicProdVsQA");
		cPITestResult.setInputFileName(prop.get("QAConnectionsPath").toString()+clients[0]+"_ent_shortlisted.json");
		CreateHtml obj=new CreateHtml();
		obj.createReport(cPITestResult);
	}

}
