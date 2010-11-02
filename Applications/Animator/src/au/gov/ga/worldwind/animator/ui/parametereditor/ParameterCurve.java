package au.gov.ga.worldwind.animator.ui.parametereditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurveModel.ParameterCurveModelListener;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A class that draws the curve for a single parameter
 */
public class ParameterCurve extends JComponent implements ParameterCurveModelListener
{
	private static final long serialVersionUID = 20101102L;

	private static final RenderingHints RENDER_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new DaemonThreadFactory("Parameter Curve Updater"));
	
	private static final int Y_PADDING = 10;
	
	/** The model backing this component */
	private ParameterCurveModel model;
	
	/** 
	 * The bounds used to limit which part of the curve to draw. Can be used to implement zooming etc.
	 * <p/>
	 * If <code>null</code>, will calculate the bounds to be the extent of the parameter
	 */
	private ParameterCurveBounds curveBounds;
	private Lock boundsLock = new ReentrantLock();
	
	/** Whether to show the axis for this curve */
	private boolean showAxis = false;
	
	private boolean isDestroyed = false;
	
	public ParameterCurve(Parameter parameter)
	{
		this(parameter, null);
	}
	
	public ParameterCurve(Parameter parameter, ParameterCurveBounds curveBounds)
	{
		Validate.notNull(parameter, "A parameter is required");
		
		model = new DefaultParameterCurveModel(parameter, THREAD_POOL);
		model.addListener(this);
		
		this.curveBounds = curveBounds;
	}

	/**
	 * Destroy's this curve. Once called, no further updates will take place for the curve.
	 */
	public void destroy()
	{
		model.destroy();
		this.isDestroyed = true;
	}
	
	/**
	 * Set the curve drawing bounds for this parameter curve
	 */
	public void setCurveBounds(ParameterCurveBounds curveBounds)
	{
		this.curveBounds = curveBounds;
		if (this.curveBounds == null)
		{
			calculateDefaultBounds();
		}
	}

	/**
	 * Set the frame bounds for this parameter curve. The value bounds will be left untouched.
	 */
	public void setCurveFrameBounds(int minFrame, int maxFrame)
	{
		if (curveBounds == null)
		{
			calculateDefaultBounds();
		}
		setCurveBounds(new ParameterCurveBounds(minFrame, maxFrame, curveBounds.getMinValue(), curveBounds.getMaxValue()));
	}
	
	/**
	 * Set the value bounds for this parameter curve. The frame bounds will be left untouched.
	 */
	public void setCurveValueBounds(double minValue, double maxValue)
	{
		if (curveBounds == null)
		{
			calculateDefaultBounds();
		}
		setCurveBounds(new ParameterCurveBounds(curveBounds.getMinFrame(), curveBounds.getMaxFrame(), minValue, maxValue));
	}
	
	/**
	 * Calculates the default bounds to use for the parameter.
	 * <p/>
	 * The default bounds [0, maxValue] - [lastFrame, minValue] 
	 */
	private void calculateDefaultBounds()
	{
		try
		{
			boundsLock.lock();
			curveBounds = new ParameterCurveBounds(0, model.getMaxFrame(), model.getMinValue(), model.getMaxValue());
		}
		finally
		{
			boundsLock.unlock();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		if (isDestroyed)
		{
			return;
		}
		
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(RENDER_HINT);

		try
		{
			boundsLock.lock();
			model.lock();
			if (curveBounds == null)
			{
				calculateDefaultBounds();
			}
			
			if (showAxis)
			{
				paintAxisLines(g2);
			}
			
			paintParameterCurve(g2);
			paintKeyFrameNodes(g2);
		}
		finally
		{
			boundsLock.unlock();
			model.unlock();
		}
	}
	
	private void paintAxisLines(Graphics2D g2)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void paintParameterCurve(Graphics2D g2)
	{
		g2.setColor(Color.GREEN); // TODO: Make dynamic
		for (int frame = curveBounds.getMinFrame(); frame < curveBounds.getMaxFrame(); frame++)
		{
			double x1 = getX(frame);
			double x2 = getX(frame + 1);
			double y1 = getY(model.getValueAtFrame(frame));
			double y2 = getY(model.getValueAtFrame(frame + 1));
			
			g2.draw(new Line2D.Double(x1, y1, x2, y2));
		}
	}

	private void paintKeyFrameNodes(Graphics2D g2)
	{
		g2.setColor(LAFConstants.getCurveKeyHandleColor()); // TODO: Make dynamic
		for (ParameterCurveKeyNode keyFrameNode : model.getKeyFrameNodes())
		{
			Rectangle2D.Double nodeShape = createNodeShape(getX(keyFrameNode.getValuePoint().frame), getY(keyFrameNode.getValuePoint().value));
			g2.draw(nodeShape);
		}
	}

	/**
	 * Create a node shape around the centroid [x,y]
	 */
	private Rectangle2D.Double createNodeShape(double x, double y)
	{
		return new Rectangle2D.Double(x - 2d, y - 2d, 4, 4);
	}

	/**
	 * Maps the provided frame number to a screen x-coordinate
	 */
	private double getX(int frame)
	{
		return (double)getWidth() * (double)(frame - curveBounds.getMinFrame()) / (double)(curveBounds.getMaxFrame() - curveBounds.getMinFrame());
	}
	
	/**
	 * Maps the provided parameter value to a screen y-coordinate
	 */
	private double getY(double parameterValue)
	{
		double h = (double)getHeight() - Y_PADDING;
		return h - (h * (parameterValue - curveBounds.getMinValue()) / (curveBounds.getMaxValue() - curveBounds.getMinValue())) + ((double)Y_PADDING / 2);
	}

	public void setShowAxis(boolean showAxis)
	{
		this.showAxis = showAxis;
	}
	
	public void setModel(ParameterCurveModel model)
	{
		this.model = model;
	}

	@Override
	public void curveChanged()
	{
		repaint();
	}
}
