package camera.interpolate;

import camera.params.Roll;

public class InterpolatableRoll implements Interpolatable<Roll, Roll>
{
	private Roll current;
	private Roll previous;

	public InterpolatableRoll(Roll current)
	{
		this.current = current;
	}

	public Roll getCurrent()
	{
		return current;
	}

	public Roll getEnd()
	{
		return current;
	}

	public Roll interpolate(double percent)
	{
		return Roll.interpolate(previous, current, percent);
	}

	public double length()
	{
		return Math.abs(Roll.difference(previous, current));
	}

	public void setPrevious(Interpolatable<Roll, Roll> previous)
	{
		this.previous = previous.getCurrent();
	}
}
