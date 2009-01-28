package stereo;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.BasicOrbitViewModel;
import settings.Settings;
import settings.Settings.ProjectionMode;
import stereo.StereoOrbitView.Eye;

public class StereoOrbitViewModel extends BasicOrbitViewModel
{
	private StereoOrbitView view;

	public StereoOrbitViewModel()
	{
		super();
	}

	public void setView(StereoOrbitView view)
	{
		this.view = view;
	}

	@Override
	public Matrix computeTransformMatrix(Globe globe, Position center,
			Angle heading, Angle pitch, double zoom)
	{
		Matrix matrix = super.computeTransformMatrix(globe, center, heading,
				pitch, zoom);
		if (view.isDrawingStereo())
		{
			ProjectionMode mode = Settings.get().getProjectionMode();
			if (mode == ProjectionMode.SIMPLE_OFFSET)
			{
				matrix = Matrix.fromTranslation(
						(view.getEye() == Eye.RIGHT ? 1 : -1) * zoom
								* Settings.get().getEyeSeparation() * 0.005,
						0, 0).multiply(matrix);
			}
			else if (mode == ProjectionMode.ASYMMETRIC_FRUSTUM)
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
