package model;

import java.util.ArrayList;

import org.poly2tri.triangulation.TriangulationPoint;

public class ChordalAxisPoint
{
	private TriangulationPoint point;
	private double height;
	private ArrayList<ChordalAxisPoint> connections = new ArrayList<ChordalAxisPoint>();
	private ArrayList<TriangulationPoint> outlinePoints = new ArrayList<TriangulationPoint>();
	
	public ChordalAxisPoint(TriangulationPoint point)
	{
		this.point = point;
		this.height = 0.0;
	}

	public ChordalAxisPoint(TriangulationPoint point, double height)
	{
		this.point = point;
		this.height = height;
	}
	
	public void connect(ChordalAxisPoint point)
	{
		if(!connections.contains(point) && point!=this)
		{
			connections.add(point);
			point.connect(this);
		}
	}
	
	public void disconnect(ChordalAxisPoint point)
	{
		if(connections.contains(point) && point!=this)
		{
			connections.remove(point);
			point.disconnect(this);
		}
	}
	
	public void addOutlinePoint(TriangulationPoint point)
	{
		if(!outlinePoints.contains(point))
			outlinePoints.add(point);
	}
	
	public ArrayList<ChordalAxisPoint> getConnections()
	{
		return connections;
	}
		
	public TriangulationPoint getPoint()
	{
		return point;
	}
	
	public double getX()
	{
		return point.getX();
	}
	
	public double getY()
	{
		return point.getY();
	}
	
	public void setZ(double z)
	{
		this.height = z;
	}
	
	public double getZ()
	{
		return height;
	}
	
	public ArrayList<TriangulationPoint> getOutlinePoints()
	{
		return outlinePoints;
	}
	
	@Override
	public String toString()
	{
		return point.toString() + ": "+connections.size()+" connections";
	}
	
	public ChordalAxisPoint clone()
	{
		ChordalAxisPoint clone = new ChordalAxisPoint(point,height);
		for (TriangulationPoint outlinePoint : outlinePoints)
		{
			clone.addOutlinePoint(outlinePoint);
		}
		return clone;
	}
}
