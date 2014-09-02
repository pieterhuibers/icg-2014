package model;

import java.util.ArrayList;

import org.poly2tri.triangulation.TriangulationPoint;

public class ChordalPoint2
{
	private TriangulationPoint point;
	private ArrayList<ChordalAxisPoint> connections = new ArrayList<ChordalAxisPoint>();
	
	public ChordalPoint2(TriangulationPoint point)
	{
		this.point = point;
	}
	
	@Override
	public String toString()
	{
		return point.toString() + ": "+connections.size()+" connections";
	}
}
