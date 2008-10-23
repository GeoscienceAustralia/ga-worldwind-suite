package stereo;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.BasicOrbitViewModel;
import stereo.StereoOrbitView.Eye;
import stereo.StereoOrbitView.StereoMode;

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
		if (view.isDrawing())
		{
			if (view.getMode() == StereoMode.OFFSET)
			{
				matrix = Matrix.fromTranslation(
						(view.getEye() == Eye.RIGHT ? 1 : -1) * zoom
								* view.getEyeSeparation() * 0.0005, 0, 0)
						.multiply(matrix);
			}
			else if (view.getMode() == StereoMode.FRUSTUM)
			{
				matrix = Matrix.fromTranslation(
						(view.getEye() == Eye.RIGHT ? 1 : -1)
								* view.getEyeSeparation() * 0.5, 0, 0)
						.multiply(matrix);
			}
		}
		return matrix;
	}
}
