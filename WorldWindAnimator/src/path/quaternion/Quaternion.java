package path.quaternion;

import path.vector.Vector3;

public class Quaternion
{
	public double x, y, z, w;

	public Quaternion()
	{
		loadIdentity();
	}

	public Quaternion(double x, double y, double z, double w)
	{
		set(x, y, z, w);
	}

	public Quaternion set(double x, double y, double z, double w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	public Quaternion set(Quaternion q)
	{
		set(q.x, q.y, q.z, q.w);
		return this;
	}

	public Quaternion(Quaternion q1, Quaternion q2, double interp)
	{
		slerp(q1, q2, interp);
	}

	public Quaternion(Quaternion q)
	{
		set(q);
	}

	public void loadIdentity()
	{
		set(0, 0, 0, 1);
	}

	public boolean isIdentity()
	{
		if (x == 0 && y == 0 && z == 0 && w == 1)
			return true;
		else
			return false;
	}

	public Quaternion fromAnglesDegrees(double yaw, double roll, double pitch)
	{
		double DEG_TO_RAD = Math.PI / 180d;
		return fromAngles(yaw * DEG_TO_RAD, roll * DEG_TO_RAD, pitch
				* DEG_TO_RAD);
	}

	public Quaternion fromAngles(double yaw, double roll, double pitch)
	{
		double angle;
		double sinRoll, sinPitch, sinYaw, cosRoll, cosPitch, cosYaw;
		angle = pitch * 0.5f;
		sinPitch = Math.sin(angle);
		cosPitch = Math.cos(angle);
		angle = roll * 0.5f;
		sinRoll = Math.sin(angle);
		cosRoll = Math.cos(angle);
		angle = yaw * 0.5f;
		sinYaw = Math.sin(angle);
		cosYaw = Math.cos(angle);

		// variables used to reduce multiplication calls.
		double cosRollXcosPitch = cosRoll * cosPitch;
		double sinRollXsinPitch = sinRoll * sinPitch;
		double cosRollXsinPitch = cosRoll * sinPitch;
		double sinRollXcosPitch = sinRoll * cosPitch;

		w = (cosRollXcosPitch * cosYaw - sinRollXsinPitch * sinYaw);
		x = (cosRollXcosPitch * sinYaw + sinRollXsinPitch * cosYaw);
		y = (sinRollXcosPitch * cosYaw + cosRollXsinPitch * sinYaw);
		z = (cosRollXsinPitch * cosYaw - sinRollXcosPitch * sinYaw);

		normalize();
		return this;
	}

	public double[] toAngles(double[] angles)
	{
		if (angles == null)
			angles = new double[3];
		else if (angles.length != 3)
			throw new IllegalArgumentException(
					"Angles array must have three elements");

		double sqw = w * w;
		double sqx = x * x;
		double sqy = y * y;
		double sqz = z * z;
		double unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
		// is correction factor
		double test = x * y + z * w;
		if (test > 0.499 * unit)
		{ // singularity at north pole
			angles[1] = 2 * Math.atan2(x, w);
			angles[2] = Math.PI / 2;
			angles[0] = 0;
		}
		else if (test < -0.499 * unit)
		{ // singularity at south pole
			angles[1] = -2 * Math.atan2(x, w);
			angles[2] = -Math.PI / 2;
			angles[0] = 0;
		}
		else
		{
			angles[1] = Math
					.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading 
			angles[2] = Math.asin(2 * test / unit); // pitch or attitude
			angles[0] = Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz
					+ sqw); // yaw or bank
		}
		return angles;
	}

	public Quaternion fromAngleAxis(double angle, Vector3 axis)
	{
		Vector3 normAxis = axis.normalize();
		fromAngleNormalAxis(angle, normAxis);
		return this;
	}

	public Quaternion fromAngleNormalAxis(double angle, Vector3 axis)
	{
		if (axis.x == 0 && axis.y == 0 && axis.z == 0)
		{
			loadIdentity();
		}
		else
		{
			double halfAngle = 0.5f * angle;
			double sin = Math.sin(halfAngle);
			w = Math.cos(halfAngle);
			x = sin * axis.x;
			y = sin * axis.y;
			z = sin * axis.z;
		}
		return this;
	}

	public double toAngleAxis(Vector3 axisStore)
	{
		double sqrLength = x * x + y * y + z * z;
		double angle;
		if (sqrLength == 0.0f)
		{
			angle = 0.0f;
			if (axisStore != null)
			{
				axisStore.x = 1.0f;
				axisStore.y = 0.0f;
				axisStore.z = 0.0f;
			}
		}
		else
		{
			angle = (2.0f * Math.acos(w));
			if (axisStore != null)
			{
				double invLength = (1.0f / Math.sqrt(sqrLength));
				axisStore.x = x * invLength;
				axisStore.y = y * invLength;
				axisStore.z = z * invLength;
			}
		}

		return angle;
	}

