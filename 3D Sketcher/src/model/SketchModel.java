package model;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.util.Tuple2;

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
	
//	private int terminalIndex = 0;
	private boolean switchToNewTerminal = true;
	private DelaunayTriangle currentPruningTerminal;
	private DelaunayTriangle previousPruningTriangle;
	private DelaunayTriangle currentPruningTriangle;
	private boolean removeTriangle = true;
	
	private DelaunayTriangle pruningTerminal;
//	private DelaunayTriangle previousPruningTriangle;
//	private DelaunayTriangle currentPruningTriangle;
	private boolean pruningFinishedOnThisTerminal = false;
	private ArrayList<TriangulationPoint> pointsToCheck = new ArrayList<TriangulationPoint>();
	
	private TriangulationPoint pruneEdgePoint1, pruneEdgePoint2;
	private TriangulationPoint circleCenter;
	private double circleRadius;
	
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
	
	public void resetPruning()
	{
		prunedTriangles.clear();
		prunedChordalAxis = chordalAxis.clone();
		pointsToCheck.clear();
		pruningFinishedOnThisTerminal = false;
		pruned = false;
		
		pruningTerminal = null;
		currentPruningTriangle = null;
		previousPruningTriangle = null;
		pruneEdgePoint1 = null;
		pruneEdgePoint2 = null;
		circleCenter = null;
		circleRadius = 0.0;
		copyTriangles();
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
		pointsToCheck.clear();
		pruningFinishedOnThisTerminal = false;
		pruned = false;
		
		pruningTerminal = null;
		currentPruningTriangle = null;
		previousPruningTriangle = null;
		pruneEdgePoint1 = null;
		pruneEdgePoint2 = null;
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
		copyTriangles();
		this.calculateTriangleTypes();
		this.calculateChordalAxis();
		this.prunedChordalAxis = this.chordalAxis.clone();
		this.pruningTerminal = t.get(0);
		this.currentPruningTriangle = t.get(0);
	}
	
	public void prune()
	{
		while(!pruned)
		{
			pruneStep();
		}
	}
	
	public void step()
	{
		if(pruned)
		{
			circleCenter = null;
			return;
		}
		if(switchToNewTerminal)
		{
			switchTerminal();
		}
		if(removeTriangle)
		{
			if(isTerminal(previousPruningTriangle))
			{
				pruneTerminal(previousPruningTriangle);
			}
			else if(isSleeve(previousPruningTriangle))
			{
				pruneSleeve(previousPruningTriangle);
			}
		}
		determineNextCircle();
		
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
	
	private void switchTerminal()
	{
		switchToNewTerminal = false;
		previousPruningTriangle = null;
		boolean lastTerminal = (t.size()-1) == (t.indexOf(currentPruningTerminal));
		if(lastTerminal)
		{
			pruned = true;
			return;
		}
		else
		{
			currentPruningTerminal = t.get(t.indexOf(currentPruningTerminal)+1);
			currentPruningTriangle = currentPruningTerminal;
		}
	}
	
	private void determineNextCircle()
	{
		Tuple2<TriangulationPoint, TriangulationPoint> edge;
		DelaunayTriangle next = getNextTriangle(currentPruningTriangle,previousPruningTriangle);
		if(isJunction(currentPruningTriangle))
		{
			switchToNewTerminal = true;
			step();
			return;
		}
		else if(isTerminal(currentPruningTriangle))
		{
			TriangulationPoint external = findExternalPoint(currentPruningTriangle);
			edge = findRemainingPoints(currentPruningTriangle, external);
			pointsToCheck.add(external);
		}
		else	//sleeve
		{
			edge = getSharedEdge(currentPruningTriangle, next);
		}
			
		circleCenter = calculateMidpoint(edge.a, edge.b);
		circleRadius = Util.distance(edge.a, edge.b)/2.0;
		pruneEdgePoint1 = edge.a;
		pruneEdgePoint2 = edge.b;
		previousPruningTriangle = currentPruningTriangle;
		currentPruningTriangle = next;
		
		removeTriangle = allPointsInsideRadius(circleCenter, circleRadius, pointsToCheck);
		if(!removeTriangle)
		{
			switchToNewTerminal = true;
		}
	}
	
	private void pruneTerminal(DelaunayTriangle terminal)
	{
		prunedTriangles.remove(terminal);
	}
	
	private void pruneSleeve(DelaunayTriangle sleeve)
	{
		prunedTriangles.remove(sleeve);
	}
	
	private void pruneIntoJunction(DelaunayTriangle junction)
	{
		
	}
	
	public void pruneStep()
	{
		if(pruned)
			return;
		//Prune one step
		if(pruningFinishedOnThisTerminal)
		{
			if(currentPruningTriangle!=pruningTerminal)
			{
				DelaunayTriangle next = getNextTriangle(currentPruningTriangle,previousPruningTriangle);
				Tuple2<TriangulationPoint, TriangulationPoint> previousEdge = getSharedEdge(next,currentPruningTriangle);
				fanOut(next, previousEdge.a, previousEdge.b, pointsToCheck);
			}
			int index = t.indexOf(pruningTerminal);
			if(index + 1 <t.size())
			{
				pruningTerminal = t.get(index+1);
				currentPruningTriangle = t.get(index+1);
				previousPruningTriangle = null;
				pruningFinishedOnThisTerminal = false;
				pointsToCheck.clear();
				pruneStep();
			}
			else
			{
				circleCenter = null;
				pruned = true;
			}
		}
		else
		{
			if(!t.contains(currentPruningTriangle))
			{
				prunedTriangles.remove(currentPruningTriangle);
				if(previousPruningTriangle!=null)
				{
					Tuple2<TriangulationPoint, TriangulationPoint> previousEdge = getSharedEdge(currentPruningTriangle,previousPruningTriangle);
					TriangulationPoint midPoint = calculateMidpoint(previousEdge.a, previousEdge.b);
					prunedChordalAxis.removePoint(midPoint);
				}
			}
			if(t.contains(currentPruningTriangle))
			{
				TriangulationPoint external = findExternalPoint(currentPruningTriangle);
				Tuple2<TriangulationPoint, TriangulationPoint> remaining = findRemainingPoints(currentPruningTriangle, external);
				pointsToCheck.add(external);
				circleCenter = calculateMidpoint(remaining.a, remaining.b);
				circleRadius = Util.distance(remaining.a, remaining.b)/2.0;
				pruneEdgePoint1 = remaining.a;
				pruneEdgePoint2 = remaining.b;
			}
			else if(j.contains(currentPruningTriangle))
			{
				//stop
				prunedTriangles.remove(currentPruningTriangle);
				
				j.remove(currentPruningTriangle);
				DelaunayTriangle neighbour1 = null;
				DelaunayTriangle neighbour2 = null;
				for (int i = 0; i < currentPruningTriangle.neighbors.length; i++)
				{
					if(currentPruningTriangle.neighbors[i]!=previousPruningTriangle)
					{
						if(neighbour1==null)
							neighbour1 = currentPruningTriangle.neighbors[i];
						else
							neighbour2 = currentPruningTriangle.neighbors[i];
					}
				}
				
				Tuple2<TriangulationPoint, TriangulationPoint> previousEdge = getSharedEdge(currentPruningTriangle,previousPruningTriangle);
				Tuple2<TriangulationPoint, TriangulationPoint> edge1 = getSharedEdge(currentPruningTriangle,neighbour1);
				Tuple2<TriangulationPoint, TriangulationPoint> edge2 = getSharedEdge(currentPruningTriangle,neighbour2);
				
				TriangulationPoint midPoint = calculateMidpoint(previousEdge.a, previousEdge.b);
				prunedChordalAxis.removePoint(midPoint);
				
				TriangulationPoint center = currentPruningTriangle.centroid();
				fanOut(center, previousEdge.a, previousEdge.b, pointsToCheck);
				DelaunayTriangle new1 = new DelaunayTriangle(center, edge1.a, edge1.b);
				DelaunayTriangle new2 = new DelaunayTriangle(center, edge2.a, edge2.b);
				prunedTriangles.add(new1);
				prunedTriangles.add(new2);
				
				
				pruningFinishedOnThisTerminal = true;
				currentPruningTriangle = pruningTerminal; //To prevent algorithm from fanning out again
				pruneStep();
			}
			else
			{
				DelaunayTriangle next = getNextTriangle(currentPruningTriangle,previousPruningTriangle);
				TriangulationPoint shared = getSharedPoint(previousPruningTriangle, currentPruningTriangle , next);
				for(int i=0; i<3; i++)
				{
					if(Util.distance(currentPruningTriangle.points[i],shared) > 0.05 && !pointsToCheck.contains(currentPruningTriangle.points[i]))
						pointsToCheck.add(currentPruningTriangle.points[i]);
				}
				Tuple2<TriangulationPoint, TriangulationPoint> edge = getSharedEdge(currentPruningTriangle,next);
				circleCenter = calculateMidpoint(edge.a, edge.b);
				circleRadius = Util.distance(edge.a, edge.b)/2.0;
				pruneEdgePoint1 = edge.a;
				pruneEdgePoint2 = edge.b;
			}
			if(allPointsInsideRadius(circleCenter, circleRadius, pointsToCheck))
			{
				prunedTriangles.remove(currentPruningTriangle);
				if(previousPruningTriangle!=null)
				{
					Tuple2<TriangulationPoint, TriangulationPoint> previousEdge = getSharedEdge(currentPruningTriangle,previousPruningTriangle);
					TriangulationPoint midPoint = calculateMidpoint(previousEdge.a, previousEdge.b);
					prunedChordalAxis.removePoint(midPoint);
				}
				DelaunayTriangle next = getNextTriangle(currentPruningTriangle,previousPruningTriangle);
				previousPruningTriangle = currentPruningTriangle;
				currentPruningTriangle = next;
			}
			else
			{
				pruningFinishedOnThisTerminal = true;
			}
		}
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
			if(Util.distance(current.getPoint(), midpoints[0]) > Util.THRESHOLD)
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
			center.setOutgoing1(newPoint1);
			center.setOutgoing2(newPoint2);
			
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
			if ((Util.distance(p1base, p1) < Util.THRESHOLD && Util.distance(p2base, p2) < Util.THRESHOLD)
					|| (Util.distance(p1base, p2) < Util.THRESHOLD && Util.distance(p2base, p1) < Util.THRESHOLD))
				return false;
		}

		return true;
	}

