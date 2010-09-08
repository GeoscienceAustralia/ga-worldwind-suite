package au.gov.ga.worldwind.animator.animation.layer.parameter;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A base implementation of the {@link LayerParameter} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class LayerParameterBase extends ParameterBase implements LayerParameter
{
	private static final long serialVersionUID = 20100907L;
	
	/** The layer this parameter is associated with */
	private Layer layer;
	
	/**
	 * @param name The name of this parameter
	 * @param animation The animation associated with this parameter
	 * @param layer The layer associated with this parameter
	 */
	public LayerParameterBase(String name, Animation animation, Layer layer)
	{
		super(name, animation);
		Validate.notNull(layer, "A layer is required");
		this.layer = layer;
	}
	
	/**
	 * Constructor used for deserialization. Not for general consumption.
	 */
	protected LayerParameterBase(Layer layer)
	{
		super();
		Validate.notNull(layer, "A layer is required");
		this.layer = layer;
	}

	@Override
	public Layer getLayer()
	{
		return layer;
	}

	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		Element result = WWXML.appendElement(parent, getType().name().toLowerCase());
		
		result.appendChild(super.toXml(result, version));
		
		return result;
	}
	
}
