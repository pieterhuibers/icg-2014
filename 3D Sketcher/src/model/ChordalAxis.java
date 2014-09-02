package model;

import java.util.ArrayList;

import org.poly2tri.triangulation.TriangulationPoint;

import util.Util;

public class ChordalAxis
{
	private ArrayList<ChordalAxisPoint> points = new ArrayList<ChordalAxisPoint>();
	
	public ChordalAxis(ArrayList<ChordalAxisPoint> points)
	{
		this.points = points;
	}
	
	public void addPoint(ChordalAxisPoint point)
	{
		if(!points.contains(point))
			points.add(point);
	}
	
	public ChordalAxis clone()
	{
		ArrayList<ChordalAxisPoint> clonedPoints = new ArrayList<ChordalAxisPoint>(points.size());
		for (ChordalAxisPoint point : points)
		{
			 clonedPoints.add(point.clone());
		}
		for (ChordalAxisPoint clonedPoint : clonedPoints)
		{
			ChordalAxisPoint original = findSamePoint(this.points, clonedPoint);
			for (ChordalAxisPoint originalNeighbour : original.getConnections())
			{
				ChordalAxisPoint clonedNeighbour = findSamePoint(clonedPoints, originalNeighbour);
				clonedPoint.connect(clonedNeighbour);
			}
		}
		ChordalAxis clone = new ChordalAxis(clonedPoints);
		return clone;
	}
	
	public boolean contains(TriangulationPoint point)
	{
		for (ChordalAxisPoint checkPoint : points)
		{
			if(Util.distance(checkPoint.getPoint(), point)<Util.THRESHOLD)
				return true;
		}
		return false;
	}
	
	public void removePoint(TriangulationPoint point)
	{
		ChordalAxisPoint pointToRemove =  findSamePoint(points, new ChordalAxisPoint(point));
		ArrayList<ChordalAxisPoint> connections = pointToRemove.getConnections();
		ArrayList<ChordalAxisPoint> oldConnections = new ArrayList<ChordalAxisPoint>(connections.size());
		for (ChordalAxisPoint connection : connections)
		{
			oldConnections.add(connection);
		}
		
		for (ChordalAxisPoint connection : oldConnections)
		{
			pointToRemove.disconnect(connection);
			for (ChordalAxisPoint connection2 : connections)
			{
				connection.connect(connection2);
			}
		}
		points.remove(pointToRemove);
	}
	
	private ChordalAxisPoint findSamePoint(ArrayList<ChordalAxisPoint> points, ChordalAxisPoint point)
	{
		for (ChordalAxisPoint checkPoint : points)
		{
			if(Util.distance(checkPoint.getPoint(), point.getPoint())<Util.THRESHOLD)
				return checkPoint;
		}
		return null;
	}
	
	public ChordalAxisPoint getPoint(TriangulationPoint point)
	{
		for (ChordalAxisPoint checkPoint : points)
		{
			if(Util.distance(checkPoint.getPoint(), point)<Util.THRESHOLD)
				return checkPoint;
		}
		return null;
	}
	
	public ArrayList<ChordalAxisPoint> getPoints()
	{
		return points;
	}
	
	public ArrayList<TriangulationPoint> getTriangulationPoints()
	{
		ArrayList<TriangulationPoint> result = new ArrayList<TriangulationPoint>(points.size());
		for (ChordalAxisPoint point : points)
		{
			result.add(point.getPoint());
		}
		return result;
	}
}
