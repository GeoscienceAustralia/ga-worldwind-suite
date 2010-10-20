package au.gov.ga.worldwind.common.view.stereo;

public class BasicStereoViewParameters implements StereoViewParameters
{
	private double focalLength = 100;
	private double eyeSeparation = 1;
	private double eyeSeparationMultiplier = 1;
	private boolean dynamicStereo = true;

	@Override
	public double getFocalLength()
	{
		return focalLength;
	}

	@Override
	public void setFocalLength(double focalLength)
	{
		this.focalLength = focalLength;
	}

	@Override
	public double getEyeSeparation()
	{
		return eyeSeparation;
	}

	@Override
	public void setEyeSeparation(double eyeSeparation)
	{
		this.eyeSeparation = eyeSeparation;
	}

	@Override
	public double getEyeSeparationMultiplier()
	{
		return eyeSeparationMultiplier;
	}

	@Override
	public void setEyeSeparationMultiplier(double eyeSeparationMultiplier)
	{
		this.eyeSeparationMultiplier = eyeSeparationMultiplier;
	}

	@Override
	public boolean isDynamicStereo()
	{
		return dynamicStereo;
	}

	@Override
	public void setDynamicStereo(boolean dynamicStereo)
	{
		this.dynamicStereo = dynamicStereo;
	}
}
