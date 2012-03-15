/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.ui.frameslider.CurrentFrameChangeListener;
import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurve.ParameterCurveListener;
import au.gov.ga.worldwind.common.util.Range;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A panel that holds multiple {@link ParameterCurve}s, and draws various annotations over them
 * (such as current frame line, mouse position line etc.)
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ParameterCurvePanel implements ParameterCurveListener, CurrentFrameChangeListener
{
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
	
	public void addCurveForParameter(Parameter p)
	{
		if (p == null)
		{
			return;
		}
		
		addCurve(new ParameterCurve(p));
	}
	
	/**
	 * Add the provided curve to this panel
	 */
	public void addCurve(ParameterCurve curve)
	{
		if (curve == null || curves.contains(curve))
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
		
		if (!curves.isEmpty())
		{
			updateCurveBounds(curves.get(0));
		}
	}
	
	/**
	 * Remove the curve for the provided
	 */
	public void removeCurveForParameter(Parameter parameter)
	{
		if (parameter == null)
		{
			return;
		}
		
		// Copy over the curves to be kept
		List<ParameterCurve> tmpCurves = new ArrayList<ParameterCurve>(curves.size());
		for (ParameterCurve curve : curves)
		{
			if (!curve.getParameter().equals(parameter))
			{
				tmpCurves.add(curve);
			}
			else
			{
				curve.destroy();
			}
		}
		
		backingPanel.removeAll();
		curves.clear();
		
		// Add them back into the panel
		for (ParameterCurve curve : tmpCurves)
		{
			addCurve(curve);
		}
		
		if (!curves.isEmpty())
		{
			updateCurveBounds(curves.get(0));
		}
		
		if (curves.isEmpty())
		{
			backingPanel.repaint();
		}
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
		updateCurveBounds(source);
	}
	
	/**
	 * Update the curve bounds of all parameter curves in this panel to
	 * match those provided.
	 */
	public void updateCurveBounds(ParameterCurve source)
	{
		ParameterCurveBounds newBounds = source.getCurveBounds();
		if (newBounds == null)
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
			
			if (curve.getCurveBounds() != null && 
					newBounds.getMinFrame() == curve.getCurveBounds().getMinFrame() && 
					newBounds.getMaxFrame() == curve.getCurveBounds().getMaxFrame())
			{
				continue;
			}
			
			curve.setCurveFrameBounds(newBounds.getMinFrame(), newBounds.getMaxFrame());
		}
		settingBounds.set(false);
		backingPanel.repaint();
	}
	
	/**
	 * Update the curve bounds of all parameter curves in this panel to
	 * match the frame range provided.
	 */
	public void updateCurveBoundsFrameRange(Range<Double> frameRange)
	{
		if (frameRange == null)
		{
			return;
		}
		
		settingBounds.set(true);
		for (ParameterCurve curve : curves)
		{
			if (curve.getCurveBounds() != null && 
					frameRange.getMinValue() == curve.getCurveBounds().getMinFrame() && 
					frameRange.getMaxValue() == curve.getCurveBounds().getMaxFrame())
			{
				continue;
			}
			
			curve.setCurveFrameBounds(frameRange.getMinValue(), frameRange.getMaxValue());
		}
		settingBounds.set(false);
		backingPanel.repaint();
	}
	
	/**
	 * Zoom all curves to fit their bounds in both the x (frame) and y (value) directions
	 */
	public void zoomToFit()
	{
		// Calculate the fitting bounds for each curve, and keep track of the frame range that encompasses all included curves
		settingBounds.set(true);
		Range<Double> frameRange = new Range<Double>(0d, 0d); 
		for (ParameterCurve curve : curves)
		{
			curve.setCurveBounds(null);
			frameRange = frameRange.union(curve.getCurveBounds().getFrameRange());
		}
		settingBounds.set(false);

		// Update each curve to use the calculated maximum frame range
		updateCurveBoundsFrameRange(frameRange);
	}

	/**
	 * Zoom all curves to fit their bounds in the x (frame) direction
	 */
	public void zoomToFitFrame()
	{
		settingBounds.set(true);
		Range<Double> frameRange = new Range<Double>(0d, 0d); 
		for (ParameterCurve curve : curves)
		{
			ParameterCurveBounds oldBounds = curve.getCurveBounds();
			curve.setCurveBounds(null);
			curve.setCurveValueBounds(oldBounds.getMinValue(), oldBounds.getMaxValue());
			frameRange = frameRange.union(curve.getCurveBounds().getFrameRange());
		}
		settingBounds.set(false);

		// Update each curve to use the calculated maximum frame range
		updateCurveBoundsFrameRange(frameRange);
	}

	/**
	 * Zoom all curves to fit their bounds in the y (value) direction
	 */
	public void zoomToFitValue()
	{
		settingBounds.set(true);
		for (ParameterCurve curve : curves)
		{
			ParameterCurveBounds oldBounds = curve.getCurveBounds();
			curve.setCurveBounds(null);
			curve.setCurveFrameBounds(oldBounds.getMinFrame(), oldBounds.getMaxFrame());
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
