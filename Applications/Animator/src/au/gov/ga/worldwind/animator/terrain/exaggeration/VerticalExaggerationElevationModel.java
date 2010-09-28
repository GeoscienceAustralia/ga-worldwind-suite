package au.gov.ga.worldwind.animator.terrain.exaggeration;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import au.gov.ga.worldwind.animator.terrain.DetailedElevationModel;

/**
 * An extension of the {@link DetailedElevationModel} that allows {@link ElevationExaggerator}s to be
 * configured.
 * <p/>
 * Multiple {@link ElevationExaggerator}s can be configured, allowing exaggerations to be 'layered'. Elevations that lie between the thresholds of
 * two exaggerators will be exaggerated according to the exaggeration amount of the 'bottom' exaggerator.
 * <p/>
 * In addition, an (optional) global elevation offset can be configured. This offset is applied <b>after</b> exaggeration.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class VerticalExaggerationElevationModel extends DetailedElevationModel
{

	/** The registered exaggerators, keyed by elevation threshold */
	private TreeMap<Double, ElevationExaggerator> exaggerators = new TreeMap<Double, ElevationExaggerator>();
	
	/** The global offset to apply after exaggeration */
	private double globalOffset = 1.0;
	
	public VerticalExaggerationElevationModel(ElevationModel source)
	{
		super(source);
	}
	
	public void addExaggerator(ElevationExaggerator exaggerator)
	{
		if (exaggerator == null)
		{
			return;
		}
		exaggerators.put(exaggerator.getElevationThreshold(), exaggerator);
	}

	public void addExaggerators(Collection<ElevationExaggerator> exaggerators)
	{
		if (exaggerators == null)
		{
			return;
		}
		for (ElevationExaggerator exaggerator : exaggerators)
		{
			addExaggerator(exaggerator);
		}
	}
	
	public void removeExaggerator(ElevationExaggerator exaggerator)
	{
		exaggerators.remove(exaggerator.getElevationThreshold());
	}
	
	public void removeExaggerator(Double threshold)
	{
		exaggerators.remove(threshold);
	}
	
	public ElevationExaggerator getExaggeratorForElevation(double elevation)
	{
		return exaggerators.floorEntry(elevation).getValue();
	}
	
	public double getGlobalOffset()
	{
		return globalOffset;
	}
	
	public void setGlobalOffset(double offset)
	{
		globalOffset = offset;
	}
	
	@Override
	public double getMaxElevation()
	{
		return exaggerateElevation(super.getMaxElevation());
	}

	@Override
	public double getMinElevation()
	{
		return exaggerateElevation(super.getMinElevation());
	}

	@Override
	public double[] getExtremeElevations(Angle latitude, Angle longitude)
	{
		double[] result = super.getExtremeElevations(latitude, longitude);
		exaggerateElevationsInPlace(result);
		return result;
	}

	@Override
	public double[] getExtremeElevations(Sector sector)
	{
		double[] result = super.getExtremeElevations(sector);
		exaggerateElevationsInPlace(result);
		return result;
	}

	@Override
	public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
	{
		double result = super.getElevations(sector, latlons, targetResolution, buffer);
		exaggerateElevationsInPlace(buffer);
		return result;
	}

	@Override
	public double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
	{
		double result = super.getElevations(sector, latlons, targetResolution, buffer);
		exaggerateElevationsInPlace(buffer);
		return result;
	}

	@Override
	public double getUnmappedElevation(Angle latitude, Angle longitude)
	{
		return exaggerateElevation(super.getUnmappedElevation(latitude, longitude));
	}
	
	/**
	 * Exaggerate the elevations contained in the provided buffer using the configured {@link ElevationExaggerator}s.
	 * <p/>
	 * <em>Note:</em> To keep consistent with elevation processing, the exaggeration is performed in-place in the provided buffer.
	 */
	protected void exaggerateElevationsInPlace(double[] buffer)
	{
		if (buffer == null)
		{
			return;
		}
		
		for (int i = 0; i < buffer.length; i++)
		{
			buffer[i] = exaggerateElevation(buffer[i]);
		}
	}
	
	/**
	 * Exaggerate the provided elevation using the configured {@link ElevationExaggerator}s.
	 */
	protected double exaggerateElevation(double elevation)
	{
		return 0; //TODO:
	}
}
