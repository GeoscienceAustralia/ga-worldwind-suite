package au.gov.ga.worldwind.common.view.stereo;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;

public class StereoViewSupport extends OrbitViewInputSupport
{
	public static Matrix transformTransformMatrix(StereoView view, Matrix matrix, double zoom,
			double eyeSeparation)
	{
		if (view.isDrawingStereo())
		{
			matrix =
					Matrix.fromTranslation(
							(view.getEye() == Eye.RIGHT ? 1 : -1) * eyeSeparation * 0.5, 0, 0)
							.multiply(matrix);
		}
		return matrix;
	}

	public static Matrix fromFrustum(double left, double right, double bottom, double top,
			double near, double far)
	{
		double A = (right + left) / (right - left);
		double B = (top + bottom) / (top - bottom);
		double C = -(far + near) / (far - near);
		double D = -(2 * far * near) / (far - near);
		double E = (2 * near) / (right - left);
		double F = (2 * near) / (top - bottom);
		return new Matrix(E, 0, A, 0, 0, F, B, 0, 0, 0, C, D, 0, 0, -1, 0);
	}
}
