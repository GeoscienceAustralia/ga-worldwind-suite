package au.gov.ga.worldwind.common.layers.borehole;

import java.awt.Color;

/**
 * Represents a sample in a borehole.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface BoreholeSample
{
	/**
	 * @return {@link Borehole} that this sample is associated with
	 */
	Borehole getBorehole();

	/**
	 * @return Top depth of this sample (in positive meters)
	 */
	double getDepthFrom();

	/**
	 * @return Bottom depth of this sample (in positive meters)
	 */
	double getDepthTo();

	/**
	 * @return Color used to display this sample
	 */
	Color getColor();

	/**
	 * @return The display text associated with this sample; eg to show as a
	 *         tooltip
	 */
	String getText();

	/**
	 * @return A URL string to a website that describes this sample (null if
	 *         none)
	 */
	String getLink();
}
