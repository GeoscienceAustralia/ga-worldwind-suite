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

/**
 * A simple container representing a curve point [frame, value]
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
class ParameterCurvePoint
{
	double frame;
	double value;
	
	ParameterCurvePoint(double frame, double value)
	{
		this.frame = frame;
		this.value = value;
	}

	public ParameterCurvePoint subtract(ParameterCurvePoint other)
	{
		return new ParameterCurvePoint(frame - other.frame, value - other.value);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + frame + ", " + value + "]";
	}

	/**
	 * @param newValue
	 * @return
	 */
	public ParameterCurvePoint add(ParameterCurvePoint newValue)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
