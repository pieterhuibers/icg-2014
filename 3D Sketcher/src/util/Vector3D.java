package util;

import java.util.Locale;

/**
 * The Vector3D class implements a 3D vector with the floating-point values x,
 * y, and z. Vectors can be thought of either as a (x,y,z) point or as a vector
 * from (0,0,0) to (x,y,z).
 */
public final class Vector3D
{
	public double x;
	public double y;
	public double z;

	public static Vector3D ORIGIN = new Vector3D(0.0, 0.0, 0.0);

	/**
	 * Creates a new Vector3D at (0,0,0).
	 */
	public Vector3D()
	{
		this(0.0, 0.0, 0.0);
	}

	/**
	 * Creates a new Vector3D with the same values as the specified Vector3D.
	 */
	public Vector3D(Vector3D v)
	{
		this(v.x, v.y, v.z);
	}

	/**
	 * Creates a new Vector3D with the specified (x, y, z) values.
	 */
	public Vector3D(double x, double y, double z)
	{
		setTo(x, y, z);
	}

	/**
	 * Checks if this Vector3D is equal to the specified Object. They are equal
	 * only if the specified Object is a Vector3D and the two Vector3D's x, y,
	 * and z coordinates are equal.
	 */
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(!(obj instanceof Vector3D))
			return false;
		Vector3D v = (Vector3D) obj;
		return (v.x >= x - Util.EPSILON && v.x <= x + Util.EPSILON
				&& v.y >= y - Util.EPSILON && v.y <= y + Util.EPSILON
				&& v.z >= z - Util.EPSILON && v.z <= z + Util.EPSILON);
	}

	public int hashCode()
	{
		// TODO May be non-uniformly distributed
		return (new Double(x).hashCode()) ^ (new Double(y).hashCode())
				^ (new Double(z).hashCode());
	}

	/**
	 * Converts this Vector3D to a String representation.
	 */
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public String toPrettyString()
	{
		return String.format(Locale.ENGLISH, "(%.2f, %.2f, %.2f)", x, y, z);
	}

	public Vector3D clone()
	{
		return new Vector3D(x, y, z);
	}

	/**
	 * Checks if this Vector3D is equal to the specified x, y, and z
	 * coordinates.
	 */
	public boolean equals(double x, double y, double z)
	{
		return (this.x >= x - Util.EPSILON && this.x <= x + Util.EPSILON
				&& this.y >= y - Util.EPSILON && this.y <= y + Util.EPSILON
				&& this.z >= z - Util.EPSILON && this.z <= z + Util.EPSILON);
	}

	/**
	 * Sets the vector to the same values as the specified Vector3D.
	 */
	public void setTo(Vector3D v)
	{
		setTo(v.x, v.y, v.z);
	}

	/**
	 * Sets this vector to the specified (x, y, z) values.
	 */
	public void setTo(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Adds the specified (x, y, z) values to this vector.
	 */
	public void add(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
	}

	/**
	 * Subtracts the specified (x, y, z) values to this vector.
	 */
	public void subtract(double x, double y, double z)
	{
		add(-x, -y, -z);
	}

	/**
	 * Adds the specified vector to this vector.
	 */
	public void add(Vector3D v)
	{
		add(v.x, v.y, v.z);
	}

	/**
	 * Subtracts the specified vector from this vector.
	 */
	public void subtract(Vector3D v)
	{
		add(-v.x, -v.y, -v.z);
	}

	/**
	 * Multiplies this vector by the specified value. The new length of this
	 * vector will be length()*s.
	 */
	public void multiply(double s)
	{
		x *= s;
		y *= s;
		z *= s;
	}

	/**
	 * Divides this vector by the specified value. The new length of this vector
	 * will be length()/s.
	 */
	public void divide(double s)
	{
		x /= s;
		y /= s;
		z /= s;
	}

	/**
	 * Returns the length of this vector as a float.
	 */
	public double getLength()
	{
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Returns the length of this vector in the plane x=0.
	 */
	public double lengthInXPlane()
	{
		return Math.sqrt(y * y + z * z);
	}

	/**
	 * Returns the length of this vector in the plane y=0.
	 */
	public double lengthInYPlane()
	{
		return Math.sqrt(x * x + z * z);
	}

	/**
	 * Returns the length of this vector in the plane z=0.
	 */
	public double lengthInZPlane()
	{
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Converts this Vector3D to a unit vector, or in other words, a vector of
	 * length 1. Same as calling v.divide(v.length()).
	 */
	public void normalize()
	{
		if(getLength() > 0.0)
			divide(getLength());
	}

	/**
	 * Rotate this vector around the x axis the specified amount. The specified
	 * angle is in radians. Use Math.toRadians() to convert from degrees to
	 * radians.
	 */
	public void rotateX(double angle)
	{
		rotateX(Math.cos(angle), Math.sin(angle));
	}

	/**
	 * Rotate this vector around the y axis the specified amount. The specified
	 * angle is in radians. Use Math.toRadians() to convert from degrees to
	 * radians.
	 */
	public void rotateY(double angle)
	{
		rotateY(Math.cos(angle), Math.sin(angle));
	}

	/**
	 * Rotate this vector around the z axis the specified amount. The specified
	 * angle is in radians. Use Math.toRadians() to convert from degrees to
	 * radians.
	 */
	public void rotateZ(double angle)
	{
		rotateZ(Math.cos(angle), Math.sin(angle));
	}

	/**
	 * Rotate this vector around the x axis the specified amount, using
	 * pre-computed cosine and sine values of the angle to rotate.
	 */
	public void rotateX(double cosAngle, double sinAngle)
	{
		double newY = y * cosAngle - z * sinAngle;
		double newZ = y * sinAngle + z * cosAngle;
		y = newY;
		z = newZ;
	}

	/**
	 * Rotate this vector around the y axis the specified amount, using
	 * pre-computed cosine and sine values of the angle to rotate.
	 */
	public void rotateY(double cosAngle, double sinAngle)
	{
		double newX = z * sinAngle + x * cosAngle;
		double newZ = z * cosAngle - x * sinAngle;
		x = newX;
		z = newZ;
	}

	/**
	 * Rotate this vector around the z axis the specified amount, using
	 * pre-computed cosine and sine values of the angle to rotate.
	 */
	public void rotateZ(double cosAngle, double sinAngle)
	{
		double newX = x * cosAngle - y * sinAngle;
		double newY = x * sinAngle + y * cosAngle;
		x = newX;
		y = newY;
	}

	/**
	 * Rotates this vector around an arbitrary rotation axis.
	 * 
	 * @param rotationAxis
	 *            Vector specifying the rotation axis
	 * @param angle
	 *            Angle of rotation in radians
	 */
	public void rotateAroundAxis(Vector3D rotationAxis, double angle)
	{
		double x = rotationAxis.x;
		double y = rotationAxis.y;
		double z = rotationAxis.z;
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double t = 1 - Math.cos(angle);
		double[][] rotationMatrix =
		{
		{ t * x * x + cos, t * x * y - sin * z, t * x * z + sin * y, 0 },
		{ t * x * y + sin * z, t * y * y + cos, t * y * z - sin * x, 0 },
		{ t * x * z - sin * y, t * y * z + sin * x, t * z * z + cos, 0 },
		{ 0, 0, 0, 1 } };
		rotationMatrix = VectorUtil.transposeMatrix(rotationMatrix);
		double[] result = VectorUtil.multiplyVectorByMatrix(new double[]
		{ this.x, this.y, this.z, 1 }, rotationMatrix);
		this.setTo(result[0], result[1], result[2]);
	}

	/**
	 * Sets the length of this vector to the length specified. This works by
	 * first normalizing the vector and then multiplying it by the specified
	 * length.
	 * 
	 * @param length
	 *            the required length of the vector
	 */
	public void setLength(double length)
	{
		this.normalize();
		this.multiply(length);
	}
}
