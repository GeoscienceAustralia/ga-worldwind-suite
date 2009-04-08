package view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.OrbitView;

/**
 * @author Michael de Hoog
 */
public interface RollOrbitView extends OrbitView
{
	Angle getRoll();

	void setRoll(Angle roll);
}
