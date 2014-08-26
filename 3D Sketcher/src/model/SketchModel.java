package model;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

public class SketchModel
{
	private PolygonPoint point1, point2;
	private Polygon base;
	private boolean closed = false;
	private List<PolygonPoint> midpoints;
	private boolean pruned = false;
	
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
		closed = true;
	}
	
	public void triangulate()
	{
		Poly2Tri.triangulate(base);
		this.calculateMidpoints();
	}
	
	private void calculateMidpoints()
	{
		midpoints = new ArrayList<PolygonPoint>();
		if(base == null || base.getTriangles()==null)
			return;
		for (DelaunayTriangle triangle : base.getTriangles())
		{
			for(int i=0; i<3; i++)
			{
				TriangulationPoint p1 = triangle.points[i]; 
				TriangulationPoint p2 = triangle.points[(i+1)%3];
				if(isInnerEdge(p1, p2))
				{
					PolygonPoint midpoint = new PolygonPoint((p1.getX()+p2.getX())/2.0, (p1.getY()+p2.getY())/2.0);
					midpoints.add(midpoint);
				}
			}			
		}
	}
	
	private boolean isInnerEdge(TriangulationPoint p1, TriangulationPoint p2)
	{
		List<TriangulationPoint> points = base.getPoints();
		int size = points.size();
		for(int i=0; i<base.getPoints().size(); i++)
		{
			TriangulationPoint p1base = points.get(i);
			TriangulationPoint p2base = points.get((i+1)%size);
			if((distance(p1base, p1)<0.01 && distance(p2base, p2)<0.01) ||
					(distance(p1base, p2)<0.01 && distance(p2base, p1)<0.01))
				return false;
		}
		
		return true;
	}
	
	private double distance(TriangulationPoint p1, TriangulationPoint p2)
	{
		return Math.sqrt((p1.getX()-p2.getX())*(p1.getX()-p2.getX())+(p1.getY()-p2.getY())*(p1.getY()-p2.getY()));
	}
	
	public void prune()
	{
		
	}
	
	public boolean firstPointClicked(PolygonPoint point, double zoomLevel)
	{
		if(base==null)
			return false;
		double a = Math.abs(point.getX()-base.getPoints().get(0).getX());
		double b = Math.abs(point.getY()-base.getPoints().get(0).getY());
		double distance = Math.sqrt(a*a+b*b);
		if(distance < 0.1*zoomLevel)
			return true;
		else		
			return false;
	}
	
	public List<TriangulationPoint> getPoints()
	{
		List<TriangulationPoint> result = new ArrayList<TriangulationPoint>();
		if(base == null)
		{
			if(point1!=null)
				result.add(point1);
			if(point2!=null)
				result.add(point2);
		}
		else
		{
			result = base.getPoints();
		}
		return result;
	}
	
	public List<DelaunayTriangle> getTriangles()
	{
		if(base==null)
			return null;
		return base.getTriangles();
	}
	
	public List<PolygonPoint> getMidpoints()
	{
		return midpoints;
	}
	
	public boolean isClosed()
	{
		return closed;
	}
	
	public boolean isPruned()
	{
		return pruned;
	}
}
