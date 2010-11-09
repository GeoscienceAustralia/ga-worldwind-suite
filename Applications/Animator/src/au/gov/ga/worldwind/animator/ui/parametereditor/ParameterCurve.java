package au.gov.ga.worldwind.animator.ui.parametereditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JPanel;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurveModel.ParameterCurveModelListener;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.util.GridHelper;
import au.gov.ga.worldwind.common.util.GridHelper.GridProperties;
import au.gov.ga.worldwind.common.util.Range;

/**
 * A class that draws the curve for a single parameter
 */
public class ParameterCurve extends JPanel implements ParameterCurveModelListener
{
	private static final long serialVersionUID = 20101102L;

	private static final RenderingHints RENDER_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new DaemonThreadFactory("Parameter Curve Updater"));
	
	private static final int NODE_SHAPE_SIZE = 8; // Pixels
	private static final Range<Integer> GRID_SIZE = new Range<Integer>(20, 40); // Pixels
	
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
	private boolean showAxis = true;
	
	/** Whether this curve has been destroyed */
	private boolean isDestroyed = false;
	
	// Holds the calculated key node markers for the curve
	private List<KeyNodeMarker> keyNodeMarkers = new ArrayList<KeyNodeMarker>();
	private ReadWriteLock keyNodeMarkersLock = new ReentrantReadWriteLock(true);
	
	private NodeMouseListener nodeMouseListener = new NodeMouseListener();

	private GridProperties axisProperties;
	
	public ParameterCurve(Parameter parameter)
	{
		this(parameter, null);
	}
	
	public ParameterCurve(Parameter parameter, ParameterCurveBounds curveBounds)
	{
		this(new DefaultParameterCurveModel(parameter, THREAD_POOL));
		this.curveBounds = curveBounds;
	}

	public ParameterCurve(ParameterCurveModel curveModel)
	{
		Validate.notNull(curveModel, "A model is required");
		this.model = curveModel;
		model.addListener(this);
		
		setOpaque(true);
		setBackground(LAFConstants.getCurveEditorBackgroundColor());
		addMouseListener(nodeMouseListener);
		addMouseMotionListener(nodeMouseListener);
		addComponentListener(new ComponentListener());
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
		axisProperties = null;
	}

	/**
	 * Set the curve drawing bounds for this parameter curve
	 */
	public void setCurveBounds(double minFrame, double maxFrame, double minValue, double maxValue)
	{
		setCurveBounds(new ParameterCurveBounds(minFrame, maxFrame, minValue, maxValue));
	}
	
	/**
	 * Set the frame bounds for this parameter curve. The value bounds will be left untouched.
	 */
	public void setCurveFrameBounds(double minFrame, double maxFrame)
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
		if (axisProperties == null)
		{
			recalculateAxisGrid();
		}
		
		int gridY = getHeight() - axisProperties.getFirstGridLineLocation();
		int numDecimalPlaces = calculateNumDecimalPlacesForGridLabel();
		double labelValue = axisProperties.getFirstGridLineValue();
		while (gridY > 0)
		{
			g2.setColor(LAFConstants.getCurveEditorGridColor().darker());
			g2.drawString(getGridLabel(labelValue, numDecimalPlaces), 5, gridY);
			
			g2.setColor(LAFConstants.getCurveEditorGridColor());
			g2.draw(new Line2D.Double(0, gridY, getWidth(), gridY));
			
			gridY -= axisProperties.getGridSpacing();
			labelValue += axisProperties.getValueChangePerGridLine();
		}
	}

	private int calculateNumDecimalPlacesForGridLabel()
	{
		int numDecimalPlaces = 0;
		
		String strideString = String.valueOf(axisProperties.getValueChangePerGridLine());
		if (strideString.indexOf('.') > -1)
		{
			numDecimalPlaces = strideString.substring(strideString.indexOf('.')+1).length();
		}
		
		return numDecimalPlaces;
	}
	
	private String getGridLabel(double labelValue, int numDecimalPlaces)
	{
		String formatString = "0";
		if (numDecimalPlaces > 0)
		{
			formatString += ".";
			for (int i = 0; i < numDecimalPlaces; i++)
			{
				formatString += "0";
			}
		}
		return new DecimalFormat(formatString).format(labelValue);
	}

	private void recalculateAxisGrid()
	{
		axisProperties = GridHelper.createGrid().ofSize(GRID_SIZE).toFitIn(getHeight()).forValueRange(curveBounds.getValueRange()).build();
		System.out.println("Axis properties: " + axisProperties);
	}
	
	private void paintParameterCurve(Graphics2D g2)
	{
		g2.setColor(Color.GREEN); // TODO: Make dynamic
		for (int frame = (int)curveBounds.getMinFrame(); frame < curveBounds.getMaxFrame(); frame++)
		{
			double x1 = getScreenX(frame);
			double x2 = getScreenX(frame + 1);
			double y1 = getScreenY(model.getValueAtFrame(frame));
			double y2 = getScreenY(model.getValueAtFrame(frame + 1));
			
			g2.draw(new Line2D.Double(x1, y1, x2, y2));
		}
	}

	private void paintKeyFrameNodes(Graphics2D g2)
	{
		try
		{
			if (keyNodeMarkers.isEmpty())
			{
				updateKeyNodeMarkers();
			}
			keyNodeMarkersLock.readLock().lock();
			for (KeyNodeMarker marker : keyNodeMarkers)
			{
				marker.paint(g2);
			}
		}
		finally
		{
			keyNodeMarkersLock.readLock().unlock();
		}
	}

	/**
	 * Maps the provided curve point to a screen point
	 */
	Point2D.Double getScreenPoint(ParameterCurvePoint p)
	{
		double screenX = getScreenX(p.frame);
		double screenY = getScreenY(p.value);
		return new Point2D.Double(screenX, screenY);
	}
	
	/**
	 * Maps the provided frame number to a screen x-coordinate
	 */
	double getScreenX(double frame)
	{
		return (double)getWidth() * (double)(frame - curveBounds.getMinFrame()) / curveBounds.getFrameWindow();
	}
	
	/**
	 * Maps the provided parameter value to a screen y-coordinate
	 */
	double getScreenY(double parameterValue)
	{
		double h = (double)getHeight();
		return h - (h * (parameterValue - curveBounds.getMinValue()) / curveBounds.getValueWindow());
	}

	/**
	 * Maps the provided screen point to a curve point
	 */
	ParameterCurvePoint getCurvePoint(Point2D.Double screenPoint)
	{
		return new ParameterCurvePoint(getCurveX(screenPoint.x), getCurveY(screenPoint.y));
	}
	
	/**
	 * Maps the provided screen x-coordinate to a curve frame coordinate
	 */
	double getCurveX(double x)
	{
		return curveBounds.getMinFrame() + ((x / (double)getWidth()) * curveBounds.getFrameWindow());
	}
	
	/**
	 * Maps the provided screen y-coordinate to a curve value coordinate
	 */
	double getCurveY(double y)
	{
		return curveBounds.getMinValue() + (((getHeight() - y) / (double)getHeight()) * curveBounds.getValueWindow());
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
		updateKeyNodeMarkers();
		repaint();
	}
	
	/**
	 * Updates the positions of the key node markers. This can occur due to a mouse drag,
	 * or because of a detected change in the curve.
	 */
	private void updateKeyNodeMarkers()
	{
		try
		{
			keyNodeMarkersLock.writeLock().lock();
			
			// Update into a new list, adding nodes for any inserted key frames as we go 
			List<KeyNodeMarker> tmpMarkers = new ArrayList<KeyNodeMarker>();
			for (ParameterCurveKeyNode node : model.getKeyFrameNodes())
			{
				KeyNodeMarker markerNode = new KeyNodeMarker(node);
				if (keyNodeMarkers.contains(markerNode))
				{
					KeyNodeMarker existingNode = keyNodeMarkers.get(keyNodeMarkers.indexOf(markerNode));
					existingNode.updateMarker(node);
					tmpMarkers.add(existingNode);
				}
				else
				{
					tmpMarkers.add(markerNode);
				}
			}
			keyNodeMarkers.clear();
			keyNodeMarkers.addAll(tmpMarkers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			keyNodeMarkersLock.writeLock().unlock();
		}
	}
	
	/**
	 * @return The key node marker corresponding to the screen point P (if any). Will use the {@link KeyNodeMarker#isInMarker(Point)} method
	 * to check for the value, in and out handle areas.
	 */
	private KeyNodeMarker getKeyNodeMarker(Point p)
	{
		if (p == null)
		{
			return null;
		}
		try
		{
			keyNodeMarkersLock.readLock().lock();
			for (KeyNodeMarker keyNodeMarker : keyNodeMarkers)
			{
				if (keyNodeMarker.isInMarker(p))
				{
					return keyNodeMarker;
				}
			}
			return null;
		}
		finally
		{
			keyNodeMarkersLock.readLock().unlock();
		}
	}
	
	/**
	 * A mouse listener used to detect and process keyframe node selections and drags
	 */
	private class NodeMouseListener extends MouseAdapter
	{
		private KeyNodeMarker lastSelected;
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			updateLastSelected(e);
			repaint();
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			KeyNodeMarker marker = lastSelected;
			if (marker == null)
			{
				return;
			}
			marker.applyHandleMove(e.getPoint());
			repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			clearLastSelected();
			repaint();
		}
		
		private void updateLastSelected(MouseEvent e)
		{
			KeyNodeMarker newLastSelected = getKeyNodeMarker(e.getPoint());
			if (newLastSelected != lastSelected && lastSelected != null)
			{
				lastSelected.deselect();
			}
			if (newLastSelected != null)
			{
				newLastSelected.setSelectedHandle(e.getPoint());
			}
			lastSelected = newLastSelected;
		}
		
		private void clearLastSelected()
		{
			if (lastSelected != null)
			{
				lastSelected.deselect();
			}
			lastSelected = null;
		}
	}
	
	/**
	 * A component listener used to add behaviour to resize events etc.
	 */
	private class ComponentListener extends ComponentAdapter
	{
		@Override
		public void componentResized(ComponentEvent e)
		{
			updateKeyNodeMarkers();
			axisProperties = null;
			repaint();
		}
	}
	
	/** An enumeration of key node handles */
	private static enum KeyNodeHandleSelection
	{
		IN, VALUE, OUT, NONE;
	}
	
	/**
	 * A marker used to draw key frame nodes, and to respond to mouse events etc.
	 */
	private class KeyNodeMarker 
	{
		private ParameterCurveKeyNode curveNode;
		
		private Shape inHandle;
		private Shape valueHandle;
		private Shape outHandle;

		private KeyNodeHandleSelection selection = KeyNodeHandleSelection.NONE;
		
		private Line2D.Double inValueJoiner;
		private Line2D.Double valueOutJoiner;
		
		KeyNodeMarker(ParameterCurveKeyNode curveNode)
		{
			updateMarker(curveNode);
		}
		
		void updateMarker(ParameterCurveKeyNode curveNode)
		{
			Validate.notNull(curveNode, "A node is required");
			this.curveNode = curveNode;
			
			valueHandle = createNodeShape(curveNode.getValuePoint());
			if (curveNode.isBezier() && curveNode.getInPoint() != null)
			{
				inHandle = createNodeShape(curveNode.getInPoint());
				inValueJoiner = new Line2D.Double(getScreenPoint(curveNode.getInPoint()), getScreenPoint(curveNode.getValuePoint()));
			}
			if (curveNode.isBezier() && curveNode.getOutPoint() != null)
			{
				outHandle = createNodeShape(curveNode.getOutPoint());
				valueOutJoiner = new Line2D.Double(getScreenPoint(curveNode.getOutPoint()), getScreenPoint(curveNode.getValuePoint()));
			}
		}
		
		void paint(Graphics2D g2)
		{
			g2.setColor(getHandleColor(valueHandleSelected()));
			g2.draw(valueHandle);
			
			if (inHandle != null)
			{
				g2.setColor(getHandleColor(inHandleSelected()));
				g2.draw(inHandle);
				g2.setColor(LAFConstants.getCurveHandleJoinerColor());
				g2.draw(inValueJoiner);
			}
			
			if (outHandle != null)
			{
				g2.setColor(getHandleColor(outHandleSelected()));
				g2.draw(outHandle);
				g2.setColor(LAFConstants.getCurveHandleJoinerColor());
				g2.draw(valueOutJoiner);
			}
		}
		
		private Color getHandleColor(boolean selected)
		{
			return selected ? LAFConstants.getCurveKeyHandleColor().brighter().brighter() : LAFConstants.getCurveKeyHandleColor();
		}
		
		/**
		 * Create a node shape around the provided curve point
		 */
		private Rectangle2D.Double createNodeShape(ParameterCurvePoint p)
		{
			double x = getScreenX(p.frame);
			double y = getScreenY(p.value);
			double offset = NODE_SHAPE_SIZE / 2d;
			return new Rectangle2D.Double(x - offset, y - offset, NODE_SHAPE_SIZE, NODE_SHAPE_SIZE);
		}
		
		/**
		 * Applies a handle move, taking the handle located at lastPoint and shifting it to the new point
		 */
		public void applyHandleMove(Point point)
		{
			// TODO: Apply delta X
			Double lastScreenPoint = getScreenPoint(getSelectedHandleCurvePoint());
			if (lastScreenPoint == null)
			{
				return;
			}
			
			int deltaY = (int)(lastScreenPoint.y - point.y);
			int deltaX = (int)(lastScreenPoint.x - point.x);
			
			if (valueHandleSelected())
			{
				// Only deltaY applied to the value handle
				if (deltaY == 0)
				{
					return;
				}
				curveNode.applyValueChange(getCurvePoint(new Point2D.Double(lastScreenPoint.x, lastScreenPoint.y - deltaY)));
			}
			else if (inHandleSelected() || outHandleSelected())
			{
				if (deltaY == 0 && deltaX == 0)
				{
					return;
				}
				ParameterCurvePoint curvePoint = getCurvePoint(new Point2D.Double(lastScreenPoint.x - deltaX, lastScreenPoint.y - deltaY));
				if (inHandleSelected())
				{
					curveNode.applyInChange(curvePoint);
				}
				else
				{
					curveNode.applyOutChange(curvePoint);
				}
			}
			updateMarker(curveNode);
		}
		
		private ParameterCurvePoint getSelectedHandleCurvePoint()
		{
			switch (selection)
			{
				case VALUE:
					return curveNode.getValuePoint();
				case IN:
					return curveNode.getInPoint();
				case OUT:
					return curveNode.getOutPoint();
				default:
					return null;
			}
		}

		/**
		 * @return Whether the provided point lies within one of the handles of this marker
		 */
		boolean isInMarker(Point point)
		{
			return valueHandle.contains(point) || (inHandle != null && inHandle.contains(point)) || (outHandle != null && outHandle.contains(point));
		}
		
		public void setSelectedHandle(Point point)
		{
			if (valueHandle.contains(point))
			{
				selection = KeyNodeHandleSelection.VALUE;
			}
			else if (inHandle != null && inHandle.contains(point))
			{
				selection = KeyNodeHandleSelection.IN;
			}
			else if (outHandle != null && outHandle.contains(point))
			{
				selection = KeyNodeHandleSelection.OUT;
			}
			else
			{
				selection = KeyNodeHandleSelection.NONE;
			}
		}
		
		public void deselect()
		{
			selection = KeyNodeHandleSelection.NONE;
		}
		
		private boolean valueHandleSelected()
		{
			return selection == KeyNodeHandleSelection.VALUE;
		}
		
		private boolean inHandleSelected()
		{
			return selection == KeyNodeHandleSelection.IN;
		}
		
		private boolean outHandleSelected()
		{
			return selection == KeyNodeHandleSelection.OUT;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}
			
			if (!(obj instanceof KeyNodeMarker))
			{
				return false;
			}
			
			return ((KeyNodeMarker)obj).curveNode.equals(this.curveNode);
		}
		
		@Override
		public int hashCode()
		{
			return curveNode.hashCode();
		}
	}
}
