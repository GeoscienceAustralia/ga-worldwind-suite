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
package au.gov.ga.worldwind.animator.ui.frameslider;

/**
 * An interface for listeners who want to detect changes to the slider's current frame.
 * <p/>
 * This can occur through user interaction (e.g. clicking or dragging), 
 * or via a programmatic setter invocation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface CurrentFrameChangeListener
{
	/**
	 * Respond to the new current frame
	 */
	void currentFrameChanged(int newCurrentFrame);
}
