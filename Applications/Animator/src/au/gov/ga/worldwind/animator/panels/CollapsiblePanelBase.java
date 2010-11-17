package au.gov.ga.worldwind.animator.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * A base implementation of the {@link AnimatorCollapsiblePanel} interface that extends
 * {@link JPanel}.
 * <p/>
 * Provides convenience implementations of the {@link AnimatorCollapsiblePanel} methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class CollapsiblePanelBase extends JPanel implements AnimatorCollapsiblePanel
{
	private static final long serialVersionUID = 20100906L;
	
	private boolean resizable = true;
	private boolean expanded = true;
	private boolean activated = true;
	private float weight = 1.0f;

	public CollapsiblePanelBase()
	{
		super(new BorderLayout());
	}
	
	@Override
	public JPanel getPanel()
	{
		return this;
	}

	@Override
	public boolean isResizable()
	{
		return resizable;
	}

	@Override
	public void setResizable(boolean resizable)
	{
		this.resizable = resizable;
	}

	@Override
	public float getWeight()
	{
		return weight;
	}

	@Override
	public void setWeight(float weight)
	{
		this.weight = weight;
	}

	@Override
	public boolean isExpanded()
	{
		return expanded;
	}

	@Override
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	@Override
	public boolean isOn()
	{
		return activated;
	}

	@Override
	public void setOn(boolean on)
	{
		this.activated = on;
	}

	@Override
	public Icon getIcon()
	{
		return null;
	}
	
	protected Window getParentWindow()
	{
		Component comp = this;
        while (comp != null && !(comp instanceof Window)) {
            comp = comp.getParent();
        }

        return (Window)comp;
	}
}
