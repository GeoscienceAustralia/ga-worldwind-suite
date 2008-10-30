package layers.geonames;

import java.awt.Color;
import java.awt.Font;

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
