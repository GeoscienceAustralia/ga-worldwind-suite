package camera.interpolate;

import camera.params.Zoom;

public class InterpolatableZoom implements Interpolatable<Zoom, Zoom>
{
	private Zoom current;
	private Zoom previous;

	public InterpolatableZoom(Zoom current)
	{
		this.current = current;
	}

	public Zoom getCurrent()
	{
		return current;
	}

	public Zoom getEnd()
	{
		return current;
	}

	public Zoom interpolate(double percent)
	{
		return Zoom.interpolate(previous, current, percent);
	}

	public double length()
	{
		return Math.abs(Zoom.difference(previous, current));
	}

	public void setPrevious(Interpolatable<Zoom, Zoom> previous)
	{
		this.previous = previous.getCurrent();
	}
}
