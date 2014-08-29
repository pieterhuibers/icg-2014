package util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

public class DrawShape
{
	public static void sphere(double x, double y, double z, double size) 
	{
	     GL11.glPushMatrix();
	     GL11.glTranslated(x, y, z);
	     Sphere s = new Sphere();
	     s.draw((float)size, 16, 16);
	     GL11.glPopMatrix();
	}
	
	public static void circle(double x, double y, double z, double radius, int steps)
	{
		int rot = 360/steps;
		double angle;
	    GL11.glBegin(GL11.GL_LINE_LOOP);
	    for(int i=0; i<steps; i++)
	    {
	    	angle = Math.toRadians(i*rot);
	    	GL11.glVertex3d(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, z);
	    }
	    GL11.glEnd();
	}
	
	public static void line(double x1, double y1, double z1, double x2, double y2, double z2)
	{	
	    GL11.glBegin(GL11.GL_LINE_STRIP);
	    GL11.glVertex3d(x1, y1, z1);
	    GL11.glVertex3d(x2, y2, z2);
	    GL11.glEnd();
	}
}
