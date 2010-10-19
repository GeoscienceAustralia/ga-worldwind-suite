package au.gov.ga.worldwind.common.view.free;

import gov.nasa.worldwind.awt.ViewInputHandler;

public interface FreeViewInputHandler extends ViewInputHandler
{
	void look(double deltaHeading, double deltaPitch, double deltaRoll);

	void move(double deltaX, double deltaY, double deltaZ);
}
