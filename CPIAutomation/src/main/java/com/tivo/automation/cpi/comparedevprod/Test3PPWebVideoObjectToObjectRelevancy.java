/**
 * 
 */
package com.tivo.automation.cpi.comparedevprod;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

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
public class Test3PPWebVideoObjectToObjectRelevancy {

	@Test
	public void test() throws JSONException, IOException, org.apache.wink.json4j.JSONException, ClassNotFoundException, SQLException {
		InputDetails inputDetails=new InputDetails();
		JsonUtil jsonUtil=new JsonUtil();
		DBModule dbModule=new DBModule();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		int index=0;
		String queryInputAll="";
		for(String client:clients) {
			if(index<5&&index!=2) {
			
				inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/QA/17April2018/KG/kgconnection_20180412T165806/"+client+"_ent_shortlisted.json");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"sk");
			queryInputAll=queryInputAll+","+queryInput;
			
			}
			index++;
		}
		queryInputAll=queryInputAll.substring(1);
		System.out.println(queryInputAll);
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

			inputDetails.setDbHost(dbHost);
			String devOutput=dbModule.runQuery("select  objectidfrom+objectidto+linkobjecttypeto+weight as sk,objectidto,linkobjecttypeto,weight from DigitalFirst..webvideoobjecttoobjectrelevancy" + 
					" where provideridfrom=216 and " + 
					" objectidfrom in("+queryInputAll+")", inputDetails);
			JSONObject jsonDev=new JSONObject(devOutput);
			
			 dbHost=cpiUtil.getHostInfo(prop,"PROD");

			inputDetails.setDbHost(dbHost);
			String prodOutput=dbModule.runQuery("select  objectidfrom+objectidto+linkobjecttypeto+weight as sk,objectidto,linkobjecttypeto,weight from DigitalFirst..webvideoobjecttoobjectrelevancy " + 
					" where provideridfrom=216 and " + 
					" objectidfrom in( "+queryInputAll+ ")", inputDetails);
			JSONObject jsonProd=new JSONObject(prodOutput);
			System.out.println(jsonProd);
			JsonComparator jsonComparator=new JsonComparator();
			MappingDetails mappingDetails=new MappingDetails();
			mappingDetails.setPrimaryAttribute("sk");
			mappingDetails.setTestingAttribute("objectidto,linkobjecttypeto,weight");
			mappingDetails.setPrimaryAttributeType(AttributeType.String);
			mappingDetails.setPartialCheck(true);
			List<LinkedHashMap> listResult1=jsonComparator.compareJson(jsonDev, jsonProd, mappingDetails);
			cpiUtil.mergeAllResult(listResultFinal,listResult1);
			
		
			CPITestResult cPITestResult=new CPITestResult();
			cPITestResult.setActualNumberOfRecords(actualCount);
			cPITestResult.setExpectedNumberOfRecords(expectedCount);
			cPITestResult.setResult(listResultFinal);
			//System.out.println(listResultFinal);
			CreateHtml obj=new CreateHtml();
			obj.createReport(cPITestResult);
	}
}
