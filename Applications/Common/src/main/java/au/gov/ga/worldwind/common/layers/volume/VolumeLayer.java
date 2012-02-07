package au.gov.ga.worldwind.common.layers.volume;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.layers.data.DataLayer;

/**
 * Data layer that renders a volume, using a 6-sided cube whose sides can be
 * dragged to slice the volume.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface VolumeLayer extends DataLayer
{
	/**
	 * Notifies this layer that the data is available. This should be called by
	 * the {@link VolumeDataProvider} once it has loaded the volume data.
	 * 
	 * @param provider
	 *            {@link VolumeDataProvider} containing the volume's data.
	 */
	void dataAvailable(VolumeDataProvider provider);

	/**
	 * @return {@link CoordinateTransformation} used to project the points in
	 *         the data into WGS84 projection. Null if no re-projection is
	 *         required.
	 */
	CoordinateTransformation getCoordinateTransformation();
}
