package terrain;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

public class DetailedCompoundElevationModel extends CompoundElevationModel
{
	protected double detailHint = 0;

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

	/*@Override
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
	}*/
}
