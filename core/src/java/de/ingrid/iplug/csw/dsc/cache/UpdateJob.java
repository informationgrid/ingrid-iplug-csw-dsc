/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

public class UpdateJob {

	private static UpdateJob instance;
	private UpdateJob() {}
	
	/**
	 * Get the singleton instance
	 * @return UpdateJob
	 */
	public static UpdateJob getInstance() {
		if (instance == null) {
			instance = new UpdateJob();
		}
		return instance;
	}
	
	public void execute() {		
	}
}