//	private void pruneFromTerminal(DelaunayTriangle terminal)
//	{
//		TriangulationPoint external = findExternalPoint(terminal);
//		Tuple2<TriangulationPoint, TriangulationPoint> remaining = findRemainingPoints(terminal, external);
//		ArrayList<TriangulationPoint> pointsToCheck = new ArrayList<TriangulationPoint>();
//		pointsToCheck.add(external);
//		prune(terminal, remaining.a, remaining.b, pointsToCheck, null);
//	}
	
//	private void prune(DelaunayTriangle triangle, TriangulationPoint a, TriangulationPoint b, List<TriangulationPoint> pointsToCheck, DelaunayTriangle previousTriangle)
//	{
//		TriangulationPoint midpoint = calculateMidpoint(a,b);
//		double radius = Util.distance(midpoint, a);
//		boolean prune = true;
//		for (TriangulationPoint point : pointsToCheck)
//		{
//			if(Util.distance(midpoint, point) > radius)
//			{
//				prune = false;
//				break;
//			}
//		}
//		if(j.contains(triangle))
//		{
//			prune = false;
//		}
//		if(prune)
//		{
//			prunedTriangles.remove(triangle);	
//			DelaunayTriangle next = getNextTriangle(triangle,previousTriangle);
//			DelaunayTriangle nextNext = getNextTriangle(next,triangle);
//			TriangulationPoint shared = getSharedPoint(triangle, next, nextNext);
//			for(int i=0; i<3; i++)
//			{
//				if(Util.distance(triangle.points[i],shared) > 0.05 && !pointsToCheck.contains(triangle.points[i]))
//					pointsToCheck.add(triangle.points[i]);
//			}
//			Tuple2<TriangulationPoint, TriangulationPoint> edge = getSharedEdge(next,nextNext);
//			prune(next,edge.a,edge.b, pointsToCheck, triangle);
//		}
//		else
//		{
//			fanOut(triangle, a, b, pointsToCheck);
//		}
//	}
	
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
			if(Util.distance(triangle.points[i], point) < Util.THRESHOLD)
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
		for (int i = 0; i < fanPoints.size()-1; i++)
		{
			DelaunayTriangle fanTriangle = new DelaunayTriangle(midpoint, fanPoints.get(i), fanPoints.get(i+1));
			prunedTriangles.add(fanTriangle);
		}
	}
	
	private void fanOut(DelaunayTriangle triangle, TriangulationPoint a, TriangulationPoint b, List<TriangulationPoint> points)
	{
		TriangulationPoint midpoint = calculateMidpoint(a,b);
		fanOut(midpoint,a,b,points);
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
			if(Util.distance(p1[0], p2[i])<Util.THRESHOLD)
			{
				result.a = p1[0];
			}
		}
		for (int i = 0; i < p2.length; i++)
		{
			if(Util.distance(p1[1], p2[i])<Util.THRESHOLD)
			{
				if(result.a==null)
					result.a = p1[1];
				else
					result.b = p1[1];
			}
		}
		for (int i = 0; i < p2.length; i++)
		{
			if(Util.distance(p1[2], p2[i])<Util.THRESHOLD)
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
	
	public List<TriangulationPoint> getPrunedChordalAxisPoints()
	{
		if(prunedChordalAxis!=null)
			return prunedChordalAxis.getAllPoints();
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
	
	public TriangulationPoint getPruneEdgePoint1()
	{
		return pruneEdgePoint1;
	}
	
	public TriangulationPoint getPruneEdgePoint2()
	{
		return pruneEdgePoint2;
	}
}
