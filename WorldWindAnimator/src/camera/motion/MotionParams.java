package camera.motion;

public class MotionParams
{
	public final double accelerationIn;
	public final double accelerationOut;
	public final double velocityIn;
	public final double velocityOut;

	public MotionParams(double accelerationIn, double accelerationOut,
			double velocityIn, double velocityOut)
	{
		this.accelerationIn = accelerationIn;
		this.accelerationOut = accelerationOut;
		this.velocityIn = velocityIn;
		this.velocityOut = velocityOut;
	}
}
