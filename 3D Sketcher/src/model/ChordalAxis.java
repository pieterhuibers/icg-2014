package model;

import java.util.List;

import org.poly2tri.triangulation.TriangulationPoint;


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
}
