package layers.geonames;

import java.awt.Color;
import java.awt.Font;

public class TextProperties
{
	public final Color backgroundColor;
	public final Color color;
	public final Font font;

	public TextProperties(Font font, Color color, Color backgroundColor)
	{
		this.font = font;
		this.color = color;
		this.backgroundColor = backgroundColor;
	}
}
