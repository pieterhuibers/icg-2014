package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.delaunay.sweep.DTSweepConstraint;
import org.poly2tri.triangulation.util.Tuple2;

public class SketchModel
{
	private PolygonPoint point1, point2;
	private Polygon base;
	private List<DelaunayTriangle> triangles;
	private boolean closed = false;
	private boolean pruned = false;
	private List<DelaunayTriangle> s;
	private List<DelaunayTriangle> j;
	private List<DelaunayTriangle> t;
	private List<DelaunayTriangle> considered;
	private ChordalAxis chordalAxis;
	private List<DelaunayTriangle> prunedTriangles;
	private ChordalAxis prunedChordalAxis;

	public SketchModel()
	{
		s = new ArrayList<DelaunayTriangle>();
		j = new ArrayList<DelaunayTriangle>();
		t = new ArrayList<DelaunayTriangle>();
		triangles = new ArrayList<DelaunayTriangle>();
		prunedTriangles = new ArrayList<DelaunayTriangle>();
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
		triangles.clear();
		prunedTriangles.clear();
		chordalAxis = null;
		prunedChordalAxis = null;
		considered.clear();
		pruned = false;
	}

	public void close()
	{
		closed = true;
	}

	public void triangulate()
	{
		Poly2Tri.triangulate(base);
		triangles = base.getTriangles();
		copyTriangles();
		this.calculateTriangleTypes();
		this.calculateChordalAxis();
		this.prune();
	}
	
