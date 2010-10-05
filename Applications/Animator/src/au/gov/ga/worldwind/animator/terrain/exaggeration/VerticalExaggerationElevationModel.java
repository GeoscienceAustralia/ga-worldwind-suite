package au.gov.ga.worldwind.animator.terrain.exaggeration;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import au.gov.ga.worldwind.animator.terrain.DetailedElevationModel;

/**
 * An extension of the {@link DetailedElevationModel} that allows {@link ElevationExaggeration}s to be
 * configured.
 * <p/>
 * Multiple {@link ElevationExaggeration}s can be configured, allowing exaggerations to be 'layered'. Elevations that lie between the thresholds of
 * two exaggerators will be exaggerated according to the exaggeration amount of the 'bottom' exaggerator.
 * <p/>
 * In addition, an (optional) global elevation offset can be configured. This offset is applied <b>after</b> exaggeration.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class VerticalExaggerationElevationModel extends DetailedElevationModel implements ElevationExaggeration.ChangeListener
{

	/** The registered exaggerators, keyed by elevation threshold */
	private TreeMap<Double, ElevationExaggeration> exaggerators = new TreeMap<Double, ElevationExaggeration>();
	
	private List<ExaggerationWindow> positiveExaggerationWindows = new ArrayList<ExaggerationWindow>();
	private List<ExaggerationWindow> negativeExaggerationWindows = new ArrayList<ExaggerationWindow>();
	
	/** The global offset to apply after exaggeration */
	private double globalOffset = 1.0;
	
	public VerticalExaggerationElevationModel(ElevationModel source)
	{
		super(source);
	}
	
	public void addExaggerator(ElevationExaggeration exaggerator)
	{
		if (exaggerator == null)
		{
			return;
		}
		doAddExaggerator(exaggerator);
		recalculateExaggerationWindows();
	}

	public void addExaggerators(Collection<ElevationExaggeration> exaggerators)
	{
		if (exaggerators == null)
		{
			return;
		}
		for (ElevationExaggeration exaggerator : exaggerators)
		{
			doAddExaggerator(exaggerator);
		}
		recalculateExaggerationWindows();
	}
	
	private void doAddExaggerator(ElevationExaggeration exaggerator)
	{
		if (exaggerator == null)
		{
			return;
		}
		exaggerators.put(exaggerator.getElevationBoundary(), exaggerator);
		exaggerator.addChangeListener(this);
	}
	
	public void removeExaggerator(ElevationExaggeration exaggerator)
	{
		if (exaggerator == null)
		{
			return;
		}
		removeExaggerator(exaggerator.getElevationBoundary());
	}
	
	public void removeExaggerator(double boundary)
	{
		if (!exaggerators.containsKey(boundary))
		{
			return;
		}
		
		exaggerators.get(boundary).removeChangeListener(this);
		exaggerators.remove(boundary);
		recalculateExaggerationWindows();
	}
	
	public boolean containsExaggeratorAtBoundary(double boundary)
	{
		return exaggerators.containsKey(boundary);
	}
	
	public ElevationExaggeration getExaggeratorForElevation(double elevation)
	{
		Entry<Double, ElevationExaggeration> floorEntry = exaggerators.floorEntry(elevation);
		if (floorEntry == null)
		{
			return null;
		}
		return floorEntry.getValue();
	}
	
	public List<ElevationExaggeration> getExaggerators()
	{
		return new ArrayList<ElevationExaggeration>(exaggerators.values());
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
	
	public double getUnexaggeratedElevation(Angle latitude, Angle longitude)
	{
		return super.getUnmappedElevation(latitude, longitude);
	}
	
	/**
	 * Recalculate all of the exaggeration windows from the current set of elevation exaggerators
	 */
	private void recalculateExaggerationWindows()
	{
		reCalculatePositiveExaggerationWindows();
		reCalculateNegativeExaggerationWindows();
	}

	private void reCalculateNegativeExaggerationWindows()
	{
		negativeExaggerationWindows.clear();
		ExaggerationWindow currentWindow = null;
		ExaggerationWindow previousWindow = null;
		
		List<ElevationExaggeration> exaggerators = getNegativeExaggeratorsDescending();
		for (int i = exaggerators.size() - 1; i >= 0; i--)
		{
			previousWindow = currentWindow;
			
			ElevationExaggeration elevationExaggeration = exaggerators.get(i);
			currentWindow = new ExaggerationWindow(elevationExaggeration.getElevationBoundary(), elevationExaggeration.getExaggeration());
			currentWindow.setLowerWindow(previousWindow);
			
			negativeExaggerationWindows.add(currentWindow);
		}
		
		// Add a [-inf x1 n] window in the negative case
		currentWindow = new ExaggerationWindow(Double.NEGATIVE_INFINITY, 1.0);
		currentWindow.setUpperWindow(negativeExaggerationWindows.get(0));
		negativeExaggerationWindows.add(0, currentWindow);
	}

	private void reCalculatePositiveExaggerationWindows()
	{
		positiveExaggerationWindows.clear();
		ExaggerationWindow currentWindow = null;
		ExaggerationWindow previousWindow = null;
		
		List<ElevationExaggeration> exaggerators = getPositiveExaggeratorsAscending();
		for (int i = 0; i < exaggerators.size(); i++)
		{
			previousWindow = currentWindow;
			
			ElevationExaggeration elevationExaggeration = exaggerators.get(i);
			currentWindow = new ExaggerationWindow(elevationExaggeration.getElevationBoundary(), elevationExaggeration.getExaggeration());
			currentWindow.setLowerWindow(previousWindow);
			
			positiveExaggerationWindows.add(currentWindow);
		}
	}
	
	/**
	 * Exaggerate the elevations contained in the provided buffer using the configured {@link ElevationExaggeration}s.
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
	 * Exaggerate the provided elevation using the configured {@link ElevationExaggeration}s.
	 */
	protected double exaggerateElevation(double elevation)
	{
		ExaggerationWindow window = getWindowForElevation(elevation);
		if (window == null)
		{
			return elevation;
		}
		return window.applyExaggeration(elevation);
	}
	
	private ExaggerationWindow getWindowForElevation(double elevation)
	{
		if (isNegativeElevation(elevation))
		{
			return findWindowForElevation(negativeExaggerationWindows, elevation);
		}
		return findWindowForElevation(positiveExaggerationWindows, elevation);
	}

	private ExaggerationWindow findWindowForElevation(List<ExaggerationWindow> windowList, double elevation)
	{
		for (ExaggerationWindow window : windowList)
		{
			if (window.elevationIsInWindow(elevation))
			{
				return window;
			}
		}
		return null;
	}

	private boolean isNegativeElevation(double elevation)
	{
		return elevation < 0.0;
	}
	
	/**
	 * @return The exaggerators with boundaries of positive elevations, ordered by ascending boundaries. 
	 * Guaranteed to have an exaggerator at 0.0 as the first element in the list.
	 */
	private List<ElevationExaggeration> getPositiveExaggeratorsAscending()
	{
		List<ElevationExaggeration> result = new ArrayList<ElevationExaggeration>(exaggerators.tailMap(0.0, true).values());
		if (result.isEmpty() || result.get(0).getElevationBoundary() > 0.0)
		{
			double zeroExaggeration = 1.0;
			if (exaggerators.floorEntry(0.0) != null)
			{
				zeroExaggeration = exaggerators.floorEntry(0.0).getValue().getExaggeration();
			}
			result.add(0, new ElevationExaggerationImpl(zeroExaggeration, 0.0));
		}
		return result;
	}
	
	/**
	 * @return The exaggerators with boundaries of negative elevations, ordered by descending boundaries. 
	 * Guaranteed to have an exaggerator at 0.0 as the first element in the list.
	 */
	private List<ElevationExaggeration> getNegativeExaggeratorsDescending()
	{
		List<ElevationExaggeration> result = new ArrayList<ElevationExaggeration>(exaggerators.headMap(0.0, false).descendingMap().values());
		if (result.isEmpty() || result.get(0).getElevationBoundary() < 0.0)
		{
			double zeroExaggeration = 1.0;
			if (exaggerators.lowerEntry(0.0) != null)
			{
				zeroExaggeration = exaggerators.lowerEntry(0.0).getValue().getExaggeration();
			}
			result.add(0, new ElevationExaggerationImpl(zeroExaggeration, 0.0));
		}
		return result;
	}

	@Override
	public void exaggerationChanged(ElevationExaggeration exaggeration)
	{
		recalculateExaggerationWindows(); //TODO: Would be more efficient to recalculate only those windows effected by the change
	}
}
