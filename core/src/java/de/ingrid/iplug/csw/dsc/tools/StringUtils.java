/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.tools;


public class StringUtils {

	public static String join(Object[] parts, String separator) {
		StringBuilder str = new StringBuilder();
		for (Object part : parts) {
			str.append(part).append(separator);
		}
		if (str.length() > 0)
			return str.substring(0, str.length()-separator.length());
		
		return str.toString();
	}
}
