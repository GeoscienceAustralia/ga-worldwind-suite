package au.gov.ga.worldwind.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.application.Application;

public class ExtendedTiledImageLayer extends BasicTiledImageLayer
{
	public ExtendedTiledImageLayer(Element domElement, AVList params)
	{
		super(domElement, setupParams(params));

		TileUrlBuilder builder = (TileUrlBuilder) params.getValue(AVKey.TILE_URL_BUILDER);
		if (builder != null && builder instanceof ExtendedUrlBuilder)
		{
			String imageFormat = (String) params.getValue(AVKey.IMAGE_FORMAT);
			((ExtendedUrlBuilder) builder).overrideFormat(imageFormat);
		}
	}

	protected static AVList setupParams(AVList params)
	{
		if (params == null)
			params = new AVListImpl();
		params.setValue(AVKey.TILE_URL_BUILDER, createURLBuilder(params));
		return params;
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

		public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
		{
			String service = tile.getLevel().getService();
			if (service == null || service.length() < 1)
				return null;

			//TEMP!!!!
			if (Application.SANDPIT)
			{
				String externalga = "http://www.ga.gov.au";
				String sandpitga = externalga + ":8500";
				if (service.startsWith(externalga))
				{
					service = sandpitga + service.substring(externalga.length());
				}
			}
			//TEMP!!!!

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
