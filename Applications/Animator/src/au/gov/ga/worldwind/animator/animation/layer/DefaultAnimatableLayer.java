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

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter.Type;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameterFactory;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.layers.AnimationLayerLoader;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.layers.LayerIdentifierFactory;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.Validate;

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
	private Map<LayerParameter.Type, LayerParameter> layerParameters =
			new HashMap<LayerParameter.Type, LayerParameter>();

	/**
	 * Constructor. Initialises the name, layer and layer parameters
	 * 
	 * @param name
	 *            The name to give to this animatable layer
	 * @param layer
	 *            The layer to associate with this animatable layer
	 * @param layerParameters
	 *            The collection of layer parameters to associate with this
	 *            layer
	 */
	public DefaultAnimatableLayer(String name, Animation animation, Layer layer,
			Collection<LayerParameter> layerParameters)
	{
		super(nameOrLayerName(name, layer), animation);
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
	
	protected static String nameOrLayerName(String name, Layer layer)
	{
		return name != null ? name : layer != null ? layer.getName() : null;
	}

	/**
	 * Constructor. Sets the name to that of the provided layer and leaves the
	 * parameters empty.
	 * 
	 * @param layer
	 *            The layer to associate with this animatable layer
	 */
	public DefaultAnimatableLayer(Animation animation, Layer layer)
	{
		this(null, animation, layer, null);
	}

	/**
	 * No-arg constructor required for de-serialisation. Not for general use.
	 */
	@SuppressWarnings("unused")
	private DefaultAnimatableLayer()
	{
		super();
	}
	
	@Override
	protected String getDefaultName()
	{
		return "Layer";
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		layer.setEnabled(enabled);
	}

	@Override
	protected void doApply()
	{
		for (LayerParameter lp : layerParameters.values())
		{
			lp.apply();
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
		Validate.isTrue(parameter.getLayer().equals(getLayer()),
				"Parameter is not linked to the correct layer. Expected '" + getLayer() + "'.");
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

	/**
	 * @return The URL of the layer associated with this instance, if available
	 */
	private URL getLayerUrl()
	{
		return (URL) layer.getValue(AVKeyMore.CONTEXT_URL);
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getAnimatableLayerElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();

		// Extract the layer properties from the XML
		String layerUrlString = WWXML.getText(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableAttributeUrl());
		if (layerUrlString == null)
		{
			Logging.logger().log(Level.WARNING, "No url found for layer " + name);
			return null;
		}
		URL layerUrl = null;
		try
		{
			layerUrl = new URL(layerUrlString);
		}
		catch (MalformedURLException e)
		{
			Logging.logger().log(Level.WARNING,
					"Unable to open layer  " + name + " from " + layerUrlString + ". Bad URL.");
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

		DefaultAnimatableLayer result = new DefaultAnimatableLayer(name, animation, loadedLayer, parameters);
		loadedLayer.setEnabled(enabled);

		return result;
	}

	@Override
	protected void saveAnimatableToXml(Element element, AnimationFileVersion version)
	{
		super.saveAnimatableToXml(element, version);

		URL layerUrl = getLayerUrl();
		if (layerUrl != null)
		{
			WWXML.setTextAttribute(element, version.getConstants().getAnimatableAttributeUrl(),
					layerUrl.toExternalForm());
		}
	}
}
