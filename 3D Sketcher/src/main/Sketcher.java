package main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import view.SketchWindow;

public class Sketcher {

	public static void main(String[] args)
	{
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		SketchWindow window = new SketchWindow(shell, SWT.NULL);
		Point size = window.getSize();
		shell.setLayout(new FillLayout());
		shell.setText("3D Sketcher");
		
		shell.layout();
		if(size.x == 0 && size.y == 0)
		{
			window.pack();
			shell.pack();
		}
		else
		{
			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
			shell.setSize(shellBounds.width, shellBounds.height);
		}

		// Center the window on the screen

		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		shell.open();
		while(!shell.isDisposed())
		{
			if(!display.readAndDispatch())
				display.sleep();
		}
		System.exit(0);
	}

}
