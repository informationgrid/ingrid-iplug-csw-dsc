/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.constants;

public enum ElementSetName {
    BRIEF {
        public String toString() {
            return "brief";
        }
    },
    SUMMARY {
        public String toString() {
            return "summary";
        }
    },
    FULL {
        public String toString() {
            return "full";
        }
    },
    IDF {
        public String toString() {
            return "idf";
        }
    }

}
