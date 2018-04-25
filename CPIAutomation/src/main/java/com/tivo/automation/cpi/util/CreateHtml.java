
package com.tivo.automation.cpi.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author skaliyaperumal
 * 23-Feb-2018
 */
public class CreateHtml {
	
	public static void main(String args[]) {
		CreateHtml obj=new CreateHtml();
		List<LinkedHashMap> listResult1=obj.getData();
     	System.out.println(listResult1.size());
     	int passed=0,failed=0;
     	for(LinkedHashMap map:listResult1) {
     		if((boolean)map.get("Status")) {
     			passed++;
     		}
     		else {
     			failed++;
     		}
     	}
     	CPITestResult cPITestResult=new CPITestResult();
		cPITestResult.setActualNumberOfRecords(3);
		cPITestResult.setExpectedNumberOfRecords(4);
		cPITestResult.setResult(listResult1);
     	obj.createReport(cPITestResult);
	}
	
	

	public void createReport(CPITestResult cPITestResult)
	
	{
		List<LinkedHashMap> listResult1=cPITestResult.getResult();
		CreateHtml obj=new CreateHtml();
		try {

			System.out.println(listResult1.size());
			int passed=0,failed=0;
			for(LinkedHashMap map:listResult1) {
				
				if((boolean)map.get("Status")) {
					passed++;
				}
				else {
					failed++;
				}
			}
			//define a HTML String Builder
			StringBuilder htmlStringBuilder=new StringBuilder();
			//append html header and title
			htmlStringBuilder.append("<html><head><title>Test Report</title></head>");
			htmlStringBuilder.append("<style>");
			htmlStringBuilder.append("table {border-collapse:collapse;width:270px;}");
			htmlStringBuilder.append("table td {border=2 2px #fab; max-width:590px; word-wrap:break-word;}");
			htmlStringBuilder.append("</style>");
			//append body
			htmlStringBuilder.append("<script src=\"http://code.jquery.com/jquery-latest.js\"></script>");
			htmlStringBuilder.append("<script src=\"jquery.easyPaginate.js\"></script>");
			htmlStringBuilder.append("<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>");
			//append table
			htmlStringBuilder.append("<script type=\"text/javascript\">\n" + 
					"      google.charts.load('current', {'packages':['corechart']});\n" + 
					"      google.charts.setOnLoadCallback(drawChart);");
			//append row
			htmlStringBuilder.append("function drawChart() {var data = google.visualization.arrayToDataTable([");
			//append row
			htmlStringBuilder.append("['Status', 'Percentage'],\n" + 
					"          ['Passed',     "+passed+"],\n" + 
					"          ['Failed',      "+failed+"]");
			//append row
			htmlStringBuilder.append("]);\n" + 
					"\n" + 
					"        var options = {\n" + 
					"          title: 'Test Result'\n" + 
					"        };");
			//close html file
			htmlStringBuilder.append("var chart = new google.visualization.PieChart(document.getElementById('piechart'));\n" + 
					"\n" + 
					"        chart.draw(data, options);} </script>\n" + 
					"  </head>\n" + 
					"  <body>\n"); 
			htmlStringBuilder.append("<br><br><font size=\"4\" color=\"blue\">Feed File:"+cPITestResult.getInputFileName()+"</font>");
			htmlStringBuilder.append("<br><br><font size=\"4\" color=\"blue\">Records in File:"+cPITestResult.getExpectedNumberOfRecords()+"</font>");
			htmlStringBuilder.append("<br><font size=\"4\" color=\"blue\">Records in DB:"+cPITestResult.getActualNumberOfRecords()+"</font>");
			htmlStringBuilder.append("<center>");
			htmlStringBuilder.append("<div id=piechart style=\"width: 900px; height: 400px;\"></div>"); 
			htmlStringBuilder.append("</center>");
			 htmlStringBuilder.append("<tr><td>Test Failure Summary</td></tr>");
			  htmlStringBuilder.append("<table border=1 bordercolor=000000>");
			//append row
			 
			htmlStringBuilder.append("<tr><td><b>Primary Id</b></td><td><b>Expected</b></td><td><b>Actual</b></td><td><b>Status</b></td></tr>");
			
			//append row
			for(LinkedHashMap map:listResult1) {
				if(!(boolean)map.get("Status")) {
					//if((boolean)map.get("Status")) {
					htmlStringBuilder.append("<tr>");
					for(Object key:map.keySet()) {
						htmlStringBuilder.append("<td>"+key+":"+map.get(key)+"</td>");
					}
					htmlStringBuilder.append("</tr>");
				}

			}
			htmlStringBuilder.append("</table>");
		
					htmlStringBuilder.append("  </body>\n" + 
					"</html>");
			//write html string content to a file

			BufferedReader br=new BufferedReader(new FileReader("/Users/skaliyaperumal/Desktop/reporthtml.txt"));
			String line="",output="";
			while((line=br.readLine())!=null) {
				output=output+line;
			}

			WriteToFile(htmlStringBuilder.toString(),"Reports/"+cPITestResult.getReportFileName()+"testfile.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<LinkedHashMap> getData(){
		List<LinkedHashMap> listResult1=new ArrayList();
		for(int i=0;i<3;i++) {
			LinkedHashMap<String, Object> treeMap=new LinkedHashMap();
			if(i%2==0) {
				treeMap.put("key", i);
				treeMap.put("Expected Value",i+"https://www.youtube.com/embed?playlist=sAapLQCASOE,NXoy9ZVYy9I,5EGrv6O8z7w,L6g-qrHSSoQ,MM-sBuymRYU,hQU08BlmYG4,432bWWRmiMA,tpsxktTqxtg,LCKGac9My3g,XIeCMhNWFQ"
						+ "Q,2S9YiDUI9hA,_Zem0_qsDg0,mEYR_M7YM60,Sy1NlWsWCOE,nBDnJBqRMpk,0Kl6TiEwcK0,NKtDEgKKtwc,sHgLdjyhbdA,");
				treeMap.put("Actual Value",i+"sdscsvcdsvvvvvvsdfsdfsdvdvndsj vsdgfejkwenfjkewnfjnewfnewfhvchsgdvchsvshdfbcdgschsdbchgsvdhcbds hcgvdsghc");
				treeMap.put("Status", false);

			}
			else {
				treeMap.put("key", i);
				treeMap.put("Expected Value",i+"sdscsvcdsvvvvvvsdffewfknwefnlwenfewkfkwefkewkfnewfsdfsdvdvndsj vsdghvchsgdvchsvshdfbcdgschsdbchgsvdhcbds hcgvdsghc");
				treeMap.put("Actual Value",i+"sdscsvcdsvvvvvvsdffwenfknewklfnkwefkew kgfbwekg kewbgjkwemgnewkfbnwkefgsdfsdvdvndsj vsdghvchsgdvchsvshdfbcdgschsdbchgsvdhcbds hcgvdsghc");
				treeMap.put("Status", false);

			}
			listResult1.add(treeMap);
		}

		return listResult1;
	}
	public static void WriteToFile(String fileContent, String fileName) throws IOException {
		String projectPath = System.getProperty("user.dir");
		String tempFile = projectPath + File.separator+fileName;
		File file = new File(tempFile);
		// if file does exists, then delete and create a new file
		if (file.exists()) {
			try {
				File newFileName = new File(projectPath + File.separator+ "backup_"+fileName);
				file.renameTo(newFileName);
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//write to file with OutputStreamWriter
		OutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
		Writer writer=new OutputStreamWriter(outputStream);
		writer.write(fileContent);
		writer.close();

	}
}

