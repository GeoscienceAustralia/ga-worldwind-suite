package au.gov.ga.worldwind.viewer.panels.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindowImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.viewer.terrain.SectionListCompoundElevationModel;

/**
 * Unit tests for the {@link LayerTreeModel} class
 */
public class LayerTreeModelTest
{
	
	private WorldWindowImpl worldWindow;
	private LayerTreeModel classUnderTest;
	
	@Before
	public void setup()
	{
		Configuration.setValue(AVKey.OFFLINE_MODE, true);
		
		worldWindow = new WorldWindowImpl();
		worldWindow.setModel(new BasicModel());
		worldWindow.getModel().setLayers(new ExtendedLayerList());
		worldWindow.getModel().getGlobe().setElevationModel(new SectionListCompoundElevationModel());
		
		LayerTree tree = new LayerTree(worldWindow, new FolderNode("Root", null, null, true));
		classUnderTest = tree.getLayerModel();
	}
	
	@Test
	public void testAddWmsLayerWithNoRootNode() throws Exception
	{
		assertNull(classUnderTest.getWmsRootNode());
		
		WMSLayerInfo wmsLayerInfo = createWmsLayerInfo();
		classUnderTest.addWmsLayer(wmsLayerInfo);
		
		// Test the tree below the wms root node is correct
		assertNotNull(classUnderTest.getWmsRootNode());
		assertEquals(1, classUnderTest.getWmsRootNode().getChildCount());
		assertTrue(classUnderTest.getWmsRootNode().getChild(0) instanceof WmsServerNode);
		
		// Test the layer was added to the layer list
		List<ILayerNode> layerNodes = classUnderTest.getLayerNodes();
		assertEquals(1, layerNodes.size());
		assertTrue(layerNodes.get(0) instanceof WmsLayerNode);
	}
	
	@Test
	public void testAddWmsLayerWithRootNodeExists() throws Exception
	{
		WmsRootNode wmsRootNode = classUnderTest.createAndAddWmsRootNode();
		
		WMSLayerInfo wmsLayerInfo = createWmsLayerInfo();
		classUnderTest.addWmsLayer(wmsLayerInfo);
		
		assertEquals(wmsRootNode, classUnderTest.getWmsRootNode());
		
		// Test the tree below the wms root node is correct
		assertNotNull(classUnderTest.getWmsRootNode());
		assertEquals(1, classUnderTest.getWmsRootNode().getChildCount());
		assertTrue(classUnderTest.getWmsRootNode().getChild(0) instanceof WmsServerNode);
		
		// Test the layer was added to the layer list
		List<ILayerNode> layerNodes = classUnderTest.getLayerNodes();
		assertEquals(1, layerNodes.size());
		assertTrue(layerNodes.get(0) instanceof WmsLayerNode);
	}
	
	@Test
	public void testAddWmsLayerWithRootNodeInsideTree() throws Exception
	{
		// Setup a tree
		//
		// Root/
		//  +-Folder1/
		//     +-Folder2/
		//     |  +-WmsRootNode/
		//     |  +-Folder4/
		//     +-Folder3/
		
		FolderNode folder1 = new FolderNode("Folder1", null, null, true);
		FolderNode folder2 = new FolderNode("Folder2", null, null, true);
		FolderNode folder3 = new FolderNode("Folder3", null, null, true);
		FolderNode folder4 = new FolderNode("Folder4", null, null, true);
		folder1.addChild(folder2);
		folder1.addChild(folder3);
		folder2.addChild(folder4);
		
		classUnderTest.addToRoot(folder1, true);
		
		WmsRootNode wmsRootNode = classUnderTest.createAndAddWmsRootNode();
		wmsRootNode.getParent().removeChild(wmsRootNode); // TODO: Node tree does not maintain parent-child integrity by itself. Need to fix.
		folder2.addChild(wmsRootNode);
		
		// Confirm there are no layers yet
		assertEquals(0, classUnderTest.getLayerNodes().size());
		
		// Add a WMS layer to the model
		WMSLayerInfo wmsLayerInfo = createWmsLayerInfo();
		classUnderTest.addWmsLayer(wmsLayerInfo);
		
		// Check the WMS root node hasn't moved
		assertEquals(wmsRootNode, classUnderTest.getWmsRootNode());
		assertEquals(folder2, wmsRootNode.getParent());
		assertTrue(folder2.getChildren().contains(wmsRootNode));
		
		// Check the layer was added below the WMS root node
		//
		// Root/
		//  +-Folder1/
		//     +-Folder2/
		//     |  +-WmsRootNode/
		//     |  |  +-WmsServerNode/
		//     |  |     +-WmsLayerNode
		//     |  +-Folder4/
		//     +-Folder3/
		
		assertEquals(1, classUnderTest.getWmsRootNode().getChildCount());
		assertTrue(classUnderTest.getWmsRootNode().getChild(0) instanceof WmsServerNode);
		WmsServerNode serverNode = (WmsServerNode)classUnderTest.getWmsRootNode().getChild(0);
		assertTrue(serverNode.isOriginOf(wmsLayerInfo));
		assertEquals(1, serverNode.getChildCount());
		assertTrue(serverNode.getChild(0) instanceof WmsLayerNode);
		WmsLayerNode layerNode = (WmsLayerNode)serverNode.getChild(0);
		assertEquals(layerNode.getLayerInfo(), wmsLayerInfo);
		
		// Check the layer was added to the model's list of layers
		List<ILayerNode> layerNodes = classUnderTest.getLayerNodes();
		assertEquals(1, layerNodes.size());
		assertEquals(layerNode, layerNodes.get(0));
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
