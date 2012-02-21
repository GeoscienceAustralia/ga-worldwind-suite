package au.gov.ga.worldwind.dataprep;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ColorToAlpha
{
	public static void main(String[] args)
	{
		File input = new File("c:/data/0338_2319.jpg");
		File output = new File("c:/data/output.png");
		Color color = new Color(190, 190, 190);

		try
		{
			BufferedImage image = ImageIO.read(input);
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

			ImageIO.write(dst, "png", output);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
