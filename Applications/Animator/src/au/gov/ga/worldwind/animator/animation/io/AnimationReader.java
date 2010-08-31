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
