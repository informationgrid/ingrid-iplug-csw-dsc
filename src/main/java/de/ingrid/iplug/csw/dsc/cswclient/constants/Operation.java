/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.constants;

public enum Operation {
	/**
	 * OGC_Service interface
	 */
	GET_CAPABILITIES {
		public String toString() {
			return "GetCapabilities";
		}
	},

	/**
	 * CSW-Discovery interface
	 */
	DESCRIBE_RECORD {
		public String toString() {
			return "DescribeRecord";
		}
	},
	GET_DOMAIN {
		public String toString() {
			return "GetDomain";
		}
	},
	GET_RECORDS {
		public String toString() {
			return "GetRecords";
		}
	},
	GET_RECORD_BY_ID {
		public String toString() {
			return "GetRecordById";
		}
	}
}
