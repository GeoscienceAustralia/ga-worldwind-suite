package au.gov.ga.worldwind.layers.tiled.image;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.application.GASandpit;

public class GABasicTiledImageLayer extends BasicTiledImageLayer
{
	public GABasicTiledImageLayer(AVList params)
	{
		this(setupParams(params), false);
	}

	public GABasicTiledImageLayer(Element domElement, AVList params)
	{
		this(domElement, setupParams(params), false);
	}

	/* The private constructors below are analogous to those above, except that
	 * params is never null (as the result from setupParams() is passed to the
	 * private constructor). This means that the constructor can pull out params
	 * set by the superclass from the params variable. */

	private GABasicTiledImageLayer(AVList params, boolean nothing)
	{
		super(params);
		initBuilder(params);
	}

	private GABasicTiledImageLayer(Element domElement, AVList params, boolean nothing)
	{
		super(domElement, params);
		initBuilder(params);
	}

	protected static AVList setupParams(AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		if (params.getValue(AVKey.TILE_URL_BUILDER) == null)
			params.setValue(AVKey.TILE_URL_BUILDER, createURLBuilder(params));
		
		return params;
	}

	protected void initBuilder(AVList params)
	{
		TileUrlBuilder builder = (TileUrlBuilder) params.getValue(AVKey.TILE_URL_BUILDER);
		if (builder != null && builder instanceof ExtendedUrlBuilder)
		{
			String imageFormat = (String) params.getValue(AVKey.IMAGE_FORMAT);
			((ExtendedUrlBuilder) builder).overrideFormat(imageFormat);
		}
	}

	protected static TileUrlBuilder createURLBuilder(AVList params)
	{
		return new ExtendedUrlBuilder();
	}

	protected static class ExtendedUrlBuilder implements TileUrlBuilder
	{
		private String imageFormat;

		public void overrideFormat(String imageFormat)
		{
			this.imageFormat = imageFormat;
		}

		@Override
		public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
		{
			String service = tile.getLevel().getService();
			if (service == null || service.length() < 1)
				return null;

			service = GASandpit.replace(service);

			StringBuffer sb = new StringBuffer(service);
			if (sb.lastIndexOf("?") < 0)
				sb.append("?");
			else
				sb.append("&");

			sb.append("T=");
			sb.append(tile.getLevel().getDataset());
			sb.append("&L=");
			sb.append(tile.getLevel().getLevelName());
			sb.append("&X=");
			sb.append(tile.getColumn());
			sb.append("&Y=");
			sb.append(tile.getRow());

			String format = imageFormat != null ? imageFormat : this.imageFormat;
			if (format != null)
				sb.append("&F=" + format);

			return new URL(sb.toString());
		}
	}
}
