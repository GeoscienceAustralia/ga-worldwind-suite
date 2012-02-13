package au.gov.ga.worldwind.viewer.layers.geonames;

/**
 * Interface for {@link GeoName} visibility calculation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface VisibilityCalculator
{
	public boolean isVisible(GeoName geoname);
}
