package au.gov.ga.worldwind.common.util.exaggeration;

import gov.nasa.worldwind.render.DrawContext;

/**
 * An interface for services that can apply vertical exaggeration to an elevation value
 */
public interface VerticalExaggerationService
{

	/** 
	 * @return The exaggerated elevation value 
	 */
	double applyVerticalExaggeration(DrawContext dc, double elevation);
	
	/** 
	 * @return The global vertical exaggeration value 
	 */
	double getGlobalVerticalExaggeration(DrawContext dc);
}
