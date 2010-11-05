package au.gov.ga.worldwind.common.layers.tiled.image.delegate.colortoalpha;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.tiled.image.delegate.Delegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageTransformerDelegate;

public class ColorLimitTransformerDelegate implements ImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "ColorLimitTransformer";

	protected final Color color;

	//for reflection instantiation
	@SuppressWarnings("unused")
	private ColorLimitTransformerDelegate()
	{
		this(Color.black);
	}

	public ColorLimitTransformerDelegate(Color color)
	{
		this.color = color;
	}

	public Color getColor()
	{
		return color;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
	}

	@Override
	public Delegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\d+),(\\d+),(\\d+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int r = Integer.parseInt(matcher.group(1));
				int g = Integer.parseInt(matcher.group(2));
				int b = Integer.parseInt(matcher.group(3));
				Color color = new Color(r, g, b);
				return new ColorLimitTransformerDelegate(color);
			}
		}
		return null;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image)
	{
		if (image == null)
			return null;

		BufferedImage dst = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int rgb = image.getRGB(x, y);
				rgb = limitColor(rgb, color);
				dst.setRGB(x, y, rgb);
			}
		}

		return dst;
	}

	protected static int limitColor(int argb, Color color)
	{
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = (argb) & 0xff;

		if (r > color.getRed() || g > color.getGreen() || b > color.getBlue())
		{
			a = 0;
		}

		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}
}
