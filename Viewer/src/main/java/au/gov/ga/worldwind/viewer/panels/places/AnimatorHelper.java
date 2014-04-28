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
package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.firstperson.FlyToFlyViewAnimator;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

/**
 * Helper class for generating view {@link Animator}s for animating from the
 * current view position to a given center/eye position.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AnimatorHelper
{
	/**
	 * Create an apply a new {@link Animator} to fly from the current position
	 * to the given center/eye positions.
	 * 
	 * @param view
	 *            View to apply the animator to
	 * @param centerPosition
	 *            Destination center position (look-at)
	 * @param eyePosition
	 *            Destination eye position (camera)
	 * @param up
	 *            Final up-vector
	 * @return Calculated length of the animation, taking into account the
	 *         {@link Settings} view iterator speed
	 */
	public static long addAnimator(View view, Position centerPosition, Position eyePosition, Vec4 up)
	{
		Globe globe = view.getGlobe();
		Vec4 eyePoint = globe.computePointFromPosition(eyePosition);
		Vec4 centerPoint = globe.computePointFromPosition(centerPosition);

		if (up == null)
		{
			up = globe.computeSurfaceNormalAtPoint(centerPoint);
			Vec4 forward = centerPoint.subtract3(eyePoint).normalize3();
			if (forward.cross3(up).getLength3() < 0.001)
			{
				Matrix modelview =
						OrbitViewInputSupport.computeTransformMatrix(globe, centerPosition, view.getHeading(),
								Angle.ZERO, view.getRoll(), 1);
				if (modelview != null)
				{
					Matrix modelviewInv = modelview.getInverse();
					if (modelviewInv != null)
					{
						up = Vec4.UNIT_Y.transformBy4(modelviewInv);
					}
				}
			}
		}

		if (up == null)
		{
			return -1;
		}


		if (view instanceof OrbitView)
		{
			OrbitViewInputSupport.OrbitViewState ovs =
					OrbitViewInputSupport.computeOrbitViewState(globe, eyePoint, centerPoint, up);

			if (ovs == null)
			{
				return -1;
			}

			OrbitView orbitView = (OrbitView) view;

			Position currentCenter = orbitView.getCenterPosition();
			long lengthMillis = SettingsUtil.getScaledLengthMillis(currentCenter, centerPosition);

			view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView, currentCenter,
					centerPosition, view.getHeading(), ovs.getHeading(), view.getPitch(), ovs.getPitch(),
					orbitView.getZoom(), ovs.getZoom(), lengthMillis, WorldWind.ABSOLUTE));

			return lengthMillis;
		}
		else if (view instanceof BasicFlyView)
		{
			ViewUtil.ViewState vs = ViewUtil.computeViewState(globe, eyePoint, centerPoint, up);

			if (vs == null)
			{
				return -1;
			}

			Vec4 currentEyePoint = view.getEyePoint();
			Vec4 currentCenterPoint = view.getCenterPoint();
			if (currentCenterPoint == null)
			{
				currentCenterPoint = currentEyePoint.add3(view.getForwardVector());
			}

			Position currentCenter = globe.computePositionFromPoint(currentCenterPoint);
			Position currentEye = globe.computePositionFromPoint(currentEyePoint);

			BasicFlyView flyView = (BasicFlyView) view;
			long lengthMillis = SettingsUtil.getScaledLengthMillis(currentCenter, centerPosition);

			FlyToFlyViewAnimator.createFlyToFlyViewAnimator(flyView, currentCenter, centerPosition, view.getHeading(),
					vs.getHeading(), view.getPitch(), vs.getPitch(), currentEye.elevation, eyePosition.elevation,
					lengthMillis, WorldWind.ABSOLUTE);

			return lengthMillis;
		}
		return -1;
	}
}
