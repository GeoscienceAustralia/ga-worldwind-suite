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
	void setDrawingStereo(boolean drawing);
	Matrix calculateProjectionMatrix(double near, double far);
}
