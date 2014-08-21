package util;

/**
 * The Utility class.
 */

public class VectorUtil
{
	/**
	 * Calculates the angle in radians between two vectors using the dot
	 * product.
	 * 
	 * @param a
	 *        first vector
	 * @param b
	 *        second vector
	 * @return the angle between vector a and b, measured in radians
	 */
	public static double getRotationAngle(Vector3D a, Vector3D b)
	{
		if(a.equals(b))
			return 0.0;
		Vector3D aNormalized = new Vector3D(a);
		aNormalized.normalize();
		Vector3D bNormalized = new Vector3D(b);
		bNormalized.normalize();
		double dot = getDotProduct(aNormalized, bNormalized);
		double angle = Math.acos(dot);
		if(Double.isNaN(angle))
			angle = 0.0;
		return angle;
	}

	/**
	 * Calculates the axis on which vector a needs to be rotated to get vector b
	 * (and vice versa). The axis is a normalized 3D vector.
	 * 
	 * @param a
	 *        first vector
	 * @param b
	 *        second vector
	 * @return the normalized vector specifying the axis on which each vector
	 *         can be rotated to yield the other vector (the angle can be
	 *         calculated by getAngle(a,b))
	 */
	public static Vector3D getRotationAxis(Vector3D a, Vector3D b)
	{
		Vector3D rotationAxis = getCrossProduct(a, b);
		rotationAxis.normalize();
		return rotationAxis;
	}

	/**
	 * Returns the angle, projected on the y=0 plane, between two vectors3D.
	 * 
	 * @author Roel Rijken
	 * @return angle in radians
	 */
	public static double getAngleInXPlane(final Vector3D a, final Vector3D b)
	{
		double cosine = 1.0;
		double length = (a.lengthInXPlane() * b.lengthInXPlane());
		if(length > 0.0000000001)
			cosine = (a.y * b.y + a.z * b.z) / length;
		// rounding errors might make dotproduct out of range for cosine
		if(cosine > 1)
			cosine = 1;
		else if(cosine < -1)
			cosine = -1;

		if((a.y * b.z - a.z * b.y) < 0)
			return Math.acos(cosine);
		else
			return -Math.acos(cosine);
	}

	/**
	 * Returns the angle, projected on the y=0 plane, between two vectors3D.
	 * 
	 * @author Roel Rijken
	 * @return angle in radians
	 */
	public static double getAngleInYPlane(final Vector3D a, final Vector3D b)
	{
		double cosine = 1.0;
		double length = (a.lengthInYPlane() * b.lengthInYPlane());
		if(length > 0.0000000001)
			cosine = (a.x * b.x + a.z * b.z) / length;
		// rounding errors might make dotproduct out of range for cosine
		if(cosine > 1)
			cosine = 1;
		else if(cosine < -1)
			cosine = -1;

		if((a.x * b.z - a.z * b.x) < 0)
			return Math.acos(cosine);
		else
			return -Math.acos(cosine);
	}

	/**
	 * Returns the angle, projected on the z=0 plane, between two vectors3D.
	 * 
	 * @author Roel Rijken
	 * @return angle in radians
	 */
	public static double getAngleInZPlane(final Vector3D a, final Vector3D b)
	{
		double cosine = 1.0;
		double length = (a.lengthInZPlane() * b.lengthInZPlane());
		if(length > 0.0000000001)
			cosine = (a.x * b.x + a.y * b.y) / length;
		// rounding errors might make dotproduct out of range for cosine
		if(cosine > 1)
			cosine = 1;
		else if(cosine < -1)
			cosine = -1;

		if((a.x * b.y - a.y * b.x) < 0)
			return Math.acos(cosine);
		else
			return -Math.acos(cosine);
	}



	public static Vector3D vectorToTarget(Vector3D source, Vector3D target)
	{
		Vector3D result = new Vector3D(target.x - source.x, target.y - source.y, target.z - source.z);
		return result;
	}
	
	public static double getDistance(Vector3D source, Vector3D target)
	{
		Vector3D result = new Vector3D(target.x - source.x, target.y - source.y, target.z - source.z);
		return result.getLength();
	}
	
	public static double getDistanceInZPlane(Vector3D source, Vector3D target)
	{
		Vector3D result = new Vector3D(target.x - source.x, target.y - source.y, 0.0);
		return result.getLength();
	}

	/**
	 * Returns the dot product of this vector and the specified vector.
	 */
	public static double getDotProduct(Vector3D a, Vector3D b)
	{
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public static Vector3D getCrossProduct(Vector3D a, Vector3D b)
	{
		double x = a.y * b.z - a.z * b.y;
		double y = a.z * b.x - a.x * b.z;
		double z = a.x * b.y - a.y * b.x;
		return new Vector3D(x, y, z);
	}
	
	public static Vector3D add(Vector3D a, Vector3D b)
	{
		double x = a.x + b.x;
		double y = a.y + b.y;
		double z = a.z + b.z;
		return new Vector3D(x, y, z);
	}
	
	public static Vector3D subtract(Vector3D a, Vector3D b)
	{
		double x = a.x - b.x;
		double y = a.y - b.y;
		double z = a.z - b.z;
		return new Vector3D(x, y, z);
	}
	
	public static Vector3D dividePerElement(Vector3D a, Vector3D b)
	{
		double x = a.x / b.x;
		double y = a.y / b.y;
		double z = a.z / b.z;
		return new Vector3D(x, y, z);
	}
	
	public static boolean compare(Vector3D a, Vector3D b, double epsilon)
	{
		boolean xInRange = (a.x >= b.x - epsilon) && (a.x <= b.x + epsilon);
		boolean yInRange = (a.y >= b.y - epsilon) && (a.y <= b.y + epsilon);
		boolean zInRange = (a.z >= b.z - epsilon) && (a.z <= b.z + epsilon);
		return xInRange && yInRange && zInRange;
	}

	public static double[] multiplyVectorByMatrix(double[] vector, double[][] matrix)
	{
		double[] result = new double[vector.length];
		for(int i = 0; i < 4; ++i)
		{
			double accumulator = 0.0;
			for(int j = 0; j < 4; ++j)
			{
				accumulator += vector[j] * matrix[j][i];
			}
			result[i] = accumulator;
		}
		return result;
	}

	public static double[][] transposeMatrix(double[][] matrix)
	{
		int rows = matrix.length;
		int columns = matrix[rows - 1].length;
		double[][] result = new double[columns][rows];
		for(int i = 0; i < rows; ++i)
		{
			for(int j = 0; j < columns; ++j)
			{
				result[j][i] = matrix[i][j];
			}
		}
		return result;
	}
	
	public static boolean linesBetweenPointsIntersectInZPlane(Vector3D a, Vector3D b, Vector3D c, Vector3D d)
	{
		double t = 0.0;
		double u = 0.0;
		Vector3D p = a.clone();
		Vector3D q = c.clone();
		Vector3D r = subtract(b, a);
		Vector3D s = subtract(d, c);
		p.z = 0;
		q.z = 0;
		r.z = 0;
		s.z = 0;
		t = dividePerElement(getCrossProduct(subtract(q, p),s),getCrossProduct(r,s)).z; 
		u = dividePerElement(getCrossProduct(subtract(q, p),r),getCrossProduct(r,s)).z;
		return (t >= 0.0 && t <= 1.0 && u >= 0.0 && u <= 1.0);
	}

}
