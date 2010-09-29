package au.gov.ga.worldwind.animator.terrain.exaggeration;

/**
 * The default implementation of the {@link ElevationExaggeration} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ElevationExaggerationImpl implements ElevationExaggeration
{

	private double exaggeration = 1.0;
	private double boundary;
	
	public ElevationExaggerationImpl(double exaggeration, double boundary)
	{
		this.exaggeration = Math.max(0, exaggeration);
		this.boundary = boundary;
	}

	@Override
	public void setExaggeration(double exaggeration)
	{
		this.exaggeration = Math.max(0, exaggeration);
	}

	@Override
	public double getExaggeration()
	{
		return exaggeration;
	}

	@Override
	public double getElevationBoundary()
	{
		return boundary;
	}

}
