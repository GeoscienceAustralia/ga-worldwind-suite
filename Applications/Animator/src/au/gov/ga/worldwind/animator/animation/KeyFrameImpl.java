/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.ChangeableBase;
import au.gov.ga.worldwind.animator.animation.event.KeyFrameEventImpl;
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
public class KeyFrameImpl extends ChangeableBase implements KeyFrame
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
	
	/**
	 * Constructor.
	 * <p/>
	 * Initialises the mandatory frame and parameter values
	 * 
	 * @param frame The frame at which this key frame applies
	 * @param parameterValues The parameter values associated with this key frame. 
	 * 						  At least one value must be provided.
	 */
	public KeyFrameImpl(int frame, ParameterValue... parameterValues)
	{
		this(frame, Arrays.asList(parameterValues));
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
	public Collection<ParameterValue> getValuesForParameters(Collection<Parameter> parameters)
	{
		Collection<ParameterValue> result = new ArrayList<ParameterValue>();
		if (parameters == null)
		{
			return result;
		}
		for (Parameter parameter : parameters)
		{
			ParameterValue value = getValueForParameter(parameter);
			if (value != null)
			{
				result.add(value);
			}
		}
		return result;
	}
	
	@Override
	public boolean hasValueForParameter(Parameter p)
	{
		return parameterValueMap.containsKey(p);
	}
	
	@Override
	public void removeValueForParameter(Parameter p)
	{
		if (parameterValueMap.containsKey(p))
		{
			ParameterValue valueToBeRemoved = parameterValueMap.get(p);
			valueToBeRemoved.removeChangeListener(this);
			parameterValueMap.remove(p);
			fireRemoveEvent(valueToBeRemoved);
		}
	}
	
	@Override
	public void removeValuesForParameters(Collection<Parameter> parameters)
	{
		if (parameters == null || parameters.isEmpty())
		{
			return;
		}
		for (Parameter parameter : parameters)
		{
			removeValueForParameter(parameter);
		}
	}
	
	@Override
	public void addParameterValue(ParameterValue value)
	{
		if (value != null)
		{
			// Make sure the parameter value frame is consistent
			value.setFrame(frame);
			
			this.parameterValueMap.put(value.getOwner(), value);
			value.addChangeListener(this);
			
			fireAddEvent(value);
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

	@Override
	public boolean hasParameterValues()
	{
		return !parameterValueMap.isEmpty();
	}
	
	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		AnimationEvent newEvent = createEvent(null, event, null);
		List<AnimationEventListener> listeners = getChangeListeners();
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).receiveAnimationEvent(newEvent);
		}
	}
	
	@Override
	protected AnimationEvent createEvent(Type type, AnimationEvent cause, Object value)
	{
		return new KeyFrameEventImpl(this, type, cause, value);
	}
	
}
