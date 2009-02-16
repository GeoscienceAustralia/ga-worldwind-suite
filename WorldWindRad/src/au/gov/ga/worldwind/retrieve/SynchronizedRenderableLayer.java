package au.gov.ga.worldwind.retrieve;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class SynchronizedRenderableLayer extends RenderableLayer
{
	@Override
	protected synchronized void doRender(DrawContext dc)
	{
		super.doRender(dc);
	}

	@Override
	public synchronized void addRenderable(Renderable renderable)
	{
		super.addRenderable(renderable);
	}

	@Override
	public synchronized void removeRenderable(Renderable renderable)
	{
		super.removeRenderable(renderable);
	}
}
