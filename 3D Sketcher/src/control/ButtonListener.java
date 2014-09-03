package control;

import model.SketchModel;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

import view.Canvas3D;
import view.SketchWindow;

public class ButtonListener implements SelectionListener
{
	private SketchModel model;
	private Canvas3D canvas;
	private SketchWindow window;
	
	public ButtonListener(SketchModel model, Canvas3D canvas, SketchWindow window)
	{
		super();
		this.model = model;
		this.canvas = canvas;
		this.window = window;
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
			window.resetPruningButtons();
			model.clear();
		}
		else if(b.getText().equals("Outline"))
		{
			boolean outlineShown = canvas.outlineShown();
			canvas.showOutline(!outlineShown);			
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
		else if(b.getText().equals("Prune Step"))
		{
			window.setShowPrunedButton(false);
			canvas.showTriangles(true);
			canvas.showPruned(true);
			model.pruneStep();
			if(model.isPruned())
				b.setEnabled(false);
		}
		else if(b.getText().equals("Prune All"))
		{
			window.setShowPrunedButton(false);
			canvas.showTriangles(true);
			canvas.showPruned(true);
			if(!model.isPruned())
				model.prune();
			b.setEnabled(false);
		}
		else if(b.getText().equals("Pruned"))
		{
			b.setText("Unpruned");
			canvas.showPruned(true);			
		}
		else if(b.getText().equals("Unpruned"))
		{
			b.setText("Pruned");
			canvas.showPruned(false);			
		}
		else if(b.getText().equals("Subdivide"))
		{
			model.subdivide();
			boolean subdivisionShown = canvas.subdivisionShown();
			canvas.showSubdivided(!subdivisionShown);
		}
		else if(b.getText().equals("Raised Axis"))
		{
			model.raiseChordalAxis();
			boolean raisedShown = canvas.raiseAxisShown();
			canvas.showRaisedAxis(!raisedShown);
		}
		else if(b.getText().equals("Mesh"))
		{
			model.prune();
			model.subdivide();
			model.createMesh(5);
			boolean meshShown = canvas.meshShown();
			canvas.showMesh(!meshShown);
		}
		else if(b.getText().equals("Axes"))
		{
			boolean axesShown = canvas.axesShown();
			canvas.showAxis(!axesShown);
		}
		else if(b.getText().equals("Reset Camera"))
		{
			canvas.getCamera().reset();
		}
		else if(b.getText().equals("Reset Pruning"))
		{
			model.resetPruning();
			window.resetPruningButtons();
		}
	}

}
