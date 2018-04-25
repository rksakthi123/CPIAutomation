/**
 * 
 */
package com.tivo.automation.cpi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.tivo.automation.cpi.util.CpiUtil;
import com.tivo.automation.cpi.util.JsonUtil;
import com.tivo.automation.dbutil.DBHost;
import com.tivo.automation.dbutil.DBModule;
import com.tivo.automation.input.InputDetails;

/**
 * @author skaliyaperumal
 *
 */
public class UUIDTest {
	
	@Test
	public void test() throws JSONException, IOException, ClassNotFoundException, SQLException, org.apache.wink.json4j.JSONException {
		List<String> listResult=new ArrayList();
		InputDetails inputDetails=new InputDetails();
		JsonUtil jsonUtil=new JsonUtil();
		CpiUtil cpiUtil=new CpiUtil();
		Properties prop=cpiUtil.loadProperty();
		DBModule dbModule=new DBModule();
		//String clients[]=prop.get("client").toString().split(",");
		String clients[]=cpiUtil.getFileNames().split(",");
		List<LinkedHashMap> listResultFinal=new ArrayList<LinkedHashMap>();
		int expectedCount=0,actualCount=0;
		String queryInputAll="";
		for(String client:clients) {
			inputDetails.setFilePath("/Users/skaliyaperumal/Projects/CPI/05 April 2018/CRAWL/all_data_20180405T165350/"+client+"_avail.data");
			JSONObject output=jsonUtil.readJsonFile(inputDetails);
			String queryInput=jsonUtil.getPrimaryIdValues(output,"plain_sk");
			if(queryInput.length()>1) {
				queryInputAll=queryInputAll+","+queryInput;
				}
		}
		queryInputAll=queryInputAll.substring(1);
			DBHost dbHost=cpiUtil.getHostInfo(prop,"QA");

			inputDetails.setDbHost(dbHost);
			String importDeleteDate=dbModule.runQuery("select programid,import_deletedate from DigitalFirst_Replica..programs where provideruri in ("+queryInputAll+")"
					+ " and import_deletedate is null", inputDetails);
			JSONObject jsonImportDeleteDate=new JSONObject(importDeleteDate);
			
			queryInputAll=jsonUtil.getPrimaryIdValues(jsonImportDeleteDate,"programid");
			String actual=dbModule.runQuery("select a.providervalue,b.babeluuid,a.babelid from Babel..Providermapping a join  babel..cluster b " + 
					"    on a.babelid=b.babelid where  providerid=216 and isactive=1 and objecttypeid=2 and providervalue in("+queryInputAll+")", inputDetails);
			List<String> listProgramId=new ArrayList();
			List<String> listUUID=new ArrayList();
			List<String> listUUIDBabelID=new ArrayList();
			JSONObject json=new JSONObject(actual);
			JSONArray jsonArray=json.getJSONArray("Result");
			for(int i=0;i<jsonArray.length();i++) {
				JSONObject jsonObject=(JSONObject)jsonArray.get(i);
				listProgramId.add(jsonObject.get("providervalue").toString());
				if(!listUUID.contains(jsonObject.get("babeluuid").toString())) {
				listUUID.add(jsonObject.get("babeluuid").toString());
				listUUIDBabelID.add(jsonObject.get("babelid").toString()+":"+jsonObject.get("babeluuid").toString());
				}
			}
			
			int count=0;
			for(int i=0;i<listUUID.size();i++) {
				if(listUUID.get(i)==null||listUUID.get(i).length()<2){
					System.out.println(listProgramId.get(i)+" "+listUUID.get(i));
				}
				else {
					count++;
				}
			}
			
			
			System.out.println("Expected count:"+listProgramId.size());
			System.out.println("Actual count:"+count);
			System.out.println(listUUIDBabelID);
		}
	
}
