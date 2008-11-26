package camera.interpolate;

import camera.params.Heading;

public class InterpolatableHeading implements Interpolatable<Heading, Heading>
{
	private Heading current;
	private Heading previous;

	public InterpolatableHeading(Heading current)
	{
		this.current = current;
	}

	public Heading getCurrent()
	{
		return current;
	}

	public Heading getEnd()
	{
		return current;
	}

	public Heading interpolate(double percent)
	{
		return Heading.interpolate(previous, current, percent);
	}

	public double length()
	{
		return Math.abs(Heading.difference(previous, current));
	}

	public void setPrevious(Interpolatable<Heading, Heading> previous)
	{
		this.previous = previous.getCurrent();
	}
}
