package au.gov.ga.worldwind.animator.ui.parametereditor;

import au.gov.ga.worldwind.animator.animation.parameter.BezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
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
	
	ParameterCurveKeyNode(ParameterValue parameterValue)
	{
		Validate.notNull(parameterValue, "A parameterValue is required");
		
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
}