package camera.interpolate;

import java.io.Serializable;

public interface Interpolatable<E, T> extends Serializable
{
	public void setPrevious(Interpolatable<E, T> previous);

	public T interpolate(double percent);

	public double length();

	public E getCurrent();

	public T getEnd();
}
