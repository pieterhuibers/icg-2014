package view;

import model.SketchModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import control.ButtonListener;


public class SketchWindow extends Composite
{
	private static final int DEFAULT_WIDTH = 860;
	private static final int DEFAULT_HEIGHT = 640;
	
	private Canvas3D canvas;
	private SketchModel model;
	
	private Button clear, outline, triangles, midpoints, chordal, pruned, raised, mesh, showAxes, resetCamera;
	
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
		SelectionListener buttonListener = new ButtonListener(model, canvas);
						
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
		
		pruned = new Button(this, SWT.NONE);
		pruned.setText("Pruned");
		pruned.addSelectionListener(buttonListener);
		
		raised = new Button(this, SWT.NONE);
		raised.setText("Raised Axis");
		raised.addSelectionListener(buttonListener);
		
		mesh = new Button(this, SWT.NONE);
		mesh.setText("Show Mesh");
		mesh.addSelectionListener(buttonListener);
		
		showAxes = new Button(this, SWT.NONE);
		showAxes.setText("Show Axes");
		showAxes.addSelectionListener(buttonListener);
		
		resetCamera = new Button(this, SWT.NONE);
		resetCamera.setText("Reset Camera");
		resetCamera.addSelectionListener(buttonListener);
	}
	
	private void resizeComponents()
	{
		int width = this.getSize().x;
		int height= this.getSize().y;
		canvas.setBounds(5, 5, width-10, height-60);
		
		clear.setBounds(5, height-50, 80, 40);
		outline.setBounds(90, height-50, 80, 40);
		triangles.setBounds(175, height-50, 80, 40);
		midpoints.setBounds(260, height-50, 80, 40);
		chordal.setBounds(345, height-50, 80, 40);
		pruned.setBounds(430, height-50, 80, 40);
		raised.setBounds(515, height-50, 80, 40);
		mesh.setBounds(600, height-50, 80, 40);
		showAxes.setBounds(685, height-50, 80, 40);
		resetCamera.setBounds(770, height-50, 80, 40);
	}
}
