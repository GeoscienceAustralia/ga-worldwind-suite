package au.gov.ga.worldwind.animator.layers.misc;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;

import nasa.worldwind.layers.FogLayer;

import org.junit.Test;
import org.w3c.dom.Document;

import au.gov.ga.worldwind.animator.util.AVKeyMore;


/**
 * Unit tests for the {@link FogLayerFactory} class
 */
public class FogLayerFactoryTest
{
	private static final double ALLOWED_ERROR = 0.0001;

	@Test
	public void testParamCtorWithAllParams()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DISPLAY_NAME, "testFogLayer");
		params.setValue(AVKeyMore.FOG_NEAR_FACTOR, 2f);
		params.setValue(AVKeyMore.FOG_FAR_FACTOR, 3f);
		params.setValue(AVKeyMore.FOG_COLOR, new Color(12,13,14,15));
		
		FogLayer result = FogLayerFactory.createFromParams(params);
		
		assertEquals("testFogLayer", result.getName());
		assertEquals(2f, result.getNearFactor(), ALLOWED_ERROR);
		assertEquals(3f, result.getFarFactor(), ALLOWED_ERROR);
		assertEquals(12, result.getColor().getRed());
		assertEquals(13, result.getColor().getGreen());
		assertEquals(14, result.getColor().getBlue());
		assertEquals(255, result.getColor().getAlpha());
	}
	
	@Test
	public void testXMLCtorWithCompleteDefinition()
	{
		Document document = WWXML.openDocument(getClass().getResource("fogLayerDefinitionComplete.xml"));
		
		FogLayer result = FogLayerFactory.createFromDefinition(document.getDocumentElement(), null);
		
		assertEquals("testName", result.getName());
		assertEquals(1.2f, result.getNearFactor(), ALLOWED_ERROR);
		assertEquals(1.3f, result.getFarFactor(), ALLOWED_ERROR);
		assertEquals(123, result.getColor().getRed());
		assertEquals(23, result.getColor().getGreen());
		assertEquals(222, result.getColor().getBlue());
		assertEquals(255, result.getColor().getAlpha());
	}
}
