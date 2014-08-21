package model;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;

public class SketchModel
{
	private PolygonPoint point1, point2;
	private Polygon base;
	private boolean closed = false;
	
	public SketchModel()
	{
		
	}
	
	public void addPoint(PolygonPoint point)
	{
		if(point1==null)
		{
			point1 = point;
		}
		else if(point2==null)
		{
			point2 = point;
		}
		else if(base == null)
		{
			base = new Polygon(point1, point2, point);
		}
		else
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
	}
	
	public void close()
	{
		//if()
	}
	
	public List<TriangulationPoint> getBase()
	{
		List<TriangulationPoint> result = new ArrayList<TriangulationPoint>();
		if(base == null)
		{
			if(point1!=null)
				result.add(point1);
			if(point2!=null)
				result.add(point1);
		}
		else
		{
			result = base.getPoints();
		}
		return result;
	}
	
	public boolean isClosed()
	{
		return closed;
	}
}
