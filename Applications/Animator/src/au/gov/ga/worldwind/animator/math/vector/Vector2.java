package au.gov.ga.worldwind.animator.math.vector;

@SuppressWarnings("serial")
public class Vector2 implements Vector<Vector2>
{
	public final static Vector2 ZERO = new Vector2(0, 0);

	public double x;
	public double y;

	public Vector2()
	{
	}

	public Vector2(Vector2 v)
	{
		set(v);
	}

	public Vector2(double x, double y)
	{
		set(x, y);
	}

	@Override
	public Vector2 createNew()
	{
		return new Vector2();
	}

	@Override
	public Vector2 clone()
	{
		return new Vector2(this);
	}

	@Override
	public Vector2 set(Vector2 v)
	{
		return set(v.x, v.y);
	}

	public Vector2 set(double x, double y)
	{
		this.x = x;
		this.y = y;
		return this;
	}

	@Override
	public Vector2 mult(Vector2 v)
	{
		return mult(v, null);
	}

	@Override
	public Vector2 mult(Vector2 v, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = x * v.x;
		store.y = y * v.y;
		return store;
	}

	@Override
	public Vector2 multLocal(Vector2 v)
	{
		x *= v.x;
		y *= v.y;
		return this;
	}

	@Override
	public Vector2 mult(double s)
	{
		return mult(s, null);
	}

	@Override
	public Vector2 mult(double s, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = x * s;
		store.y = y * s;
		return store;
	}

	@Override
	public Vector2 multLocal(double s)
	{
		x *= s;
		y *= s;
		return this;
	}

	@Override
	public Vector2 divide(Vector2 v)
	{
		return divide(v, null);
	}

	@Override
	public Vector2 divide(Vector2 v, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = x / v.x;
		store.y = y / v.y;
		return store;
	}

	@Override
	public Vector2 divideLocal(Vector2 v)
	{
		x /= v.x;
		y /= v.y;
		return this;
	}

	@Override
	public Vector2 divide(double s)
	{
		return divide(s, null);
	}

	@Override
	public Vector2 divide(double s, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = x / s;
		store.y = y / s;
		return store;
	}

	@Override
	public Vector2 divideLocal(double s)
	{
		x /= s;
		y /= s;
		return this;
	}

	@Override
	public Vector2 add(Vector2 v)
	{
		return add(v, null);
	}

	@Override
	public Vector2 add(Vector2 v, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = x + v.x;
		store.y = y + v.y;
		return store;
	}

	@Override
	public Vector2 addLocal(Vector2 v)
	{
		x += v.x;
		y += v.y;
		return this;
	}

	@Override
	public Vector2 subtract(Vector2 v)
	{
		return subtract(v, null);
	}

	@Override
	public Vector2 subtract(Vector2 v, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = x - v.x;
		store.y = y - v.y;
		return store;
	}

	@Override
	public Vector2 subtractLocal(Vector2 v)
	{
		x -= v.x;
		y -= v.y;
		return this;
	}

	@Override
	public Vector2 max(Vector2 v)
	{
		return max(v, null);
	}

	@Override
	public Vector2 max(Vector2 v, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = Math.max(x, v.x);
		store.y = Math.max(y, v.y);
		return store;
	}

	@Override
	public Vector2 maxLocal(Vector2 v)
	{
		x = Math.max(x, v.x);
		y = Math.max(y, v.y);
		return this;
	}

	@Override
	public Vector2 min(Vector2 v)
	{
		return min(v, null);
	}

	@Override
	public Vector2 min(Vector2 v, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = Math.min(x, v.x);
		store.y = Math.min(y, v.y);
		return store;
	}

	@Override
	public Vector2 minLocal(Vector2 v)
	{
		x = Math.min(x, v.x);
		y = Math.min(y, v.y);
		return this;
	}

	@Override
	public double distanceSquared()
	{
		return x * x + y * y;
	}

	@Override
	public double distance()
	{
		return Math.sqrt(distanceSquared());
	}

	@Override
	public Vector2 zeroLocal()
	{
		x = 0d;
		y = 0d;
		return this;
	}

	@Override
	public Vector2 negate()
	{
		return negate(null);
	}

	@Override
	public Vector2 negate(Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = -x;
		store.y = -y;
		return store;
	}

	@Override
	public Vector2 negateLocal()
	{
		x = -x;
		y = -y;
		return this;
	}

	@Override
	public Vector2 interpolate(Vector2 v, double percent)
	{
		return interpolate(v, percent, null);
	}

	@Override
	public Vector2 interpolate(Vector2 v, double percent, Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.x = interpolate(x, v.x, percent);
		store.y = interpolate(y, v.y, percent);
		return store;
	}

	@Override
	public Vector2 interpolateLocal(Vector2 v, double percent)
	{
		x = interpolate(x, v.x, percent);
		y = interpolate(y, v.y, percent);
		return this;
	}

	public static double interpolate(double d1, double d2, double percent)
	{
		return d1 * (1 - percent) + d2 * percent;
	}

	@Override
	public Vector2 normalize()
	{
		return normalize(null);
	}

	@Override
	public Vector2 normalize(Vector2 store)
	{
		if (store == null)
			store = createNew();
		store.divide(distance());
		return store;
	}

	@Override
	public Vector2 normalizeLocal()
	{
		return divideLocal(distance());
	}
	
	@Override
	public boolean equals(Object other)
	{
		return (other instanceof Vector2) &&
				other != null &&
				this.x == ((Vector2)other).x &&
				this.y == ((Vector2)other).y;
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getName() + " (" + x + ", " + y + ")";
	}
}
