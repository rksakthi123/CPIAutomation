/**
 * 
 */
package com.tivo.automation.cpi.util;

import org.testng.annotations.Test;

/**
 * @author skaliyaperumal
 *
 */
public class TestCpiUtil {
	
	@Test
	public void testGetFileNames() {
		CpiUtil cpiUtil=new CpiUtil();
		System.out.println(cpiUtil.getFileNames());
	}

}
