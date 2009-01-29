/**
 * 
 */
package de.ingrid.iplug.csw.dsc.tools;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;

/**
 * @author Administrator
 *
 */
public class SimpleSpringBeanFactoryTest extends TestCase {

	/**
	 * Test method for {@link de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory#getBean(java.lang.String, java.lang.Class)}.
	 */
	public void testGetBean() {
		assertNotNull(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_FACTORY, CSWFactory.class));
		assertNotNull(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_QUERY_TEMPLATE, CSWQuery.class));
		assertNotNull(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_CACHE, Cache.class));
		assertNotNull(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_HARVEST_FILTER, String.class));
		assertNotNull(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_MAPPER, DocumentMapper.class));
	}

}
