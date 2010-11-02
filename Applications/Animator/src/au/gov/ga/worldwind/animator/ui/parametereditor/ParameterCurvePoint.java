package au.gov.ga.worldwind.animator.ui.parametereditor;

/**
 * A simple container representing a curve point [frame, value]
 */
class ParameterCurvePoint
{
	double frame;
	double value;
	
	ParameterCurvePoint(double frame, double value)
	{
		this.frame = frame;
		this.value = value;
	}
}