package au.gov.ga.worldwind.animator.layers.immediate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IDelegatorLayer;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTextureTile;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageURLRequesterDelegate;

/**
 * A URL requester delegate that will immediately perform download and texture
 * load from a URL if {@link ImmediateMode#isImmediate()} returns
 * <code>true</code>.
 * <p/>
 * Used to ensure hi-res versions of layers are available at render time for an
 * animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImmediateURLRequesterDelegate extends ImageURLRequesterDelegate
{
	@Override
	public Runnable createRequestTask(DelegatorTextureTile tile, IDelegatorLayer<DelegatorTextureTile> layer)
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
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new ImmediateURLRequesterDelegate();
		return null;
	}
}
