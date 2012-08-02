package au.gov.ga.worldwind.viewer.panels.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.w3c.dom.Document;

import au.gov.ga.worldwind.common.util.URLUtil;
import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.test.util.TestUtils;

/**
 * Unit tests for the {@link LayerTreePersistance} class
 */
public class LayerTreePersistenceTest
{

	@Test
	public void testSave() throws Exception
	{
		INode root = createNodeTree();
		
		Document document = LayerTreePersistance.saveToDocument(root);
		
		String result = getFormattedXml(document);
		String expected = TestUtils.readStreamToString(getClass().getResourceAsStream("layerTreePersistence.xml"));
		
		assertEquals(removeXmlDeclaration(convertLineBreaks(expected.trim())), 
				     removeXmlDeclaration(convertLineBreaks(result.trim())));
	}
	
	@Test
	public void testSaveWithNull() throws Exception
	{
		assertEquals(null, LayerTreePersistance.saveToDocument(null));
	}

	@Test
	public void testLoad() throws Exception
	{
		// TODO: This test runs very slowly!
		INode result = LayerTreePersistance.readFromXML(getClass().getResource("layerTreePersistence.xml"));
		INode expected = createNodeTree();
		
		assertNodeTreesEqual(expected, result);
	}
	
	@Test
	public void testLoadWithNull() throws Exception
	{
		assertEquals(null, LayerTreePersistance.readFromXML(null));
	}
	
	private INode createNodeTree()
	{
		FolderNode root = new FolderNode(null, null, null, true);
		
		FolderNode folder1 = new FolderNode("folder1", URLUtil.fromString("http://folder1/info"), URLUtil.fromString("http://folder1/icon"), true);
		folder1.addChild(new LayerNode("layer1", URLUtil.fromString("file://folder1/layer1/info"), URLUtil.fromString("file://folder1/layer1/icon"), true, URLUtil.fromString("file://folder1/layer1/layer"), true, 1.0, null));
		folder1.addChild(new LayerNode("layer2", URLUtil.fromString("file://folder1/layer2/info"), URLUtil.fromString("file://folder1/layer2/icon"), false, URLUtil.fromString("file://folder1/layer2/layer"), false, 0.5, null));
		root.addChild(folder1);
		
		WmsRootNode wmsRoot = new WmsRootNode("wmsRoot", URLUtil.fromString("http://wmsRoot/icon"), true);
		root.addChild(wmsRoot);
		
		wmsRoot.addChild(new WmsLayerNode("wmsLayer", URLUtil.fromString("http://wmsRoot/wmslayer/info"), URLUtil.fromString("http://wmsRoot/wmslayer/icon"), true, URLUtil.fromString("http://wmsRoot/wmslayer/layer"), true, 0.3, 1000L, URLUtil.fromString("http://wmsRoot/wmslayer/legend"), "theWmsLayer"));
		
		WmsServerNode wmsServer = new WmsServerNode("wmsServer", URLUtil.fromString("http://wmsRoot/wmsServer/icon"), false, URLUtil.fromString("http://wmsRoot/wmsServer/capabilities"));
		wmsRoot.addChild(wmsServer);
		wmsServer.addChild(new WmsLayerNode("wmsLayer1", URLUtil.fromString("http://wmsRoot/wmsServer/wmslayer1/info"), URLUtil.fromString("http://wmsRoot/wmsServer/wmslayer1/icon"), true, URLUtil.fromString("http://wmsRoot/wmsServer/wmslayer1/layer"), true, 0.4, 1001L, URLUtil.fromString("http://wmsRoot/wmsServer/wmslayer1/legend"), "theOtherWmsLayer"));
		
		return root;
	}
	
	private String getFormattedXml(Document document)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XMLUtil.saveDocumentToFormattedStream(document, outputStream);
		return outputStream.toString();
	}
	
	/**
	 * Standardise line break characters to allow proper comparison.
	 */
	private String convertLineBreaks(String string)
	{
		return string.replaceAll("(\\r|\\n)+", "\n");
	}
	
	/**
	 * Remove the XML declaration to prevent false test failures due to XML transformer
	 */
	private String removeXmlDeclaration(String string)
	{
		return string.replaceAll("<\\?xml.*\\?>", "").trim();
	}
	
	private void assertNodeTreesEqual(INode expected, INode result)
	{
		if (expected == null)
		{
			assertEquals(null, result);
			return;
		}
		
		if (expected instanceof FolderNode)
		{
			assertTrue(result instanceof FolderNode);
			assertFolderNodesEqual((FolderNode)expected, (FolderNode)result);
			return;
		}
		
		if (expected instanceof WmsLayerNode)
		{
			assertTrue(result instanceof WmsLayerNode);
			assertWmsNodesEqual((WmsLayerNode)expected, (WmsLayerNode)result);
			return;
		}
		
		if (expected instanceof LayerNode)
		{
			assertTrue(result instanceof LayerNode);
			assertLayerNodesEqual((LayerNode)expected, (LayerNode)result);
			return;
		}
	}
	
	private void assertFolderNodesEqual(FolderNode expected, FolderNode result)
	{
		assertEquals(expected.getName(), result.getName());
		assertEquals(expected.getIconURL(), result.getIconURL());
		assertEquals(expected.getInfoURL(), result.getInfoURL());
		assertEquals(expected.isExpanded(), result.isExpanded());
		assertEquals(expected.getChildCount(), result.getChildCount());
		for (int i = 0; i < expected.getChildCount(); i++)
		{
			assertNodeTreesEqual(expected.getChild(i), result.getChild(i));
		}
	}
	
	private void assertLayerNodesEqual(LayerNode expected, LayerNode result)
	{
		assertEquals(expected.getName(), result.getName());
		assertEquals(expected.getIconURL(), result.getIconURL());
		assertEquals(expected.getInfoURL(), result.getInfoURL());
		assertEquals(expected.isExpanded(), result.isExpanded());
		assertEquals(expected.getLayerURL(), result.getLayerURL());
		assertEquals(expected.getLegendURL(), result.getLegendURL());
		assertEquals(expected.getOpacity(), result.getOpacity(), 0.0001);
		assertEquals(expected.isEnabled(), result.isEnabled());
		assertEquals(expected.getExpiryTime(), result.getExpiryTime());
	}
	
	private void assertWmsNodesEqual(WmsLayerNode expected, WmsLayerNode result)
	{
		assertLayerNodesEqual(expected, result);
		assertEquals(expected.getLayerId(), result.getLayerId());
	}

}
