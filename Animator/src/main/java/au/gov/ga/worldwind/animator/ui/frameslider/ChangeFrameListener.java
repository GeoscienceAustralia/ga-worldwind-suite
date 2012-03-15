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
 * A listener interface that allows clients to listen for changes to frames in the {@link FrameSlider}.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 */
public interface ChangeFrameListener
{
	/**
	 * Invoked when a frame has been changed on the slider. Usually this occurs
	 * when a key frame has been dragged-and-dropped.
	 * 
	 * @param index The index of the key frame in question
	 * @param oldFrame The previous frame of the key frame
	 * @param newFrame The new frame of the key frame
	 */
	public void frameChanged(int index, int oldFrame, int newFrame);
}
