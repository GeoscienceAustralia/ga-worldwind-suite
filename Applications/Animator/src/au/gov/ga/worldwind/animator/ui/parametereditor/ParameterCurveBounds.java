package au.gov.ga.worldwind.animator.ui.parametereditor;

import au.gov.ga.worldwind.common.util.Range;


/**
 * A container that defines a bounding box for a drawn parameter curve. 
 */
class ParameterCurveBounds
{
	ParameterCurvePoint bottomLeft;
	ParameterCurvePoint topRight;
	
	ParameterCurveBounds(ParameterCurvePoint bottomLeft, ParameterCurvePoint topRight)
	{
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
	}
	
	ParameterCurveBounds(double minFrame, double maxFrame, double minValue, double maxValue)
	{
		this.bottomLeft = new ParameterCurvePoint(minFrame, minValue);
		this.topRight = new ParameterCurvePoint(maxFrame, maxValue);
	}
	
	double getMinFrame()
	{
		return bottomLeft.frame;
	}
	
	double getMaxFrame()
	{
		return topRight.frame;
	}
	
	double getFrameWindow()
	{
		return getMaxFrame() - getMinFrame();
	}
	
	Range<Double> getFrameRange()
	{
		return new Range<Double>(getMinFrame(), getMaxFrame());
	}
	
	double getMinValue()
	{
		return bottomLeft.value;
	}
	
	double getMaxValue()
	{
		return topRight.value;
	}
	
	double getValueWindow()
	{
		return getMaxValue() - getMinValue();
	}
	
	Range<Double> getValueRange()
	{
		return new Range<Double>(getMinValue(), getMaxValue());
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[X:" + getMinFrame() + "-" + getMaxFrame() + ", Y:" + getMinValue() + "-" + getMaxValue() + "]"; 
	}
}