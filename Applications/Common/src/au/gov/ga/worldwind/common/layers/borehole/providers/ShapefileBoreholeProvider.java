package au.gov.ga.worldwind.common.layers.borehole.providers;

import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileUtils;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.net.URL;
import java.util.logging.Level;

import au.gov.ga.worldwind.common.layers.borehole.BoreholeLayer;
import au.gov.ga.worldwind.common.layers.borehole.BoreholeProvider;
import au.gov.ga.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.worldwind.common.util.URLUtil;

public class ShapefileBoreholeProvider extends AbstractDataProvider<BoreholeLayer> implements BoreholeProvider
{
	private Sector sector;

	@Override
	protected boolean doLoadData(URL url, BoreholeLayer layer)
	{
		try
		{
			Shapefile shapefile = ShapefileUtils.openZippedShapefile(URLUtil.urlToFile(url));
			while (shapefile.hasNext())
			{
				ShapefileRecord record = shapefile.nextRecord();
				DBaseRecord values = record.getAttributes();

				for (int part = 0; part < record.getNumberOfParts(); part++)
				{
					VecBuffer buffer = record.getPointBuffer(part);
					int size = buffer.getSize();
					for (int i = 0; i < size; i++)
					{
						layer.addBoreholeSample(buffer.getPosition(i), values);
					}
				}
			}

			sector = Sector.fromDegrees(shapefile.getBoundingRectangle());
			layer.loadComplete();
		}
		catch (Exception e)
		{
			String message = "Error loading points";
			Logging.logger().log(Level.SEVERE, message, e);
			return false;
		}
		return true;
	}

	@Override
	public Sector getSector()
	{
		return sector;
	}
}
