package model;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.delaunay.sweep.DTSweepConstraint;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import util.Util;

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
	private List<DelaunayTriangle> subdividedTriangles;
	
	private DelaunayTriangle currentTerminal;
	private DelaunayTriangle currentTriangle;
	private DTSweepConstraint currentEdge;
	private ArrayList<TriangulationPoint> pointsToCheck = new ArrayList<TriangulationPoint>();
	
	private TriangulationPoint circleCenter;
	private double circleRadius;
	
	public SketchModel()
	{
		s = new ArrayList<DelaunayTriangle>();
		j = new ArrayList<DelaunayTriangle>();
		t = new ArrayList<DelaunayTriangle>();
		triangles = new ArrayList<DelaunayTriangle>();
		prunedTriangles = new ArrayList<DelaunayTriangle>();
		subdividedTriangles = new ArrayList<DelaunayTriangle>();
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
	
	public void resetPruning()
	{
		prunedTriangles.clear();
		prunedChordalAxis = chordalAxis.clone();
		pointsToCheck.clear();
		pruned = false;
		
		currentTerminal = null;
		currentTriangle = null;
		currentEdge = null;
		circleCenter = null;
		circleRadius = 0.0;
		copyTriangles(triangles, prunedTriangles);
		subdividedTriangles.clear();
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
		subdividedTriangles.clear();
		chordalAxis = null;
		prunedChordalAxis = null;
		considered.clear();
		pointsToCheck.clear();
		pruned = false;
		
		currentTerminal = null;
		currentTriangle = null;
		currentEdge = null;
		circleCenter = null;
		circleRadius = 0.0;
	}

	public void close()
	{
		closed = true;
	}

	public void triangulate()
	{
		Poly2Tri.triangulate(base);
		triangles = base.getTriangles();
		copyTriangles(triangles, prunedTriangles);
		this.calculateTriangleTypes();
		this.calculateChordalAxis();
		this.prunedChordalAxis = this.chordalAxis.clone();
	}
	
	public void prune()
	{
		while(!pruned)
		{
			pruneStep();
		}
	}
	
	public void pruneStep()
	{
		if(pruned)
		{
			circleCenter = null;
			return;
		}
		if(currentEdge==null)
		{
			currentTerminal = t.get(0);
			pruneCurrentTerminal();
		}
		else
		{
			pruneCurrentEdge();
		}
	}
	
	public void subdivide()
	{
		subdividedTriangles.clear();
		List<TriangulationPoint> chordalAxisPoints = prunedChordalAxis.getTriangulationPoints();
		for (DelaunayTriangle triangle : prunedTriangles)
		{
			if(containAnyPoint(triangle, chordalAxisPoints))
			{
				//This is one of the fanned out triangles
				subdividedTriangles.add(triangle);
			}
			else if(isSleeve(triangle))
			{
				subdivideSleeve(triangle);
			}
			else
			{
				//junction triangle
				subdivideJunction(triangle);
			}
			
		}
	}
	
	private void subdivideSleeve(DelaunayTriangle sleeve)
	{
		//find the chordal axis part dissecting this triangle
		DTSweepConstraint chordalEdge = findIntersectingEdge(sleeve);
		double d0 = Util.distance(chordalEdge, sleeve.points[0]);
		double d1 = Util.distance(chordalEdge, sleeve.points[1]);
		double d2 = Util.distance(chordalEdge, sleeve.points[2]);
		
		ArrayList<TriangulationPoint> pos = new ArrayList<TriangulationPoint>();
		ArrayList<TriangulationPoint> neg = new ArrayList<TriangulationPoint>();
		if(d0>0)
			pos.add(sleeve.points[0]);
		else
			neg.add(sleeve.points[0]);
		if(d1>0)
			pos.add(sleeve.points[1]);
		else
			neg.add(sleeve.points[1]);
		if(d2>0)
			pos.add(sleeve.points[2]);
		else
			neg.add(sleeve.points[2]);
		ChordalAxisPoint p = prunedChordalAxis.getPoint(chordalEdge.p);
		ChordalAxisPoint q = prunedChordalAxis.getPoint(chordalEdge.q);
		if(pos.size()==2)
		{
			double p0 = Math.abs(Util.distance(new DTSweepConstraint(pos.get(0), neg.get(0)),chordalEdge.p));
			double p1 = Math.abs(Util.distance(new DTSweepConstraint(pos.get(1), neg.get(0)),chordalEdge.p));
			boolean pOnPos0Edge = p0 < p1;
			p.addOutlinePoint(pos.get(0));
			p.addOutlinePoint(pos.get(1));
			p.addOutlinePoint(neg.get(0));
			q.addOutlinePoint(neg.get(0));
			if(pOnPos0Edge)
			{
				q.addOutlinePoint(pos.get(1));
			}
			else
			{
				
				q.addOutlinePoint(pos.get(0));
			}
		}
		else
		{
			double n0 = Math.abs(Util.distance(new DTSweepConstraint(neg.get(0), pos.get(0)),chordalEdge.p));
			double n1 = Math.abs(Util.distance(new DTSweepConstraint(neg.get(1), pos.get(0)),chordalEdge.p));
			boolean pOnNeg0Edge = n0 < n1;
			p.addOutlinePoint(neg.get(0));
			p.addOutlinePoint(neg.get(1));
			p.addOutlinePoint(pos.get(0));
			q.addOutlinePoint(pos.get(0));
			if(pOnNeg0Edge)
			{
				q.addOutlinePoint(neg.get(1));
			}
			else
			{
				q.addOutlinePoint(neg.get(0));
			}
		}
	}
	
	private void subdivideJunction(DelaunayTriangle junction)
	{
		TriangulationPoint center = junction.centroid();
		TriangulationPoint[] points = junction.points;
		
		ChordalAxisPoint axisPoint = prunedChordalAxis.getPoint(center);
		axisPoint.addOutlinePoint(points[0]);
		axisPoint.addOutlinePoint(points[1]);
		axisPoint.addOutlinePoint(points[2]);
		
//		DelaunayTriangle t1 = new DelaunayTriangle(center, points[0], points[1]);
//		DelaunayTriangle t2 = new DelaunayTriangle(center, points[1], points[2]);
//		DelaunayTriangle t3 = new DelaunayTriangle(center, points[0], points[2]);
//		
//		subdividedTriangles.add(t1);
//		subdividedTriangles.add(t2);
//		subdividedTriangles.add(t3);
	}
	
	private DTSweepConstraint findIntersectingEdge(DelaunayTriangle triangle)
	{
		TriangulationPoint[] midpoints = getMidPoints(triangle);
		
		boolean a = prunedChordalAxis.contains(midpoints[0]);
		boolean b = prunedChordalAxis.contains(midpoints[1]);
//		boolean c = prunedChordalAxis.contains(midpoints[2]);
		if(a && b)
		{
			return new DTSweepConstraint(midpoints[0], midpoints[1]);
		}
		else
		{
			return null;
		}
	}
	
	private boolean containAnyPoint(DelaunayTriangle triangle, List<TriangulationPoint> points)
	{
		for (TriangulationPoint point : points)
		{
			if(containsPoint(triangle, point))
				return true;
		}
		return false;
	}
	
	private boolean containsPoint(DelaunayTriangle triangle, TriangulationPoint point)
	{
		if(Util.distance(triangle.points[0], point)<Util.THRESHOLD)
			return true;
		if(Util.distance(triangle.points[1], point)<Util.THRESHOLD)
			return true;
		if(Util.distance(triangle.points[2], point)<Util.THRESHOLD)
			return true;
		return false;
	}
	
	private void pruneCurrentTerminal()
	{
		prunedTriangles.remove(currentTerminal);
		TriangulationPoint external = getExternalPoint(currentTerminal);
		prunedChordalAxis.removePoint(external);
		currentTriangle = getNextTriangle(currentTerminal, null);
		currentEdge = getOppositeEdge(currentTerminal, external);
		updateCircle();
		pointsToCheck.clear();
		pointsToCheck.add(external);
	}
	
	private void pruneCurrentEdge()
	{
		if(isSleeve(currentTriangle))
		{
			if(allPointsInsideRadius(currentEdge, pointsToCheck))
			{
				prunedTriangles.remove(currentTriangle);
				TriangulationPoint midpoint = Util.getMidpoint(currentEdge.p, currentEdge.q);
				prunedChordalAxis.removePoint(midpoint);
				pointsToCheck.add(currentEdge.p);
				pointsToCheck.add(currentEdge.q);
				DelaunayTriangle nextTriangle = getOppositeTriangle(currentEdge, currentTriangle);
				DTSweepConstraint nextEdge = Util.getSharedEdge(currentTriangle, nextTriangle);
				currentTriangle = nextTriangle;
				currentEdge = nextEdge;
				updateCircle();
			}
			else
			{
				TriangulationPoint midpoint = Util.getMidpoint(currentEdge.p, currentEdge.q);
				fanOut(midpoint, currentEdge.p, currentEdge.q, pointsToCheck);
				selectNextTerminal();
			}
		}
		else if(isJunction(currentTriangle))
		{
			prunedTriangles.remove(currentTriangle);
			TriangulationPoint remainingPoint = Util.getRemainingPoint(currentTriangle, currentEdge);
			TriangulationPoint midpoint = Util.getMidpoint(currentEdge.p, currentEdge.q);
			prunedChordalAxis.removePoint(midpoint);
			TriangulationPoint center = currentTriangle.centroid();
			fanOut(center, currentEdge.p, currentEdge.q, pointsToCheck);
			ChordalAxisPoint axisPoint = prunedChordalAxis.getPoint(center);
			axisPoint.addOutlinePoint(remainingPoint);
			selectNextTerminal();
		}
		else if(isTerminal(currentTriangle))
		{
			TriangulationPoint midpoint = Util.getMidpoint(currentEdge.p, currentEdge.q);
			fanOut(midpoint, currentEdge.p, currentEdge.q, pointsToCheck);
			selectNextTerminal();
		}
		else
		{
			System.err.println("Could not determine type of triangle");
		}
	}
	
	private void selectNextTerminal()
	{
		int index = t.indexOf(currentTerminal);
		if(index==t.size()-1)
		{
			pruned = true;
			currentTerminal = null;
			currentTriangle = null;
			currentEdge = null;
			updateCircle();
		}
		else
		{
			currentTerminal = t.get(index+1);
			pruneCurrentTerminal();
		}
	}
	
	private void updateCircle()
	{
		if(currentEdge==null)
			circleCenter = null;
		else
		{
			circleCenter=Util.getMidpoint(currentEdge.p, currentEdge.q);
			circleRadius = Util.distance(currentEdge.p, currentEdge.q)/2.0;
		}
	}
	
	private boolean isTerminal(DelaunayTriangle triangle)
	{
		return t.contains(triangle);
	}
	
	private boolean isSleeve(DelaunayTriangle triangle)
	{
		return s.contains(triangle);
	}
	
	private boolean isJunction(DelaunayTriangle triangle)
	{
		return j.contains(triangle);
	}
	
	private boolean allPointsInsideRadius(DTSweepConstraint edge, List<TriangulationPoint> points)
	{
		TriangulationPoint midpoint = Util.getMidpoint(edge.p, edge.q);
		double radius = Util.distance(edge.p, edge.q) / 2.0;
		return allPointsInsideRadius(midpoint, radius, points);
	}
	
	private boolean allPointsInsideRadius(TriangulationPoint center, double radius, List<TriangulationPoint> points)
	{
		for (TriangulationPoint point : points)
		{
			if(Util.distance(center, point) > radius)
				return false;
		}
		return true;
	}
	
	private void copyTriangles(List<DelaunayTriangle> original, List<DelaunayTriangle> source)
	{
		for (DelaunayTriangle triangle : original)
		{
			source.add(triangle);
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
	
	private TriangulationPoint[] getMidPoints(DelaunayTriangle triangle)
	{
		TriangulationPoint[] result = null;
		if(isTerminal(triangle))
		{
			result = new TriangulationPoint[1];
			for (int i = 0; i < 3; i++)
			{
				TriangulationPoint p1 = triangle.points[i];
				TriangulationPoint p2 = triangle.points[(i + 1) % 3];
				if (isInnerEdge(p1, p2))
				{
					TriangulationPoint midpoint = Util.getMidpoint(p1,p2);
						result[0] = midpoint;
				}
			}
		}
		else if(isSleeve(triangle))
		{
			result = new TriangulationPoint[2];
			int index = 0;
			for (int i = 0; i < 3; i++)
			{
				TriangulationPoint p1 = triangle.points[i];
				TriangulationPoint p2 = triangle.points[(i + 1) % 3];
				if (isInnerEdge(p1, p2))
				{
					TriangulationPoint midpoint = Util.getMidpoint(p1,p2);
						result[index] = midpoint;
						index++;
				}
			}
		}
		else if(isJunction(triangle))
		{
			result = new TriangulationPoint[4];
			int index = 0;
			for (int i = 0; i < 3; i++)
			{
				TriangulationPoint p1 = triangle.points[i];
				TriangulationPoint p2 = triangle.points[(i + 1) % 3];
				if (isInnerEdge(p1, p2))
				{
					TriangulationPoint midpoint = Util.getMidpoint(p1,p2);
						result[index] = midpoint;
						index++;
				}
			}
		}
		return result;
	}
	
	private void calculateChordalAxis()
	{
		ArrayList<ChordalAxisPoint> points = new ArrayList<ChordalAxisPoint>();
		
		//add points to 'points'
		DelaunayTriangle terminal = t.get(0);
		TriangulationPoint startPoint = getExternalPoint(terminal);
		ChordalAxisPoint start = new ChordalAxisPoint(startPoint);
		points.add(start);
		TriangulationPoint[] midpoints = this.getMidPoints(terminal);
		if(midpoints.length!=1)
			System.out.println("Number of midpoints on terminal triangle should equal 0 instead of "+midpoints.length);
		ChordalAxisPoint next = new ChordalAxisPoint(midpoints[0]);
		start.connect(next);
		points.add(next);
		considered.add(terminal);
		DelaunayTriangle[] neighbours = this.getInternalNeighbours(terminal);
		growChordalAxis(next,neighbours[0],points);
		chordalAxis = new ChordalAxis(points);	
	}
	
	private ArrayList<ChordalAxisPoint> growChordalAxis(ChordalAxisPoint current, DelaunayTriangle neighbour, ArrayList<ChordalAxisPoint> points)
	{
		considered.add(neighbour);
		TriangulationPoint[] midpoints = getMidPoints(neighbour);
		if(t.contains(neighbour))
		{
			TriangulationPoint point = getExternalPoint(neighbour);
			ChordalAxisPoint newPoint = new ChordalAxisPoint(point);
			current.connect(newPoint);
			points.add(newPoint);
		}
		else if(s.contains(neighbour))
		{
			ChordalAxisPoint newPoint;
			if(Util.distance(current.getPoint(), midpoints[0]) > Util.THRESHOLD)
			{
				newPoint = new ChordalAxisPoint(midpoints[0]);
			}
			else
			{
				newPoint = new ChordalAxisPoint(midpoints[1]);
			}
			current.connect(newPoint);
			points.add(newPoint);
			DelaunayTriangle[] allNeighbours = getInternalNeighbours(neighbour);
			DelaunayTriangle[] neighbours = discardConsideredNeighbours(allNeighbours);
			//We know there is only one unconsidered neighbour
			growChordalAxis(newPoint, neighbours[0],points);
		}
		else if(j.contains(neighbour))
		{
			ChordalAxisPoint center = new ChordalAxisPoint(new PolygonPoint(neighbour.centroid().getX(),neighbour.centroid().getY()));
			current.connect(center);
			points.add(center);
			DelaunayTriangle[] allNeighbours = getInternalNeighbours(neighbour);
			DelaunayTriangle[] neighbours = discardConsideredNeighbours(allNeighbours);
			int index1 = -1;
			int index2 = -1;
			for (int i = 0; i < 3; i++)
			{
				if(Util.distance(current.getPoint(), midpoints[i]) > Util.THRESHOLD)
				{
					if(index1 == -1)
						index1 = i;
					else if(index2 == -1)
						index2 = i;
				}
			}
			ChordalAxisPoint newPoint1 = new ChordalAxisPoint(midpoints[index1]);
			ChordalAxisPoint newPoint2 = new ChordalAxisPoint(midpoints[index2]);
			center.connect(newPoint1);
			center.connect(newPoint2);
			points.add(newPoint1);
			points.add(newPoint2);
			
			//We know there are two unconsidered neighbour
			
			boolean connectNeighbour1ToMidpoint1 = false;
			TriangulationPoint[] neighbour1Midpoints = getMidPoints(neighbours[0]);
			for (int i = 0; i < neighbour1Midpoints.length; i++)
			{
				if(Util.distance(newPoint1.getPoint(),neighbour1Midpoints[i]) < Util.THRESHOLD)
					connectNeighbour1ToMidpoint1 = true;
			}
			if(connectNeighbour1ToMidpoint1)
			{
				growChordalAxis(newPoint1, neighbours[0],points);
				growChordalAxis(newPoint2, neighbours[1],points);
			}
			else
			{
				growChordalAxis(newPoint1, neighbours[1],points);
				growChordalAxis(newPoint2, neighbours[0],points);
			}
		}
		return points;
	}

	private TriangulationPoint getExternalPoint(DelaunayTriangle terminal)
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
			if ((Util.distance(p1base, p1) < Util.THRESHOLD && Util.distance(p2base, p2) < Util.THRESHOLD)
					|| (Util.distance(p1base, p2) < Util.THRESHOLD && Util.distance(p2base, p1) < Util.THRESHOLD))
				return false;
		}

		return true;
	}
	
	private DelaunayTriangle getOppositeTriangle(DTSweepConstraint edge, DelaunayTriangle current)
	{
		DelaunayTriangle opposite1 = current.neighborAcross(edge.p);
		DelaunayTriangle opposite2 = current.neighborAcross(edge.q);
		if(opposite1!= null && opposite1.isInterior())
			return opposite1;
		else if(opposite2!= null && opposite2.isInterior())
			return opposite2;
		return null;
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
		else if(neighbours.length==2)
		{
			if(neighbours[0]==previous)
				next = neighbours[1];
			else
				next = neighbours[0];
		}
		else
		{
			//No problem, current triangle will not be pruned since it is a junction triangle
			System.err.println("Junction triangle should not be pruned");
			next = neighbours[0];
		}
		return next;
	}
	
	private void fanOut(TriangulationPoint midpoint, TriangulationPoint a, TriangulationPoint b, List<TriangulationPoint> points)
	{
		List<TriangulationPoint> fanPoints = getIntermediatePoints(a, b);
		ChordalAxisPoint axisPoint = prunedChordalAxis.getPoint(midpoint);
		for (int i = 0; i < fanPoints.size()-1; i++)
		{
			DelaunayTriangle fanTriangle = new DelaunayTriangle(midpoint, fanPoints.get(i), fanPoints.get(i+1));
			prunedTriangles.add(fanTriangle);
			axisPoint.addOutlinePoint(fanPoints.get(i));
		}
		axisPoint.addOutlinePoint(fanPoints.get(fanPoints.size()-1));
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
	
	public static DTSweepConstraint getBorderEdge(DelaunayTriangle triangle1, DelaunayTriangle triangle2)
	{
		return null;
	}
	
	
	private DTSweepConstraint getOppositeEdge(DelaunayTriangle triangle, TriangulationPoint point)
	{
		if(point==triangle.points[0])
		{
			return new DTSweepConstraint(triangle.points[1], triangle.points[2]);
		}
		else if(point==triangle.points[1])
		{
			return new DTSweepConstraint(triangle.points[0], triangle.points[2]);
		}
		else if(point==triangle.points[2])
		{
			return new DTSweepConstraint(triangle.points[0], triangle.points[1]);
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
	
	public List<DelaunayTriangle> getSubdividedTriangles()
	{
		return subdividedTriangles;
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
			return chordalAxis.getTriangulationPoints();
		else
			return null;
	}
	
	public List<TriangulationPoint> getPrunedChordalAxisPoints()
	{
		if(prunedChordalAxis!=null)
			return prunedChordalAxis.getTriangulationPoints();
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
	
	public TriangulationPoint getCircleCenter()
	{
		return circleCenter;
	}
	
	public double getCircleRadius()
	{
		return circleRadius;
	}
	
	public TriangulationPoint getEdgePoint1()
	{
		return currentEdge.p;
	}
	
	public TriangulationPoint getEdgePoint2()
	{
		return currentEdge.q;
	}
}
