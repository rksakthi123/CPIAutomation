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
public class Test3ppProgramRichMediasClip {
	
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
		for(String client:clients) {
			if(index<5&&index!=2) {
				inputDetails.setFilePath(prop.get("QAFilePath").toString()+client+"_otherlinks.data");

			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			System.out.println(queryInput);
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

			inputDetails.setDbHost(dbHost);
			String devOutput=dbModule.runQuery("select b.sf_ClipURL+a.ProgramID+c.provideruri as primaryId, b.sf_ClipURL,a.ProgramID from DigitalFirst..programs c join DigitalFirst..programrichmedias a on "+
					"c.programid=a.programid join DigitalFirst..richmedias b on a.richmediaid=b.richmediaid" + 
					" where c.provideruri in("+queryInput+") and b.richmediatype='Clip'", inputDetails);
			JSONObject jsonDev=new JSONObject(devOutput);
			
			 dbHost=cpiUtil.getHostInfo(prop,"PROD");

			inputDetails.setDbHost(dbHost);
			String prodOutput=dbModule.runQuery("select b.sf_ClipURL+a.ProgramID+c.provideruri as primaryId, b.sf_ClipURL,a.ProgramID "
					+ "from DigitalFirst..programs c join DigitalFirst..programrichmedias a on "+
					"c.programid=a.programid join DigitalFirst..richmedias b on a.richmediaid=b.richmediaid" + 
					" where c.provideruri in("+queryInput+") and b.richmediatype='Clip'", inputDetails);
			JSONObject jsonProd=new JSONObject(prodOutput);
			JsonComparator jsonComparator=new JsonComparator();
			MappingDetails mappingDetails=new MappingDetails();
			mappingDetails.setPrimaryAttribute("primaryId");
			mappingDetails.setTestingAttribute("sf_ClipURL,ProgramID");
			mappingDetails.setPrimaryAttributeType(AttributeType.String);
			mappingDetails.setPartialCheck(true);
			List<LinkedHashMap> listResult1=jsonComparator.compareJson(jsonDev, jsonProd, mappingDetails);
			cpiUtil.mergeAllResult(listResultFinal,listResult1);
			}
			index++;
		}
			CPITestResult cPITestResult=new CPITestResult();
			cPITestResult.setActualNumberOfRecords(actualCount);
			cPITestResult.setExpectedNumberOfRecords(expectedCount);
			cPITestResult.setResult(listResultFinal);
			//System.out.println(listResultFinal);
			CreateHtml obj=new CreateHtml();
			obj.createReport(cPITestResult);
	}

}
