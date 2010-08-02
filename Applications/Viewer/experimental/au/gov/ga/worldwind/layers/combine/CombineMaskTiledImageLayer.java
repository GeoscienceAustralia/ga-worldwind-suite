package au.gov.ga.worldwind.layers.combine;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.TextureTile;

import java.awt.image.BufferedImage;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.mask.MaskTiledImageLayer;

public class CombineMaskTiledImageLayer extends MaskTiledImageLayer
{
	public CombineMaskTiledImageLayer(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	@Override
	protected DownloadPostProcessor createPostProcessor(TextureTile tile)
	{
		return new CombinePostProcessor(tile, this);
	}

	protected class CombinePostProcessor extends DownloadPostProcessor
	{
		public CombinePostProcessor(TextureTile tile, MaskTiledImageLayer layer)
		{
			super(tile, layer);
		}

		@Override
		protected BufferedImage transformPixels(BufferedImage image)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				for (int x = 0; x < image.getWidth(); x++)
				{
					int rgb = image.getRGB(x, y);
					int r = rgb & 0xff;
					int a = (rgb >> 24) & 0xff;

					float s = r / 255f;
					s = Math.abs((s - 0.5f) * 2f);
					s *= s;
					s *= a / 255f;

					rgb = (rgb & 0xffffff) + (((int) (s * 255f)) << 24);
					image.setRGB(x, y, rgb);
				}
			}
			return super.transformPixels(image);
		}
	}
}
