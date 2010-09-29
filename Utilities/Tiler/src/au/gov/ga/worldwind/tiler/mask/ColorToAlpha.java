package au.gov.ga.worldwind.tiler.mask;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import au.gov.ga.worldwind.tiler.util.Util;

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
					rgb = Util.colorToAlpha(rgb, color);
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
}
