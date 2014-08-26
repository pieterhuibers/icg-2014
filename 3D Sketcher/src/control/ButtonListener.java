package control;

import model.SketchModel;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

import view.Canvas3D;

public class ButtonListener implements SelectionListener
{
	private SketchModel model;
	private Canvas3D canvas;
	
	public ButtonListener(SketchModel model, Canvas3D canvas)
	{
		super();
		this.model = model;
		this.canvas = canvas;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void widgetSelected(SelectionEvent e)
	{
		Button b = (Button)(e.getSource());
		if(b.getText().equals("Clear"))
		{
			model.clear();
		}
		else if(b.getText().equals("Triangles"))
		{
			boolean trianglesShown = canvas.trianglesShown();
			canvas.showTriangles(!trianglesShown);			
		}
		else if(b.getText().equals("Midpoints"))
		{
			boolean midpointsShown = canvas.midpointsShown();
			canvas.showMidpoints(!midpointsShown);			
		}
		else if(b.getText().equals("Chordal Axis"))
		{
			boolean chordalAxisShown = canvas.chordalAxisShown();
			canvas.showChordalAxis(!chordalAxisShown);			
		}
		else if(b.getText().equals("Show Axes"))
		{
			boolean axesShown = canvas.axesShown();
			canvas.showAxis(!axesShown);
		}
		else if(b.getText().equals("Reset Camera"))
		{
			canvas.getCamera().reset();
		}
	}

}
