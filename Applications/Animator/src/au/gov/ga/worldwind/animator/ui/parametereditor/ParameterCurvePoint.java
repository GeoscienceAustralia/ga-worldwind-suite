package au.gov.ga.worldwind.animator.ui.parametereditor;

/**
 * A simple container representing a curve point [frame, value]
 */
class ParameterCurvePoint
{
	int frame;
	double value;
	
	ParameterCurvePoint(int frame, double value)
	{
		this.frame = frame;
		this.value = value;
	}
}