package model;

import java.util.List;

import org.poly2tri.triangulation.TriangulationPoint;

import util.Util;


public class ChordalAxis
{
	private ChordalAxisPoint start;	
	
	public ChordalAxis(ChordalAxisPoint start)
	{
		this.start = start;
	}
	
	public ChordalAxisPoint getStartPoint()
	{
		return start;
	}

	public List<TriangulationPoint> getAllPoints()
	{
		return start.getNextPoints();
	}
	
	public void removePoint(ChordalAxisPoint point)
	{
		if(point==start)
		{
			start = start.getOutgoing1();
		}
		else
		{
			start.removePoint(point);
		}
	}
	
	public void removePoint(TriangulationPoint point)
	{
		if(Util.distance(start.getPoint(),point)<Util.THRESHOLD)
		{
			start = start.getOutgoing1();
		}
		else
		{
			start.removePoint(point);
		}
	}
	
	public ChordalAxis clone()
	{
		ChordalAxisPoint startClone = start.clone();
		ChordalAxis clone = new ChordalAxis(startClone);
		return clone;
	}
}
