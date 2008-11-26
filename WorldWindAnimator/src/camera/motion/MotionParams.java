package camera.motion;

public class MotionParams
{
	public final double accelerationIn;
	public final double accelerationOut;
	public final double velocityIn;
	public final double velocityOut;
	public final boolean ignoreOut;
	public final boolean ignoreIn;
	public final boolean usePreviousForIn;

	public MotionParams(double accelerationIn, double accelerationOut,
			double velocityIn, double velocityOut)
	{
		this(accelerationIn, accelerationOut, velocityIn, velocityOut, false,
				false, false);
	}

	public MotionParams(double accelerationIn, double accelerationOut,
			double velocityIn, double velocityOut, boolean usePreviousForIn,
			boolean ignoreOut)
	{
		this(accelerationIn, accelerationOut, velocityIn, velocityOut,
				usePreviousForIn, false, ignoreOut);
	}

	public MotionParams(double accelerationIn, double accelerationOut,
			double velocityIn, double velocityOut, boolean usePreviousForIn,
			boolean ignoreIn, boolean ignoreOut)
	{
		this.accelerationIn = accelerationIn;
		this.accelerationOut = accelerationOut;
		this.velocityIn = velocityIn;
		this.velocityOut = velocityOut;
		this.usePreviousForIn = usePreviousForIn;
		this.ignoreIn = ignoreIn;
		this.ignoreOut = ignoreOut;
	}
}
