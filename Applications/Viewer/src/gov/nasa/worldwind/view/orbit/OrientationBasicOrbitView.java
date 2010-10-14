package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport.OrbitViewState;

public class OrientationBasicOrbitView extends BasicOrbitView
{
	private boolean validModelCoordinates;
	private boolean settingOrientationInCopyViewState;

	protected boolean trySetOrientation(Position eyePosition, Position centerPosition)
	{
		settingOrientationInCopyViewState = true;
		setOrientation(eyePosition, centerPosition);
		settingOrientationInCopyViewState = false;
		return validModelCoordinates;
	}

	@Override
	protected boolean validateModelCoordinates(OrbitViewState modelCoords)
	{
		validModelCoordinates = super.validateModelCoordinates(modelCoords);

		//if we are copying the view state, always return true, so an error is not thrown
		if (settingOrientationInCopyViewState)
			return true;

		return validModelCoordinates;
	}
}
