package au.gov.ga.worldwind.common.layers.tiled.image.delegate.resize;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.tiled.image.delegate.Delegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageTransformerDelegate;

public class ResizeTransformerDelegate implements ImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "ResizeTransformer";

	private final int width;
	private final int height;

	@SuppressWarnings("unused")
	private ResizeTransformerDelegate()
	{
		this(512, 512);
	}

	public ResizeTransformerDelegate(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image)
	{
		BufferedImage resized = new BufferedImage(width, height, image.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resized;
	}

	@Override
	public Delegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\d+),(\\d+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int width = Integer.parseInt(matcher.group(1));
				int height = Integer.parseInt(matcher.group(2));
				return new ResizeTransformerDelegate(width, height);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + width + "," + height + ")";
	}
}
