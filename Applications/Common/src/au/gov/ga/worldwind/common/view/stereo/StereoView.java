package au.gov.ga.worldwind.common.view.stereo;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;

public interface StereoView extends View
{
	public enum Eye
	{
		LEFT,
		RIGHT
	}
	
	Eye getEye();
	void setEye(Eye eye);
	boolean isDrawingStereo();
	void setDrawingStereo(boolean drawingStereo);
	double getSeparationExaggeration();
	void setSeparationExaggeration(double separationExaggeration);
	Matrix calculateProjectionMatrix(double nearDistance, double farDistance);
}
