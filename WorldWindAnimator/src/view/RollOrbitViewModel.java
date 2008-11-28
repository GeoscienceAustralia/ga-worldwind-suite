package view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.OrbitViewModel;

public interface RollOrbitViewModel extends OrbitViewModel
{
	public interface RollModelCoordinates extends
			OrbitViewModel.ModelCoordinates
	{
		Angle getRoll();
	}

	Matrix computeTransformMatrix(Globe globe, Position center, Angle heading,
			Angle pitch, Angle roll, double zoom);

	RollModelCoordinates computeModelCoordinates(Globe globe, Vec4 eyePoint,
			Vec4 centerPoint, Vec4 up);

	RollModelCoordinates computeModelCoordinates(Globe globe, Matrix modelview,
			Vec4 centerPoint);
}
