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
 */
public class DetailedElevationModel extends AbstractElevationModel
{
	private ElevationModel sourceModel;
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

	@Override
	public double getMaxElevation()
	{
		return sourceModel.getMaxElevation();
	}

	@Override
	public double getMinElevation()
	{
		return sourceModel.getMinElevation();
	}

	@Override
	public double[] getExtremeElevations(Angle latitude, Angle longitude)
	{
		return sourceModel.getExtremeElevations(latitude, longitude);
	}

	@Override
	public double[] getExtremeElevations(Sector sector)
	{
		return sourceModel.getExtremeElevations(sector);
	}

	@Override
	public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
	{
		return sourceModel.getElevations(sector, latlons, targetResolution, buffer);
	}

	@Override
	public double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
	{
		return sourceModel.getElevations(sector, latlons, targetResolution, buffer);
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
		return sourceModel.getUnmappedElevation(latitude, longitude);
	}
}
