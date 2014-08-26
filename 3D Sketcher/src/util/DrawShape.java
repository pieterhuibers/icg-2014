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
}
