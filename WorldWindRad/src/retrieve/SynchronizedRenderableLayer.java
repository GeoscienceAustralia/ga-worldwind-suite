package retrieve;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class SynchronizedRenderableLayer extends RenderableLayer
{
	protected Object lock = new Object();
	
	@Override
	protected void doRender(DrawContext dc)
	{
		synchronized (lock)
		{
			super.doRender(dc);
		}
	}

	@Override
	public void addRenderable(Renderable renderable)
	{
		synchronized (lock)
		{
			super.addRenderable(renderable);
		}
	}

	@Override
	public void removeRenderable(Renderable renderable)
	{
		synchronized (lock)
		{
			super.removeRenderable(renderable);
		}
	}
}
