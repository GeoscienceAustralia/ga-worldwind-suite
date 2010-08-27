package au.gov.ga.worldwind.common.ui;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

public class JVisibleDialog extends JDialog
{
	public static interface VisibilityListener
	{
		public void visibleChanged(boolean visible);
	}

	private List<VisibilityListener> listeners = new ArrayList<VisibilityListener>();
	private boolean centerInOwner = false;
	private Frame owner;
	
	public JVisibleDialog(Frame owner, String title)
	{
		super(owner, title);
		this.owner = owner;
	}

	@Override
	public void setVisible(boolean b)
	{
		if(b && centerInOwner)
		{
			setLocationRelativeTo(owner);
			centerInOwner = false;
		}
		super.setVisible(b);
		notifyVisibilityListeners();
	}

	public void addVisibilityListener(VisibilityListener listener)
	{
		listeners.add(listener);
	}

	public void removeVisibilityListener(VisibilityListener listener)
	{
		listeners.remove(listener);
	}

	protected void notifyVisibilityListeners()
	{
		for (VisibilityListener listener : listeners)
		{
			listener.visibleChanged(isVisible());
		}
	}

	public void centerInOwnerWhenShown()
	{
		centerInOwner = true;
	}
}
