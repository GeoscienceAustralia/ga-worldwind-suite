/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The default implementation of the {@link KeyFrame} interface
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class KeyFrameImpl implements KeyFrame
{
	private static final long serialVersionUID = 20100819L;

	/**
	 * The map of Parameter->ParameterValue of values recorded in this key frame 
	 */
	private Map<Parameter, ParameterValue> parameterValueMap = new HashMap<Parameter, ParameterValue>();
	
	/**
	 * The frame of the animation this key frame corresponds to
	 */
	private int frame;
	
	/**
	 * Constructor.
	 * <p/>
	 * Initialises the mandatory frame and parameter values
	 * 
	 * @param frame The frame at which this key frame applies
	 * @param parameterValues The parameter values associated with this key frame. 
	 * 						  At least one value must be provided.
	 */
	public KeyFrameImpl(int frame, Collection<ParameterValue> parameterValues)
	{
		// At least one parameter value is required.
		Validate.notNull(parameterValues, "Parameter values are required");
		Validate.isTrue(!parameterValues.isEmpty(), "Parameter values are required");
		
		this.frame = frame;
		for (ParameterValue parameterValue : parameterValues)
		{
			addParameterValue(parameterValue);
		}
	}
	
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
	public Collection<ParameterValue> getParameterValues()
	{
		return parameterValueMap.values();
	}
	
	@Override
	public ParameterValue getValueForParameter(Parameter p)
	{
		return parameterValueMap.get(p);
	}

	@Override
	public boolean hasValueForParameter(Parameter p)
	{
		return parameterValueMap.containsKey(p);
	}
	
	@Override
	public void addParameterValue(ParameterValue value)
	{
		if (value != null)
		{
			this.parameterValueMap.put(value.getOwner(), value);
		}
	}
	
	@Override
	public void addParameterValues(Collection<ParameterValue> values)
	{
		for (ParameterValue parameterValue : values)
		{
			addParameterValue(parameterValue);
		}
		
	}

	@Override
	public int getFrame()
	{
		return frame;
	}

}
