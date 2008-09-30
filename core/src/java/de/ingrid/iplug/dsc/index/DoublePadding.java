/**
 * 
 */
package de.ingrid.iplug.dsc.index;

import java.text.DecimalFormat;

/**
 * @author marko
 * 
 */
public class DoublePadding {

    private static final DecimalFormat FORMAT = new DecimalFormat("0000000000.####");

    /**
     * This method fills a number with leading zeros. So we get a number with ten digits before the point a
     * and four digits past the point
     * 
     * @param number
     *            The number to pad.
     * @return The padded number as a string.
     */
    public static String padding(double number) {
        return FORMAT.format(number);
    }
}
