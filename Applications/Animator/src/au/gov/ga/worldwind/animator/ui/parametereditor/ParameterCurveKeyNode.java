package au.gov.ga.worldwind.animator.ui.parametereditor;

import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.parameter.BezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueType;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * Holds information about a keyframe node on a parameter curve
 */
class ParameterCurveKeyNode
{
	private ParameterCurvePoint valuePoint;
	private ParameterCurvePoint inPoint;
	private ParameterCurvePoint outPoint;
	
	private ParameterValueType type;
	
	private boolean locked = true;
	
	private ParameterValue parameterValue;
	
	ParameterCurveKeyNode(ParameterValue parameterValue)
	{
		Validate.notNull(parameterValue, "A parameterValue is required");
		this.parameterValue = parameterValue;
		updateNode();
	}

	private void updateNode()
	{
		this.valuePoint = new ParameterCurvePoint(parameterValue.getFrame(), parameterValue.getValue());
		this.type = parameterValue.getType();
		
		if (isBezier())
		{
			BezierParameterValue bezierValue = (BezierParameterValue)parameterValue;
			
			this.locked = bezierValue.isLocked();
			
			this.inPoint = calculateInPoint(bezierValue);
			this.outPoint = calculateOutPoint(bezierValue);
		}
	}
	
	/**
	 * Calculates the 'In' point by converting the in percentage to a frame value
	 */
	private static ParameterCurvePoint calculateInPoint(BezierParameterValue bezierValue)
	{
		ParameterValue previousValue = bezierValue.getOwner().getValueAtKeyFrameBeforeFrame(bezierValue.getFrame());
		if (previousValue == null)
		{
			return null;
		}
		int frame = (int)(bezierValue.getFrame() - (bezierValue.getInPercent() * (bezierValue.getFrame() - previousValue.getFrame())));
		return new ParameterCurvePoint(frame, bezierValue.getInValue());
	}
	
	/**
	 * Calculates the 'Out' point by converting the out percentage to a frame value
	 */
	private static ParameterCurvePoint calculateOutPoint(BezierParameterValue bezierValue)
	{
		ParameterValue nextValue = bezierValue.getOwner().getValueAtKeyFrameAfterFrame(bezierValue.getFrame());
		if (nextValue == null)
		{
			return null;
		}
		int frame = (int)(bezierValue.getFrame() + (bezierValue.getOutPercent() * (nextValue.getFrame() - bezierValue.getFrame())));
		return new ParameterCurvePoint(frame, bezierValue.getOutValue());
	}

	ParameterCurvePoint getValuePoint()
	{
		return valuePoint;
	}
	
	ParameterCurvePoint getInPoint()
	{
		return inPoint;
	}
	
	ParameterCurvePoint getOutPoint()
	{
		return outPoint;
	}
	
	boolean isLinear()
	{
		return type == ParameterValueType.LINEAR;
	}
	
	boolean isBezier()
	{
		return type == ParameterValueType.BEZIER;
	}
	
	boolean isLocked()
	{
		return locked;
	}
	
	public ParameterValue getParameterValue()
	{
		return parameterValue;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof ParameterCurveKeyNode))
		{
			return false;
		}
		
		return ((ParameterCurveKeyNode)obj).getParameterValue().getFrame() == this.getParameterValue().getFrame();
	}
	
	@Override
	public int hashCode()
	{
		return this.getParameterValue().getFrame();
	}

	/**
	 * Apply the provided value point to the parameter value this node reflects
	 */
	public void applyValueChange(ParameterCurvePoint newValue)
	{
		ParameterCurvePoint delta = newValue.subtract(valuePoint);
		parameterValue.translate(delta.value);
		updateNode();
	}
	
	/**
	 * Apply the provided value point to the parameter value this node reflects
	 */
	public void applyInChange(ParameterCurvePoint newIn)
	{
		if (!(parameterValue instanceof BezierParameterValue))
		{
			return;
		}
		((BezierParameterValue)parameterValue).setInPercent(calculateInPercent((BezierParameterValue)parameterValue, newIn.frame));
		((BezierParameterValue)parameterValue).setInValue(newIn.value);
		updateNode();
	}
	
	/** Calculate the percent value for the in handle, given the new frame position of the in handle */
	private static Double calculateInPercent(BezierParameterValue bezierValue, double inFrame)
	{
		ParameterValue previousValue = bezierValue.getOwner().getValueAtKeyFrameBeforeFrame(bezierValue.getFrame());
		if (previousValue == null)
		{
			return null;
		}
		return 1 - (double)(inFrame - previousValue.getFrame()) / (double)(bezierValue.getFrame() - previousValue.getFrame());
	}
	
	/**
	 * Apply the provided value point to the parameter value this node reflects
	 */
	public void applyOutChange(ParameterCurvePoint newOut)
	{
		if (!(parameterValue instanceof BezierParameterValue))
		{
			return;
		}
		((BezierParameterValue)parameterValue).setOutPercent(calculateOutPercent((BezierParameterValue)parameterValue, newOut.frame));
		((BezierParameterValue)parameterValue).setOutValue(newOut.value);
		updateNode();
	}
	
	/** Calculate the percent value for the out handle, given the new frame position of the out handle */
	private static Double calculateOutPercent(BezierParameterValue bezierValue, double outFrame)
	{
		ParameterValue nextValue = bezierValue.getOwner().getValueAtKeyFrameAfterFrame(bezierValue.getFrame());
		if (nextValue == null)
		{
			return null;
		}
		return (double)(outFrame - bezierValue.getFrame()) / (double)(nextValue.getFrame() - bezierValue.getFrame());
	}

	public void convertToLinear()
	{
		if (isLinear())
		{
			return;
		}
		
		replaceParameterValue(ParameterValueFactory.convertParameterValue(parameterValue, ParameterValueType.LINEAR));
		updateNode();
	}

	public void convertToLockedBezier()
	{
		if (!isBezier())
		{
			replaceParameterValue(ParameterValueFactory.convertParameterValue(parameterValue, ParameterValueType.BEZIER));
		}
		
		((BezierParameterValue)parameterValue).setLocked(true);
		updateNode();
	}

	public void convertToUnlockedBezier()
	{
		if (!isBezier())
		{
			replaceParameterValue(ParameterValueFactory.convertParameterValue(parameterValue, ParameterValueType.BEZIER));
		}
		
		((BezierParameterValue)parameterValue).setLocked(false);
		updateNode();
	}
	

	private void replaceParameterValue(ParameterValue replacementValue)
	{
		KeyFrame keyFrame = replacementValue.getOwner().getAnimation().getKeyFrame(replacementValue.getFrame());
		keyFrame.addParameterValue(replacementValue);
		parameterValue = replacementValue;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[In:" + inPoint + ", Value:" + valuePoint + ", Out:" + outPoint + "]";
	}
}