	private void copyTriangles()
	{
		for (DelaunayTriangle triangle : triangles)
		{
//			TriangulationPoint[] points = triangle.points;
//			DelaunayTriangle clone = new DelaunayTriangle(points[0], points[1], points[2]);
			prunedTriangles.add(triangle);
		}
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
	
	private TriangulationPoint calculateMidpoint(TriangulationPoint p1, TriangulationPoint p2)
	{
		TriangulationPoint midpoint = new PolygonPoint(
				(p1.getX() + p2.getX()) / 2.0,
				(p1.getY() + p2.getY()) / 2.0);
		return midpoint;
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
					TriangulationPoint midpoint = calculateMidpoint(p1,p2);
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
					TriangulationPoint midpoint = calculateMidpoint(p1,p2);
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
					TriangulationPoint midpoint = calculateMidpoint(p1,p2);
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

	private void prune()
	{
		for (DelaunayTriangle terminal : t)
		{
			pruneFromTerminal(terminal);
		}
	}
	
	private void pruneFromTerminal(DelaunayTriangle terminal)
	{
		TriangulationPoint external = findExternalPoint(terminal);
		Tuple2<TriangulationPoint, TriangulationPoint> remaining = findRemainingPoints(terminal, external);
		ArrayList<TriangulationPoint> pointsToCheck = new ArrayList<TriangulationPoint>();
		pointsToCheck.add(external);
		prune(terminal, remaining.a, remaining.b, pointsToCheck, null);
	}
	
	private void prune(DelaunayTriangle triangle, TriangulationPoint a, TriangulationPoint b, List<TriangulationPoint> pointsToCheck, DelaunayTriangle previousTriangle)
	{
		TriangulationPoint midpoint = calculateMidpoint(a,b);
		double radius = distance(midpoint, a);
		boolean prune = true;
		for (TriangulationPoint point : pointsToCheck)
		{
			if(distance(midpoint, point) > radius)
			{
				prune = false;
				break;
			}
		}
		if(j.contains(triangle))
		{
			prune = false;
		}
		if(prune)
		{
			prunedTriangles.remove(triangle);	
			DelaunayTriangle next = getNextTriangle(triangle,previousTriangle);
			DelaunayTriangle nextNext = getNextTriangle(next,triangle);
			TriangulationPoint shared = getSharedPoint(triangle, next, nextNext);
			for(int i=0; i<3; i++)
			{
				if(distance(triangle.points[i],shared) > 0.05 && !pointsToCheck.contains(triangle.points[i]))
					pointsToCheck.add(triangle.points[i]);
			}
			Tuple2<TriangulationPoint, TriangulationPoint> edge = getSharedEdge(next,nextNext);
			prune(next,edge.a,edge.b, pointsToCheck, triangle);
		}
		else
		{
			fanOut(triangle, a, b, pointsToCheck);
		}
	}
	
	private TriangulationPoint getSharedPoint(DelaunayTriangle triangle1, DelaunayTriangle triangle2, DelaunayTriangle triangle3)
	{
		for (int i = 0; i < triangle1.points.length; i++)
		{
			if(hasPoint(triangle2, triangle1.points[i]) && hasPoint(triangle2, triangle1.points[i]))
				return triangle1.points[i];
		}
		return null;
	}
	
	private boolean hasPoint(DelaunayTriangle triangle, TriangulationPoint point)
	{
		for (int i = 0; i < triangle.points.length; i++)
		{
			if(distance(triangle.points[i], point) < 0.005)
				return true;
		}
		return false;
	}
	
	//This only works for terminal or sleeve triangles (triangles with one or two internal neighbouring triangles
	private DelaunayTriangle getNextTriangle(DelaunayTriangle current, DelaunayTriangle previous)
	{
		DelaunayTriangle next;
		DelaunayTriangle[] neighbours = getInternalNeighbours(current);
		if(neighbours.length==1)
		{
			next = neighbours[0];
		}
		else if(neighbours.length==1)
		{
			if(neighbours[0]==previous)
				next = neighbours[1];
			else
				next = neighbours[0];
		}
		else
		{
			//No problem, current triangle will not be pruned since it is a junction triangle
			next = neighbours[0];
		}
		return next;
	}
	
	private void fanOut(DelaunayTriangle triangle, TriangulationPoint a, TriangulationPoint b, List<TriangulationPoint> points)
	{
		TriangulationPoint midpoint = calculateMidpoint(a,b);
		List<TriangulationPoint> fanPoints = getIntermediatePoints(a, b);
		for (int i = 0; i < fanPoints.size()-1; i++)
		{
			DelaunayTriangle fanTriangle = new DelaunayTriangle(midpoint, fanPoints.get(i), fanPoints.get(i+1));
			prunedTriangles.add(fanTriangle);
		}
	}
	
	private List<TriangulationPoint> getIntermediatePoints(TriangulationPoint start, TriangulationPoint end)
	{
		ArrayList<TriangulationPoint> result;
		int startIndex = base.getPoints().indexOf(start);
		int endIndex = base.getPoints().indexOf(end);
		
		int distanceInside = Math.abs(endIndex-startIndex) + 1;
		int distanceOutside = base.getPoints().size() - Math.max(startIndex,endIndex) + Math.min(startIndex,endIndex) + 1;
		boolean startIndexSmaller = Math.min(startIndex, endIndex)==startIndex;
		if(startIndexSmaller)
		{
			if(distanceInside<=distanceOutside)
			{
				result = buildList(startIndex,endIndex,true);
			}
			else
			{
				result = buildList(startIndex,endIndex,false);
			}
		}
		else
		{
			if(distanceInside<=distanceOutside)
			{
				result = buildList(startIndex,endIndex,false);
			}
			else
			{
				result = buildList(startIndex,endIndex,true);
			}
		}
		
		
		return result;
	}
	
	private ArrayList<TriangulationPoint> buildList(int startIndex, int endIndex, boolean forward)
	{
		List<TriangulationPoint> points = base.getPoints();
		ArrayList<TriangulationPoint> result = new ArrayList<TriangulationPoint>();
		int index = startIndex;
		while(index!=endIndex)
		{
			result.add(points.get(index));
			if(forward)
			{
				index++;
				if(index==points.size())
					index = 0;
			}
			else
			{
				index--;
				if(index==-1)
					index = points.size()-1;
			}
		}
		result.add(points.get(endIndex));
		return result;
	}
	
	private Tuple2<TriangulationPoint, TriangulationPoint> getSharedEdge(DelaunayTriangle triangle1, DelaunayTriangle triangle2)
	{
		Tuple2<TriangulationPoint, TriangulationPoint> result = new Tuple2<TriangulationPoint, TriangulationPoint>(null, null);
		
		TriangulationPoint[] p1 = triangle1.points;
		TriangulationPoint[] p2 = triangle2.points;
		
		for (int i = 0; i < p2.length; i++)
		{
			if(distance(p1[0], p2[i])<0.005)
			{
				result.a = p1[0];
			}
		}
		for (int i = 0; i < p2.length; i++)
		{
			if(distance(p1[1], p2[i])<0.005)
			{
				if(result.a==null)
					result.a = p1[1];
				else
					result.b = p1[1];
			}
		}
		for (int i = 0; i < p2.length; i++)
		{
			if(distance(p1[2], p2[i])<0.005)
			{
				result.b = p1[2];
			}
		}
		return result;
	}
	
	
	private Tuple2<TriangulationPoint, TriangulationPoint> findRemainingPoints(DelaunayTriangle triangle, TriangulationPoint point)
	{
		if(point==triangle.points[0])
		{
			return new Tuple2<TriangulationPoint, TriangulationPoint>(triangle.points[1], triangle.points[2]);
		}
		else if(point==triangle.points[1])
		{
			return new Tuple2<TriangulationPoint, TriangulationPoint>(triangle.points[0], triangle.points[2]);
		}
		else if(point==triangle.points[2])
		{
			return new Tuple2<TriangulationPoint, TriangulationPoint>(triangle.points[0], triangle.points[1]);
		}
		return null;
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
		return triangles;
	}
	
	public List<DelaunayTriangle> getPrunedTriangles()
	{
		return prunedTriangles;
	}
	
	public ChordalAxis getPrunedChordalAxis()
	{
		return prunedChordalAxis;
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
