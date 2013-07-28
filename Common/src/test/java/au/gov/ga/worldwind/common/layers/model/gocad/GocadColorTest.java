package au.gov.ga.worldwind.common.layers.model.gocad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;

import org.junit.Test;

public class GocadColorTest
{
	@Test
	public void testRGB4String()
	{
		Color c = GocadColor.gocadLineToColor("*solid*color:1 0 0 0");
		
		assertEquals(255, c.getRed());
		assertEquals(0, c.getGreen());
		assertEquals(0, c.getBlue());
		assertEquals(0, c.getAlpha());
	}
	
	@Test
	public void testRGB3String()
	{
		Color c = GocadColor.gocadLineToColor("*solid*color:1 0 1");
		
		assertEquals(255, c.getRed());
		assertEquals(0, c.getGreen());
		assertEquals(255, c.getBlue());
		assertEquals(255, c.getAlpha());
	}
	
	@Test
	public void testNamedColorWithSpaces()
	{
		assertEquals(GocadColor.DarkOliveGreen.color, GocadColor.gocadLineToColor("*solid*color:dark olive green"));
	}
	
	@Test
	public void testNamedColorWithoutSpaces()
	{
		assertEquals(GocadColor.DarkOliveGreen.color, GocadColor.gocadLineToColor("*solid*color:darkolivegreen"));
	}
	
	@Test
	public void testHexCodeString()
	{
		Color c = GocadColor.gocadLineToColor("*solid*color:#0f66e0");
		
		assertEquals(15, c.getRed());
		assertEquals(102, c.getGreen());
		assertEquals(224, c.getBlue());
		assertEquals(255, c.getAlpha());
	}
	
	@Test
	public void testInvalidHexCodeString()
	{
		Color c = GocadColor.gocadLineToColor("*solid*color:#00GG00");
		
		assertNull(c);
	}
	
	@Test
	public void testNullString()
	{
		assertNull(GocadColor.gocadLineToColor(null));
	}
	
	@Test
	public void testEmptyString()
	{
		assertNull(GocadColor.gocadLineToColor(""));
	}
	
	@Test
	public void testInvalidColorString()
	{
		assertNull(GocadColor.gocadLineToColor("*solid*color:bob 123"));
	}
}
