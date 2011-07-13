package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import static org.junit.Assert.*;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.awt.Color;
import java.net.URL;

import org.junit.Test;

/**
 * Unit tests for the {@link MutableScreenOverlayAttributesImpl} class
 */
public class MutableScreenOverlayAttributesImplTest
{
	private static final float DELTA = 0.0001f;

	// Constructor tests
	@Test
	public void testCreateFromParamsWithUrl() throws Exception
	{
		AVListImpl params = new AVListImpl();
		params.setValue(ScreenOverlayKeys.URL, new URL("http://somewhere.com/Something.html"));
		
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(params);
		
		assertEquals("http://somewhere.com/Something.html", classUnderTest.getSourceUrl().toExternalForm());
		assertNull(classUnderTest.getSourceHtml());
		
		assertDefaultsRemain(classUnderTest);
	}
	
	@Test
	public void testCreateFromParamsWithStringUrlAndContextUrl() throws Exception
	{
		AVListImpl params = new AVListImpl();
		params.setValue(ScreenOverlayKeys.CONTEXT_URL, new URL("http://somewhere.com"));
		params.setValue(ScreenOverlayKeys.URL, "Something.html");
		
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(params);
		
		assertEquals("http://somewhere.com/Something.html", classUnderTest.getSourceUrl().toExternalForm());
		assertNull(classUnderTest.getSourceHtml());
		
		assertDefaultsRemain(classUnderTest);
	}
	
	@Test
	public void testCreateFromParamsWithStringUrlButNoContextUrl() throws Exception
	{
		AVListImpl params = new AVListImpl();
		params.setValue(ScreenOverlayKeys.URL, "http://somewhere.com/Something.html");
		
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(params);
		
		assertEquals("http://somewhere.com/Something.html", classUnderTest.getSourceUrl().toExternalForm());
		assertNull(classUnderTest.getSourceHtml());
		
		assertDefaultsRemain(classUnderTest);
	}
	
	@Test
	public void testCreateFromParamsWithNoUrlButHtml() throws Exception
	{
		AVListImpl params = new AVListImpl();
		params.setValue(ScreenOverlayKeys.OVERLAY_CONTENT, "http://somewhere.com/Something.html");
		
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(params);
		
		assertEquals("http://somewhere.com/Something.html", classUnderTest.getSourceHtml());
		assertNull(classUnderTest.getSourceUrl());
		
		assertDefaultsRemain(classUnderTest);
	}
	
	private void assertDefaultsRemain(MutableScreenOverlayAttributesImpl classUnderTest)
	{
		MutableScreenOverlayAttributesImpl defaults = new MutableScreenOverlayAttributesImpl();
		
		assertEquals(defaults.getPosition(), classUnderTest.getPosition());
		
		assertEquals(defaults.getMinHeight(), classUnderTest.getMinHeight());
		assertEquals(defaults.getMaxHeight(), classUnderTest.getMaxHeight());
		assertEquals(defaults.getMinWidth(), classUnderTest.getMinWidth());
		assertEquals(defaults.getMaxWidth(), classUnderTest.getMaxWidth());
		
		assertEquals(defaults.isDrawBorder(), classUnderTest.isDrawBorder());
		assertEquals(defaults.getBorderColor(), classUnderTest.getBorderColor());
		assertEquals(defaults.getBorderWidth(), classUnderTest.getBorderWidth());
	}

