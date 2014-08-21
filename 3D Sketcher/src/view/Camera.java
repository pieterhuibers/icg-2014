package view;


import org.lwjgl.opengl.GL11;

import util.Vector3D;

public class Camera
{
	private Vector3D initialCenter;
	private Vector3D center;
	private double rotX;
	private double rotY;
	private double zoom;
	

	public Camera(double x, double y, double z, double rotX, double rotY, double zoom)
	{
		this.initialCenter = new Vector3D(x, y, z);
		this.center = new Vector3D(x, y, z);
		this.rotX = rotX;
		this.rotY = rotY;
		this.zoom = zoom;
	}
	
	public void rotateAroundX(double x)
	{
		this.rotX = this.rotX + (float)x;
		if(this.rotX > 0)
			this.rotX = 0;
		if(this.rotX < -90)
			this.rotX = -90;
	}

	public void rotateAroundY(double y)
	{
		this.rotY = this.rotY + (float)y;
	}
	
	public void setZoom(double zoom)
	{
		this.zoom = zoom;
	}

	

	public void reset()
	{
		center.x = initialCenter.x;
		center.y = initialCenter.y;
		center.z = initialCenter.z;
		this.rotX = 0.0;
		this.rotY = 0.0;
		this.zoom = 1.0;
	}

	public void lookThrough()
	{
		GL11.glLoadIdentity();
		
		GL11.glTranslated(0.0f, 0.0f, -5.0/zoom);
		GL11.glRotated(rotX, 1.0, 0.0, 0.0);
		GL11.glRotated(rotY, 0.0, 0.0, 1.0);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	}



	/**
	 * Getters and setters
	 */
	
	public Vector3D getCenter()
	{
		return center;
	}
	
	public Vector3D getPosition()
	{
		Vector3D position = new Vector3D();
		
		double z = Math.sin(Math.toRadians(90+rotX)) * 5.0/zoom;
		
		double s = Math.cos(Math.toRadians(90+rotX)) * 5.0/zoom;
		
		position.x = Math.cos(Math.toRadians(90+rotY)) * s;
		position.y = -Math.sin(Math.toRadians(90+rotY)) * s;
		position.z = z;
		return position;
	}
	
	public double getRotX()
	{
		return rotX;
	}
	
	public double getRotY()
	{
		return rotY;
	}
	
	public double getPitch()
	{
		double cameraPitch = -90 - rotX;
		return cameraPitch;
	}
	
	public double getYaw()
	{
		double cameraYaw = rotY;
		return cameraYaw;
	}
}
