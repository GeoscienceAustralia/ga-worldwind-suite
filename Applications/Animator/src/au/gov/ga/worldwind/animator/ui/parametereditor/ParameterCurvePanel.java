package au.gov.ga.worldwind.animator.ui.parametereditor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.ui.frameslider.CurrentFrameChangeListener;
import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurve.ParameterCurveListener;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A panel that holds multiple {@link ParameterCurve}s, and draws various annotations over them
 * (such as current frame line, mouse position line etc.)
 *
 */
public class ParameterCurvePanel implements ParameterCurveListener, CurrentFrameChangeListener
{
	private static final long serialVersionUID = 20101110L;

	private List<ParameterCurve> curves = new ArrayList<ParameterCurve>();
	
	private AtomicBoolean settingBounds = new AtomicBoolean(false);

	private Animator targetApplication;
	
	private BackingPanel backingPanel;
	
	public ParameterCurvePanel(Animator targetApplication)
	{
		Validate.notNull(targetApplication, "A target application is required");
		this.targetApplication = targetApplication;
		this.targetApplication.getFrameSlider().addChangeListener(this);
		
		backingPanel = new BackingPanel();
		backingPanel.setLayout(new BoxLayout(backingPanel, BoxLayout.Y_AXIS));
		
		MouseMoveListener mouseListener = new MouseMoveListener();
		backingPanel.addMouseMotionListener(mouseListener);
		
	}
	
	public JPanel getJPanel()
	{
		return backingPanel;
	}
	
	/**
	 * Add the provided curve to this panel
	 */
	public void addCurve(ParameterCurve curve)
	{
		if (curves.contains(curve))
		{
			return;
		}
		
		if (curves.isEmpty())
		{
			backingPanel.add(Box.createVerticalStrut(10));
		}
		
		curves.add(curve);
		
		backingPanel.add(curve);
		backingPanel.add(Box.createVerticalStrut(10));
		
		curve.addCurveListener(this);
		
		backingPanel.validate();
		backingPanel.repaint();
	}
	
	public void destroy()
	{
		for (ParameterCurve curve : curves)
		{
			backingPanel.remove(curve);
			curve.destroy();
		}
		backingPanel.removeAll();
		curves.clear();
		backingPanel.validate();
	}
	
	@Override
	public void curveBoundsChanged(ParameterCurve source, ParameterCurveBounds newBounds)
	{
		if (settingBounds.get())
		{
			return;
		}
		settingBounds.set(true);
		for (ParameterCurve curve : curves)
		{
			if (curve == source)
			{
				continue;
			}
			
			if (newBounds.getMinFrame() == curve.getCurveBounds().getMinFrame() && 
					newBounds.getMaxFrame() == curve.getCurveBounds().getMaxFrame())
			{
				continue;
			}
			
			curve.setCurveFrameBounds(newBounds.getMinFrame(), newBounds.getMaxFrame());
		}
		settingBounds.set(false);
		backingPanel.repaint();
	}
	
	@Override
	public void currentFrameChanged(int newCurrentFrame)
	{
		backingPanel.repaint();
	}
	
	private class MouseMoveListener extends MouseAdapter
	{
		@Override
		public void mouseMoved(MouseEvent e)
		{
			backingPanel.repaint();
		}
		
		@Override
		public void mouseExited(MouseEvent e)
		{
			backingPanel.repaint();
		}
	}
	
	/**
	 * The JPanel that backs the curve panel, extended to perform custom painting
	 */
	@SuppressWarnings("serial")
	private class BackingPanel extends JPanel
	{
		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			
			Graphics2D g2 = (Graphics2D)g;
			paintCurrentFrameLine(g2);
			paintCurrentMousePositionLine(g2);
		}
		
		/** Paint a line at the current animation frame through all parameter curves */
		private void paintCurrentFrameLine(Graphics2D g2)
		{
			if (curves.isEmpty())
			{
				return;
			}
			
			// Use the first curve to calculate the value
			double x = curves.get(0).getScreenX(targetApplication.getCurrentAnimation().getCurrentFrame());
			
			g2.setColor(LAFConstants.getCurveEditorCurrentFrameColor());
			g2.draw(new Line2D.Double(x, 0, x, getHeight()));
		}
		
		/** Paint a line at the current mouse x position */
		private void paintCurrentMousePositionLine(Graphics2D g2)
		{
			if (curves.isEmpty())
			{
				return;
			}
			
			Point mousePosition = getMousePosition(true);
			if (mousePosition == null)
			{
				return;
			}
			
			double mouseX = mousePosition.getX();
			
			g2.setColor(LAFConstants.getCurveEditorCurrentMousePositionColor());
			g2.draw(new Line2D.Double(mouseX, 0, mouseX, getHeight()));
			
		}
	}
}
