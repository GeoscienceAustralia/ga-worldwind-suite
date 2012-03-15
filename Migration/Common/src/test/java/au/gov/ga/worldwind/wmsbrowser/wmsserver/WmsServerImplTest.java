package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.net.URL;

import org.junit.Test;

/**
 * Tests for the {@link WmsServerImpl} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class WmsServerImplTest
{

	@Test
	public void testCreateWithNullUrl()
	{
		try
		{
			new WmsServerImpl((URL)null);
			fail("Expected illegal argument exception");
		}
		catch(IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	@Test
	public void testCreateWithEatlasWms()
	{
		URL capabilites = this.getClass().getClassLoader().getResource("wmsbrowser/eatlasWmsCapabilities.xml");
		WmsServerImpl classUnderTest = new WmsServerImpl(capabilites);
		
		// Should not have loaded anything yet...
		assertEquals(capabilites.toExternalForm(), classUnderTest.getName());
		assertNull(classUnderTest.getCapabilities());
		assertNull(classUnderTest.getLayers());
	}
	
	@Test
	public void testLoadWithEatlasWms() throws Exception
	{
		URL capabilites = this.getClass().getClassLoader().getResource("wmsbrowser/eatlasWmsCapabilities.xml");
		WmsServerImpl classUnderTest = new WmsServerImpl(capabilites);
		classUnderTest.setCapabilitiesService(new LocalResourceCapabilitiesService());
		classUnderTest.loadLayersImmediately();
		
		assertNotNull(classUnderTest.getLayers());
		assertEquals(90, classUnderTest.getLayers().size());
		
		WMSLayerInfo wmsLayerInfo = classUnderTest.getLayers().get(0);
		assertEquals("ea:GBR_JCU_3DGBR-geomorph_GBR-dryreef", wmsLayerInfo.getTitle());
		assertNotNull(wmsLayerInfo.getCaps());
		
		assertEquals(capabilites.toExternalForm(), classUnderTest.getName());
	}
	
	@Test
	public void testCreateWithNasaNeoWms()
	{
		URL capabilites = this.getClass().getClassLoader().getResource("wmsbrowser/nasaNeoWmsCapabilities.xml");
		WmsServerImpl classUnderTest = new WmsServerImpl(capabilites);
		
		// Should not have loaded anything yet...
		assertEquals(capabilites.toExternalForm(), classUnderTest.getName());
		assertNull(classUnderTest.getCapabilities());
		assertNull(classUnderTest.getLayers());
	}
	
	@Test
	public void testLoadWithNasaNeoWms() throws Exception
	{
		URL capabilites = this.getClass().getClassLoader().getResource("wmsbrowser/nasaNeoWmsCapabilities.xml");
		WmsServerImpl classUnderTest = new WmsServerImpl(capabilites);
		classUnderTest.setCapabilitiesService(new LocalResourceCapabilitiesService());
		classUnderTest.loadLayersImmediately();
		
		assertNotNull(classUnderTest.getLayers());
		assertEquals(127, classUnderTest.getLayers().size());
		
		WMSLayerInfo wmsLayerInfo = classUnderTest.getLayers().get(0);
		assertEquals("Active Fires (1 month - Terra/MODIS)", wmsLayerInfo.getTitle());
		assertNotNull(wmsLayerInfo.getCaps());
		
		assertEquals(capabilites.toExternalForm(), classUnderTest.getName());
	}
}
