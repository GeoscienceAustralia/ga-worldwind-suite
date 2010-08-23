package au.gov.ga.worldwind.layers.tiled.image.delegate.colortoalpha;

import java.awt.Color;
import java.awt.image.BufferedImage;

import au.gov.ga.worldwind.layers.tiled.image.delegate.ImageTransformerDelegate;

public class ColorToAlphaTransformerDelegate implements ImageTransformerDelegate
{
	protected final Color color;

	public ColorToAlphaTransformerDelegate(Color color)
	{
		this.color = color;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image)
	{
		if (image == null)
			return null;

		BufferedImage dst =
				new BufferedImage(image.getWidth(), image.getHeight(),
						BufferedImage.TYPE_INT_ARGB_PRE);

		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int rgb = image.getRGB(x, y);
				rgb = colorToAlpha(rgb, color);
				dst.setRGB(x, y, rgb);
			}
		}

		return dst;
	}

	public static int colorToAlpha(int argb, Color color)
	{
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = (argb) & 0xff;

		float pr = distancePercent(r, color.getRed(), 0, 255);
		float pg = distancePercent(g, color.getGreen(), 0, 255);
		float pb = distancePercent(b, color.getBlue(), 0, 255);
		float percent = Math.max(pr, Math.max(pg, pb));

		//(image - color) / alpha + color
		if (percent > 0)
		{
			r = (int) ((r - color.getRed()) / percent) + color.getRed();
			g = (int) ((g - color.getGreen()) / percent) + color.getGreen();
			b = (int) ((b - color.getBlue()) / percent) + color.getBlue();
		}
		a = (int) (a * percent);

		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}

	private static float distancePercent(int value, int distanceTo, int min, int max)
	{
		float diff = 0f;
		if (value < distanceTo)
		{
			diff = (distanceTo - value) / (float) (distanceTo - min);
		}
		else if (value > distanceTo)
		{
			diff = (value - distanceTo) / (float) (max - distanceTo);
		}
		return Math.max(0f, Math.min(1f, diff));
	}
}
