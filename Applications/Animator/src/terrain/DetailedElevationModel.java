package terrain;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.AbstractElevationModel;

import java.util.List;

public class DetailedElevationModel extends AbstractElevationModel
{
	private ElevationModel sourceModel;
	private double min = -Double.MAX_VALUE;
	private double max = Double.MAX_VALUE;
	private double offset = 0d;
	private double detailHint = 0d;

	public DetailedElevationModel(ElevationModel source)
	{
		this.sourceModel = source;
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
		return this.sourceModel;
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

	public double getMaxElevation()
	{
		return this.clampElevation(this.sourceModel.getMaxElevation());
	}

	public double getMinElevation()
	{
		return this.clampElevation(this.sourceModel.getMinElevation());
	}

	public double[] getExtremeElevations(Angle latitude, Angle longitude)
	{
		double[] elevs = this.sourceModel.getExtremeElevations(latitude,
				longitude);
		if (elevs == null)
			return elevs;

		return new double[]
		{ this.clampElevation(elevs[0]), this.clampElevation(elevs[1]) };
	}

	public double[] getExtremeElevations(Sector sector)
	{
		double[] elevs = this.sourceModel.getExtremeElevations(sector);
		if (elevs == null)
			return elevs;

		return new double[]
		{ this.clampElevation(elevs[0]), this.clampElevation(elevs[1]) };
	}

	public double getElevations(Sector sector, List<? extends LatLon> latlons,
			double targetResolution, double[] buffer)
	{
		double resolution = this.sourceModel.getElevations(sector, latlons,
				targetResolution, buffer);

		for (int i = 0; i < latlons.size(); i++)
		{
			LatLon ll = latlons.get(i);
			if (this.sourceModel.contains(ll.getLatitude(), ll.getLongitude()))
				buffer[i] = clampElevation(buffer[i]);
		}

		return resolution;
	}

	public double getUnmappedElevations(Sector sector,
			List<? extends LatLon> latlons, double targetResolution,
			double[] buffer)
	{
		double resolution = this.sourceModel.getElevations(sector, latlons,
				targetResolution, buffer);

		for (int i = 0; i < latlons.size(); i++)
		{
			LatLon ll = latlons.get(i);
			if (this.sourceModel.contains(ll.getLatitude(), ll.getLongitude())
					&& buffer[i] != this.sourceModel.getMissingDataSignal())
				buffer[i] = clampElevation(buffer[i]);
		}

		return resolution;
	}

	public int intersects(Sector sector)
	{
		return this.sourceModel.intersects(sector);
	}

	public boolean contains(Angle latitude, Angle longitude)
	{
		return this.sourceModel.contains(latitude, longitude);
	}

	public double getBestResolution(Sector sector)
	{
		return this.sourceModel.getBestResolution(sector);
	}

	public double getUnmappedElevation(Angle latitude, Angle longitude)
	{
		double elev = this.sourceModel
				.getUnmappedElevation(latitude, longitude);
		return elev == this.sourceModel.getMissingDataSignal() ? elev
				: clampElevation(elev);
	}

	protected double clampElevation(double elevation)
	{
		return elevation < min ? min : elevation > max ? max : elevation + offset;
	}
}
