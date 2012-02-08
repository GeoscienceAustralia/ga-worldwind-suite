package au.gov.ga.worldwind.common.layers.styled;

/**
 * Class which stores the style information for a particular point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StyleAndText
{
	public final Style style;
	public final String text;
	public final String link;

	public StyleAndText(Style style, String text, String link)
	{
		this.style = style;
		this.text = text;
		this.link = link;
	}
}
