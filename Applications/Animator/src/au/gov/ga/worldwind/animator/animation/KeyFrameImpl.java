/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.math.vector.Vector;

/**
 * The default implementation of the {@link KeyFrame} interface
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class KeyFrameImpl implements KeyFrame
{

	/**
	 * The map of Parameter->ParameterValue of values recorded in this key frame 
	 */
	private Map<Parameter<? extends Vector<?>>, ParameterValue<? extends Vector<?>>> parameterValueMap = 
		new HashMap<Parameter<? extends Vector<?>>, ParameterValue<? extends Vector<?>>>();
	
	/**
	 * The frame of the animation this key frame corresponds to
	 */
	private int frame;
	
	@Override
	public String getRestorableState()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restoreState(String stateInXml)
	{
		// TODO Auto-generated method stub

	}
	
	@Override
	public Collection<ParameterValue<? extends Vector<?>>> getParameterValues()
	{
		return parameterValueMap.values();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <V extends Vector<V>> ParameterValue<V> getValueForParameter(Parameter<V> p)
	{
		return (ParameterValue<V>)parameterValueMap.get(p);
	}

	@Override
	public <V extends Vector<V>> void addParameterValue(ParameterValue<V> value)
	{
		if (value != null)
		{
			this.parameterValueMap.put(value.getOwner(), value);
		}
	}

	@Override
	public int getFrame()
	{
		return frame;
	}

}
