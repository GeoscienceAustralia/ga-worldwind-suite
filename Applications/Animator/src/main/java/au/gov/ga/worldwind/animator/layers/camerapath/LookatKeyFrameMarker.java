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
package au.gov.ga.worldwind.animator.layers.camerapath;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * A key frame marker used to mark the position of the camera look-at at a specific key frame.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LookatKeyFrameMarker extends KeyFrameMarker
{

	public LookatKeyFrameMarker(Animation animation, int frame)
	{
		super(animation, frame, animation.getCamera().getLookatPositionAtFrame(frame));
	}

	@Override
	protected Parameter getLatParameter()
	{
		return getAnimation().getCamera().getLookAtLat();
	}

	@Override
	protected Parameter getLonParameter()
	{
		return getAnimation().getCamera().getLookAtLon();
	}

	@Override
	protected Parameter getElevationParameter()
	{
		return getAnimation().getCamera().getLookAtElevation();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof LookatKeyFrameMarker))
		{
			return false;
		}
		
		return ((LookatKeyFrameMarker)obj).getFrame() == this.getFrame();
	}
	
	@Override
	public int hashCode()
	{
		return this.getFrame();
	}
	
}
