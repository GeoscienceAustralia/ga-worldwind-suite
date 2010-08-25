package au.gov.ga.worldwind.layers.tiled.image.delegate.transparentcolor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.gov.ga.worldwind.layers.tiled.image.delegate.Delegate;
import au.gov.ga.worldwind.layers.tiled.image.delegate.ImageTransformerDelegate;

public class TransparentColorTransformerDelegate implements ImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "TransparentColorTransformer";

	protected final Color color;
	protected final double fuzz;

	public TransparentColorTransformerDelegate()
	{
		this(Color.black, 0d);
	}

	public TransparentColorTransformerDelegate(Color color, double fuzz)
	{
		this.color = color;
		this.fuzz = fuzz;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image)
	{
		int fuzzi = Math.max(0, Math.min(255, (int) Math.round(fuzz * 255d)));
		BufferedImage trans =
				new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		int cr = color.getRed();
		int cg = color.getGreen();
		int cb = color.getBlue();

		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				int rgb = image.getRGB(x, y);
				int sr = (rgb >> 16) & 0xff;
				int sg = (rgb >> 8) & 0xff;
				int sb = (rgb >> 0) & 0xff;
				if (cr - fuzzi <= sr && sr <= cr + fuzzi && cg - fuzzi <= sg && sg <= cg + fuzzi
						&& cb - fuzzi <= sb && sb <= cb + fuzzi)
				{
					rgb = (rgb & 0xffffff);
				}
				trans.setRGB(x, y, rgb);
			}
		}

		return trans;
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:(\\d+),(\\d+),(\\d+),(\\d*\\.?\\d*))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int r = Integer.parseInt(matcher.group(1));
				int g = Integer.parseInt(matcher.group(2));
				int b = Integer.parseInt(matcher.group(3));
				double fuzz = Double.parseDouble(matcher.group(4));
				Color color = new Color(r, g, b);
				return new TransparentColorTransformerDelegate(color, fuzz);
			}
		}
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING + "(" + color.getRed() + "," + color.getGreen() + ","
				+ color.getBlue() + "," + fuzz + ")";
	}
}
