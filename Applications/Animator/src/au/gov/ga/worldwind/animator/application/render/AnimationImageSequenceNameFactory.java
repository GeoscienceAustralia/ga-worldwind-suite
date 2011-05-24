package au.gov.ga.worldwind.animator.application.render;

import java.io.File;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.util.FileUtil;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;

public class AnimationImageSequenceNameFactory
{
	/**
	 * Create an image sequence file name for the specified frame of the provided animation.
	 * <p/>
	 * Of the form <code>[frameName][padded sequence number].tga</code>
	 * <p/>
	 * e.g. <code>frecinet0020.tga</code>
	 */
	public static String createImageSequenceFileName(Animation animation, int frame, String frameName)
	{
		return createImageSequenceName(animation, frame, frameName) + ".tga";
	}
	
	/**
	 * Create an image sequence name for the specified frame of the provided animation.
	 * <p/>
	 * Of the form <code>[frameName][padded sequence number]</code>
	 * <p/>
	 * e.g. <code>freycinet0020</code>
	 */
	public static String createImageSequenceName(Animation animation, int frame, String frameName)
	{
		return createImageSequenceName(frame, frameName, String.valueOf(animation.getFrameCount()).length());
	}
	
	/**
	 * Create an image sequence file for the specified animation in the specified output directory.
	 */
	public static File createImageSequenceFile(Animation animation, int frame, String frameName, File outputDir)
	{
		return new File(outputDir, createImageSequenceFileName(animation, frame, frameName));
	}
	
	/**
	 * Create an image sequence file for the specified stereo animation output in the specified output directory.
	 */
	public static File createStereoImageSequenceFile(Animation animation, int frame, String frameName, File outputDir, Eye eye)
	{
		String eyeString = eye == Eye.LEFT ? "left" : "right";
		File dir = new File(outputDir, frameName + "_" + eyeString);
		
		return new File(dir, createImageSequenceFileName(animation, frame, frameName));
	}
	
	private static String createImageSequenceName(int sequenceNumber, String prefix, int padTo)
	{
		return prefix + FileUtil.paddedInt(sequenceNumber, padTo);
	}
}
