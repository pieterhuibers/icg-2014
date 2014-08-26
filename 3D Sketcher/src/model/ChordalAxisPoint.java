package model;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.triangulation.TriangulationPoint;

public class ChordalAxisPoint
{
	private TriangulationPoint point;
	private ChordalAxisPoint incoming;
	private ChordalAxisPoint outgoing1;
	private ChordalAxisPoint outgoing2;
	
	public ChordalAxisPoint(TriangulationPoint point)
	{
		this.point = point;
	}
	
	public void setIncoming(ChordalAxisPoint incoming)
	{
		this.incoming = incoming;
	}
	
	public void setOutgoing1(ChordalAxisPoint outgoing1)
	{
		this.outgoing1 = outgoing1;
		this.outgoing1.setIncoming(this);
	}
	
	public void setOutgoing2(ChordalAxisPoint outgoing2)
	{
		this.outgoing2 = outgoing2;
		this.outgoing2.setIncoming(this);
	}
	
	public List<TriangulationPoint> getNextPoints()
	{
		List<TriangulationPoint> points = new ArrayList<TriangulationPoint>();
		points.add(this.point);
		if(outgoing1!=null)
			points.addAll(outgoing1.getNextPoints());
		if(outgoing2!=null)
			points.addAll(outgoing2.getNextPoints());
		return points;
	}
	
	public TriangulationPoint getPoint()
	{
		return point;
	}
	
	public ChordalAxisPoint getOutgoing1()
	{
		return outgoing1;
	}
	
	public ChordalAxisPoint getOutgoing2()
	{
		return outgoing2;
	}
	
	public double getX()
	{
		return point.getX();
	}
	
	public double getY()
	{
		return point.getY();
	}
}