package au.gov.ga.worldwind.viewer.panels.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;

public class ViewStateBasicFlyView extends BasicFlyView
{
	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();
		Position eyePosition = view.getCurrentEyePosition();

		Matrix positionTransform = ViewUtil.computePositionTransform(globe, eyePosition);
		Matrix invPositionTransform = positionTransform.getInverse();

		if (invPositionTransform != null)
		{
			Matrix transform = view.getModelviewMatrix().multiply(invPositionTransform);
			setEyePosition(eyePosition);
			setHeading(ViewUtil.computeHeading(transform));
			setPitch(ViewUtil.computePitch(transform));
		}
		else
		{
			setEyePosition(eyePosition);
			setHeading(view.getHeading());
			setPitch(view.getPitch());
		}
	}
}
