package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

public class SketchModel
{
	private PolygonPoint point1, point2;
	private Polygon base;
	private boolean closed = false;
//	private List<PolygonPoint> midpoints;
	private boolean pruned = false;
	private List<DelaunayTriangle> s;
	private List<DelaunayTriangle> j;
	private List<DelaunayTriangle> t;
	private List<DelaunayTriangle> considered;
	private ChordalAxis chordalAxis;

	public SketchModel()
	{
//		midpoints = new ArrayList<PolygonPoint>();
		s = new ArrayList<DelaunayTriangle>();
		j = new ArrayList<DelaunayTriangle>();
		t = new ArrayList<DelaunayTriangle>();
		considered = new ArrayList<DelaunayTriangle>();
	}

	public void addPoint(PolygonPoint point)
	{
		if (point1 == null)
		{
			point1 = point;
		} else if (point2 == null)
		{
			point2 = point;
		} else if (base == null)
		{
			base = new Polygon(point1, point2, point);
		} else
		{
			ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>();
			points.add(point);
			base.addPoints(points);
		}
	}

	public void clear()
	{
		point1 = null;
		point2 = null;
		base = null;
		closed = false;
		s.clear();
		j.clear();
		t.clear();
		considered.clear();
//		midpoints.clear();
		pruned = false;
	}

	public void close()
	{
		closed = true;
	}

	public void triangulate()
	{
		Poly2Tri.triangulate(base);
		calculateTriangleTypes();
		this.calculateChordalAxis();
	}

	private void calculateTriangleTypes()
	{
		for (DelaunayTriangle triangle : base.getTriangles())
		{
			int nrOfNeighbours = getNumberOfInternalNeighbours(triangle);
			if (nrOfNeighbours == 1)
				t.add(triangle);
			else if (nrOfNeighbours == 2)
				s.add(triangle);
			else if (nrOfNeighbours == 3)
				j.add(triangle);
		}
	}

	private int getNumberOfInternalNeighbours(DelaunayTriangle triangle)
	{
		DelaunayTriangle[] neighbours = triangle.neighbors;
		int nrOfInternals = 0;
		for (int i = 0; i < neighbours.length; i++)
		{
			if ((neighbours[i] != null) && neighbours[i].isInterior())
				nrOfInternals++;
		}
		return nrOfInternals;
	}

	private DelaunayTriangle[] getInternalNeighbours(DelaunayTriangle triangle)
	{
		int nrOfNeighbours = this.getNumberOfInternalNeighbours(triangle);
		DelaunayTriangle[] result = new DelaunayTriangle[nrOfNeighbours];
		DelaunayTriangle[] neighbours = triangle.neighbors;
		int index = 0;
		for (int i = 0; i < neighbours.length; i++)
		{
			if ((neighbours[i] != null) && neighbours[i].isInterior())
			{
				result[index] = neighbours[i];
				index++;
			}
		}
		return result;
	}

	private TriangulationPoint[] getMidPoints(DelaunayTriangle triangle)
	{
		TriangulationPoint[] result = null;
		if(t.contains(triangle))
		{
			result = new TriangulationPoint[1];
			for (int i = 0; i < 3; i++)
			{
				TriangulationPoint p1 = triangle.points[i];
				TriangulationPoint p2 = triangle.points[(i + 1) % 3];
				if (isInnerEdge(p1, p2))
				{
					TriangulationPoint midpoint = new PolygonPoint(
							(p1.getX() + p2.getX()) / 2.0,
							(p1.getY() + p2.getY()) / 2.0);
						result[0] = midpoint;
				}
			}
		}
		else if(s.contains(triangle))
		{
			result = new TriangulationPoint[2];
			int index = 0;
			for (int i = 0; i < 3; i++)
			{
				TriangulationPoint p1 = triangle.points[i];
				TriangulationPoint p2 = triangle.points[(i + 1) % 3];
				if (isInnerEdge(p1, p2))
				{
					TriangulationPoint midpoint = new PolygonPoint(
							(p1.getX() + p2.getX()) / 2.0,
							(p1.getY() + p2.getY()) / 2.0);
						result[index] = midpoint;
						index++;
				}
			}
		}
		else if(j.contains(triangle))
		{
			result = new TriangulationPoint[4];
			int index = 0;
			for (int i = 0; i < 3; i++)
			{
				TriangulationPoint p1 = triangle.points[i];
				TriangulationPoint p2 = triangle.points[(i + 1) % 3];
				if (isInnerEdge(p1, p2))
				{
					TriangulationPoint midpoint = new PolygonPoint(
							(p1.getX() + p2.getX()) / 2.0,
							(p1.getY() + p2.getY()) / 2.0);
						result[index] = midpoint;
						index++;
				}
			}
		}
		return result;
	}

