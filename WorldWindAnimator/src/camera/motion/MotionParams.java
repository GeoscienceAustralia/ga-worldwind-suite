package camera.motion;

import java.io.Serializable;

public class MotionParams implements Serializable
{
	public final double accelerationIn;
	public final double accelerationOut;
	public final double velocityIn;
	public final double velocityOut;
	public final boolean ignoreOut;
	public final boolean ignoreIn;
	public final boolean usePreviousForIn;
	public final boolean calculateAccelerations;
	public final boolean constantVelocity;

	public MotionParams(double accelerationIn, double accelerationOut,
			double velocityIn, double velocityOut, boolean usePreviousForIn,
			boolean ignoreIn, boolean ignoreOut,
			boolean calculateAccelerations, boolean constantVelocity)
	{
		this.accelerationIn = accelerationIn;
		this.accelerationOut = accelerationOut;
		this.velocityIn = velocityIn;
		this.velocityOut = velocityOut;
		this.usePreviousForIn = usePreviousForIn;
		this.ignoreIn = ignoreIn;
		this.ignoreOut = ignoreOut;
		this.calculateAccelerations = calculateAccelerations;
		this.constantVelocity = constantVelocity;
	}

	public static MotionParams standard(double accelerationIn,
			double accelerationOut, double velocityIn, double velocityOut)
	{
		return new MotionParams(accelerationIn, accelerationOut, velocityIn,
				velocityOut, false, false, false, false, false);
	}

	public static MotionParams usePrevious(double accelerationIn,
			double accelerationOut, double velocityOut)
	{
		return new MotionParams(accelerationIn, accelerationOut, 0,
				velocityOut, true, false, false, false, false);
	}

	public static MotionParams ignoreIn(double accelerationOut,
			double velocityOut)
	{
		return new MotionParams(0, accelerationOut, 0, velocityOut, false,
				true, false, false, false);
	}

	public static MotionParams ignoreOut(double accelerationIn,
			double velocityIn)
	{
		return new MotionParams(accelerationIn, 0, velocityIn, 0, false, false,
				true, false, false);
	}

	public static MotionParams usePreviousIgnoreOut(double accelerationIn)
	{
		return new MotionParams(accelerationIn, 0, 0, 0, true, false, true,
				false, false);
	}

	public static MotionParams calculateAcceleration(double velocityIn,
			double velocityOut)
	{
		return new MotionParams(0, 0, velocityIn, velocityOut, false, false,
				false, true, false);
	}

	public static MotionParams calculateAccelerationUsePrevious(
			double velocityOut)
	{
		return new MotionParams(0, 0, 0, velocityOut, true, false, false, true,
				false);
	}

	/*public static MotionParams calculateAccelerationIgnoreIn(double velocityOut)
	{
		return new MotionParams(0, 0, 0, velocityOut, false, true, false, true,
				false);
	}

	public static MotionParams calculateAccelerationIgnoreOut(double velocityIn)
	{
		return new MotionParams(0, 0, velocityIn, 0, false, false, true, true,
				false);
	}

	public static MotionParams calculateAccelerationUsePreviousIgnoreOut()
	{
		return new MotionParams(0, 0, 0, 0, true, false, true, true, false);
	}*/

	public static MotionParams constantVelocity()
	{
		return new MotionParams(0, 0, 0, 0, false, false, false, false, true);
	}
}
