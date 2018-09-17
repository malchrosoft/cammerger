/**
 * 
 */
package com.malchrosoft.cammerger;

import com.malchrosoft.debug.Log;

/**
 * @author Aymeric Malchrowicz
 */
public class CamMerger
{

	public static String TITLE = "MalchroSoft CamMerger";

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Log.info("Run MalchroSoft CamMerger");
		CamMergerFrameControllerNew controller = new CamMergerFrameControllerNew();
//		CamMergerFrameController controller = new CamMergerFrameController();
		controller.launchRefreshLoop();
	}

}
