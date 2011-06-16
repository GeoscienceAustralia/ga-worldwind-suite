package au.gov.ga.worldwind.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.common.util.GASandpit.SandpitURLTransform;

/**
 * Unit tests for the {@link GASandpit} class
 */
public class GASandpitTest
{

	@Before
	public void setup()
	{
		URLTransformer.clearTransforms();
		GASandpit.setSandpitMode(false);
	}
	
	/*
	 * Tests for the GASandpit singleton
	 */
	@Test
	public void testEnableAddsSandpitTransform()
	{
		assertTrue(URLTransformer.getTransforms().isEmpty());
		
		GASandpit.setSandpitMode(true);
		
		assertFalse(URLTransformer.getTransforms().isEmpty());
		assertEquals(1, URLTransformer.getTransforms().size());
		assertTrue(URLTransformer.getTransforms().get(0) instanceof SandpitURLTransform);
		
		assertTrue(GASandpit.isSandpitMode());
	}
	
	@Test
	public void testEnableTwiceAddsSingleSandpitTransform()
	{
		assertTrue(URLTransformer.getTransforms().isEmpty());
		
		GASandpit.setSandpitMode(true);
		GASandpit.setSandpitMode(true);
		
		assertFalse(URLTransformer.getTransforms().isEmpty());
		assertEquals(1, URLTransformer.getTransforms().size());
		assertTrue(URLTransformer.getTransforms().get(0) instanceof SandpitURLTransform);
		
		assertTrue(GASandpit.isSandpitMode());
	}
	
	@Test
	public void testDisabledRemovesSandpitTransform()
	{
		assertTrue(URLTransformer.getTransforms().isEmpty());
		
		GASandpit.setSandpitMode(true);
		GASandpit.setSandpitMode(false);
		
		assertTrue(URLTransformer.getTransforms().isEmpty());
		
		assertFalse(GASandpit.isSandpitMode());
	}
	
	@Test
	public void testEnableSetsNonProxyHosts()
	{
		assertEquals(null, System.getProperty("http.nonProxyHosts"));
		
		GASandpit.setSandpitMode(true);
		
		assertEquals("*.ga.gov.au", System.getProperty("http.nonProxyHosts"));
	}
	
	@Test
	public void testDisableRemovesNonProxyHosts()
	{
		GASandpit.setSandpitMode(true);
		System.setProperty("http.nonProxyHosts", "bob");
		
		GASandpit.setSandpitMode(false);
		
		assertEquals(null, System.getProperty("http.nonProxyHosts"));
	}
	
	/*
	 * Tests for the SandpitUrlTransform
	 */
	
	@Test
	public void testSandpitUrlTransformWithNull()
	{
		SandpitURLTransform classUnderTest = new SandpitURLTransform();
		
		assertEquals(null, classUnderTest.transformURL(null));
	}
	
	@Test
	public void testSandpitUrlTransformWithBlank()
	{
		SandpitURLTransform classUnderTest = new SandpitURLTransform();
		
		assertEquals(null, classUnderTest.transformURL("   "));
	}
	
	@Test
	public void testSandpitUrlTransformWithGAExternalUrl()
	{
		SandpitURLTransform classUnderTest = new SandpitURLTransform();
		
		assertEquals("http://www.ga.gov.au:8500/some/url.xml", classUnderTest.transformURL("http://www.ga.gov.au/some/url.xml"));
	}
	
	@Test
	public void testSandpitUrlTransformWithGAInternalUrl()
	{
		SandpitURLTransform classUnderTest = new SandpitURLTransform();
		
		assertEquals("http://www.ga.gov.au:8500/some/url.xml", classUnderTest.transformURL("http://www.ga.gov.au:8500/some/url.xml"));
	}
	
	@Test
	public void testSandpitUrlTransformWithExternalUrl()
	{
		SandpitURLTransform classUnderTest = new SandpitURLTransform();
		
		assertEquals("http://www.somewhere.com/some/url.xml", classUnderTest.transformURL("http://www.somewhere.com/some/url.xml"));
	}
}
