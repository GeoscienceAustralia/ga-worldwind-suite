package au.gov.ga.worldwind.viewer.panels.layers;

import static au.gov.ga.worldwind.viewer.data.messages.ViewerMessageConstants.getTreeWmsRootNodeLabel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.URLUtil;
import au.gov.ga.worldwind.common.util.message.MessageSource;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;

/**
 * Unit tests for the {@link WmsRootNode} class
 */
public class WmsRootNodeTest
{
	private static MessageSource originalMessageSource;
	
	private static StaticMessageSource messageSource;
	
	@BeforeClass
	public static void setMessages()
	{
		originalMessageSource = MessageSourceAccessor.get();
		messageSource = new StaticMessageSource();
		MessageSourceAccessor.set(messageSource);
	}
	
	@AfterClass
	public static void resetMessages()
	{
		MessageSourceAccessor.set(originalMessageSource);
	}
	
	@Test
	public void testDefaultConstructor()
	{
		messageSource.addMessage(getTreeWmsRootNodeLabel(), "theName");
		
		WmsRootNode classUnderTest = new WmsRootNode();
		
		assertEquals("theName", classUnderTest.getName());
		assertEquals(true, classUnderTest.isExpanded());
		assertEquals(Icons.wmsbrowser.getURL(), classUnderTest.getIconURL());
	}
	
	@Test
	public void testCreationOfLayerNodesFromRootNode() throws Exception
	{
		WmsRootNode classUnderTest = new WmsRootNode();
		
		WMSLayerInfo layer = createWmsLayerInfo();
		WmsLayerNode layerNode = classUnderTest.addWmsLayer(layer);
		
		// The layer node should contain the correct urls
		assertEquals("http://neo.sci.gsfc.nasa.gov/palettes/files/modis_fire_l3.gif", layerNode.getLegendURL().toExternalForm());
		assertEquals("http://neo.sci.gsfc.nasa.gov/Search.html?datasetId=MOD14A1_M_FIRE", layerNode.getInfoURL().toExternalForm());
	}
	
	@Test
	public void testAddWmsWithNoServerNodes() throws Exception
	{
		WmsRootNode classUnderTest = new WmsRootNode();
		
		WMSLayerInfo layer = createWmsLayerInfo();
		WmsLayerNode layerNode = classUnderTest.addWmsLayer(layer);
		
		// The root should now have exactly 1 child, being the server node
		assertEquals(1, classUnderTest.getChildCount());
		assertTrue(classUnderTest.getChild(0) instanceof WmsServerNode);
		
		// The server node should have exactly 1 child, being the layer node
		WmsServerNode serverNode = (WmsServerNode)classUnderTest.getChild(0);
		assertEquals("NASA Earth Observations (NEO) WMS", serverNode.getName());
		assertEquals("http://neowms.sci.gsfc.nasa.gov/wms/wms", serverNode.getServerCapabilitiesUrl());
		assertEquals(1, serverNode.getChildCount());
		assertEquals(layerNode, serverNode.getChild(0));
	}

	@Test
	public void testAddWmsWithExistingServerNode() throws Exception
	{
		WmsRootNode classUnderTest = new WmsRootNode();
		
		WmsServerNode serverNode = new WmsServerNode("dummyServer", null, true, URLUtil.fromString("http://neowms.sci.gsfc.nasa.gov/wms/wms"));
		classUnderTest.addChild(serverNode);
		
		WMSLayerInfo layer = createWmsLayerInfo();
		WmsLayerNode layerNode = classUnderTest.addWmsLayer(layer);
		
		// The root should now have exactly 1 child, being the original server node
		assertEquals(1, classUnderTest.getChildCount());
		assertEquals(serverNode, classUnderTest.getChild(0));
		
		// The original server node should now have the layer as a child
		assertEquals(1, serverNode.getChildCount());
		assertEquals(layerNode, serverNode.getChild(0));
	}
	
	/**
	 * Creates a default WMS layer with the following properties:
	 * <ul>
	 * 	<li>Server Capabilities URL: http://neowms.sci.gsfc.nasa.gov/wms/wms
	 * 	<li>Server Name: NASA Earth Observations (NEO) WMS
	 * 	<li>Layer Name: MOD14A1_M_FIRE
	 * 	<li>Layer Style: rgb
	 * 	<li>Layer Legend: http://neo.sci.gsfc.nasa.gov/palettes/files/modis_fire_l3.gif
	 * 	<li>Layer Info: http://neo.sci.gsfc.nasa.gov/Search.html?datasetId=MOD14A1_M_FIRE
	 * </ul>
	 */
	private WMSLayerInfo createWmsLayerInfo() throws Exception
	{
		WMSCapabilities caps = new WMSCapabilities(getClass().getResourceAsStream("wmsCapabilities.xml")).parse();
		WMSLayerCapabilities layerCaps = caps.getLayerByName("MOD14A1_M_FIRE");
		WMSLayerStyle style = layerCaps.getStyleByName("rgb");
		
		WMSLayerInfo result = new WMSLayerInfo(caps, layerCaps, style);
		return result;
	}
	
}
