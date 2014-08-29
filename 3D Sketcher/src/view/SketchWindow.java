package view;

import model.SketchModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import control.ButtonListener;


public class SketchWindow extends Composite
{
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 640;
	
	private Canvas3D canvas;
	private SketchModel model;
	
	private Button clear, outline, triangles, midpoints, chordal, pruneStep, pruneAll, pruned, raised, mesh, axes, resetCamera, resetPruning;
	
	public SketchWindow(Composite parent, int style)
	{
		super(parent, style);
		this.setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
		this.createComponents();
		this.resizeComponents();
	}
	
	private void createComponents()
	{
		model = new SketchModel();
		canvas = new Canvas3D(this, SWT.BORDER | SWT.DOUBLE_BUFFERED, model);
		canvas.setVisible(true);
		SelectionListener buttonListener = new ButtonListener(model, canvas, this);
						
		clear = new Button(this, SWT.NONE);
		clear.setText("Clear");
		clear.addSelectionListener(buttonListener);
		
		outline = new Button(this, SWT.NONE);
		outline.setText("Outline");
		outline.addSelectionListener(buttonListener);
		
		triangles = new Button(this, SWT.NONE);
		triangles.setText("Triangles");
		triangles.addSelectionListener(buttonListener);
		
		midpoints = new Button(this, SWT.NONE);
		midpoints.setText("Midpoints");
		midpoints.addSelectionListener(buttonListener);
		
		chordal = new Button(this, SWT.NONE);
		chordal.setText("Chordal Axis");
		chordal.addSelectionListener(buttonListener);
		
		pruneStep = new Button(this, SWT.NONE);
		pruneStep.setText("Prune Step");
		pruneStep.addSelectionListener(buttonListener);
		
		pruneAll = new Button(this, SWT.NONE);
		pruneAll.setText("Prune All");
		pruneAll.addSelectionListener(buttonListener);
		
		pruned = new Button(this, SWT.NONE);
		pruned.setText("Pruned");
		pruned.addSelectionListener(buttonListener);
		
		raised = new Button(this, SWT.NONE);
		raised.setText("Raised Axis");
		raised.addSelectionListener(buttonListener);
		
		mesh = new Button(this, SWT.NONE);
		mesh.setText("Mesh");
		mesh.addSelectionListener(buttonListener);
		
		axes = new Button(this, SWT.NONE);
		axes.setText("Axes");
		axes.addSelectionListener(buttonListener);
		
		resetCamera = new Button(this, SWT.NONE);
		resetCamera.setText("Reset Camera");
		resetCamera.addSelectionListener(buttonListener);
		
		resetPruning = new Button(this, SWT.NONE);
		resetPruning.setText("Reset Pruning");
		resetPruning.addSelectionListener(buttonListener);
	}
	
	private void resizeComponents()
	{
		int width = this.getSize().x;
		int height= this.getSize().y;
		canvas.setBounds(5, 5, width-10, height-100);
		
		outline.setBounds(10, height-90, 80, 40);
		triangles.setBounds(90, height-90, 80, 40);
		midpoints.setBounds(170, height-90, 80, 40);
		chordal.setBounds(250, height-90, 80, 40);
		pruneStep.setBounds(330, height-90, 80, 40);
		pruneAll.setBounds(410, height-90, 80, 40);
		pruned.setBounds(490, height-90, 80, 40);
		raised.setBounds(570, height-90, 80, 40);
		mesh.setBounds(650, height-90, 80, 40);
		
		axes.setBounds(10, height-50, 80, 40);
		resetCamera.setBounds(90, height-50, 80, 40);
		clear.setBounds(170, height-50, 80, 40);
		resetPruning.setBounds(250, height-50, 80, 40);
		
	}
	
	public void resetPruningButtons()
	{
		pruneStep.setEnabled(true);
		pruneAll.setEnabled(true);
	}
	
	public void setShowPrunedButton(boolean pruned)
	{
		if(pruned)
		{
			this.pruned.setText("Pruned");
		}
		else
		{
			this.pruned.setText("Unpruned");
		}
	}
}
