package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.render.DrawContext;

/**
 * Instances of {@link RenderDelegate} are called before and after rendering a
 * layer.
 * 
 * @author Michael de Hoog
 */
public interface RenderDelegate extends Delegate
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
