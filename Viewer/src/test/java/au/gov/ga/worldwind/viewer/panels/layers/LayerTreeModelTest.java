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
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLContext;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.common.ui.SwingEDTException;
import au.gov.ga.worldwind.common.ui.SwingEDTInvoker;
import au.gov.ga.worldwind.common.ui.SwingUtil;


/**
 * Unit tests for the {@link LayerTreeModel} class
 */
public class LayerTreeModelTest
{
	
	private WorldWindowImpl worldWindow;
	private LayerTreeModel classUnderTest;
	private TreeModelTestListener treeListener;
	
	@Before
	public void setup()
	{
		Configuration.setValue(AVKey.OFFLINE_MODE, true);
		
		BasicModel model = new BasicModel();
		model.setLayers(new SectionListLayerList());
		model.getGlobe().setElevationModel(new SectionListCompoundElevationModel());
		
		worldWindow = new WorldWindowTestImpl();
		worldWindow.setModel(model);
		
		LayerTree tree = new LayerTree(worldWindow, new FolderNode("Root", null, null, true));
		classUnderTest = tree.getLayerModel();
		
		treeListener = new TreeModelTestListener();
		classUnderTest.addTreeModelListener(treeListener);
		
		SwingUtil.setInvoker(new SwingEDTInvoker()
		{
			@Override
			public void invokeTaskOnEDT(Runnable task) throws SwingEDTException
			{
				task.run();
			}
			
			@Override
			public void invokeLaterTaskOnEDT(Runnable task)
			{
				task.run();
			}
		});
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
	
	@Test
	public void testAddWmsLayerFiresNodeInsertedEventsCorrectlyNoRootNode() throws Exception
	{
		assertNull(classUnderTest.getWmsRootNode());
		
		WMSLayerInfo wmsLayerInfo = createWmsLayerInfo();
		classUnderTest.addWmsLayer(wmsLayerInfo);
		
		// Expect three events:
		// 1. New WMS root node
		// 2. New WMS server node
		// 3. New WMS layer node
		assertEquals(3, treeListener.nodesInstertedEvents.size());
		assertTrue(treeListener.nodesInstertedEvents.get(0).getChildren()[0] instanceof WmsRootNode);
		assertTrue(treeListener.nodesInstertedEvents.get(1).getChildren()[0] instanceof WmsServerNode);
		assertTrue(treeListener.nodesInstertedEvents.get(2).getChildren()[0] instanceof WmsLayerNode);
	}
	
	@Test
	public void testAddWmsLayerFiresNodeInsertedEventsCorrectlyExistingTree() throws Exception
	{
		assertNull(classUnderTest.getWmsRootNode());
		
		WMSLayerInfo wmsLayerInfo = createWmsLayerInfo();
		classUnderTest.addWmsLayer(wmsLayerInfo);
		treeListener.clearLists();
		
		classUnderTest.addWmsLayer(wmsLayerInfo);
		
		// Expect one events:
		// 1. New WMS layer node
		assertEquals(1, treeListener.nodesInstertedEvents.size());
		assertTrue(treeListener.nodesInstertedEvents.get(0).getChildren()[0] instanceof WmsLayerNode);
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
	
	private static class TreeModelTestListener implements TreeModelListener
	{
		List<TreeModelEvent> treeStructureEvents = new ArrayList<TreeModelEvent>();
		List<TreeModelEvent> nodesRemovedEvents = new ArrayList<TreeModelEvent>();
		List<TreeModelEvent> nodesInstertedEvents = new ArrayList<TreeModelEvent>();
		List<TreeModelEvent> nodesChangedEvents = new ArrayList<TreeModelEvent>();

		@Override
		public void treeStructureChanged(TreeModelEvent e)
		{
			treeStructureEvents.add(e);
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent e)
		{
			nodesRemovedEvents.add(e);
		}

		@Override
		public void treeNodesInserted(TreeModelEvent e)
		{
			nodesInstertedEvents.add(e);
		}

		@Override
		public void treeNodesChanged(TreeModelEvent e)
		{
			nodesChangedEvents.add(e);
		}
		
		public void clearLists()
		{
			treeStructureEvents.clear();
			nodesRemovedEvents.clear();
			nodesInstertedEvents.clear();
			nodesChangedEvents.clear();
		}
	}
	
	private static class WorldWindowTestImpl extends WorldWindowImpl
	{
		@Override
		public GLContext getContext()
		{
			return null;
		}
		
	}
}
