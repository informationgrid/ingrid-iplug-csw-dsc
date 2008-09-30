/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */
package de.ingrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for mapping partner initials to partner full names.
 * Example : bb -> Brandenburg. 
 * 
 * <p/>created on 30.03.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 *  
 */
public class PartnerMapping {

    private static Map fPartnersByInitials = new HashMap(18);

    static{
        init();
    }
    /**
     * Fills the map.
     */
    public static void init() {
        fPartnersByInitials.put("bund", "Bund");
        fPartnersByInitials.put("bb", "Brandenburg");
        fPartnersByInitials.put("be", "Berlin");
        fPartnersByInitials.put("bw", "Baden-Württemberg");
        fPartnersByInitials.put("by", "Bayern");
        fPartnersByInitials.put("hb", "Bremen");
        fPartnersByInitials.put("he", "Hessen");
        fPartnersByInitials.put("hh", "Hamburg");
        fPartnersByInitials.put("mv", "Mecklenburg-Vorpommern");
        fPartnersByInitials.put("ni", "Niedersachsen");
        fPartnersByInitials.put("nw", "Nordrhein-Westfalen");
        fPartnersByInitials.put("rp", "Rheinland-Pfalz");
        fPartnersByInitials.put("sh", "Schleswig-Holstein");
        fPartnersByInitials.put("sl", "Saarland");
        fPartnersByInitials.put("sn", "Sachsen");
        fPartnersByInitials.put("st", "Sachsen-Anhalt");
        fPartnersByInitials.put("th", "Thüringen");
    }
    
    /**
     * @param initial
     * @return the full partner string
     */
    public static String getPartner(String initial) {
        return (String) fPartnersByInitials.get(initial);
    }
    
    /**
     * @return all initials of all partners
     */
    public static List getInitials() {
        List list=new ArrayList(fPartnersByInitials.keySet());
        Collections.sort(list);
        return list;
    }

}