	@Test
	public void testCreateFromParamsWithNoUrlOrHtml() throws Exception
	{
		try
		{
			AVListImpl params = new AVListImpl();
		
			new MutableScreenOverlayAttributesImpl(params);
			
			fail("Expected illegal argument exception for missing source data");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	@Test
	public void testCreateFromParamsWithAllParams() throws Exception
	{
		AVListImpl params = new AVListImpl();
		params.setValue(ScreenOverlayKeys.URL, "http://somewhere.com/Something.html");
		params.setValue(ScreenOverlayKeys.POSITION, ScreenOverlayPosition.NORTHEAST);
		params.setValue(ScreenOverlayKeys.MIN_HEIGHT, "100px");
		params.setValue(ScreenOverlayKeys.MAX_HEIGHT, new LengthExpression("200%"));
		params.setValue(ScreenOverlayKeys.MIN_WIDTH, null);
		params.setValue(ScreenOverlayKeys.MAX_WIDTH, "300%");
		params.setValue(ScreenOverlayKeys.BORDER_WIDTH, 3);
		params.setValue(ScreenOverlayKeys.BORDER_COLOR, Color.RED);
		params.setValue(ScreenOverlayKeys.DRAW_BORDER, true);
		
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(params);
		
		assertEquals("http://somewhere.com/Something.html", classUnderTest.getSourceUrl().toExternalForm());
		assertNull(classUnderTest.getSourceHtml());
		
		assertEquals(ScreenOverlayPosition.NORTHEAST, classUnderTest.getPosition());
		
		assertEquals(new LengthExpression("100px"), classUnderTest.getMinHeight());
		assertEquals(new LengthExpression("200%"), classUnderTest.getMaxHeight());
		assertEquals(null, classUnderTest.getMinWidth());
		assertEquals(new LengthExpression("300%"), classUnderTest.getMaxWidth());
		
		assertEquals(true, classUnderTest.isDrawBorder());
		assertEquals(Color.RED, classUnderTest.getBorderColor());
		assertEquals(3, classUnderTest.getBorderWidth());
	}
	
	// Dimension tests
	
	@Test
	public void testGetWidthFixedRangeScreenLarger()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("100px");
		classUnderTest.setMaxWidth("200px");
		
		assertEquals(200, classUnderTest.getWidth(250), DELTA);
	}
	
	@Test
	public void testGetWidthFixedRangeScreenSmaller()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("100px");
		classUnderTest.setMaxWidth("200px");
		
		assertEquals(100, classUnderTest.getWidth(50), DELTA);
	}
	
	@Test
	public void testGetWidthFixedRangeScreenInRange()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("100px");
		classUnderTest.setMaxWidth("200px");
		
