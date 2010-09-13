package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.layers.TextureTile;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.Delegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTiledImageLayer;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.URLRequesterDelegate;

/**
 * A URL requester delegate that will immediately perform download and texture
 * load from a URL if {@link ImmediateMode#isImmediate()} returns
 * <code>true</code>.
 * <p/>
 * Used to ensure hi-res versions of layers are available at render time for an
 * animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 */
public class ImmediateURLRequesterDelegate extends URLRequesterDelegate
{
	@Override
	public Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		Runnable task = super.createRequestTask(tile, layer);
		if (!ImmediateMode.isImmediate())
		{
			return task;
		}

		//run twice: once for download, second time for load texture
		task.run();
		task.run();
		return null;
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		Delegate superDelegate = super.fromDefinition(definition);
		if (superDelegate != null)
		{
			return new ImmediateURLRequesterDelegate();
		}
		return null;
	}
}
