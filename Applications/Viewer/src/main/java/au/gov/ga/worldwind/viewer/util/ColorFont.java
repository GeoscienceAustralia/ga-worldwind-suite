package au.gov.ga.worldwind.viewer.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Simple container class for storing a font with a color and background color.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorFont
{
	public final Color backgroundColor;
	public final Color color;
	public final Font font;

	public ColorFont(Font font, Color color, Color backgroundColor)
	{
		this.font = font;
		this.color = color;
		this.backgroundColor = backgroundColor;
	}
}
