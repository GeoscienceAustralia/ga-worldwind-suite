package view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.view.OrbitView;

public interface RollOrbitView extends OrbitView
{
	Angle getRoll();

	void setRoll(Angle roll);

	void setEye(LatLon eye, Angle heading, Angle pitch, Angle roll, double zoom);

	void setCenter(LatLon center, Angle heading, Angle pitch, Angle roll,
			double zoom);
}
