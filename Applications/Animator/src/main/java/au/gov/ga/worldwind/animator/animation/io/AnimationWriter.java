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
package au.gov.ga.worldwind.animator.animation.io;

import java.io.File;
import java.io.IOException;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * An interface for writers that can persist an {@link Animation} to a file
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationWriter
{
	/**
	 * Write the provided {@link Animation} to the given file.
	 * <p/>
	 * Existing files will be overwritten.
	 * 
	 * @param fileName The name of the file to write to
	 * @param animation The animation to write to file
	 * 
	 * @throws IOException if a problem occurs during writing
	 */
	void writeAnimation(String fileName, Animation animation) throws IOException;
	
	/**
	 * Write the provided {@link Animation} to the given file.
	 * <p/>
	 * Existing files will be overwritten.
	 * 
	 * @param file The file to write to
	 * @param animation The animation to write to file
	 * 
	 * @throws IOException if a problem occurs during writing
	 */
	void writeAnimation(File file, Animation animation) throws IOException;
}
