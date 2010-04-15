package terrain;

import java.util.List;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

public class OffsetCompoundElevationModel extends CompoundElevationModel
{
	protected double elevationOffset = 0;
	protected double detailHint = 0;

	@Override
	public double getUnmappedElevation(Angle latitude, Angle longitude)
	{
		return super.getUnmappedElevation(latitude, longitude)
				+ elevationOffset;
	}

	@Override
	public double getElevations(Sector sector, List<? extends LatLon> latlons,
			double targetResolution, double[] buffer)
	{
		double retval = super.getElevations(sector, latlons, targetResolution,
				buffer);
		for (int i = 0; i < buffer.length; i++)
		{
			buffer[i] += elevationOffset;
		}
		return retval;
	}

	public double getElevationOffset()
	{
		return elevationOffset;
	}

	public void setElevationOffset(double elevationOffset)
	{
		this.elevationOffset = elevationOffset;
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

	@Override
	public double getBestResolution(Sector sector)
	{
		if (sector != null)
			return super.getBestResolution(sector);

		//fix bug in CompoundElevationModel if sector is null

		double res = Double.MAX_VALUE;
		for (ElevationModel em : this.elevationModels)
		{
			//TODO em may be a CompoundElevationModel, so cannot pass null
			double r = em.getBestResolution(Sector.FULL_SPHERE);

			if (r < res)
				res = r;
		}
		return res;
	}
}
