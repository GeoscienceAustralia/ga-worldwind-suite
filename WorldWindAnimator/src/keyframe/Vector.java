package keyframe;

import java.io.Serializable;

public interface Vector<E> extends Cloneable, Serializable
{
	public E createNew();

	public E clone();

	public E set(E v);

	public E mult(E v);

	public E mult(E v, E store);

	public E multLocal(E v);

	public E mult(double s);

	public E mult(double s, E store);

	public E multLocal(double s);

	public E divide(E v);

	public E divide(E v, E store);

	public E divideLocal(E v);

	public E divide(double s);

	public E divide(double s, E store);

	public E divideLocal(double s);

	public E add(E v);

	public E add(E v, E store);

	public E addLocal(E v);

	public E subtract(E v);

	public E subtract(E v, E store);

	public E subtractLocal(E v);

	public E max(E v);

	public E max(E v, E store);

	public E maxLocal(E v);

	public E min(E v);

	public E min(E v, E store);

	public E minLocal(E v);

	public double distanceSquared();

	public double distance();

	public E zeroLocal();

	public E negate();

	public E negate(E store);

	public E negateLocal();
}
