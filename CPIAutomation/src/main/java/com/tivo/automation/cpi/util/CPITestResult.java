/**
 * 
 */
package com.tivo.automation.cpi.util;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author skaliyaperumal
 *
 */
public class CPITestResult {
	private String inputFileName;
	private String reportFileName;
	private int expectedNumberOfRecords;
	private int actualNumberOfRecords;
	private List<LinkedHashMap> result;
	public int getExpectedNumberOfRecords() {
		return expectedNumberOfRecords;
	}
	public void setExpectedNumberOfRecords(int expectedNumberOfRecords) {
		this.expectedNumberOfRecords = expectedNumberOfRecords;
	}
	public int getActualNumberOfRecords() {
		return actualNumberOfRecords;
	}
	public void setActualNumberOfRecords(int actualNumberOfRecords) {
		this.actualNumberOfRecords = actualNumberOfRecords;
	}
	public List<LinkedHashMap> getResult() {
		return result;
	}
	public void setResult(List<LinkedHashMap> result) {
		this.result = result;
	}
	public String getInputFileName() {
		return inputFileName;
	}
	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	public String getReportFileName() {
		return reportFileName;
	}
	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}
	

}
