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

/**
 * An interface for views that have a configurable clipping distance
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ClipConfigurableView
{
	
	/**
	 * Sets whether or not to auto-calculate the near clipping distance. If <code>true</code>,
	 * values set previously via {@link #setNearClipDistance(double)} will be ignored and 
	 * the view will auto-calculate an appropriate value.
	 */
	void setAutoCalculateNearClipDistance(boolean autoCalculate);

	/**
	 * Sets the near clipping distance. Calling this method will disable near clip auto-calculation in the same way as 
	 * calling {@link #setAutoCalculateNearClipDistance(boolean)} with <code>false</code>.
	 * 
	 * @param nearClip the near clipping distance to set. If this is >= <code>farClip</code>, <code>farClip</code> will be set to <code>nearClip</code>+1.
	 */
	void setNearClipDistance(double nearClip);
	
	/**
	 * Sets whether or not to auto-calculate the far clipping distance. If <code>true</code>,
	 * values set previously via {@link #setFarClipDistance(double)} will be ignored and 
	 * the view will auto-calculate an appropriate value.
	 */
	void setAutoCalculateFarClipDistance(boolean autoCalculate);

	/**
	 * Sets the near clipping distance. Calling this method will disable near clip auto-calculation in the same way as 
	 * calling {@link #setAutoCalculateNearClipDistance(boolean)} with <code>false</code>.
	 * 
	 * @param farClip the far clipping distance to set. If this is <= <code>nearClip</code>, <code>nearClip</code> will be set to <code>farClip</code>-1.
	 */
	void setFarClipDistance(double farClip);
	
}
