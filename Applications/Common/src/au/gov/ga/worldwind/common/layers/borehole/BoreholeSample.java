package au.gov.ga.worldwind.common.layers.borehole;

import java.awt.Color;

/**
 * Represents a sample in a borehole.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface BoreholeSample
{
	Borehole getBorehole();

	double getDepthFrom();

	double getDepthTo();

	Color getColor();

	String getText();

	String getLink();
}
