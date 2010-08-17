package au.gov.ga.worldwind.layers.file;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.AVListTiledImageLayer;
import au.gov.ga.worldwind.util.AVKeyMore;
import au.gov.ga.worldwind.util.Util;
import au.gov.ga.worldwind.util.XMLUtil;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class FileTiledImageLayer extends AVListTiledImageLayer
{
	private boolean masked;
	private URL context;

	public FileTiledImageLayer(LevelSet levelSet, boolean masked)
	{
		super(levelSet);
		this.masked = masked;
		getFileParams(levelSet.getFirstLevel().getParams());
	}

	public FileTiledImageLayer(AVList params, boolean masked)
	{
		super(params);
		this.masked = masked;
		getFileParams(params);
	}

	public FileTiledImageLayer(Element domElement, AVList params, boolean masked)
	{
		this(getFileParamsFromDocument(domElement, params), masked);
	}

	protected static AVList getFileParamsFromDocument(Element domElement, AVList params)
	{
		AVList list = getParamsFromDocument(domElement, params);

		//add a random string to the data cache name, so that nothing else in the texture
		//cache will be used as textures for this layer
		String dataCacheName = (String) list.getValue(AVKey.DATA_CACHE_NAME);
		if (dataCacheName == null)
			dataCacheName = "";
		else
			dataCacheName += "_";
		dataCacheName += Util.randomString(8);
		list.setValue(AVKey.DATA_CACHE_NAME, dataCacheName);

		WWXML.checkAndSetColorParam(domElement, params, AVKeyMore.TRANSPARENT_COLOR,
				"TransparentColor", null);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.TRANSPARENT_FUZZ,
				"TransparentFuzz", null);

		return list;
	}

	public static void createTiledImageLayerElements(Element context, AVList params)
	{
		Color color = (Color) params.getValue(AVKeyMore.TRANSPARENT_COLOR);
		if (color != null)
			XMLUtil.appendColor(context, "TransparentColor", color);

		Double fuzz = (Double) params.getValue(AVKeyMore.TRANSPARENT_FUZZ);
		if (fuzz != null)
			WWXML.appendDouble(context, "TransparentFuzz", fuzz);
	}

	protected void getFileParams(AVList params)
	{
		if (params == null)
			return;

		Object o = params.getValue(AVKeyMore.CONTEXT_URL);
		if (o != null && o instanceof URL)
			context = (URL) o;
	}

	protected File getTileFile(TextureTile tile, boolean mask)
	{
		String service = tile.getLevel().getService();
		String dataset = tile.getLevel().getDataset();

		if (dataset == null || dataset.length() <= 0)
			dataset = service;
		else if (service != null && service.length() > 0)
			dataset = service + "/" + dataset;

		if (dataset == null)
			dataset = "";

		if (mask)
		{
			int lastIndexOfSlash = Math.max(dataset.lastIndexOf('/'), dataset.lastIndexOf('\\'));
			if (lastIndexOfSlash < 0)
				dataset = "";
			else
				dataset = dataset.substring(0, lastIndexOfSlash + 1);
			dataset += "mask";
		}

		File directory = getDirectory(context, dataset);
		if (directory == null)
			return null;

		//default to JPG
		String ext = "jpg";

		//mask is always PNG
		if (mask)
		{
			ext = "png";
		}
		else
		{
			String format = getDefaultImageFormat();
			if (format != null)
			{
				format = format.toLowerCase();
				if (format.contains("jpg") || format.contains("jpeg"))
					ext = "jpg";
				else if (format.contains("png"))
					ext = "png";
				else if (format.contains("dds"))
					ext = "dds";
				else if (format.contains("bmp"))
					ext = "bmp";
				else if (format.contains("gif"))
					ext = "gif";
			}
		}

		return new File(directory, tile.getLevelNumber() + File.separator
				+ Util.paddedInt(tile.getRow(), 4) + File.separator
				+ Util.paddedInt(tile.getRow(), 4) + "_" + Util.paddedInt(tile.getColumn(), 4)
				+ "." + ext);
	}

	protected File getDirectory(URL context, String path)
	{
		//first attempt finding of the directory using a URL
		try
		{
			URL url = context == null ? new URL(path) : new URL(context, path);
			File file = Util.urlToFile(url);
			if (file != null && file.isDirectory())
				return file;
		}
		catch (Exception e)
		{
		}

		//next try parsing the context to pull out a parent file
		File parent = null;
		if (context != null)
		{
			File file = Util.urlToFile(context);
			if (file != null && file.isFile())
			{
				parent = file.getParentFile();
				if (parent != null && !parent.isDirectory())
					parent = null;
			}
		}

		//if the parent isn't null, try using it as a parent file
		if (parent != null)
		{
			try
			{
				File dir = new File(parent, path);
				if (dir.isDirectory())
					return dir;
			}
			catch (Exception e)
			{
			}
		}

		//otherwise ignore the parent and just attempt the path
		File dir = new File(path);
		if (dir.isDirectory())
			return dir;
		return null;
	}

	@Override
	protected void forceTextureLoad(TextureTile tile)
	{
		File file = getTileFile(tile, false);
		File mask = masked ? getTileFile(tile, true) : null;
		if (file != null && file.exists() && (mask == null || mask.exists()))
		{
			loadTexture(tile, file, mask);
		}
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
		if (this.getReferencePoint(dc) != null)
			tile.setPriority(centroid.distanceTo3(this.getReferencePoint(dc)));

		RequestTask task = new RequestTask(tile, this);
		this.getRequestQ().add(task);
	}

	protected void requestTextureLoad(TextureTile tile)
	{
		File file = getTileFile(tile, false);
		File mask = masked ? getTileFile(tile, true) : null;
		if (file != null && file.exists() && (mask == null || mask.exists()))
		{
			loadTexture(tile, file, mask);
			getLevels().unmarkResourceAbsent(tile);
			firePropertyChange(AVKey.LAYER, null, this);
		}
		else
		{
			getLevels().markResourceAbsent(tile);
		}
	}

	protected boolean loadTexture(TextureTile tile, File file, File mask)
	{
		TextureData textureData =
				readTexture(tile, file, mask, this.isCompressTextures(), this.isUseMipMaps());

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			this.addTileToCache(tile);

		return true;
	}

	protected TextureData readTexture(TextureTile tile, File file, File mask,
			boolean compressTextures, boolean useMipMaps)
	{
		try
		{
			//are there any transparent colors defined?
			int[] transparencyColors = null;
			Color transparentColor = null;
			Double transparentFuzz = null;
			if (tile.getLevel().getParams() != null)
			{
				transparencyColors =
						(int[]) tile.getLevel().getParams().getValue(AVKey.TRANSPARENCY_COLORS);
				transparentColor =
						(Color) tile.getLevel().getParams().getValue(AVKeyMore.TRANSPARENT_COLOR);
				transparentFuzz =
						(Double) tile.getLevel().getParams().getValue(AVKeyMore.TRANSPARENT_FUZZ);
			}

			//extract the file extension from the filename
			String ext = null;
			int lastIndexOfPeriod = file.getName().lastIndexOf('.');
			if (lastIndexOfPeriod >= 0)
				ext = file.getName().substring(lastIndexOfPeriod + 1).toLowerCase();

			//check if we need to convert to dds
			boolean isDDS = "dds".equals(ext);
			String format = tile.getLevel().getFormatSuffix();
			boolean useDDS = format == null ? false : format.toLowerCase().contains("dds");

			//if we don't need to do any processing, then load the file directly
			if (isDDS
					|| (!useDDS && mask == null && transparencyColors == null && transparentColor == null))
			{
				return TextureIO.newTextureData(file, useMipMaps, ext);
			}

			//read the file into an image
			BufferedImage image = ImageIO.read(file);

			//if the standard transparency colors have been defined, map them
			if (transparencyColors != null)
			{
				image = ImageUtil.mapTransparencyColors(image, transparencyColors);
			}

			//if the custom fuzzy transparency color has been defined, map it
			if (transparentColor != null)
			{
				if (transparentFuzz == null)
					transparentFuzz = 0d;

				image = mapFuzzyTransparency(image, transparentColor, transparentFuzz);
			}

			//if the texture is masked, read the mask image, and then composite them together
			if (mask != null)
			{
				BufferedImage maskImage = ImageIO.read(mask);

				Graphics2D g2d = maskImage.createGraphics();
				g2d.setComposite(AlphaComposite.SrcIn);
				g2d.drawImage(image, 0, 0, null);
				g2d.dispose();

				image = maskImage;
			}

			//if dds is not used, return new texture data from the image
			if (!useDDS)
				return TextureIO.newTextureData(image, useMipMaps);

			//convert the image to DDS, then return the texture data
			ByteBuffer buffer = DDSCompressor.compressImage(image);
			InputStream is = WWIO.getInputStreamFromByteBuffer(buffer);
			return TextureIO.newTextureData(is, useMipMaps, "dds");
		}
		catch (Exception e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"layers.TextureLayer.ExceptionAttemptingToReadTextureFile", e);
			return null;
		}
	}

	protected void addTileToCache(TextureTile tile)
	{
		TextureTile.getMemoryCache().add(tile.getTileKey(), tile);
	}

	@Override
	protected void setBlendingFunction(DrawContext dc)
	{
		super.setBlendingFunction(dc);
		GL gl = dc.getGL();
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}

	protected BufferedImage mapFuzzyTransparency(BufferedImage image, Color color,
			double fuzzPercent)
	{
		int fuzz = Math.max(0, (int) Math.round(fuzzPercent * 255d));
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
				if (cr - fuzz <= sr && sr <= cr + fuzz && cg - fuzz <= sg && sg <= cg + fuzz
						&& cb - fuzz <= sb && sb <= cb + fuzz)
				{
					rgb = (rgb & 0xffffff);
				}
				trans.setRGB(x, y, rgb);
			}
		}

		return trans;
	}

	protected static class RequestTask implements Runnable, Comparable<RequestTask>
	{
		private final FileTiledImageLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, FileTiledImageLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		@Override
		public void run()
		{
			layer.requestTextureLoad(tile);
		}

		@Override
		public int compareTo(RequestTask that)
		{
			if (that == null)
			{
				String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}
			return this.tile.getPriority() == that.tile.getPriority() ? 0
					: this.tile.getPriority() < that.tile.getPriority() ? -1 : 1;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RequestTask that = (RequestTask) o;

			// Don't include layer in comparison so that requests are shared among layers
			return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
		}

		@Override
		public int hashCode()
		{
			return (tile != null ? tile.hashCode() : 0);
		}

		@Override
		public String toString()
		{
			return this.tile.toString();
		}
	}
}
