package au.gov.ga.worldwind.panels;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.WorldWindow;

import javax.swing.JPanel;

public interface WWPanel extends Disposable
{
	public void setup(WorldWindow wwd);
	public JPanel getPanel();
	public String getName();
}
