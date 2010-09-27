package au.gov.ga.worldwind.animator.terrain;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.AbstractElevationModel;

import java.util.List;

/**
 * An {@link ElevationModel} that includes a global detail hint indicating how detailed the 
 * elevation model should be resolved to.
 * <p/>
 * Actual elevation data is handled by a delegate {@link ElevationModel}. 
 * <p/>
 * The global detail hint replaces any sector-specific hints provided by the delegate's {@link #getDetailHint(Sector)} method. 
 * <p/>
 * This implementation also allows elevation values to be clamped between a specified min and max elevation.
 */
public class DetailedElevationModel extends AbstractElevationModel
{
	private ElevationModel sourceModel;
	private double min = -Double.MAX_VALUE;
	private double max = Double.MAX_VALUE;
	private double offset = 0d;
	private double detailHint = 0d;

	public DetailedElevationModel(ElevationModel source)
	{
		sourceModel = source;
	}

	public double getDetailHint()
	{
		return detailHint;
	}

	@Override
	public double getDetailHint(Sector sector)
	{
		return detailHint;
	}

	public void setDetailHint(double detailHint)
	{
		this.detailHint = detailHint;
	}

	public ElevationModel getSourceModel()
	{
		return sourceModel;
	}

	public double getMin()
	{
		return min;
	}

	public void setMin(double min)
	{
		this.min = min;
	}

	public double getMax()
	{
		return max;
	}

	public void setMax(double max)
	{
		this.max = max;
	}

	public double getOffset()
	{
		return offset;
	}

	public void setOffset(double offset)
	{
		this.offset = offset;
	}

	@Override
	public double getMaxElevation()
	{
		return clampElevation(sourceModel.getMaxElevation());
	}

	@Override
	public double getMinElevation()
	{
		return clampElevation(sourceModel.getMinElevation());
	}

	@Override
	public double[] getExtremeElevations(Angle latitude, Angle longitude)
	{
		double[] sourceElevations = sourceModel.getExtremeElevations(latitude, longitude);
		if (sourceElevations == null)
		{
			return sourceElevations;
		}

		return new double[] { clampElevation(sourceElevations[0]), clampElevation(sourceElevations[1]) };
	}

	@Override
	public double[] getExtremeElevations(Sector sector)
	{
		double[] elevs = sourceModel.getExtremeElevations(sector);
		if (elevs == null)
		{
			return elevs;
		}

		return new double[] { clampElevation(elevs[0]), clampElevation(elevs[1]) };
	}

	@Override
	public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
	{
		double resolution = sourceModel.getElevations(sector, latlons, targetResolution, buffer);

		for (int i = 0; i < latlons.size(); i++)
		{
			LatLon ll = latlons.get(i);
			if (sourceModel.contains(ll.getLatitude(), ll.getLongitude()))
			{
				buffer[i] = clampElevation(buffer[i]);
			}
		}

		return resolution;
	}

	@Override
	public double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
	{
		double resolution = sourceModel.getElevations(sector, latlons, targetResolution, buffer);

		for (int i = 0; i < latlons.size(); i++)
		{
			LatLon ll = latlons.get(i);
			if (sourceModel.contains(ll.getLatitude(), ll.getLongitude()) && !isMissingDataSignal(buffer[i]))
			{
				buffer[i] = clampElevation(buffer[i]);
			}
		}

		return resolution;
	}

	@Override
	public int intersects(Sector sector)
	{
		return sourceModel.intersects(sector);
	}

	@Override
	public boolean contains(Angle latitude, Angle longitude)
	{
		return sourceModel.contains(latitude, longitude);
	}

	@Override
	public double getBestResolution(Sector sector)
	{
		return sourceModel.getBestResolution(sector);
	}

	@Override
	public double getUnmappedElevation(Angle latitude, Angle longitude)
	{
		double elevation = sourceModel.getUnmappedElevation(latitude, longitude);
		return isMissingDataSignal(elevation) ? elevation : clampElevation(elevation);
	}

	private boolean isMissingDataSignal(double data)
	{
		return data == sourceModel.getMissingDataSignal();
	}
	
	/**
	 * Clamp the provided elevation value to be on the interval <code>[min, max]</code>
	 * 
	 * @see #getMin()
	 * @see #setMin(double)
	 * @see #getMax()
	 * @see #setMax(double)
	 */
	protected double clampElevation(double elevation)
	{
		return elevation < min ? min : elevation > max ? max : elevation + offset;
	}
}
