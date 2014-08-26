package control;

import java.awt.geom.Point2D;

import model.SketchModel;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.poly2tri.geometry.polygon.PolygonPoint;

import util.Util;
import util.Vector3D;
import util.VectorUtil;
import view.Canvas3D;

public class CanvasListener implements MouseListener, MouseMoveListener, KeyListener, MouseWheelListener
{
	private SketchModel model;
	private Canvas3D canvas;
	
	private boolean leftMouseDown = false;
	private boolean rightMouseDown = false;
	
	private Point start;
	private Point end;
	
//	private static final double DEFAULT_ROTATION_SPEED = 0.1;
//	private static final double FAST_ROTATION_SPEED = 0.5;

	public CanvasListener(SketchModel model, Canvas3D canvas)
	{
		super();
		this.model = model;
		this.canvas = canvas;
		start = new Point(0, 0);
		end = new Point(0, 0);
	}

	@Override
	public void mouseDoubleClick(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDown(MouseEvent e)
	{
		if(e.button == 1)
		{
			leftMouseDown = true;
			if(!model.isClosed())
				addClickedPosition(e.x,e.y);
		}
		else if(e.button == 3)
		{
			rightMouseDown = true;
		}
		start.x = e.x;
		start.y = e.y;

	}

	@Override
	public void mouseUp(MouseEvent e)
	{
		if(e.button == 1)
			leftMouseDown = false;
		else if(e.button == 3)
			rightMouseDown = false;
		end.x = e.x;
		end.y = e.y;
	}

	@Override
	public void mouseMove(MouseEvent e)
	{
		if(rightMouseDown)
		{
			int diffX = e.x-start.x;
			int diffY = e.y-start.y;
			canvas.rotate(diffY, diffX);
			start.x = e.x;
			start.y = e.y;
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.keyCode == 0x100002b)
		{
			canvas.zoomIn();
		}
		else if(e.keyCode == 0x100002d)
		{
			canvas.zoomOut();
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void addClickedPosition(int x, int y)
	{
		Vector3D clickedVector = getClickedVector(x,y);
		Vector3D camPosition = canvas.getCamera().getPosition();
		double factor = -camPosition.z/clickedVector.z;
		PolygonPoint point = new PolygonPoint((camPosition.x+factor*clickedVector.x), (camPosition.y+factor*clickedVector.y));
		if(model.firstPointClicked(point, canvas.getZoom()))
		{
			model.close();
			model.triangulate();
		}
		else
		{		
			model.addPoint(point);
		}
	}
	
	private Vector3D getClickedVector(int x, int y)
	{
		double cameraPitch = canvas.getCamera().getPitch();
		double cameraYaw = canvas.getCamera().getYaw();
		Vector3D cameraVector = Util.toVelocityVector(cameraYaw, cameraPitch, 1.0);
		Vector3D up = Util.toVelocityVector(cameraYaw, cameraPitch+90.0, 1.0);
		Point2D point = getOffsetFromScreenCenter(x, y);
		double aspectRatio = (double)canvas.getSize().x/(double)canvas.getSize().y;
		double yawDegrees = Math.toDegrees(Math.atan(point.getX()*0.61));
		double pitchDegrees =  Math.toDegrees(Math.atan(-point.getY()*0.61/aspectRatio));
		Vector3D clickedVector = new Vector3D(cameraVector);
		clickedVector.rotateAroundAxis(up, Math.toRadians(-yawDegrees));
		Vector3D left =  VectorUtil.getCrossProduct(clickedVector, up);
		clickedVector.rotateAroundAxis(left, Math.toRadians(pitchDegrees));
		return clickedVector;
	}
	
	private Point2D getOffsetFromScreenCenter(int x, int y)
	{
		
		double xOffset = (x - canvas.getSize().x/2)/(double)(canvas.getSize().x)*2.0;
		double yOffset = (y - canvas.getSize().y/2)/(double)(canvas.getSize().y)*2.0;
		
		return new Point2D.Double(xOffset, yOffset);
	}

	@Override
	public void mouseScrolled(MouseEvent e)
	{
		if(e.count > 0)
		{
			canvas.zoomIn();
		}
		else if(e.count < 0)
		{
			canvas.zoomOut();
		}
		
	}

}
