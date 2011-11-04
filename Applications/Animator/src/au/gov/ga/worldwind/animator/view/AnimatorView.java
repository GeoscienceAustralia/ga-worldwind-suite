package au.gov.ga.worldwind.animator.view;

import gov.nasa.worldwind.geom.Position;
import au.gov.ga.worldwind.common.view.stereo.StereoOrbitView;

public class AnimatorView extends StereoOrbitView implements ClipConfigurableView
{
	private boolean autoNearClip = true;
	private boolean autoFarClip = true;
	
	@Override
	protected double computeNearDistance(Position eyePosition)
	{
		if(autoNearClip)
		{
			return AnimatorViewUtils.computeNearClippingDistance(dc);
		}
		return this.nearClipDistance;
	}
	
	@Override
	protected double computeFarDistance(Position eyePosition)
	{
		if(autoFarClip)
		{
			return AnimatorViewUtils.computeFarClippingDistance(dc);
		}
		return this.farClipDistance;
	}
	
	@Override
	public void setNearClipDistance(double clipDistance)
	{
		autoNearClip = false;
		super.setNearClipDistance(Math.min(clipDistance, this.farClipDistance - 1));
	}
	
	@Override
	public void setFarClipDistance(double clipDistance)
	{
		autoFarClip = false;
		super.setFarClipDistance(Math.max(clipDistance, this.nearClipDistance + 1));
	}

	@Override
	public void setAutoCalculateNearClipDistance(boolean autoCalculate)
	{
		autoNearClip = autoCalculate;
	}

	@Override
	public void setAutoCalculateFarClipDistance(boolean autoCalculate)
	{
		autoFarClip = autoCalculate;
	}
}
