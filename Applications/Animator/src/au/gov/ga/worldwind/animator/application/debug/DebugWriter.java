package au.gov.ga.worldwind.animator.application.debug;

import java.io.FileWriter;
import java.util.Collection;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.parameter.BezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * A class used to write debug output from the animator
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class DebugWriter
{

	/**
	 * Dump the parameter values of key frame values into a file for debugging purposes
	 * 
	 * @param fileName The name of the file to write to
	 * @param animation The animation whose key frames are to be dumped
	 */
	public static void dumpKeyFrameValues(String fileName, Animation animation)
	{
		try
		{
			FileWriter writer = new FileWriter(fileName, false);
			writer.append("Key frame values. Last written " + System.currentTimeMillis() + "\n\n");
			
			for (KeyFrame keyFrame : animation.getKeyFrames())
			{
				writer.append("Frame: ").append("" + keyFrame.getFrame()).append('\n');
				for (ParameterValue value : keyFrame.getParameterValues())
				{
					writer.append("Value: ").append(value.getOwner().getName()).append('\n');
					writer.append("" + value.getValue()).append('|');
					if (value instanceof BezierParameterValue)
					{
						BezierParameterValue bezierValue = (BezierParameterValue) value;
						writer.append("" + bezierValue.getInValue()).append(',').append("" + bezierValue.getInPercent()).append('|');
						writer.append("" + bezierValue.getOutValue()).append(',').append("" + bezierValue.getOutPercent()).append('\n');
					}
				}
				writer.append("\n\n");
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Dump the values of the provided parameter into a file for debugging purposes
	 * 
	 * @param fileName The name of the file to write to
	 * @param p The parameter whose values are to be written
	 * @param start The start frame
	 * @param end The end frame
	 * @param context The current animation context
	 */
	public static void dumpParameterValues(String fileName, Animation animation, Collection<Parameter> parameters, int start, int end)
	{
		try
		{
			FileWriter writer = new FileWriter(fileName, false);
			
			writer.append("Last written: ").append("" + System.currentTimeMillis()).append("\n\n");
			
			for (Parameter p : parameters)
			{
				writer.append("Values for: ").append(p.getName()).append("\n\n");
				
				for (int i = start; i < end; i++)
				{
					writer.append("" + i).append(", ").append("" + p.getValueAtFrame(i).getValue()).append('\n');
				}
				
				writer.append("\n\n");
			}
			
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		
	}
	
}
