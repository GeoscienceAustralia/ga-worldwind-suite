package au.gov.ga.worldwind.util;

import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitView;
import gov.nasa.worldwind.view.BasicOrbitViewAnimator;
import gov.nasa.worldwind.view.BasicOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.view.OrbitViewAnimator;
import gov.nasa.worldwind.view.OrbitViewPropertyAccessor;
import gov.nasa.worldwind.view.ScheduledOrbitViewInterpolator;

public class EyePositionViewStateIterator extends BasicOrbitViewStateIterator
{
	private final int maxSmoothing;

	protected EyePositionViewStateIterator(long lengthMillis,
			OrbitViewAnimator animator, boolean doSmoothing)
	{
		this(new ScheduledOrbitViewInterpolator(lengthMillis), animator,
				doSmoothing);
	}

	protected EyePositionViewStateIterator(
			ScheduledOrbitViewInterpolator interpolator,
			OrbitViewAnimator animator, boolean doSmoothing)
	{
		super(false, interpolator, animator);
		this.maxSmoothing = maxSmoothingFromFlag(doSmoothing);
	}

	public final boolean isSmoothing()
	{
		return this.maxSmoothing != 0;
	}

	public void doNextState(double interpolant, BasicOrbitView orbitView)
	{
		if (orbitView == null)
		{
			String message = Logging.getMessage("nullValue.OrbitViewIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		double smoothedInterpolant = interpolantSmoothed(interpolant,
				this.maxSmoothing);
		super.doNextState(smoothedInterpolant, orbitView);
	}

	private static double interpolantSmoothed(double interpolant,
			int smoothingIterations)
	{
		// Apply iterative hermite smoothing.
		double smoothed = interpolant;
		for (int i = 0; i < smoothingIterations; i++)
		{
			smoothed = smoothed * smoothed * (3.0 - 2.0 * smoothed);
		}
		return smoothed;
	}

	private static int maxSmoothingFromFlag(boolean doSmoothing)
	{
		if (doSmoothing)
			return 1;
		else
			return 0;
	}

	public static ViewStateIterator createIterator(Position begin,
			Position end, long lengthMillis, boolean smoothed)
	{
		if (begin == null || end == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}
		if (lengthMillis < 0)
		{
			String message = Logging.getMessage("generic.ArgumentOutOfRange");
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}

		OrbitViewPropertyAccessor.PositionAccessor positionAccessor = new EyePositionAccessor();
		OrbitViewAnimator animator = new BasicOrbitViewAnimator.PositionAnimator(
				begin, end, positionAccessor);
		return new EyePositionViewStateIterator(lengthMillis, animator,
				smoothed);
	}

	public static class EyePositionAccessor implements
			OrbitViewPropertyAccessor.PositionAccessor
	{
		public Position getPosition(OrbitView orbitView)
		{
			if (orbitView == null)
				return null;

			return orbitView.getEyePosition();
		}

		public boolean setPosition(OrbitView orbitView, Position value)
		{
			if (orbitView == null || value == null)
				return false;

			try
			{
				orbitView.setEyePosition(value);
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
		}
	}
}
