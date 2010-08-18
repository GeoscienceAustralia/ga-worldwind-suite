package au.gov.ga.worldwind.animator.math.vector;

import java.io.Serializable;

public interface Vector<V> extends Cloneable, Serializable
{
	public V createNew();

	public V clone();

	public V set(V v);

	public V mult(V v);

	public V mult(V v, V store);

	public V multLocal(V v);

	public V mult(double s);

	public V mult(double s, V store);

	public V multLocal(double s);

	public V divide(V v);

	public V divide(V v, V store);

	public V divideLocal(V v);

	public V divide(double s);

	public V divide(double s, V store);

	public V divideLocal(double s);

	public V add(V v);

	public V add(V v, V store);

	public V addLocal(V v);

	public V subtract(V v);

	public V subtract(V v, V store);

	public V subtractLocal(V v);

	public V max(V v);

	public V max(V v, V store);

	public V maxLocal(V v);

	public V min(V v);

	public V min(V v, V store);

	public V minLocal(V v);

	public double distanceSquared();

	public double distance();

	public V zeroLocal();

	public V negate();

	public V negate(V store);

	public V negateLocal();

	public V interpolate(V v, double percent);

	public V interpolate(V v, double percent, V store);

	public V interpolateLocal(V v, double percent);

	public V normalize();

	public V normalize(V store);

	public V normalizeLocal();
	
	public boolean equals(V other);
}
