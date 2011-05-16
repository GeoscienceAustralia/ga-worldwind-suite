package au.gov.ga.worldwind.common.layers.earthquakes;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.earthquakes.RSSEarthquakesLayer.Earthquake;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Unit tests for the {@link RSSEarthquakesLayer} and it's inner classes
 */
public class RSSEarthquakesLayerTest
{
	@Test
	public void testNewEarthquakeWithNull()
	{
		try
		{
			new Earthquake(null);
			fail("Expected an illegal argument exception. Got none.");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	@Test
	public void testNewEarthquakeWithBasicCase()
	{
		Earthquake quake = new Earthquake(loadXmlElementFromResource("basicRssElementExample.xml"));
		
		assertNotNull(quake);
	}
	
	private Element loadXmlElementFromResource(String resourceName)
	{
		Document doc = XMLUtil.openDocument(this.getClass().getResourceAsStream(resourceName));
		return doc.getDocumentElement();
	}
}