	private void calculateChordalAxis()
	{

		DelaunayTriangle terminal = t.get(0);
		TriangulationPoint startPoint = findExternalPoint(terminal);
		
		ChordalAxisPoint start = new ChordalAxisPoint(startPoint);
		TriangulationPoint[] midpoints = this.getMidPoints(terminal);
		if(midpoints.length!=1)
			System.out.println("Number of midpoints on terminal triangle should equal 0 instead of "+midpoints.length);
		ChordalAxisPoint next = new ChordalAxisPoint(midpoints[0]);
		start.setOutgoing1(next);
		considered.add(terminal);
		DelaunayTriangle[] neighbours = this.getInternalNeighbours(terminal);
		growChordalAxis(next,neighbours[0]);
		chordalAxis = new ChordalAxis(start);
	}
	
	private TriangulationPoint findExternalPoint(DelaunayTriangle terminal)
	{
		int index = -1;
		if (terminal.cEdge[0] == false)
			index = 0;
		else if (terminal.cEdge[1] == false)
			index = 1;
		else if (terminal.cEdge[2] == false)
			index = 2;
		if (index == -1)
		{
			System.err.println("Could not find external point on terminal triangle");
			return null;
		}
		else
		{
			return terminal.points[index];
		}
	}

	private void growChordalAxis(ChordalAxisPoint current, DelaunayTriangle neighbour)
	{
		considered.add(neighbour);
		TriangulationPoint[] midpoints = getMidPoints(neighbour);
		if(t.contains(neighbour))
		{
			TriangulationPoint point = findExternalPoint(neighbour);
			ChordalAxisPoint newPoint = new ChordalAxisPoint(point);
			current.setOutgoing1(newPoint);
			return;
		}
		else if(s.contains(neighbour))
		{
			ChordalAxisPoint newPoint;
			if(distance(current.getPoint(), midpoints[0]) > 0.005)
			{
				newPoint = new ChordalAxisPoint(midpoints[0]);
			}
			else
			{
				newPoint = new ChordalAxisPoint(midpoints[1]);
			}
			current.setOutgoing1(newPoint);;
			DelaunayTriangle[] allNeighbours = getInternalNeighbours(neighbour);
			DelaunayTriangle[] neighbours = discardConsideredNeighbours(allNeighbours);
			//We know there is only one unconsidered neighbour
			growChordalAxis(newPoint, neighbours[0]);
		}
		else if(j.contains(neighbour))
		{
			ChordalAxisPoint center = new ChordalAxisPoint(new PolygonPoint(neighbour.centroid().getX(),neighbour.centroid().getY()));
			current.setOutgoing1(center);
			current.setOutgoing1(center);
			DelaunayTriangle[] allNeighbours = getInternalNeighbours(neighbour);
			DelaunayTriangle[] neighbours = discardConsideredNeighbours(allNeighbours);
			int index1 = -1;
			int index2 = -1;
			for (int i = 0; i < 3; i++)
			{
				if(distance(current.getPoint(), midpoints[i]) > 0.005)
				{
					if(index1 == -1)
						index1 = i;
					else if(index2 == -1)
						index2 = i;
				}
			}
			ChordalAxisPoint newPoint1 = new ChordalAxisPoint(midpoints[index1]);
			ChordalAxisPoint newPoint2 = new ChordalAxisPoint(midpoints[index2]);
			center.setOutgoing1(newPoint1);
			center.setOutgoing2(newPoint2);
			
			//We know there are two unconsidered neighbour
			
			boolean connectNeighbour1ToMidpoint1 = false;
			TriangulationPoint[] neighbour1Midpoints = getMidPoints(neighbours[0]);
			for (int i = 0; i < neighbour1Midpoints.length; i++)
			{
				if(distance(newPoint1.getPoint(),neighbour1Midpoints[i]) < 0.005)
					connectNeighbour1ToMidpoint1 = true;
			}
			if(connectNeighbour1ToMidpoint1)
			{
				growChordalAxis(newPoint1, neighbours[0]);
				growChordalAxis(newPoint2, neighbours[1]);
			}
			else
			{
				growChordalAxis(newPoint1, neighbours[1]);
				growChordalAxis(newPoint2, neighbours[0]);
			}
		}
	}
	
