/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import au.gov.ga.worldwind.common.view.delegate.DelegateOrbitView;
import au.gov.ga.worldwind.common.view.stereo.StereoViewDelegate;

/**
 * {@link StereoViewDelegate} subclass that provides support for configuration
 * of the clipping far/near distances.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatorView extends DelegateOrbitView implements ClipConfigurableView
{
	private boolean autoNearClip = true;
	private boolean autoFarClip = true;

	private Quaternion headRotation = Quaternion.IDENTITY;
	private Vec4 headPosition = Vec4.ZERO;
	private boolean rotated = false;

	public AnimatorView()
	{
		setPrioritizeFarClipping(false);
	}
	
	@Override
	public void setHeading(Angle heading)
	{
		super.setHeading(heading);
		System.out.println(heading);
	}

	public Quaternion getHeadRotation()
	{
		return headRotation;
	}

	public void setHeadRotation(Quaternion headRotation)
	{
		this.headRotation = headRotation;
	}

	public Vec4 getHeadPosition()
	{
		return headPosition;
	}

	public void setHeadPosition(Vec4 headPosition)
	{
		this.headPosition = headPosition;
	}

	@Override
	public Matrix computeModelView()
	{
		Matrix modelView = super.computeModelView();
		if (!rotated)
		{
			rotated = true;
			Matrix positionM = Matrix.fromTranslation(headPosition.multiply3(-1));
			Matrix rotationM = Matrix.fromQuaternion(headRotation.getInverse());
			modelView = rotationM.multiply(positionM.multiply(modelView));
		}
		return modelView;
	}

	@Override
	protected void doApply(gov.nasa.worldwind.render.DrawContext dc)
	{
		rotated = false;
		super.doApply(dc);
	}

	@Override
	protected double computeNearDistance(Position eyePosition)
	{
		if (autoNearClip)
		{
			return AnimatorViewUtils.computeNearClippingDistance(dc);
		}
		return this.nearClipDistance;
	}

	@Override
	protected double computeFarDistance(Position eyePosition)
	{
		if (autoFarClip)
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
