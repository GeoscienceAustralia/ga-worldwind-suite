package au.gov.ga.worldwind.viewer.layers.geonames;

import java.util.HashMap;

import au.gov.ga.worldwind.viewer.util.ColorFont;

/**
 * Simple map between a String and {@link ColorFont}. Used for providing
 * different font/color combinations for different attribute values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorFontProvider extends HashMap<String, ColorFont>
{
	private ColorFont def;

	public ColorFontProvider()
	{
		def = new ColorFont(null, null, null);
	}

	public ColorFontProvider(ColorFont def)
	{
		this.def = def;
	}

	@Override
	public ColorFont get(Object key)
	{
		ColorFont font = super.get(key);
		if (font == null)
		{
			font = def;
		}
		return font;
	}
}
