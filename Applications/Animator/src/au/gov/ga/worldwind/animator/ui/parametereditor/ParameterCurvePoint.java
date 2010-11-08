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

	public ParameterCurvePoint subtract(ParameterCurvePoint other)
	{
		return new ParameterCurvePoint(frame - other.frame, value - other.value);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + frame + ", " + value + "]";
	}

	/**
	 * @param newValue
	 * @return
	 */
	public ParameterCurvePoint add(ParameterCurvePoint newValue)
	{
		// TODO Auto-generated method stub
		return null;
	}
}