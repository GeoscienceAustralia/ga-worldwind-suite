package au.gov.ga.worldwind.animator.animation.layer;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameterFactory;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter.Type;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.layers.AnimationLayerLoader;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.layers.LayerIdentifierFactory;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A default implementation of the {@link AnimatableLayer} interface.
 * <p/>
 * Can be used to wrap around any {@link Layer} implementation.
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
	
	/**
	 * No-arg constructor required for de-serialisation. Not for general use.
	 */
	@SuppressWarnings("unused")
	private DefaultAnimatableLayer(){super();};

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		layer.setEnabled(enabled);
	}
	
	@Override
	protected void doApply(AnimationContext animationContext, int frame)
	{
		for (LayerParameter lp : layerParameters.values())
		{
			if (lp.isEnabled())
			{
				lp.apply(animationContext, frame);
			}
			else
			{
				lp.applyDefaultValue(animationContext);
			}
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
		Validate.isTrue(parameter.getLayer().equals(getLayer()), "Parameter is not linked to the correct layer. Expected '" + getLayer() + "'.");
		layerParameters.put(parameter.getType(), parameter);
		
		parameter.addChangeListener(this);
	}

	@Override
	public LayerParameter getParameterOfType(Type type)
	{
		return layerParameters.get(type);
	}
	
	@Override
	public LayerIdentifier getLayerIdentifier()
	{
		return LayerIdentifierFactory.createFromLayer(layer);
	}
	
	@Override
	public boolean hasError()
	{
		return false;
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getAnimatableLayerName());
		WWXML.setTextAttribute(result, constants.getAnimatableLayerAttributeName(), getName());
		WWXML.setBooleanAttribute(result, constants.getAnimatableLayerAttributeEnabled(), isEnabled());
		
		URL layerUrl = getLayerUrl();
		if (layerUrl != null)
		{
			WWXML.setTextAttribute(result, constants.getAnimatableLayerAttributeUrl(), getLayerUrl().toExternalForm());
		}

		for (LayerParameter parameter : layerParameters.values())
		{
			result.appendChild(parameter.toXml(result, version));
		}
		
		return result;
	}

	/**
	 * @return The URL of the layer associated with this instance, if available
	 */
	private URL getLayerUrl()
	{
		return (URL)layer.getValue(AVKeyMore.CONTEXT_URL);
	}

	@Override
	public Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		Validate.notNull(context, "A context is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		switch (version)
		{
			case VERSION020:
			{
				Validate.isTrue(context.hasKey(constants.getAnimationKey()), "An animation is required in context.");
				
				// Extract the layer properties from the XML
				String layerName = WWXML.getText(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableLayerAttributeName());
				String layerUrlString = WWXML.getText(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableLayerAttributeUrl());
				if (layerUrlString == null)
				{
					Logging.logger().log(Level.WARNING, "No url found for layer " + layerName);
					return null;
				}
				URL layerUrl = null;
				try
				{
					layerUrl = new URL(layerUrlString);
				}
				catch (MalformedURLException e)
				{
					Logging.logger().log(Level.WARNING, "Unable to open layer  " + layerName + " from " + layerUrlString + ". Bad URL.");
					return null;
				}
				
				// Load the layer
				Layer loadedLayer = AnimationLayerLoader.loadLayer(layerUrl);
				
				// Load the parameters for the layer
				context.setValue(constants.getCurrentLayerKey(), loadedLayer);
				List<LayerParameter> parameters = new ArrayList<LayerParameter>();
				Element[] parameterElements = WWXML.getElements(element, "./*", null);
				if (parameterElements != null)
				{
					for (Element parameterElement : parameterElements)
					{
						parameters.add(LayerParameterFactory.fromXml(parameterElement, version, context));
					}
				}
				DefaultAnimatableLayer result = new DefaultAnimatableLayer(layerName, loadedLayer, parameters);
				
				Boolean enabled = XMLUtil.getBoolean(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableLayerAttributeEnabled(), true);
				result.setEnabled(enabled, false);
				loadedLayer.setEnabled(enabled);
				
				return result;
			}
		}
		
		return null;
	}
	
}
