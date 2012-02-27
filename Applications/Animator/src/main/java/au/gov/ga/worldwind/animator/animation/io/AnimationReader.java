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

import gov.nasa.worldwind.WorldWindow;
import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * An interface for classes that are able to read animations from a file.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationReader
{
	
	/**
	 * Read an animation from the provided file.
	 * 
	 * @param fileName The name of the file to read from
	 * @param worldWindow The world window to attach to the animation
	 * 
	 * @return The animation read from the file
	 */
	Animation readAnimation(String fileName, WorldWindow worldWindow);
	
	/**
	 * Read an animation from the provided file.
	 * 
	 * @param file The file to read from
	 * @param worldWindow The world window to attach to the animation
	 * 
	 * @return The animation read from the file
	 */
	Animation readAnimation(File file, WorldWindow worldWindow);
	
	/**
	 * Return the file format version of the provided animation file
	 * 
	 * @param fileName The name of the file to read the animation from
	 * 
	 * @return The file format version of the provided animation file, or <code>null</code> if it is not a valid animation file
	 */
	AnimationFileVersion getFileVersion(String fileName);
	
	/**
	 * Return the file format version of the provided animation file
	 * 
	 * @param file The file to read the animation from
	 * 
	 * @return The file format version of the provided animation file, or <code>null</code> if it is not a valid animation file
	 */
	AnimationFileVersion getFileVersion(File file);
	
}
