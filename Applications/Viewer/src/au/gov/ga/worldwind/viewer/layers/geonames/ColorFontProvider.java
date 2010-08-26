package au.gov.ga.worldwind.viewer.layers.geonames;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.viewer.util.ColorFont;


public class ColorFontProvider
{
	private ColorFont def;
	private Map<String, ColorFont> fontMap = new HashMap<String, ColorFont>();

	public ColorFontProvider()
	{
		def = new ColorFont(null, null, null);
	}

	public ColorFontProvider(ColorFont def)
	{
		this.def = def;
	}

	public void put(String key, ColorFont font)
	{
		fontMap.put(key, font);
	}

	public void put(String key, Font font, Color color, Color backgroundColor)
	{
		put(key, new ColorFont(font, color, backgroundColor));
	}

	public ColorFont get(String key)
	{
		ColorFont font = fontMap.get(key);
		if (font == null)
		{
			font = def;
		}
		return font;
	}
}
