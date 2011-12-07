package au.gov.ga.worldwind.common.layers.point;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import org.junit.Test;
import org.w3c.dom.Document;

import au.gov.ga.worldwind.common.layers.styled.Attribute;
import au.gov.ga.worldwind.common.layers.styled.StringWithPlaceholderGetter;
import au.gov.ga.worldwind.common.util.AVKeyMore;

/**
 * Unit tests for the {@link PointLayerUtils} class
 */
public class PointLayerUtilsTest
{
	@Test
	public void testCreatePointLayerNoAttributes()
	{
		Document doc = WWXML.openDocument(getClass().getResourceAsStream("pointLayerNoAttributes.xml"));
		PointLayer pointLayer = PointLayerUtils.createPointLayer(doc.getDocumentElement(), new AVListImpl());
		
		AVList params = (AVList)pointLayer.getValue(AVKeyMore.CONSTRUCTION_PARAMETERS);
		assertNotNull(params);
		
		// Attributes should be empty
		Attribute[] attributes = (Attribute[])params.getValue(AVKeyMore.POINT_ATTRIBUTES);
		assertNotNull(attributes);
		assertTrue(attributes.length == 0);
		
		// Test regular parameters
		assertEquals("PointLayerNoAttributes", pointLayer.getName());
		assertEquals("GA/TEST/testpoints.zip", pointLayer.getDataCacheName());
	}
	
	@Test
	public void testCreatePointLayerWithAttributes()
	{
		Document doc = WWXML.openDocument(getClass().getResourceAsStream("pointLayerWithAttributes.xml"));
		PointLayer pointLayer = PointLayerUtils.createPointLayer(doc.getDocumentElement(), new AVListImpl());
		
		AVList params = (AVList)pointLayer.getValue(AVKeyMore.CONSTRUCTION_PARAMETERS);
		assertNotNull(params);
		
		// Attributes should not be empty
		Attribute[] attributes = (Attribute[])params.getValue(AVKeyMore.POINT_ATTRIBUTES);
		assertNotNull(attributes);
		assertTrue(attributes.length == 1);
		
		assertEquals("NAME", attributes[0].getName());
		assertEquals("Name: %v%", StringWithPlaceholderGetter.getTextString(attributes[0]));
		assertEquals("%v%", StringWithPlaceholderGetter.getTextPlaceholder(attributes[0]));
		
		// Test regular parameters
		assertEquals("PointLayerWithAttributes", pointLayer.getName());
		assertEquals("GA/TEST/testpoints.zip", pointLayer.getDataCacheName());
	}
}
