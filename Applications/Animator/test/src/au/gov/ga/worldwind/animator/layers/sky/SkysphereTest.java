package au.gov.ga.worldwind.animator.layers.sky;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.WWXML;

import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Document;

import au.gov.ga.worldwind.animator.util.AVKeyMore;
import au.gov.ga.worldwind.test.util.TestUtils;


/**
 * Unit tests for the {@link Skysphere} class
 */
public class SkysphereTest
{
	
	private Skysphere classToBeTested; 
	
	@Test
	public void testParamCtorWithAllParams() throws Exception
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DISPLAY_NAME, "testName");
		params.setValue(AVKeyMore.URL, "testTexture.png");
		params.setValue(AVKeyMore.CONTEXT_URL, new URL("http://test/url"));
		params.setValue(AVKeyMore.SKYSPHERE_SLICES, 23);
		params.setValue(AVKeyMore.SKYSPHERE_SEGMENTS, 32);
		params.setValue(AVKeyMore.SKYSPHERE_ANGLE, 14d);
		
		classToBeTested = new Skysphere(params);
		
		assertEquals("testName", classToBeTested.getName());
		assertEquals("testTexture.png", TestUtils.getField(classToBeTested, "textureLocation", String.class));
		assertEquals(new URL("http://test/url"), TestUtils.getField(classToBeTested, "context", URL.class));
		
		assertEquals(23, (int)TestUtils.getField(classToBeTested, "slices", Integer.class));
		assertEquals(32, (int)TestUtils.getField(classToBeTested, "segments", Integer.class));
		assertEquals(Angle.fromDegrees(14), (Angle)TestUtils.getField(classToBeTested, "rotation", Angle.class));
	}
	
	@Test
	public void testParamCtorWithOptionalMissing() throws Exception
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DISPLAY_NAME, "testName");
		params.setValue(AVKeyMore.URL, "testTexture.png");
		params.setValue(AVKeyMore.CONTEXT_URL, new URL("http://test/url"));
		// params.setValue(AVKeyMore.SKYSPHERE_SLICES, 23); -- Test missing optional params
		// params.setValue(AVKeyMore.SKYSPHERE_SEGMENTS, 32);
		// params.setValue(AVKeyMore.SKYSPHERE_ANGLE, 14d);
		
		classToBeTested = new Skysphere(params);
		
		assertEquals("testName", classToBeTested.getName());
		assertEquals("testTexture.png", TestUtils.getField(classToBeTested, "textureLocation", String.class));
		assertEquals(new URL("http://test/url"), TestUtils.getField(classToBeTested, "context", URL.class));
		
		assertEquals(20, (int)TestUtils.getField(classToBeTested, "slices", Integer.class));
		assertEquals(20, (int)TestUtils.getField(classToBeTested, "segments", Integer.class));
		assertEquals(Angle.fromDegrees(0), (Angle)TestUtils.getField(classToBeTested, "rotation", Angle.class));
	}
	
	@Test
	public void testParamCtorWithMandatoryTextureLocationMissing() throws Exception
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DISPLAY_NAME, "testName");
		// params.setValue(AVKeyMore.URL, "testTexture.png"); -- Test missing texture location
		params.setValue(AVKeyMore.CONTEXT_URL, new URL("http://test/url"));
		params.setValue(AVKeyMore.SKYSPHERE_SLICES, 23);
		params.setValue(AVKeyMore.SKYSPHERE_SEGMENTS, 32);
		params.setValue(AVKeyMore.SKYSPHERE_ANGLE, 14d);
		
		try
		{
			classToBeTested = new Skysphere(params);
			fail("Expected illegal argument exception for mandatory texture location.");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	@Test
	public void testParamCtorWithMandatoryContextUrlMissing() throws Exception
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.DISPLAY_NAME, "testName");
		params.setValue(AVKeyMore.URL, "testTexture.png");
		// params.setValue(AVKeyMore.CONTEXT_URL, new URL("http://test/url")); -- Test missing context url
		params.setValue(AVKeyMore.SKYSPHERE_SLICES, 23);
		params.setValue(AVKeyMore.SKYSPHERE_SEGMENTS, 32);
		params.setValue(AVKeyMore.SKYSPHERE_ANGLE, 14d);
		
		try
		{
			classToBeTested = new Skysphere(params);
			fail("Expected illegal argument exception for mandatory context url.");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	@Test
	public void testXMLCtorWithCompleteDefinition() throws Exception
	{
		Document document = WWXML.openDocument(getClass().getResource("skysphereDefinitionComplete.xml"));
		
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.CONTEXT_URL, new URL("http://test/url"));
		
		classToBeTested = new Skysphere(document.getDocumentElement(), params);
		
		assertEquals("testName", classToBeTested.getName());
		assertEquals("testTexture.png", TestUtils.getField(classToBeTested, "textureLocation", String.class));
		assertEquals(new URL("http://test/url"), TestUtils.getField(classToBeTested, "context", URL.class));
		
		assertEquals(23, (int)TestUtils.getField(classToBeTested, "slices", Integer.class));
		assertEquals(32, (int)TestUtils.getField(classToBeTested, "segments", Integer.class));
		assertEquals(Angle.fromDegrees(14), (Angle)TestUtils.getField(classToBeTested, "rotation", Angle.class));
	}
	
	@Test
	public void testXMLCtorWithMinimumDefinition() throws Exception
	{
		Document document = WWXML.openDocument(getClass().getResource("skysphereDefinitionMinimum.xml"));
		
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.CONTEXT_URL, new URL("http://test/url"));
		
		classToBeTested = new Skysphere(document.getDocumentElement(), params);
		
		assertEquals("testTexture.png", TestUtils.getField(classToBeTested, "textureLocation", String.class));
		assertEquals(new URL("http://test/url"), TestUtils.getField(classToBeTested, "context", URL.class));
		
		assertEquals(20, (int)TestUtils.getField(classToBeTested, "slices", Integer.class));
		assertEquals(20, (int)TestUtils.getField(classToBeTested, "segments", Integer.class));
		assertEquals(Angle.fromDegrees(0), (Angle)TestUtils.getField(classToBeTested, "rotation", Angle.class));
	}
	
}
