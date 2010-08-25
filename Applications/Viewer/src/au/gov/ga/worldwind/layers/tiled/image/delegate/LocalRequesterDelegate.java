package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.util.Util;

public class LocalRequesterDelegate implements TileRequesterDelegate
{
	private final static String DEFINITION_STRING = "LocalRequester";

	@Override
	public void forceTextureLoad(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		loadTexture(tile, layer);
	}

	@Override
	public Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		return new RequestTask(tile, layer);
	}

	protected boolean loadTexture(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		File file = getTileFile(tile, layer);
		if (file != null && file.exists())
		{
			try
			{
				URL url = file.toURI().toURL();
				return layer.loadTexture(tile, url);
			}
			catch (MalformedURLException e)
			{
				String msg = "Converting tile file to URL failed";
				Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
			}
		}
		return false;
	}

	protected File getTileFile(Tile tile, DelegatorTiledImageLayer layer)
	{
		String service = tile.getLevel().getService();
		String dataset = tile.getLevel().getDataset();

		if (dataset == null || dataset.length() <= 0)
			dataset = service;
		else if (service != null && service.length() > 0)
			dataset = service + "/" + dataset;

		if (dataset == null)
			dataset = "";

		File directory = Util.getPathWithinContext(dataset, layer.context);

		if (directory == null)
			return null;

		//default to JPG
		String ext = "jpg";
		String format = layer.getDefaultImageFormat();
		if (format != null)
		{
			format = format.toLowerCase();
			if (format.contains("jpg") || format.contains("jpeg"))
				ext = "jpg";
			else if (format.contains("png"))
				ext = "png";
			else if (format.contains("zip"))
				ext = "zip";
			else if (format.contains("dds"))
				ext = "dds";
			else if (format.contains("bmp"))
				ext = "bmp";
			else if (format.contains("gif"))
				ext = "gif";
		}

		return new File(directory, tile.getLevelNumber() + File.separator
				+ Util.paddedInt(tile.getRow(), 4) + File.separator
				+ Util.paddedInt(tile.getRow(), 4) + "_" + Util.paddedInt(tile.getColumn(), 4)
				+ "." + ext);
	}

	protected class RequestTask implements Runnable, Comparable<RequestTask>
	{
		private final DelegatorTiledImageLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		@Override
		public void run()
		{
			if (loadTexture(tile, layer))
			{
				layer.getLevels().unmarkResourceAbsent(tile);
				layer.firePropertyChange(AVKey.LAYER, null, this);
			}
			else
			{
				layer.getLevels().markResourceAbsent(tile);
			}
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

	@Override
	public Delegate fromDefinition(String definition)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new LocalRequesterDelegate();
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING;
	}
}
