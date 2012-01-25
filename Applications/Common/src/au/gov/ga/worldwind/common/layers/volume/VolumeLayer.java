package au.gov.ga.worldwind.common.layers.volume;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.layers.data.DataLayer;

public interface VolumeLayer extends DataLayer
{
	void dataAvailable(VolumeDataProvider provider);
	CoordinateTransformation getCoordinateTransformation();
}
