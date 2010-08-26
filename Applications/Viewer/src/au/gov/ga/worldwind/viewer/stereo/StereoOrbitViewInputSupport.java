package au.gov.ga.worldwind.viewer.stereo;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.settings.Settings.ProjectionMode;
import au.gov.ga.worldwind.viewer.stereo.StereoOrbitView.Eye;

public class StereoOrbitViewInputSupport extends OrbitViewInputSupport
{
	public static Matrix transformTransformMatrix(StereoOrbitView view,
			Matrix matrix, double zoom)
	{
		if (view.isDrawingStereo())
		{
			ProjectionMode mode = Settings.get().getProjectionMode();
			if (mode == ProjectionMode.SIMPLE_OFFSET)
			{
				matrix = Matrix.fromTranslation(
						(view.getEye() == Eye.RIGHT ? 1 : -1) * zoom
								* Settings.get().getEyeSeparation() * 0.005, 0,
						0).multiply(matrix);
			} else if (mode == ProjectionMode.ASYMMETRIC_FRUSTUM)
			{
				matrix = Matrix
						.fromTranslation(
								(view.getEye() == Eye.RIGHT ? 1 : -1)
										* Settings.get().getEyeSeparation()
										* 0.5, 0, 0).multiply(matrix);
			}
		}
		return matrix;
	}
}