	public Quaternion slerp(Quaternion q1, Quaternion q2, double t)
	{
		// Create a local quaternion to store the interpolated quaternion
		if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w)
		{
			this.set(q1);
			return this;
		}

		double result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z)
				+ (q1.w * q2.w);

		if (result < 0.0f)
		{
			// Negate the second quaternion and the result of the dot product
			q2.x = -q2.x;
			q2.y = -q2.y;
			q2.z = -q2.z;
			q2.w = -q2.w;
			result = -result;
		}

		// Set the first and second scale for the interpolation
		double scale0 = 1 - t;
		double scale1 = t;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - result) > 0.1f)
		{// Get the angle between the 2 quaternions,
			// and then store the sin() of that angle
			double theta = Math.acos(result);
			double invSinTheta = 1f / Math.sin(theta);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = Math.sin((1 - t) * theta) * invSinTheta;
			scale1 = Math.sin((t * theta)) * invSinTheta;
		}

		// Calculate the x, y, z and w values for the quaternion by using a
		// special
		// form of linear interpolation for quaternions.
		this.x = (scale0 * q1.x) + (scale1 * q2.x);
		this.y = (scale0 * q1.y) + (scale1 * q2.y);
		this.z = (scale0 * q1.z) + (scale1 * q2.z);
		this.w = (scale0 * q1.w) + (scale1 * q2.w);

		// Return the interpolated quaternion
		return this;
	}

	public void slerp(Quaternion q2, double changeAmnt)
	{
		if (this.x == q2.x && this.y == q2.y && this.z == q2.z
				&& this.w == q2.w)
		{
			return;
		}

		double result = (this.x * q2.x) + (this.y * q2.y) + (this.z * q2.z)
				+ (this.w * q2.w);

		if (result < 0.0f)
		{
			// Negate the second quaternion and the result of the dot product
			q2.x = -q2.x;
			q2.y = -q2.y;
			q2.z = -q2.z;
			q2.w = -q2.w;
			result = -result;
		}

		// Set the first and second scale for the interpolation
		double scale0 = 1 - changeAmnt;
		double scale1 = changeAmnt;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - result) > 0.1f)
		{
			// Get the angle between the 2 quaternions, and then store the sin()
			// of that angle
			double theta = Math.acos(result);
			double invSinTheta = 1f / Math.sin(theta);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = Math.sin((1 - changeAmnt) * theta) * invSinTheta;
			scale1 = Math.sin((changeAmnt * theta)) * invSinTheta;
		}

		// Calculate the x, y, z and w values for the quaternion by using a
		// special
		// form of linear interpolation for quaternions.
		this.x = (scale0 * this.x) + (scale1 * q2.x);
		this.y = (scale0 * this.y) + (scale1 * q2.y);
		this.z = (scale0 * this.z) + (scale1 * q2.z);
		this.w = (scale0 * this.w) + (scale1 * q2.w);
	}

	public Quaternion add(Quaternion q)
	{
		return new Quaternion(x + q.x, y + q.y, z + q.z, w + q.w);
	}

	public Quaternion addLocal(Quaternion q)
	{
		this.x += q.x;
		this.y += q.y;
		this.z += q.z;
		this.w += q.w;
		return this;
	}

	public Quaternion subtract(Quaternion q)
	{
		return new Quaternion(x - q.x, y - q.y, z - q.z, w - q.w);
	}

	public Quaternion subtractLocal(Quaternion q)
	{
		this.x -= q.x;
		this.y -= q.y;
		this.z -= q.z;
		this.w -= q.w;
		return this;
	}

	public Quaternion mult(Quaternion q)
	{
		return mult(q, null);
	}

	public Quaternion mult(Quaternion q, Quaternion res)
	{
		if (res == null)
			res = new Quaternion();
		double qw = q.w, qx = q.x, qy = q.y, qz = q.z;
		res.x = x * qw + y * qz - z * qy + w * qx;
		res.y = -x * qz + y * qw + z * qx + w * qy;
		res.z = x * qy - y * qx + z * qw + w * qz;
		res.w = -x * qx - y * qy - z * qz + w * qw;
		return res;
	}

	public Vector3 mult(Vector3 v)
	{
		return mult(v, null);
	}

	public Vector3 multLocal(Vector3 v)
	{
		double tempX, tempY;
		tempX = w * w * v.x + 2 * y * w * v.z - 2 * z * w * v.y + x * x * v.x
				+ 2 * y * x * v.y + 2 * z * x * v.z - z * z * v.x - y * y * v.x;
		tempY = 2 * x * y * v.x + y * y * v.y + 2 * z * y * v.z + 2 * w * z
				* v.x - z * z * v.y + w * w * v.y - 2 * x * w * v.z - x * x
				* v.y;
		v.z = 2 * x * z * v.x + 2 * y * z * v.y + z * z * v.z - 2 * w * y * v.x
				- y * y * v.z + 2 * w * x * v.y - x * x * v.z + w * w * v.z;
		v.x = tempX;
		v.y = tempY;
		return v;
	}

	public Quaternion multLocal(Quaternion q)
	{
		double x1 = x * q.w + y * q.z - z * q.y + w * q.x;
		double y1 = -x * q.z + y * q.w + z * q.x + w * q.y;
		double z1 = x * q.y - y * q.x + z * q.w + w * q.z;
		w = -x * q.x - y * q.y - z * q.z + w * q.w;
		x = x1;
		y = y1;
		z = z1;
		return this;
	}

	public Quaternion multLocal(double qx, double qy, double qz, double qw)
	{
		double x1 = x * qw + y * qz - z * qy + w * qx;
		double y1 = -x * qz + y * qw + z * qx + w * qy;
		double z1 = x * qy - y * qx + z * qw + w * qz;
		w = -x * qx - y * qy - z * qz + w * qw;
		x = x1;
		y = y1;
		z = z1;
		return this;
	}

	public Vector3 mult(Vector3 v, Vector3 store)
	{
		if (store == null)
			store = new Vector3();
		if (v.x == 0 && v.y == 0 && v.z == 0)
		{
			store.set(0, 0, 0);
		}
		else
		{
			double vx = v.x, vy = v.y, vz = v.z;
			store.x = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x * vx
					+ 2 * y * x * vy + 2 * z * x * vz - z * z * vx - y * y * vx;
			store.y = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w * z
					* vx - z * z * vy + w * w * vy - 2 * x * w * vz - x * x
					* vy;
			store.z = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w * y
					* vx - y * y * vz + 2 * w * x * vy - x * x * vz + w * w
					* vz;
		}
		return store;
	}

	public Quaternion mult(double scalar)
	{
		return new Quaternion(scalar * x, scalar * y, scalar * z, scalar * w);
	}

	public Quaternion multLocal(double scalar)
	{
		w *= scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	public double dot(Quaternion q)
	{
		return w * q.w + x * q.x + y * q.y + z * q.z;
	}

	public double norm()
	{
		return w * w + x * x + y * y + z * z;
	}

	public void normalize()
	{
		double n = Math.sqrt(norm());
		x /= n;
		y /= n;
		z /= n;
		w /= n;
	}

	public Quaternion inverse()
	{
		double norm = norm();
		if (norm > 0.0)
		{
			double invNorm = 1.0f / norm;
			return new Quaternion(-x * invNorm, -y * invNorm, -z * invNorm, w
					* invNorm);
		}
		// return an invalid result to flag the error
		return null;
	}

	public Quaternion inverseLocal()
	{
		double norm = norm();
		if (norm > 0.0)
		{
			double invNorm = 1.0f / norm;
			x *= -invNorm;
			y *= -invNorm;
			z *= -invNorm;
			w *= invNorm;
		}
		return this;
	}

	public void negate()
	{
		x *= -1;
		y *= -1;
		z *= -1;
		w *= -1;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof Quaternion))
		{
			return false;
		}

		if (this == o)
		{
			return true;
		}

		Quaternion comp = (Quaternion) o;
		if (Double.compare(x, comp.x) != 0)
			return false;
		if (Double.compare(y, comp.y) != 0)
			return false;
		if (Double.compare(z, comp.z) != 0)
			return false;
		if (Double.compare(w, comp.w) != 0)
			return false;
		return true;
	}

	public int hashCode()
	{
		int hash = 37;
		hash = 37 * hash + (int) Double.doubleToLongBits(x);
		hash = 37 * hash + (int) Double.doubleToLongBits(y);
		hash = 37 * hash + (int) Double.doubleToLongBits(z);
		hash = 37 * hash + (int) Double.doubleToLongBits(w);
		return hash;

	}

	public Quaternion opposite()
	{
		return opposite(null);
	}

	public Quaternion opposite(Quaternion store)
	{
		if (store == null)
			store = new Quaternion();

		Vector3 axis = new Vector3();
		double angle = toAngleAxis(axis);

		store.fromAngleAxis(Math.PI + angle, axis);
		return store;
	}

	public Quaternion oppositeLocal()
	{
		return opposite(this);
	}
}
