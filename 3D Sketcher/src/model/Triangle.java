package model;

import util.Vector3D;

public class Triangle
{
	public Vector3D p1,p2,p3;
	public Vector3D[] points;
	
	public Triangle(Vector3D p1, Vector3D p2, Vector3D p3)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.points = new Vector3D[3];
		this.points[0] = p1;
		this.points[1] = p2;
		this.points[2] = p3;
	}
	
	
}
