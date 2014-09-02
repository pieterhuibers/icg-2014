package util;

import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.delaunay.sweep.DTSweepConstraint;


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

	public static TriangulationPoint getMidpoint(TriangulationPoint p1, TriangulationPoint p2)
	{
		TriangulationPoint midpoint = new PolygonPoint(
				(p1.getX() + p2.getX()) / 2.0,
				(p1.getY() + p2.getY()) / 2.0);
		return midpoint;
	}

	public static DTSweepConstraint getSharedEdge(DelaunayTriangle triangle1, DelaunayTriangle triangle2)
	{
		DTSweepConstraint result = new DTSweepConstraint(triangle1.points[0], triangle1.points[1]);
		
		TriangulationPoint[] p1 = triangle1.points;
		TriangulationPoint[] p2 = triangle2.points;
		
		boolean p1Set = false;
		
		for (int i = 0; i < p2.length; i++)
		{
			if(distance(p1[0], p2[i])<THRESHOLD)
			{
				result.p = p1[0];
				p1Set = true;
			}
		}
		for (int i = 0; i < p2.length; i++)
		{
			if(distance(p1[1], p2[i])<THRESHOLD)
			{
				if(!p1Set)
					result.p = p1[1];
				else
					result.q = p1[1];
			}
		}
		for (int i = 0; i < p2.length; i++)
		{
			if(distance(p1[2], p2[i])<THRESHOLD)
			{
				result.q = p1[2];
			}
		}
		return result;
	}
	
	public static double distance(DTSweepConstraint edge, TriangulationPoint point)
	{
		double x0 = point.getX();
		double y0 = point.getY();
		double x1 = edge.p.getX();
		double y1 = edge.p.getY();
		double x2 = edge.q.getX();
		double y2 = edge.q.getY();
		
		double a = (x2-x1)*(y1-y0)-(x1-x0)*(y2-y1);
		double b = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		return a/b;
	}
	
	public static TriangulationPoint getRemainingPoint(DelaunayTriangle triangle, DTSweepConstraint edge)
	{
		boolean p1 = (pointsEqual(triangle.points[0], edge.p) || pointsEqual(triangle.points[0], edge.q));
		boolean p2 = (pointsEqual(triangle.points[1], edge.p) || pointsEqual(triangle.points[1], edge.q));
		boolean p3 = (pointsEqual(triangle.points[2], edge.p) || pointsEqual(triangle.points[2], edge.q));
		if(!p1)
			return triangle.points[0];
		else if(!p2)
			return triangle.points[1];
		else if(!p3)
			return triangle.points[2];
		return null;
	}
	
	public static boolean pointsEqual(TriangulationPoint pointA, TriangulationPoint pointB)
	{
		return Util.distance(pointA, pointB)<Util.EPSILON;
	}
}