	private DelaunayTriangle[] discardConsideredNeighbours(DelaunayTriangle[] neighbours)
	{
		int unconsidered = neighbours.length;
		for (int i = 0; i < neighbours.length; i++)
		{
			if(considered.contains(neighbours[i]))
				unconsidered--;
		}
		DelaunayTriangle[] result = new DelaunayTriangle[unconsidered];
		int index = 0;
		for (int i = 0; i < neighbours.length; i++)
		{
			if(!considered.contains(neighbours[i]))
			{
				result[index] = neighbours[i];
				index++;
			}
		}
		return result;
	}

	private void calculatePrunedChordalAxis()
	{

	}

//	private boolean pointAlreadyExists(TriangulationPoint point)
//	{
//		for (TriangulationPoint midPoint : midpoints)
//		{
//			if (distance(point, midPoint) < 0.005)
//				return true;
//		}
//		return false;
//	}

	private boolean isInnerEdge(TriangulationPoint p1, TriangulationPoint p2)
	{
		List<TriangulationPoint> points = base.getPoints();
		int size = points.size();
		for (int i = 0; i < base.getPoints().size(); i++)
		{
			TriangulationPoint p1base = points.get(i);
			TriangulationPoint p2base = points.get((i + 1) % size);
			if ((distance(p1base, p1) < 0.005 && distance(p2base, p2) < 0.005)
					|| (distance(p1base, p2) < 0.005 && distance(p2base, p1) < 0.005))
				return false;
		}

		return true;
	}

	private double distance(TriangulationPoint p1, TriangulationPoint p2)
	{
		return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX())
				+ (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
	}

	public void prune()
	{

	}

	public boolean firstPointClicked(PolygonPoint point, double zoomLevel)
	{
		if (base == null)
			return false;
		double a = Math.abs(point.getX() - base.getPoints().get(0).getX());
		double b = Math.abs(point.getY() - base.getPoints().get(0).getY());
		double distance = Math.sqrt(a * a + b * b);
		if (distance < 0.1 * zoomLevel)
			return true;
		else
			return false;
	}

	public List<TriangulationPoint> getPoints()
	{
		List<TriangulationPoint> result = new ArrayList<TriangulationPoint>();
		if (base == null)
		{
			if (point1 != null)
				result.add(point1);
			if (point2 != null)
				result.add(point2);
		} else
		{
			result = base.getPoints();
		}
		return result;
	}

	public List<DelaunayTriangle> getTriangles()
	{
		if (base == null)
			return null;
		return base.getTriangles();
	}
	
	public ChordalAxis getChordalAxis()
	{
		return chordalAxis;
	}

	public List<TriangulationPoint> getChordalAxisPoints()
	{
		if(chordalAxis!=null)
			return chordalAxis.getAllPoints();
		else
			return null;
	}

	public boolean isClosed()
	{
		return closed;
	}

	public boolean isPruned()
	{
		return pruned;
	}
}
