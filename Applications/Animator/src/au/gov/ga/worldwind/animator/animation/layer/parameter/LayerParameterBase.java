package au.gov.ga.worldwind.animator.animation.layer.parameter;

import gov.nasa.worldwind.layers.AbstractLayer;
import au.gov.ga.worldwind.animator.animation.Animation;
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
	private AbstractLayer layer;
	
	/**
	 * @param name The name of this parameter
	 * @param animation The animation associated with this parameter
	 * @param layer The layer associated with this parameter
	 */
	public LayerParameterBase(String name, Animation animation, AbstractLayer layer)
	{
		super(name, animation);
		Validate.notNull(layer, "A layer is required");
		this.layer = layer;
	}
	
	/**
	 * Constructor used for deserialization. Not for general consumption.
	 */
	protected LayerParameterBase(AbstractLayer layer)
	{
		super();
		Validate.notNull(layer, "A layer is required");
		this.layer = layer;
	}

	@Override
	public AbstractLayer getLayer()
	{
		return layer;
	}

}
