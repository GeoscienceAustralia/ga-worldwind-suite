package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IDelegatorLayer;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTextureTile;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageLocalRequesterDelegate;

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
public class ImmediateLocalRequesterDelegate extends ImageLocalRequesterDelegate
{
	@Override
	public Runnable createRequestTask(DelegatorTextureTile tile, IDelegatorLayer<DelegatorTextureTile> layer)
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
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new ImmediateLocalRequesterDelegate();
		return null;
	}
}
