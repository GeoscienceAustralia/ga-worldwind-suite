package au.gov.ga.worldwind.common.layers.delegate;

import gov.nasa.worldwind.render.DrawContext;

/**
 * Instances of {@link IRenderDelegate} are called before and after rendering a
 * layer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRenderDelegate extends IDelegate
{
	/**
	 * Setup GL state; called before rendering the layer.
	 * 
	 * @param dc
	 */
	void preRender(DrawContext dc);

	/**
	 * Pack down GL state; called after rendering the layer.
	 * 
	 * @param dc
	 */
	void postRender(DrawContext dc);
}
