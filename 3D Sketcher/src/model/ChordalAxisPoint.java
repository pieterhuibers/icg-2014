package model;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.triangulation.TriangulationPoint;

import util.Util;

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
		if(this.outgoing1!=null)
			this.outgoing1.setIncoming(this);
	}
	
	public void setOutgoing2(ChordalAxisPoint outgoing2)
	{
		this.outgoing2 = outgoing2;
		if(this.outgoing2!=null)
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
	
	public void removePoint(ChordalAxisPoint point)
	{
		if(this==point)
		{
			if(incoming!=null)
			{
				incoming.setOutgoing1(outgoing1);
				incoming.setOutgoing1(outgoing2);
			}
		}
		else
		{
			if(outgoing1!=null)
				outgoing1.removePoint(point);
			if(outgoing2!=null)
				outgoing2.removePoint(point);
		}
	}
	
	public void removePoint(TriangulationPoint point, int out)
	{
		if(Util.distance(this.point, point)<Util.THRESHOLD)
		{
			if(incoming!=null)
			{
				if(out==1)
					incoming.setOutgoing1(outgoing1);
				else if(out==2)
					incoming.setOutgoing2(outgoing1);	//we can only delete a node that has at much 1 outgoing connection
			}
		}
		else
		{
			if(outgoing1!=null)
				outgoing1.removePoint(point,1);
			if(outgoing2!=null)
				outgoing2.removePoint(point,2);
		}
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
	
	public ChordalAxisPoint clone()
	{
		ChordalAxisPoint clone = new ChordalAxisPoint(point);
		if(outgoing1!=null)
		{
			ChordalAxisPoint out1Clone = outgoing1.clone();
			clone.setOutgoing1(out1Clone);
		}
		if(outgoing2!=null)
		{
			ChordalAxisPoint out2Clone = outgoing2.clone();
			clone.setOutgoing2(out2Clone);
		}
		return clone;
	}
}