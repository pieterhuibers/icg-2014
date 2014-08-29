package util;

import org.poly2tri.triangulation.TriangulationPoint;


public class Util
{
	public static double EPSILON = 0.000001;

	public static boolean approximatelyEqual(double a, double b)
	{
		return approximatelyEqual(a, b, EPSILON);
	}

	public static boolean approximatelyEqual(double a, double b, double epsilon)
	{
		return Math.abs(a - b) <= ((Math.abs(a) < Math.abs(b) ? Math.abs(b)
				: Math.abs(a)) * epsilon);
	}

	public static boolean essentiallyEqual(double a, double b)
	{
		return approximatelyEqual(a, b, EPSILON);
	}

	public static boolean essentiallyEqual(double a, double b, double epsilon)
	{
		return Math.abs(a - b) <= ((Math.abs(a) > Math.abs(b) ? Math.abs(b)
				: Math.abs(a)) * epsilon);
	}

	public static boolean definitelyGreaterThan(double a, double b)
	{
		return definitelyGreaterThan(a, b, EPSILON);
	}

	public static boolean definitelyGreaterThan(double a, double b,
			double epsilon)
	{
		return (a - b) > ((Math.abs(a) < Math.abs(b) ? Math.abs(b) : Math
				.abs(a)) * epsilon);
	}

	public static boolean definitelyLessThan(double a, double b)
	{
		return definitelyLessThan(a, b, EPSILON);
	}

	public static boolean definitelyLessThan(double a, double b, double epsilon)
	{
		return (b - a) > ((Math.abs(a) < Math.abs(b) ? Math.abs(b) : Math
				.abs(a)) * epsilon);
	}
	
	/**
	 * Calculates the 3D velocity vector for the aircraft, based on the heading,
	 * pitch and speed.
	 * 
	 * @param heading
	 *        the heading in degrees, 0=360=north
	 * @param pitch
	 *        the pitch angle, -90 is nose straight down, 90 is nose straight up
	 * @param speed
	 *        the speed, in m/s
	 * @return a Vector3D object with x,y,z values corresponding to the given
	 *         heading, pitch and speed
	 */
	public static Vector3D toVelocityVector(double heading, double pitch, double speed)
	{
		Vector3D result = new Vector3D(1, 0, 0);
		double radians = Math.toRadians(normalizeHeadingInDegrees(90 - heading));
		result.rotateZ(radians);

		double angle = -Math.toRadians(pitch);
		Vector3D rotationAxis = new Vector3D(-result.y, result.x, 0);
		result.rotateAroundAxis(rotationAxis, angle);
		result.normalize();

		result.multiply(speed);
		return result;
	}
	
	/**
	 * Calculates the 3D position vector (vector from origin to position) based
	 * on the platforms latitude, longitude and altitude.
	 * 
	 * @param lat
	 *        the platforms latitude
	 * @param lon
	 *        the platforms longitude
	 * @param alt
	 *        the platforms altitude
	 * @return a Vector3D object with x,y,z values corresponding to the
	 *         latitude, longitude and altitude of the platform
	 */
	public static Vector3D toPositionVector(double lat, double lon, double alt)
	{
		double x = lat;
		double y = lon;
		double z = alt;
		Vector3D result = new Vector3D(x, y, z);
		return result;
	}

	/**
	 * Normalizes the heading of a plane between 0 and 360.
	 * 
	 * @param heading
	 *        the heading (might be outside the 0-360 range)
	 * @return the heading normalized between 0 and 360
	 */
	public static double normalizeHeadingInDegrees(double heading)
	{
		if(heading > 360)
			return heading - 360;
		if(heading < 0)
			return 360 + heading;
		return heading;
	}
	
	

	public static double THRESHOLD = 0.0000001;

	public static double distance(TriangulationPoint p1, TriangulationPoint p2)
	{
		if(p1==null || p2==null)
		{
			System.err.println("Measuring distance between null point(s)");
			return Double.MAX_VALUE;
		}
		return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX())
				+ (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
	}
}
