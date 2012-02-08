package au.gov.ga.worldwind.common.terrain;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.common.util.transform.URLTransformer;

/**
 * {@link BasicElevationModel} that uses a custom {@link TileUrlBuilder} that
 * uses the {@link URLTransformer} to transform elevation tile download URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLTransformerBasicElevationModel extends BoundedBasicElevationModel
{
	public URLTransformerBasicElevationModel(AVList params)
	{
		super(setupParams(params));
	}

	protected static AVList setupParams(AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		if (params.getValue(AVKey.TILE_URL_BUILDER) == null)
			params.setValue(AVKey.TILE_URL_BUILDER, createURLBuilder(params));

		return params;
	}

	protected static TileUrlBuilder createURLBuilder(AVList params)
	{
		return new ExtendedUrlBuilder();
	}

	protected static class ExtendedUrlBuilder implements TileUrlBuilder
	{
		@Override
		public URL getURL(Tile tile, String altImageFormat) throws MalformedURLException
		{
			String service = tile.getLevel().getService();
			if (service == null || service.length() < 1)
				return null;

			service = URLTransformer.transform(service);

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

			// Convention for NASA WWN tiles is to request them with common dataset name but without dds.
			return new URL(altImageFormat == null ? sb.toString() : sb.toString()
					.replace("dds", ""));
		}
	}
}
