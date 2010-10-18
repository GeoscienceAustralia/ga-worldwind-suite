package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport.OrbitViewState;

public class LenientBasicOrbitView extends BasicOrbitView
{
	@Override
	protected boolean validateModelCoordinates(OrbitViewState modelCoords)
	{
		return (modelCoords != null
	            && modelCoords.getCenterPosition() != null
	            && modelCoords.getCenterPosition().getLatitude().degrees >= -90
	            && modelCoords.getCenterPosition().getLatitude().degrees <= 90
	            && modelCoords.getHeading() != null
	            && modelCoords.getPitch() != null
	            && modelCoords.getPitch().degrees >= -90
	            && modelCoords.getPitch().degrees <= 90
	            && modelCoords.getZoom() >= 0);
	}
}
