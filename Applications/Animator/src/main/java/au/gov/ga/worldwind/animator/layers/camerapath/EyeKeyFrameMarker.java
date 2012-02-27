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

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

import java.awt.Color;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * A key frame marker used to mark the position of the camera eye at a specific key frame.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class EyeKeyFrameMarker extends KeyFrameMarker
{
	private static final BasicMarkerAttributes DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	static
	{
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES = new BasicMarkerAttributes();
		DEFAULT_UNHIGHLIGHTED_ATTRIBUTES.setMaterial(new Material(Color.BLUE));
	}
	
	public EyeKeyFrameMarker(Animation animation, int frame)
	{
		super(animation, frame, animation.getCamera().getEyePositionAtFrame(frame));
	}

	@Override
	public BasicMarkerAttributes getUnhighlightedAttributes()
	{
		return DEFAULT_UNHIGHLIGHTED_ATTRIBUTES;
	}

	@Override
	protected Parameter getLatParameter()
	{
		return getAnimation().getCamera().getEyeLat();
	}

	@Override
	protected Parameter getLonParameter()
	{
		return getAnimation().getCamera().getEyeLon();
	}

	@Override
	protected Parameter getElevationParameter()
	{
		return getAnimation().getCamera().getEyeElevation();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof EyeKeyFrameMarker))
		{
			return false;
		}
		
		return ((EyeKeyFrameMarker)obj).getFrame() == this.getFrame();
	}
	
	@Override
	public int hashCode()
	{
		return this.getFrame();
	}
	
}
