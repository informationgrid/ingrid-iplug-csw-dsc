package de.ingrid.iplug.csw.dsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

public class TestUtil {

	final protected static Log log = LogFactory.getLog(TestUtil.class);

	/**
	 * File related methods
	 */

	public static final boolean deleteDirectory(File directory) {
		if (!directory.exists()) {
			return true;
		}
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					if (!deleteDirectory(file)) {
						return false;
					}
				} else {
					if (!file.delete()) {
						return false;
					}
				}
			}
		}
		return directory.delete();
	}

	public static void copy(File sourceFile, File targetFile) {
		targetFile.getParentFile().mkdirs();
		try {
			InputStream in = new FileInputStream(sourceFile);
			OutputStream out = new FileOutputStream(targetFile);
			copy(in, out, true);
		} catch (IOException e) {
			throw new RuntimeException("could not copy " + sourceFile + " to "
					+ targetFile, e);
		}
	}

	public static void copy(InputStream inputStream, OutputStream outputStream,
			boolean close) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = inputStream.read(buf)) > 0) {
			outputStream.write(buf, 0, len);
		}
		if (close) {
			inputStream.close();
			outputStream.close();
		}
	}
	
	public static PlugDescription getPlugDescription() throws IOException {
		// read the PlugDescription
	    File descFile = new File("src/conf/plugdescription_test_csw-2.0.2-AP-ISO-1.0.xml");
		XMLSerializer serializer = new XMLSerializer();
		serializer.aliasClass(PlugDescription.class.getName(), PlugDescription.class);
		PlugDescription desc = (PlugDescription)serializer.deSerialize(descFile);
		return desc;
	}
	
	/**
	 * Record related methods
	 */

    private final static String dataFolder = "./resources/test_records";

	public static Set<String> getRecordIds() {
		Set<String> recordIds = new HashSet<String>();
		
		// read all record ids from the storage
		File dataLocation = new File(dataFolder);
		File[] files = dataLocation.listFiles();
		if (files != null) {
			for (int i=0; i<files.length; i++) {
				File file = files[i];
				if (!file.isDirectory() && file.getName().endsWith(".xml")) {
					String filename = file.getName();
					recordIds.add(filename.substring(0, filename.lastIndexOf("_")));
				}
			}
		}
		return recordIds;
	}

	public static CSWRecord getRecord(String id, ElementSetName elementSetName, CSWRecord record) {
		try {
			log.debug("getting Record '"+id+"'");
			return getRecordNode(id, elementSetName, record);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getRecordTitle(CSWRecord record) {
		return XPathUtils.getString(record.getOriginalResponse(), "//title/CharacterString");
	}

	public static void setRecordTitle(CSWRecord record, String title) {
		Node titleNode = XPathUtils.getNode(record.getOriginalResponse(), "//title/CharacterString");
		titleNode.setTextContent(title);
	}

	private static CSWRecord getRecordNode(String id, ElementSetName elementSetName, CSWRecord record) throws Exception {
		
		File file = new File(dataFolder+"/"+id+"_"+elementSetName.toString()+".xml");
		StringBuilder content = new StringBuilder();
		BufferedReader input =  new BufferedReader(new FileReader(file));
		try {
			String line = null;
			while((line = input.readLine()) != null) {
				content.append(line);
				content.append(System.getProperty("line.separator"));
			}
			input.close();
			input = null;
			
			Document document = StringUtils.stringToDocument(content.toString());
			record.initialize(elementSetName, document.getFirstChild());
			return record;
		}
		finally {
			if (input != null)
				input.close();
		}
	}
}
