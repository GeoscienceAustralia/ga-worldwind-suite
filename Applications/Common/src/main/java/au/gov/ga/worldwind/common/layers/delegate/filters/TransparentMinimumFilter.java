package au.gov.ga.worldwind.common.layers.delegate.filters;

import java.awt.Rectangle;

import com.jhlabs.image.MinimumFilter;
import com.jhlabs.image.PixelUtils;

/**
 * Minimum (dilate) filter that fixes the {@link MinimumFilter} to support
 * transparency.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TransparentMinimumFilter extends MinimumFilter
{
	@Override
	protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace)
	{
		int index = 0;
		int[] outPixels = new int[width * height];

		for (int y = 0; y < height; y++)
		{
			int yoffset = y * width;
			for (int x = 0; x < width; x++)
			{
				int pixel = inPixels[yoffset + x] | 0xffffff;
				for (int dy = -1; dy <= 1; dy++)
				{
					int iy = y + dy;
					int ioffset;
					if (0 <= iy && iy < height)
					{
						ioffset = iy * width;
						for (int dx = -1; dx <= 1; dx++)
						{
							int ix = x + dx;
							if (0 <= ix && ix < width)
							{
								pixel = PixelUtils.combinePixels(pixel, inPixels[ioffset + ix], PixelUtils.MIN);
							}
						}
					}
				}
				outPixels[index++] = pixel;
			}
		}
		return outPixels;
	}
}