		assertEquals(150, classUnderTest.getWidth(150), DELTA);
	}
	
	@Test
	public void testGetWidthPercentRange()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("50%");
		classUnderTest.setMaxWidth("80%");
		
		assertEquals(80, classUnderTest.getWidth(100), DELTA);
	}
	
	@Test
	public void testGetWidthMixedRangeBottomPercentScreenLarger()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("50%");
		classUnderTest.setMaxWidth("100px");
		
		assertEquals(100, classUnderTest.getWidth(150), DELTA);
	}
	
	@Test
	public void testGetWidthMixedRangeBottomPercentScreenSmaller()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("50%");
		classUnderTest.setMaxWidth("100px");
		
		assertEquals(80, classUnderTest.getWidth(80), DELTA);
	}
	
	@Test
	public void testGetWidthMixedRangeUpperPercentScreenLarger()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("100px");
		classUnderTest.setMaxWidth("80%");
		
		assertEquals(800, classUnderTest.getWidth(1000), DELTA);
	}
	
	@Test
	public void testGetWidthMixedRangeUpperPercentScreenSmaller()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinWidth("100px");
		classUnderTest.setMaxWidth("80%");
		
		assertEquals(100, classUnderTest.getWidth(50), DELTA);
	}
	
	@Test
	public void testGetHeightFixedRangeScreenLarger()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("100px");
		classUnderTest.setMaxHeight("200px");
		
		assertEquals(200, classUnderTest.getHeight(250), DELTA);
	}
	
	@Test
	public void testGetHeightFixedRangeScreenSmaller()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("100px");
		classUnderTest.setMaxHeight("200px");
		
		assertEquals(100, classUnderTest.getHeight(50), DELTA);
	}
	
	@Test
	public void testGetHeightFixedRangeScreenInRange()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("100px");
		classUnderTest.setMaxHeight("200px");
		
		assertEquals(150, classUnderTest.getHeight(150), DELTA);
	}
	
	@Test
	public void testGetHeightPercentRange()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("50%");
		classUnderTest.setMaxHeight("80%");
		
		assertEquals(80, classUnderTest.getHeight(100), DELTA);
	}
	
	@Test
	public void testGetHeightMixedRangeBottomPercentScreenLarger()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("50%");
		classUnderTest.setMaxHeight("100px");
		
		assertEquals(100, classUnderTest.getHeight(150), DELTA);
	}
	
	@Test
	public void testGetHeightMixedRangeBottomPercentScreenSmaller()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("50%");
		classUnderTest.setMaxHeight("100px");
		
		assertEquals(80, classUnderTest.getHeight(80), DELTA);
	}
	
	@Test
	public void testGetHeightMixedRangeUpperPercentScreenLarger()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("100px");
		classUnderTest.setMaxHeight("80%");
		
		assertEquals(800, classUnderTest.getHeight(1000), DELTA);
	}
	
	@Test
	public void testGetHeightMixedRangeUpperPercentScreenSmaller()
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl();
		classUnderTest.setMinHeight("100px");
		classUnderTest.setMaxHeight("80%");
		
		assertEquals(100, classUnderTest.getHeight(50), DELTA);
	}
	
	
	// ID checksum tests
	
	@Test
	public void testSameUrlsEqualSourceId() throws Exception
	{
		MutableScreenOverlayAttributesImpl c1 = new MutableScreenOverlayAttributesImpl(new URL("http://somewhere.com/Something.html"));
		MutableScreenOverlayAttributesImpl c2 = new MutableScreenOverlayAttributesImpl(new URL("http://somewhere.com/Something.html"));
		
		assertTrue(c1.getSourceId().equalsIgnoreCase(c2.getSourceId()));
	}
	
	@Test
	public void testDifferentUrlsDifferentSourceId() throws Exception
	{
		MutableScreenOverlayAttributesImpl c1 = new MutableScreenOverlayAttributesImpl(new URL("http://somewhere.com/Something.html"));
		MutableScreenOverlayAttributesImpl c2 = new MutableScreenOverlayAttributesImpl(new URL("http://somewhere.com/Something1.html"));
		
		assertFalse(c1.getSourceId().equalsIgnoreCase(c2.getSourceId()));
	}
	
	@Test
	public void testSameHtmlEqualSourceId() throws Exception
	{
		MutableScreenOverlayAttributesImpl c1 = new MutableScreenOverlayAttributesImpl("<html><body>Some content</body></html>");
		MutableScreenOverlayAttributesImpl c2 = new MutableScreenOverlayAttributesImpl("<html><body>Some content</body></html>");
		
		assertTrue(c1.getSourceId().equalsIgnoreCase(c2.getSourceId()));
	}
	
	@Test
	public void testDifferentHtmlDifferentSourceId() throws Exception
	{
		MutableScreenOverlayAttributesImpl c1 = new MutableScreenOverlayAttributesImpl("<html><body>Some content</body></html>");
		MutableScreenOverlayAttributesImpl c2 = new MutableScreenOverlayAttributesImpl("<html><body>Some content1</body></html>");
		
		assertFalse(c1.getSourceId().equalsIgnoreCase(c2.getSourceId()));
	}
	
	@Test
	public void testDifferentSourceDifferentSourceId() throws Exception
	{
		MutableScreenOverlayAttributesImpl c1 = new MutableScreenOverlayAttributesImpl(new URL("http://somewhere.com/Something.html"));
		MutableScreenOverlayAttributesImpl c2 = new MutableScreenOverlayAttributesImpl("<html><body>Some content1</body></html>");
		
		assertFalse(c1.getSourceId().equalsIgnoreCase(c2.getSourceId()));
	}
	
	// Image vs Html tests
	
	@Test
	public void testSourceFormatWithLowercaseImage() throws Exception
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(new URL("file:/c:/someImage.jpg"));
		
		assertTrue(classUnderTest.isSourceImage());
		assertFalse(classUnderTest.isSourceHtml());
	}
	
	@Test
	public void testSourceFormatWithUppercaseImage() throws Exception
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(new URL("file:/c:/someImage.PNG"));
		
		assertTrue(classUnderTest.isSourceImage());
		assertFalse(classUnderTest.isSourceHtml());
	}
	
	@Test
	public void testSourceFormatWithHtml() throws Exception
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl(new URL("file:/c:/someHtml.htm"));
		
		assertFalse(classUnderTest.isSourceImage());
		assertTrue(classUnderTest.isSourceHtml());
	}
	
	@Test
	public void testSourceFormatWithHtmlContent() throws Exception
	{
		MutableScreenOverlayAttributesImpl classUnderTest = new MutableScreenOverlayAttributesImpl("My content");
		
		assertFalse(classUnderTest.isSourceImage());
		assertTrue(classUnderTest.isSourceHtml());
	}
}
