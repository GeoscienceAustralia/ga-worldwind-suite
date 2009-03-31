package view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.OrbitViewModel;

public interface RollOrbitViewModel extends OrbitViewModel
{
	Angle getRoll();

	void setRoll(Angle roll);
}
