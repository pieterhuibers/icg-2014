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
	private static final int DEFAULT_HEIGHT = 600;
	
	private Canvas3D canvas;
	private SketchModel model;
	
	private Button clear, triangulate, midpoints, chordal, showAxes, resetCamera;
	
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
		
		triangulate = new Button(this, SWT.NONE);
		triangulate.setText("Triangulate");
		triangulate.addSelectionListener(buttonListener);
		
		midpoints = new Button(this, SWT.NONE);
		midpoints.setText("Midpoints");
		midpoints.addSelectionListener(buttonListener);
		
		chordal = new Button(this, SWT.NONE);
		chordal.setText("Chordal Axis");
		chordal.addSelectionListener(buttonListener);
		
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
		triangulate.setBounds(90, height-50, 80, 40);
		midpoints.setBounds(175, height-50, 80, 40);
		chordal.setBounds(260, height-50, 80, 40);
		showAxes.setBounds(345, height-50, 80, 40);
		resetCamera.setBounds(430, height-50, 80, 40);
	}
}
