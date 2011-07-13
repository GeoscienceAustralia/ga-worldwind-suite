package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;

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
		
		assertEquals(ScreenOverlayPosition.SOUTH, attributes.getPosition());
		
		assertEquals(true, attributes.isDrawBorder());
		assertEquals(4, attributes.getBorderWidth());
		assertEquals(new Color(128, 128, 255), attributes.getBorderColor());
		
		assertEquals(new LengthExpression("100px"), attributes.getMinHeight());
		assertEquals(new LengthExpression("40%"), attributes.getMaxHeight());
		assertEquals(new LengthExpression("400px"), attributes.getMinWidth());
		assertEquals(new LengthExpression("500px"), attributes.getMaxWidth());
	}

	private Element load(String documentName)
	{
		Document document = XMLUtil.openDocument(getClass().getResource(documentName));
		return document.getDocumentElement();
	}
}
