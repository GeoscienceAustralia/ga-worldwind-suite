package au.gov.ga.worldwind.common.layers.point;

/**
 * Class which stores the style information for a particular point.
 * 
 * @author Michael de Hoog
 */
public class PointProperties
{
	public final Style style;
	public final String text;
	public final String link;

	public PointProperties(Style style, String text, String link)
	{
		this.style = style;
		this.text = text;
		this.link = link;
	}
}
