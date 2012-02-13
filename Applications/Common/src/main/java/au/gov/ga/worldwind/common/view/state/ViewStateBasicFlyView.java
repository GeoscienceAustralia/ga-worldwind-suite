package au.gov.ga.worldwind.common.view.state;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.ViewUtil;
import au.gov.ga.worldwind.common.view.transform.TransformBasicFlyView;

/**
 * Fly view with better support for copying view state from other views.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ViewStateBasicFlyView extends TransformBasicFlyView
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
