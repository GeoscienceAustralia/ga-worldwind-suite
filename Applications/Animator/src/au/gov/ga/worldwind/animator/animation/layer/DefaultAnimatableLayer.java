package au.gov.ga.worldwind.animator.animation.layer;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter.Type;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A default implementation of the {@link AnimatableLayer} interface.
 * <p/>
 * Can be used to wrap around any {@link Layer} implementation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class DefaultAnimatableLayer extends AnimatableBase implements AnimatableLayer
{
	private static final long serialVersionUID = 20100907L;

	/** The layer this instance wraps */
	private Layer layer;
	
	/** The map of parameter type -> parameter */
	private Map<LayerParameter.Type, LayerParameter> layerParameters = new HashMap<LayerParameter.Type, LayerParameter>();
	
	/**
	 * Constructor. Initialises the name, layer and layer parameters
	 * 
	 * @param name The name to give to this animatable layer
	 * @param layer The layer to associate with this animatable layer
	 * @param layerParameters The collection of layer parameters to associate with this layer
	 */
	public DefaultAnimatableLayer(String name, Layer layer, Collection<LayerParameter> layerParameters)
	{
		super(name);
		Validate.notNull(layer, "A layer is required");
		this.layer = layer;
		
		// Initialise the layer parameters
		if (layerParameters != null)
		{
			for (LayerParameter layerParameter : layerParameters)
			{
				addParameter(layerParameter);
			}
		}
	}

	/**
	 * Constructor. Sets the name to that of the provided layer and leaves the parameters empty.
	 * 
	 * @param layer The layer to associate with this animatable layer
	 */
	public DefaultAnimatableLayer(Layer layer)
	{
		this(layer.getName(), layer, null);
	}
	

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		for (LayerParameter lp : layerParameters.values())
		{
			lp.apply(animationContext, frame);
		}
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		return new ArrayList<Parameter>(layerParameters.values());
	}

	@Override
	public Layer getLayer()
	{
		return layer;
	}

	@Override
	public void addParameter(LayerParameter parameter)
	{
		if (parameter == null)
		{
			return;
		}
		Validate.isTrue(parameter.getLayer().equals(getLayer()), "Parameter is not linked to the correct layer. Expected '" + getLayer().getName() + "'.");
		layerParameters.put(parameter.getType(), parameter);
	}

	@Override
	public LayerParameter getParameterOfType(Type type)
	{
		return layerParameters.get(type);
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		// TODO Implement me!
		return null;
	}

	@Override
	public Animatable fromXml(Element element, AnimationFileVersion versionId, AVList context)
	{
		// TODO Implement me!
		return null;
	}
}
