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
package au.gov.ga.worldwind.animator.ui.parametereditor;

import au.gov.ga.worldwind.common.util.Range;


/**
 * A container that defines a bounding box for a drawn parameter curve.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
class ParameterCurveBounds
{
	ParameterCurvePoint bottomLeft;
	ParameterCurvePoint topRight;
	
	ParameterCurveBounds(ParameterCurvePoint bottomLeft, ParameterCurvePoint topRight)
	{
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
	}
	
	ParameterCurveBounds(double minFrame, double maxFrame, double minValue, double maxValue)
	{
		this.bottomLeft = new ParameterCurvePoint(minFrame, minValue);
		this.topRight = new ParameterCurvePoint(maxFrame, maxValue);
	}
	
	double getMinFrame()
	{
		return bottomLeft.frame;
	}
	
	double getMaxFrame()
	{
		return topRight.frame;
	}
	
	double getFrameWindow()
	{
		return getMaxFrame() - getMinFrame();
	}
	
	Range<Double> getFrameRange()
	{
		return new Range<Double>(getMinFrame(), getMaxFrame());
	}
	
	double getMinValue()
	{
		return bottomLeft.value;
	}
	
	double getMaxValue()
	{
		return topRight.value;
	}
	
	double getValueWindow()
	{
		return getMaxValue() - getMinValue();
	}
	
	Range<Double> getValueRange()
	{
		return new Range<Double>(getMinValue(), getMaxValue());
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[X:" + getMinFrame() + "-" + getMaxFrame() + ", Y:" + getMinValue() + "-" + getMaxValue() + "]"; 
	}
}
