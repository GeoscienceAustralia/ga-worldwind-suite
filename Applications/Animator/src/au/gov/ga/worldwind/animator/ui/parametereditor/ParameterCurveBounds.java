package au.gov.ga.worldwind.animator.ui.parametereditor;


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
	
	ParameterCurveBounds(int minFrame, int maxFrame, double minValue, double maxValue)
	{
		this.bottomLeft = new ParameterCurvePoint(minFrame, minValue);
		this.topRight = new ParameterCurvePoint(maxFrame, maxValue);
	}
	
	int getMinFrame()
	{
		return bottomLeft.frame;
	}
	
	int getMaxFrame()
	{
		return topRight.frame;
	}
	
	double getMinValue()
	{
		return bottomLeft.value;
	}
	
	double getMaxValue()
	{
		return topRight.value;
	}
}