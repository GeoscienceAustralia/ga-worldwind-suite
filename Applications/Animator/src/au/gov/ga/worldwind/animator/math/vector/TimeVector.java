package au.gov.ga.worldwind.animator.math.vector;

/**
 * A subclass of {@link Vector2} that holds time (x) and value (y).
 * <p/>
 * Distance functions are replaced to calculate the distance on the time (x) axis only.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class TimeVector extends Vector2 implements Vector<Vector2>
{
	private static final long serialVersionUID = 20100824L;

	public TimeVector()
	{
		super();
	}

	public TimeVector(double x, double y)
	{
		super(x, y);
	}

	public TimeVector(Vector2 v)
	{
		super(v);
	}

	@Override
	public double distanceSquared()
	{
		return x * x;
	}
	
	@Override
	public double distance()
	{
		return Math.abs(x);
	}
	
	@Override
	public Vector2 createNew()
	{
		return new TimeVector();
	}
	
	@Override
	public Vector2 clone()
	{
		return new TimeVector(this);
	}
	
}
