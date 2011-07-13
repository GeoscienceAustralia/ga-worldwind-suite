package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.awt.Color;
import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Unit tests for the {@link ScreenOverlayLayerFactory} class
 */
public class ScreenOverlayLayerFactoryTest
{
	@Test
	public void testCreateFromFullXml()
	{
		ScreenOverlayLayer createdLayer = ScreenOverlayLayerFactory.createScreenOverlayLayer(load("screenOverlayLayerFull.xml"), null);
		
		assertNotNull(createdLayer);
		assertEquals("Overlay01", createdLayer.getName());
		
		ScreenOverlayAttributes attributes = createdLayer.getAttributes();
		assertEquals("http://somewhere.com/Something.html", attributes.getSourceUrl().toExternalForm());
		assertEquals(null, attributes.getSourceHtml());
		
		assertEquals(ScreenOverlayPosition.SOUTH, attributes.getPosition());
		
		assertEquals(true, attributes.isDrawBorder());
		assertEquals(4, attributes.getBorderWidth());
		assertEquals(new Color(128, 128, 255), attributes.getBorderColor());
		
		assertEquals(new LengthExpression("100px"), attributes.getMinHeight());
		assertEquals(new LengthExpression("40%"), attributes.getMaxHeight());
		assertEquals(new LengthExpression("400px"), attributes.getMinWidth());
		assertEquals(new LengthExpression("500px"), attributes.getMaxWidth());
	}

	@Test
	public void testCreateFromMinXmlWithContent()
	{
		ScreenOverlayLayer createdLayer = ScreenOverlayLayerFactory.createScreenOverlayLayer(load("screenOverlayMinWithContent.xml"), null);
		
		assertNotNull(createdLayer);
		
		ScreenOverlayAttributes attributes = createdLayer.getAttributes();
		assertEquals(null, attributes.getSourceUrl());
		assertEquals("<html><body>This is my dummy content</body></html>", attributes.getSourceHtml());
		
		assertDefaultsRemain(attributes);
	}

	@Test
	public void testCreateFromMinXmlWithUrl() throws Exception
	{
		AVListImpl params = new AVListImpl();
		params.setValue(ScreenOverlayKeys.CONTEXT_URL, new URL("http://somewhere.com"));
		ScreenOverlayLayer createdLayer = ScreenOverlayLayerFactory.createScreenOverlayLayer(load("screenOverlayMinWithUrl.xml"), params);
		
		assertNotNull(createdLayer);
		
		ScreenOverlayAttributes attributes = createdLayer.getAttributes();
		assertEquals("http://somewhere.com/Something.html", attributes.getSourceUrl().toExternalForm());
		assertEquals(null, attributes.getSourceHtml());
		
		assertDefaultsRemain(attributes);
	}
	
	private Element load(String documentName)
	{
		Document document = XMLUtil.openDocument(getClass().getResource(documentName));
		return document.getDocumentElement();
	}
	
	private ScreenOverlayAttributes getAttributeDefaults()
	{
		return new MutableScreenOverlayAttributesImpl();
	}
	
	private void assertDefaultsRemain(ScreenOverlayAttributes attributes)
	{
		ScreenOverlayAttributes defaults = getAttributeDefaults();
		assertEquals(defaults.getPosition(), attributes.getPosition());
		assertEquals(defaults.isDrawBorder(), attributes.isDrawBorder());
		assertEquals(defaults.getBorderWidth(), attributes.getBorderWidth());
		assertEquals(defaults.getBorderColor(), attributes.getBorderColor());
		assertEquals(defaults.getMinHeight(), attributes.getMinHeight());
		assertEquals(defaults.getMaxHeight(), attributes.getMaxHeight());
		assertEquals(defaults.getMinWidth(), attributes.getMinWidth());
		assertEquals(defaults.getMaxWidth(), attributes.getMaxWidth());
	}
}
