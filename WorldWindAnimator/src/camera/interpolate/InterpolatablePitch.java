package camera.interpolate;

import camera.params.Pitch;

public class InterpolatablePitch implements Interpolatable<Pitch, Pitch>
{
	private Pitch current;
	private Pitch previous;

	public InterpolatablePitch(Pitch current)
	{
		this.current = current;
	}

	public Pitch getCurrent()
	{
		return current;
	}

	public Pitch getEnd()
	{
		return current;
	}

	public Pitch interpolate(double percent)
	{
		return Pitch.interpolate(previous, current, percent);
	}

	public double length()
	{
		return Math.abs(Pitch.difference(previous, current));
	}

	public void setPrevious(Interpolatable<Pitch, Pitch> previous)
	{
		this.previous = previous.getCurrent();
	}
}
