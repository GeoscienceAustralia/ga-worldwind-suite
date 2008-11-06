package path;

import java.io.Serializable;

public class Vector implements Serializable
{
	public double x;
	public double y;
	public double z;

	public Vector()
	{
		this(0d, 0d, 0d);
	}

	public Vector(Vector vector)
	{
		this(vector.x, vector.y, vector.z);
	}

	public Vector(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector normalizeLocal()
	{
		double length = length();
		if (length != 0)
		{
			return divideLocal(length);
		}
		return this;
	}

	public Vector normalize()
	{
		double length = length();
		if (length != 0)
		{
			return divide(length);
		}
		return divide(1);
	}

	public Vector divideLocal(double scalar)
	{
		x /= scalar;
		y /= scalar;
		z /= scalar;
		return this;
	}

	public Vector divideLocal(Vector scalar)
	{
		x /= scalar.x;
		y /= scalar.y;
		z /= scalar.z;
		return this;
	}

	public Vector divide(Vector scalar)
	{
		return new Vector(x / scalar.x, y / scalar.y, z / scalar.z);
	}

	public Vector divide(double scalar)
	{
		return new Vector(x / scalar, y / scalar, z / scalar);
	}

	public Vector multLocal(double scalar)
	{
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	public Vector multLocal(Vector scalar)
	{
		x *= scalar.x;
		y *= scalar.y;
		z *= scalar.z;
		return this;
	}

	public Vector mult(Vector scalar)
	{
		return new Vector(x * scalar.x, y * scalar.y, z * scalar.z);
	}

	public Vector mult(double scalar)
	{
		return new Vector(x * scalar, y * scalar, z * scalar);
	}

	public double lengthSquared()
	{
		return x * x + y * y + z * z;
	}

	public double length()
	{
		return Math.sqrt(lengthSquared());
	}

	public Vector negate()
	{
		return new Vector(-x, -y, -z);
	}

	public Vector negateLocal()
	{
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Vector subtract(Vector vec)
	{
		return new Vector(x - vec.x, y - vec.y, z - vec.z);
	}

	public Vector subtractLocal(Vector vec)
	{
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		return this;
	}

	public Vector add(Vector vec)
	{
		return new Vector(x + vec.x, y + vec.y, z + vec.z);
	}

	public Vector addLocal(Vector vec)
	{
		x += vec.x;
		y += vec.y;
		z += vec.z;
		return this;
	}

	public void zeroLocal()
	{
		x = y = z = 0;
	}

	public double distanceSquared(Vector v)
	{
		double dx = x - v.x;
		double dy = y - v.y;
		double dz = z - v.z;
		return dx * dx + dy * dy + dz * dz;
	}

	public double distance(Vector v)
	{
		return Math.sqrt(distanceSquared(v));
	}

	public double dot(Vector vec)
	{
		return x * vec.x + y * vec.y + z * vec.z;
	}

	public Vector cross(Vector other)
	{
		double resX = ((y * other.z) - (z * other.y));
		double resY = ((z * other.x) - (x * other.z));
		double resZ = ((x * other.y) - (y * other.x));
		return new Vector(resX, resY, resZ);
	}

	public Vector crossLocal(Vector other)
	{
		double tempx = (y * other.z) - (z * other.y);
		double tempy = (z * other.x) - (x * other.z);
		z = (x * other.y) - (y * other.x);
		x = tempx;
		y = tempy;
		return this;
	}
}
