package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;

public class URLRequesterDelegate implements TileRequesterDelegate
{
	@Override
	public void forceTextureLoad(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		final URL textureURL = layer.getDataFileStore().findFile(tile.getPath(), true);

		if (textureURL != null
				&& !layer.isTextureFileExpired(tile, textureURL, layer.getDataFileStore()))
		{
			loadTexture(tile, textureURL, layer);
		}
	}

	@Override
	public Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		return new RequestTask(tile, layer);
	}

	protected boolean loadTexture(TextureTile tile, URL textureURL, DelegatorTiledImageLayer layer)
	{
		return layer.loadTexture(tile, textureURL);
	}
	
	/* **********************************************************************************************
	 * Below here is copied from BasicTiledImageLayer, with some modifications to use the delegates *
	 ********************************************************************************************** */

	private class RequestTask implements Runnable, Comparable<RequestTask>
	{
		private final TextureTile tile;
		private final DelegatorTiledImageLayer layer;

		private RequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		@Override
		public void run()
		{
			// TODO: check to ensure load is still needed

			final java.net.URL textureURL =
					this.layer.getDataFileStore().findFile(tile.getPath(), false);
			if (textureURL != null
					&& !this.layer.isTextureFileExpired(tile, textureURL,
							this.layer.getDataFileStore()))
			{
				if (loadTexture(tile, textureURL, layer))
				{
					layer.getLevels().unmarkResourceAbsent(this.tile);
					this.layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
				else
				{
					// Assume that something's wrong with the file and delete it.
					this.layer.getDataFileStore().removeFile(textureURL);
					String message =
							Logging.getMessage("generic.DeletedCorruptDataFile", textureURL);
					Logging.logger().info(message);
				}
			}

			this.layer.downloadTexture(this.tile, null);
		}

		/**
		 * @param that
		 *            the task to compare
		 * 
		 * @return -1 if <code>this</code> less than <code>that</code>, 1 if
		 *         greater than, 0 if equal
		 * 
		 * @throws IllegalArgumentException
		 *             if <code>that</code> is null
		 */
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
