package layers;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.sun.opengl.util.FileUtil;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class TransparentTiledImageLayer extends BasicTiledImageLayer
{
	public TransparentTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	public TransparentTiledImageLayer(AVList params)
	{
		super(params);
	}

	protected boolean loadTexture(TextureTile tile, java.net.URL textureURL)
	{
		TextureData textureData;

		synchronized (this.fileLock)
		{
			textureData = readTexture(textureURL, this.isUseMipMaps());
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			this.addTileToCache(tile);

		return true;
	}

	private TextureData readTexture(java.net.URL url, boolean useMipMaps)
	{
		try
		{
			if ("jpg".equalsIgnoreCase(FileUtil.getFileSuffix(url.getPath())))
			{
				BufferedImage src = ImageIO.read(url);
				BufferedImage dst = new BufferedImage(src.getWidth(), src
						.getHeight(), BufferedImage.TYPE_INT_ARGB);

				for (int x = 0; x < src.getWidth(); x++)
				{
					for (int y = 0; y < src.getHeight(); y++)
					{
						int rgb = src.getRGB(x, y);
						int r = (rgb >> 16) & 0xFF;
						int g = (rgb >> 8) & 0xFF;
						int b = (rgb >> 0) & 0xFF;
						if (isTransparentRGB(r, g, b))
						{
							dst.setRGB(x, y, 0);
						}
						else
						{
							dst.setRGB(x, y, rgb);
						}
					}
				}

				return TextureIO.newTextureData(dst, useMipMaps);
			}
			else
			{
				return TextureIO.newTextureData(url, useMipMaps, null);
			}
		}
		catch (Exception e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
					e);
			return null;
		}
	}

	protected boolean isTransparentRGB(int r, int g, int b)
	{
		return false;
	}
}
