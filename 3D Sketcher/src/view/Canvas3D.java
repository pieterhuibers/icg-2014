package view;

import java.util.List;

import model.ChordalAxis;
import model.ChordalAxisPoint;
import model.SketchModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import util.DrawShape;
import control.CanvasListener;

public class Canvas3D extends Composite implements Runnable
{
	private static int AXIS_LENGTH = 10;
	private static int TICK_SIZE = 1;
	private static double TICK_EXTRUSION = 0.2;

	private GLCanvas glCanvas;
	private SketchModel model;

	private Display display;
	private Camera camera;

	private float zoom = 1.0f;

	private boolean showOutline = true;
	private boolean showTriangles = false;
	private boolean showMidpoints = false;
	private boolean showChordalAxis = false;
	private boolean showRaisedAxis = false;
	private boolean showMesh = false;
	
	private boolean showPruned = false;
	private boolean showSubdivided = false;
	private boolean showAxes = true;

	public Canvas3D(Composite parent, int style, SketchModel model)
	{
		super(parent, style);
		this.model = model;
		this.setLayout(new FillLayout());

		display = this.getShell().getDisplay();
		camera = new Camera(0, 0, 0, 0, 0, 1.0);

		GLData data = new GLData();
		data.doubleBuffer = true;
		glCanvas = new GLCanvas(this, SWT.NONE, data);

		CanvasListener canvasListener = new CanvasListener(model, this);
		glCanvas.addMouseListener(canvasListener);
		glCanvas.addMouseMoveListener(canvasListener);
		glCanvas.addMouseWheelListener(canvasListener);
		glCanvas.addKeyListener(canvasListener);

		glCanvas.setCurrent();
		try
		{
			GLContext.useContext(glCanvas);
		} catch (LWJGLException e)
		{
			e.printStackTrace();
		}

		glCanvas.addListener(SWT.Resize, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				Rectangle bounds = glCanvas.getBounds();
				float fAspect = (float) bounds.width / (float) bounds.height;
				glCanvas.setCurrent();
				try
				{
					GLContext.useContext(glCanvas);
				} catch (LWJGLException e)
				{
					e.printStackTrace();
				}
				GL11.glViewport(0, 0, bounds.width, bounds.height);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GLU.gluPerspective(45.0f, fAspect, 0.5f, 400.0f);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
			}
		});

		GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		GL11.glClearDepth(1.0);
		GL11.glLineWidth(2);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GLU.gluPerspective(60.0f, this.getSize().x / (float) this.getSize().y,
				1f, 6000f);

		glCanvas.addListener(SWT.Paint, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				run();
			}
		});
		display.asyncExec(this);
	}

	@Override
	public void run()
	{

		if (!glCanvas.isDisposed())
		{
			glCanvas.setCurrent();
			try
			{
				GLContext.useContext(glCanvas);
			} catch (LWJGLException e)
			{
				e.printStackTrace();
			}

			setCamera();
			drawScene();

			glCanvas.swapBuffers();
			display.asyncExec(this);
		}
	}

	private void setCamera()
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		camera.lookThrough();
	}

	public void rotate(double x, double y)
	{
		camera.rotateAroundX(x);
		camera.rotateAroundY(y);
	}

	public void zoomIn()
	{
		this.zoom = (float) (zoom * 1.2);
		camera.setZoom(zoom);
	}

	public void zoomOut()
	{
		this.zoom = (float) (zoom / 1.2);
		camera.setZoom(zoom);
	}

	private void drawScene()
	{
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		
		if (showAxes)
			drawAxes();
		drawPlane();
		if(showOutline)
		{
			drawOutline();
			drawPoints();
		}
		drawPruningCircle();
		if (showTriangles)
			drawTriangles();
		if (showMidpoints)
			drawMidpoints();
		if (showChordalAxis)
			drawChordalAxis();
		

	}
	
	private void drawOutline()
	{
		List<TriangulationPoint> points = model.getPoints();
		GL11.glLineWidth(3.0f);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glColor3f(.3f, .5f, .8f);
		for (TriangulationPoint point : points)
		{
			GL11.glVertex3d(point.getX(), point.getY(), 0.0);
		}
		if (model.isClosed())
		{
			GL11.glVertex3d(points.get(0).getX(), points.get(0).getY(), 0.0);
		}
		GL11.glEnd();
	}
	
	private void drawPruningCircle()
	{
		TriangulationPoint center = model.getCircleCenter();
		if(center!=null)
		{
			TriangulationPoint p1 = model.getEdgePoint1();
			TriangulationPoint p2 = model.getEdgePoint2();
			double radius = model.getCircleRadius();
			GL11.glLineWidth(2);
			GL11.glColor3f(.6f, .2f, .2f);
			DrawShape.sphere(center.getX(), center.getY(), 0.0, 0.02);
			DrawShape.circle(center.getX(), center.getY(), 0.0, radius, 50);
			GL11.glColor3f(.1f, .8f, .8f);
			DrawShape.line(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
		}
	}
	
	private void drawPoints()
	{
		List<TriangulationPoint> points = model.getPoints();
		GL11.glColor3d(0.6, 0.2, 0.3);
		GL11.glLineWidth(2.0f);
		for (TriangulationPoint point : points)
		{
			DrawShape.sphere(point.getX(), point.getY(), 0.0, 0.02);
		}
	}

	private void drawTriangles()
	{
		List<DelaunayTriangle> triangles;
		if(showPruned)
			triangles = model.getPrunedTriangles();
		else if(showSubdivided)
			triangles = model.getSubdividedTriangles();
		else
			triangles = model.getTriangles();
		if (triangles == null)
			return;
		GL11.glLineWidth(1.0f);
		GL11.glColor3f(.6f, .6f, .6f);
		for (DelaunayTriangle triangle : triangles)
		{
			GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glVertex3d(triangle.points[0].getX(),
					triangle.points[0].getY(), 0.0);
			GL11.glVertex3d(triangle.points[1].getX(),
					triangle.points[1].getY(), 0.0);
			GL11.glVertex3d(triangle.points[2].getX(),
					triangle.points[2].getY(), 0.0);
			GL11.glVertex3d(triangle.points[0].getX(),
					triangle.points[0].getY(), 0.0);
			GL11.glEnd();
		}
	}

	private void drawMidpoints()
	{
		if (model.getChordalAxisPoints() == null)
			return;
		GL11.glLineWidth(2.0f);
		GL11.glColor3d(0.6, 0.2, 0.3);
		List<TriangulationPoint> points;
		if(!showPruned)
			points = model.getChordalAxisPoints();
		else
			points = model.getPrunedChordalAxisPoints();
		for (TriangulationPoint midpoint : points)
		{
			DrawShape.sphere(midpoint.getX(), midpoint.getY(), 0.0, 0.02);
		}
	}
	
	private void drawChordalAxis()
	{
		if(model.getChordalAxis()==null)
			return;
		ChordalAxis axis;
		if(!showPruned && !showSubdivided)
			axis = model.getChordalAxis();
		else
			axis = model.getPrunedChordalAxis();
		GL11.glLineWidth(5.0f);
		GL11.glColor3f(.3f, .5f, .3f);
				
		for (ChordalAxisPoint point : axis.getPoints())
		{
			for (ChordalAxisPoint connection : point.getConnections())
			{
				GL11.glBegin(GL11.GL_LINE_STRIP);
				GL11.glVertex3d(point.getX(), point.getY(), 0.0);
				GL11.glVertex3d(connection.getX(), connection.getY(), 0.0);
				GL11.glEnd();
			}
		}
	}

	/*
	private void drawChordalAxis()
	{
		if(model.getChordalAxis()==null)
			return;
		ChordalAxisPoint start;
		
		if(!showPruned && !showSubdivided)
			start = model.getChordalAxis().getStartPoint();
//		else if(showSubdivided)
//			start = model.getPrunedChordalAxis().getStartPoint();
		else
			start = model.getPrunedChordalAxis().getStartPoint();
		
		
		drawChordalAxis(start,0.0);
		if(showRaisedAxis)
		{
			drawChordalAxis(start,1.0);
		}
	}
	
	private void drawChordalAxis(ChordalAxisPoint point, double height)
	{
		GL11.glLineWidth(5.0f);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glColor3f(.3f, .5f, .3f);
		ChordalAxisPoint o1 = point.getOutgoing1();
		ChordalAxisPoint o2 = point.getOutgoing2();
		if(o1==null && o2==null)	//terminal point
		{
			GL11.glVertex3d(point.getX(), point.getY(), height);
			GL11.glEnd();
		}
		else if(o1 != null && o2 == null) //one section
		{
			GL11.glVertex3d(point.getX(), point.getY(), height);
			drawChordalAxis(o1,height);
		}
		else if(o1 != null && o2 != null) //split
		{
			GL11.glVertex3d(point.getX(), point.getY(), height);
			drawChordalAxis(o1,height);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glVertex3d(point.getX(), point.getY(), height);
			drawChordalAxis(o2,height);
		}
	}*/

	private void drawPlane()
	{
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor3d(0.5, 0.5, 0.5);
		GL11.glVertex3f(-10f, -10f, -0.001f);
		GL11.glVertex3f(10f, -10f, -0.001f);
		GL11.glVertex3f(10f, 10f, -0.001f);
		GL11.glVertex3f(-10f, 10f, -0.001f);

		GL11.glEnd();
	}

	private void drawAxes()
	{
		GL11.glLineWidth(1.0f);

		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3d(0.2, 0.2, 0.2);
		GL11.glVertex3d(-AXIS_LENGTH, 0.0, -0.01 / zoom);
		GL11.glVertex3d(AXIS_LENGTH, 0.0, -0.01 / zoom);
		GL11.glVertex3d(0.0, -AXIS_LENGTH, -0.01 / zoom);
		GL11.glVertex3d(0.0, AXIS_LENGTH, -0.01 / zoom);

		int ticks = AXIS_LENGTH / TICK_SIZE;
		for (int i = 1; i < ticks; i++)
		{
			GL11.glVertex3d(-i * TICK_SIZE, -TICK_EXTRUSION, -0.01 / zoom);
			GL11.glVertex3d(-i * TICK_SIZE, TICK_EXTRUSION, -0.01 / zoom);

			GL11.glVertex3d(i * TICK_SIZE, -TICK_EXTRUSION, -0.01 / zoom);
			GL11.glVertex3d(i * TICK_SIZE, TICK_EXTRUSION, -0.01 / zoom);

			GL11.glVertex3d(-TICK_EXTRUSION, -i * TICK_SIZE, -0.01 / zoom);
			GL11.glVertex3d(TICK_EXTRUSION, -i * TICK_SIZE, -0.01 / zoom);

			GL11.glVertex3d(-TICK_EXTRUSION, i * TICK_SIZE, -0.01 / zoom);
			GL11.glVertex3d(TICK_EXTRUSION, i * TICK_SIZE, -0.01 / zoom);
		}
		GL11.glEnd();
		drawGrid();
	}
	
	private void drawGrid()
	{
		GL11.glLineWidth(0.1f);
		GL11.glColor3d(0.7, 0.7, 0.7);
		GL11.glBegin(GL11.GL_LINES);
		int ticks = AXIS_LENGTH / TICK_SIZE;
		for (int i = 1; i < ticks; i++)
		{
			GL11.glVertex3d(-i * TICK_SIZE, -AXIS_LENGTH, -0.01 / zoom);
			GL11.glVertex3d(-i * TICK_SIZE, AXIS_LENGTH, -0.01 / zoom);
			
			GL11.glVertex3d(i * TICK_SIZE, -AXIS_LENGTH, -0.01 / zoom);
			GL11.glVertex3d(i * TICK_SIZE, AXIS_LENGTH, -0.01 / zoom);
			
			GL11.glVertex3d(-AXIS_LENGTH, i * TICK_SIZE, -0.01 / zoom);
			GL11.glVertex3d(AXIS_LENGTH, i * TICK_SIZE, -0.01 / zoom);
			
			GL11.glVertex3d(-AXIS_LENGTH, -i * TICK_SIZE, -0.01 / zoom);
			GL11.glVertex3d(AXIS_LENGTH, -i * TICK_SIZE, -0.01 / zoom);
		}
		GL11.glEnd();
	}

	public void showOutline(boolean show)
	{
		this.showOutline = show;
	}
	
	public void showAxis(boolean show)
	{
		this.showAxes = show;
	}

	public void showTriangles(boolean show)
	{
		this.showTriangles = show;
	}

	public void showMidpoints(boolean show)
	{
		this.showMidpoints = show;
	}

	public void showChordalAxis(boolean show)
	{
		this.showChordalAxis = show;
	}
	
	public void showPruned(boolean show)
	{
		this.showPruned = show;
		this.showSubdivided = !show;
	}
	
	public void showRaisedAxis(boolean show)
	{
		this.showRaisedAxis = show;
	}
	
	public void showMesh(boolean show)
	{
		this.showMesh = show;
	}
	
	public void showSubdivided(boolean show)
	{
		this.showPruned = !show;
		this.showSubdivided = show;
	}

	public boolean axesShown()
	{
		return showAxes;
	}

	public Camera getCamera()
	{
		return camera;
	}

	public float getZoom()
	{
		return zoom;
	}
	
	public boolean outlineShown()
	{
		return showOutline;
	}

	public boolean trianglesShown()
	{
		return showTriangles;
	}

	public boolean midpointsShown()
	{
		return showMidpoints;
	}

	public boolean chordalAxisShown()
	{
		return showChordalAxis;
	}
	
	public boolean prunedShown()
	{
		return showPruned;
	}
	
	public boolean raiseAxisShown()
	{
		return showRaisedAxis;
	}
	
	public boolean meshShown()
	{
		return showMesh;
	}
	
	public boolean subdivisionShown()
	{
		return showSubdivided;
	}

}
