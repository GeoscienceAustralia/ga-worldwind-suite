package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.layers.TextureTile;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.Delegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTiledImageLayer;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.LocalRequesterDelegate;

/**
 * A local requester delegate that will immediately perform texture load from a
 * local tileset if {@link ImmediateMode#isImmediate()} returns
 * <code>true</code>.
 * <p/>
 * Used to ensure hi-res versions of layers are available at render time for an
 * animation.
 * 
 * @author Michael de Hoog
 */
public class ImmediateLocalRequesterDelegate extends LocalRequesterDelegate
{
	@Override
	public Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		Runnable task = super.createRequestTask(tile, layer);
		if (!ImmediateMode.isImmediate())
		{
			return task;
		}

		//run immediately to load texture
		task.run();
		return null;
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		Delegate superDelegate = super.fromDefinition(definition);
		if (superDelegate != null)
		{
			return new ImmediateLocalRequesterDelegate();
		}
		return null;
	}
}
