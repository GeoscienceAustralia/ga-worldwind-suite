package au.gov.ga.worldwind.animator.ui.parametereditor;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterEditorLinearMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterEditorLockedBezierMenuLabelKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getParameterEditorUnlockedBezierMenuLabelKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurveModel.ParameterCurveModelListener;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.common.ui.SelectableAction;
import au.gov.ga.worldwind.common.util.GridHelper;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.GridHelper.GridProperties;
import au.gov.ga.worldwind.common.util.LenientReadWriteLock;

/**
 * A class that draws the curve for a single parameter
 */
public class ParameterCurve extends JPanel implements ParameterCurveModelListener
{
	public static interface ParameterCurveListener
	{
		void curveBoundsChanged(ParameterCurve source, ParameterCurveBounds newBounds);
	}
	
	private static final long serialVersionUID = 20101102L;

	private static final RenderingHints RENDER_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	private static final int NODE_SHAPE_SIZE = 8; // Pixels
	private static final int GRID_SIZE = 40; // Pixels
	private static final double ZOOM_PERCENT_PER_WHEEL_CLICK = 0.1; // 10% zoom per wheel click
	private static final DecimalFormat VALUE_LABEL_FORMAT = new DecimalFormat("0.00000");
	
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
	private ReadWriteLock keyNodeMarkersLock = new LenientReadWriteLock();
	private AtomicBoolean markersDirty = new AtomicBoolean(true);
	
	// Listeners used to interact with the curve
	private NodeDragListener nodeMouseListener = new NodeDragListener();
	private PanZoomListener panZoomListener = new PanZoomListener();
	private MouseMoveListener mouseMoveListener = new MouseMoveListener();
	private PopupMenuListener popupListener = new PopupMenuListener();
	
	private GridProperties axisProperties;
	
	private List<ParameterCurveListener> curveListeners = new ArrayList<ParameterCurveListener>(); 
	
	private JPopupMenu popup;
	
	public ParameterCurve(Parameter parameter)
	{
		this(parameter, null);
	}
	
	public ParameterCurve(Parameter parameter, ParameterCurveBounds curveBounds)
	{
		this(new DefaultParameterCurveModel(parameter, Executors.newSingleThreadExecutor(new DaemonThreadFactory("Parameter Curve Updater"))));
		this.curveBounds = curveBounds;
	}

	public ParameterCurve(ParameterCurveModel curveModel)
	{
		Validate.notNull(curveModel, "A model is required");
		this.model = curveModel;
		model.addListener(this);
		
		setOpaque(true);
		setBackground(LAFConstants.getCurveEditorInactiveBackgroundColor());
		
		addMouseListener(nodeMouseListener);
		addMouseMotionListener(nodeMouseListener);
		
		addMouseListener(panZoomListener);
		addMouseMotionListener(panZoomListener);
		addMouseWheelListener(panZoomListener);
		
		addMouseMotionListener(mouseMoveListener);
		
		addComponentListener(new ComponentListener());
		
		addMouseListener(popupListener);
		
		popup = new JPopupMenu();
		popupListener.getConvertLockedBezierAction().addToPopupMenu(popup);
		popupListener.getConvertUnlockedBezierAction().addToPopupMenu(popup);
		popupListener.getConvertLinearAction().addToPopupMenu(popup);
	}
	
	/**
	 * Destroy's this curve. Once called, no further updates will take place for the curve.
	 */
	public void destroy()
	{
		model.destroy();
		this.curveListeners.clear();
		this.isDestroyed = true;
	}
	
	public void addCurveListener(ParameterCurveListener curveListener)
	{
		if (!curveListeners.contains(curveListener))
		{
			curveListeners.add(curveListener);
		}
	}
	
	/**
	 * Set the curve drawing bounds for this parameter curve
	 */
	public void setCurveBounds(ParameterCurveBounds curveBounds)
	{
		this.curveBounds = curveBounds;
		if (this.curveBounds == null)
		{
			calculateFittingBounds();
		}
		axisProperties = null;
		markersDirty.set(true);
		notifyCurveBoundsChanged();
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
			calculateFittingBounds();
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
			calculateFittingBounds();
		}
		setCurveBounds(new ParameterCurveBounds(curveBounds.getMinFrame(), curveBounds.getMaxFrame(), minValue, maxValue));
	}
	
	/**
	 * Calculates the bounds to use for the parameter that will fit the entirety of the parameter curve.
	 * <p/>
	 * The bounds will be [0, maxValue] - [lastFrame, minValue] 
	 */
	private void calculateFittingBounds()
	{
		try
		{
			boundsLock.lock();
			curveBounds = new ParameterCurveBounds(0, model.getMaxFrame(), model.getMinValue(), model.getMaxValue());
			
			// If the value is contstant, adjust the curve bounds so we don't end up with minValue == maxValue
			if (curveBounds.getValueWindow() == 0)
			{
				curveBounds = new ParameterCurveBounds(curveBounds.getMinFrame(), curveBounds.getMaxFrame(), curveBounds.getMinValue() - 1, curveBounds.getMaxValue() + 1);
			}
		}
		finally
		{
			boundsLock.unlock();
		}
	}
	
	private void notifyCurveBoundsChanged()
	{
		for (int i = curveListeners.size() - 1; i>= 0; i--)
		{
			curveListeners.get(i).curveBoundsChanged(this, curveBounds);
		}
	}
	
	public ParameterCurveBounds getCurveBounds()
	{
		return curveBounds;
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
				calculateFittingBounds();
			}
			
			if (curveBounds.getFrameWindow() == 0)
			{
				return;
			}
			
			paintActiveArea(g2);
			
			if (showAxis)
			{
				paintAxisLines(g2);
				paintParameterLabel(g2);
				paintCurrentFrameValueLabel(g2);
				paintCurrentMousePositionValueLabel(g2);
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
	
	/**
	 * Paint the axis grid lines for the 'value' axis
	 */
	private void paintAxisLines(Graphics2D g2)
	{
		// Don't generate axis lines if there is nothing to plot
		if (curveBounds.getFrameWindow() == 0 || curveBounds.getValueWindow() == 0)
		{
			return;
		}
		
		if (axisProperties == null)
		{
			recalculateAxisGrid();
		}
		
		double labelValue = axisProperties.getFirstGridLineValue();
		while (labelValue <= curveBounds.getMaxValue())
		{
			int gridY = (int)getScreenY(labelValue);
			g2.setColor(LAFConstants.getCurveEditorGridColor().darker());
			g2.drawString(getGridLabel(labelValue, axisProperties.getNumberDecimalPlaces()), 5, gridY);
			
			g2.setColor(LAFConstants.getCurveEditorGridColor());
			g2.draw(new Line2D.Double(0, gridY, getWidth(), gridY));
			
			labelValue += axisProperties.getValueChangePerGridLine();
		}
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
		if (curveBounds.getValueWindow() == 0)
		{
			return;
		}
		axisProperties = GridHelper.createGrid().ofSize(GRID_SIZE).toFitIn(getHeight()).forValueRange(curveBounds.getValueRange()).build();
	}
	
	/**
	 * Paint the parameter name in the top-right corner
	 */
	private void paintParameterLabel(Graphics2D g2)
	{
		String parameterLabel = model.getParameterLabel();
		
		FontMetrics fm = g2.getFontMetrics();
		Rectangle2D stringBounds = fm.getStringBounds(parameterLabel, g2);
		
		g2.setColor(LAFConstants.getCurveEditorGridColor().darker());
		g2.drawString(parameterLabel, (int)(getWidth() - stringBounds.getWidth()) - 10, (int)(stringBounds.getHeight() + 1));
	}
	
	/**
	 * Paint the value at the current frame on the bottom right
	 */
	private void paintCurrentFrameValueLabel(Graphics2D g2)
	{
		String valueString = VALUE_LABEL_FORMAT.format(model.getValueAtFrame(model.getCurrentFrame()));
		
		FontMetrics fm = g2.getFontMetrics();
		Rectangle2D stringBounds = fm.getStringBounds(valueString, g2);
		
		g2.setColor(LAFConstants.getCurveEditorCurrentFrameColor());
		g2.drawString(valueString, (int)(getWidth() - stringBounds.getWidth()) - 10, getHeight() - (int)(stringBounds.getHeight() + 1));
	}
	
	/**
	 * Paint the value at the current mouse position on the bottom right
	 */
	private void paintCurrentMousePositionValueLabel(Graphics2D g2)
	{
		Point mousePoint = getParent().getMousePosition(true);
		if (mousePoint == null)
		{
			return;
		}
		
		String valueString = VALUE_LABEL_FORMAT.format(model.getValueAtFrame((int)getCurveX(mousePoint.getX())));
		
		FontMetrics fm = g2.getFontMetrics();
		Rectangle2D stringBounds = fm.getStringBounds(valueString, g2);
		
		g2.setColor(LAFConstants.getCurveEditorCurrentMousePositionColor());
		g2.drawString(valueString, (int)(getWidth() - stringBounds.getWidth()) - 10, getHeight() - 2 * (int)(stringBounds.getHeight() + 1));
	}
	
	/**
	 * Paint the active area in a different colour to distinguish what is 'in' the animation.
	 */
	private void paintActiveArea(Graphics2D g2)
	{
		double x = getScreenX(0);
		double y = 0;
		double h = getHeight();
		double w = getScreenX(model.getAnimationFrameCount()) - x;
		
		Rectangle2D.Double activeAreaRectangle = new Rectangle2D.Double(x, y, w, h);
		
		g2.setColor(LAFConstants.getCurveEditorActiveBackgroundColor());
		g2.draw(activeAreaRectangle);
		g2.fill(activeAreaRectangle);
	}

	/**
	 * Paint the curve itself using the backing model
	 */
	private void paintParameterCurve(Graphics2D g2)
	{
		g2.setColor(Color.GREEN); // TODO: Make dynamic
		
		// Draw the curve within [0, framecount], cropped by the curve bounds
		int startFrame = curveBounds.getMinFrame() < 0 ? 0 : (int)curveBounds.getMinFrame();
		int lastFrame = curveBounds.getMaxFrame() > model.getAnimationFrameCount() ? model.getAnimationFrameCount() : (int)curveBounds.getMaxFrame();
		
		for (int frame = startFrame; frame <= lastFrame; frame++)
		{
			double x1 = getScreenX(frame);
			double x2 = getScreenX(frame + 1);
			double y1 = getScreenY(model.getValueAtFrame(frame));
			double y2 = getScreenY(model.getValueAtFrame(frame + 1));
			
			g2.draw(new Line2D.Double(x1, y1, x2, y2));
		}
		
		// Draw the parts of the curve outside [0, framecount] (if any) as a single line
		if (curveBounds.getMinFrame() < 0)
		{
			double x1 = 0;
			double x2 = getScreenX(0);
			double y = getScreenY(model.getValueAtFrame(0));
			g2.draw(new Line2D.Double(x1, y, x2, y));
		}
		if (curveBounds.getMaxFrame() > model.getAnimationFrameCount())
		{
			double x1 = getScreenX(model.getAnimationFrameCount());
			double x2 = getWidth();
			double y = getScreenY(model.getValueAtFrame(model.getAnimationFrameCount()));
			g2.draw(new Line2D.Double(x1, y, x2, y));
		}
	}

	/**
	 * Paint the key frame node markers using the local list of markers
	 */
	private void paintKeyFrameNodes(Graphics2D g2)
	{
		try
		{
			if (keyNodeMarkers.isEmpty() || markersDirty.get())
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
	 * Maps the provided screen point to a curve point
	 */
	ParameterCurvePoint getCurvePoint(Point screenPoint)
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
		getParent().repaint();
	}
	
	/**
	 * Updates the positions of the key node markers. This can occur due to a mouse drag,
	 * or because of a detected change in the curve.
	 */
	private void updateKeyNodeMarkers()
	{
		if (isDestroyed)
		{
			return;
		}
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
			markersDirty.set(false);
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
	 * @return The parameter this curve is representing
	 */
	public Parameter getParameter()
	{
		return model.getParameter();
	}
	
	/**
	 * A mouse listener used to control panning and zooming via the middle mouse button
	 */
	private class PanZoomListener extends MouseAdapter
	{
		private ParameterCurvePoint lastMousePoint = null;
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			double zoomer = isZoomIn(e) ? 1 + ZOOM_PERCENT_PER_WHEEL_CLICK : 1 - ZOOM_PERCENT_PER_WHEEL_CLICK;
			double zoomAmount = Math.pow(zoomer, Math.abs(e.getWheelRotation()));
			
			// Contract/expand the bounds based on the zoom amount
			double deltaY = zoomValueAxis(e) ? ((curveBounds.getValueWindow() * zoomAmount) - curveBounds.getValueWindow()) / 2 : 0;
			double deltaX = zoomFrameAxis(e) ? ((curveBounds.getFrameWindow() * zoomAmount) - curveBounds.getFrameWindow()) / 2 : 0;
			
			ParameterCurvePoint originalMousePosition = getCurvePoint(e.getPoint());
			
			// Resize the curve bounds to the new zoomed bounds
			resizeCurveBounds(deltaX, deltaY);
			
			ParameterCurvePoint newMousePosition = getCurvePoint(e.getPoint());
			
			ParameterCurvePoint curveTranslation = originalMousePosition.subtract(newMousePosition);
			
			// Reposition the curve bounds so the mouse remains in the same place in curve space 
			translateCurveBounds(curveTranslation.frame, curveTranslation.value);
			
			repaintCurve();
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (!isPanClick(e))
			{
				return;
			}
			updateLastMousePoint(getCurvePoint(e.getPoint()));
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (lastMousePoint == null)
			{
				return;
			}

			ParameterCurvePoint translation = lastMousePoint.subtract(getCurvePoint(e.getPoint()));
			translateCurveBounds(translation.frame, translation.value);
			
			updateLastMousePoint(getCurvePoint(e.getPoint()));
			
			repaintCurve();
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (!isPanClick(e))
			{
				return;
			}
			updateLastMousePoint(null);
		}

		/** The frame axis is zoomed if CTRL or no action key is pressed */
		private boolean zoomFrameAxis(MouseWheelEvent e)
		{
			return e.isControlDown() || !(e.isAltDown() || e.isAltGraphDown() || e.isControlDown() || e.isMetaDown() || e.isShiftDown());
		}
		
		/** The value axis is zoomed if SHIFT or no action key is pressed */
		private boolean zoomValueAxis(MouseWheelEvent e)
		{
			return e.isShiftDown() || !(e.isAltDown() || e.isAltGraphDown() || e.isControlDown() || e.isMetaDown() || e.isShiftDown());
		}

		private boolean isZoomIn(MouseWheelEvent e)
		{
			// Zoom in for positive direction, out for negative
			return e.getWheelRotation() > 0;
		}
		
		private void resizeCurveBounds(double deltaX, double deltaY)
		{
			setCurveBounds(curveBounds.getMinFrame() - deltaX, curveBounds.getMaxFrame() + deltaX, 
						   curveBounds.getMinValue() - deltaY, curveBounds.getMaxValue() + deltaY);
		}
		
		private void translateCurveBounds(double deltaX, double deltaY)
		{
			setCurveBounds(curveBounds.getMinFrame() + deltaX, curveBounds.getMaxFrame() + deltaX, 
						   curveBounds.getMinValue() + deltaY, curveBounds.getMaxValue() + deltaY);
		}
		
		private boolean isPanClick(MouseEvent e)
		{
			return e.getButton() == MouseEvent.BUTTON2;
		}
		
		private void updateLastMousePoint(ParameterCurvePoint curvePoint)
		{
			lastMousePoint = curvePoint;
		}
		
		private void repaintCurve()
		{
			updateKeyNodeMarkers();
			getParent().repaint();
		}
	}
	
	/**
	 * A mouse listener used to detect and process keyframe node selections and drags
	 */
	private class NodeDragListener extends MouseAdapter
	{
		private KeyNodeMarker lastSelected;
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (!isNodeDragClick(e))
			{
				return;
			}
			updateLastSelected(e);
			getParent().repaint();
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
			getParent().repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (!isNodeDragClick(e))
			{
				return;
			}
			clearLastSelected();
			getParent().repaint();
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
		
		private boolean isNodeDragClick(MouseEvent e)
		{
			return e.getButton() == MouseEvent.BUTTON1;
		}
	}
	
	/**
	 * A mouse listener that triggers a repaint on mouse move/drag 
	 * (ensures the current mouse / current frame lines are redrawn appropriately)
	 */
	private class MouseMoveListener extends MouseAdapter
	{
		@Override
		public void mouseMoved(MouseEvent e)
		{
			getParent().repaint();
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			getParent().repaint();
		}
	}
	
	/**
	 * A mouse listener that triggers the popup menu on a selected node 
	 */
	private class PopupMenuListener extends MouseAdapter
	{
		private SelectableAction convertLockedBezierAction;
		private SelectableAction convertUnlockedBezierAction;
		private SelectableAction convertLinearAction;
		
		private KeyNodeMarker selectedMarker;
		
		public PopupMenuListener()
		{
			convertLockedBezierAction = new SelectableAction(getMessage(getParameterEditorLockedBezierMenuLabelKey()), null, true);
			convertLockedBezierAction.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					selectedMarker.makeLockedBezier();
				}
			});
			
			convertUnlockedBezierAction = new SelectableAction(getMessage(getParameterEditorUnlockedBezierMenuLabelKey()), null, true);
			convertUnlockedBezierAction.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					selectedMarker.makeUnlockedBezier();
				}
			});
			
			convertLinearAction = new SelectableAction(getMessage(getParameterEditorLinearMenuLabelKey()), null, true);
			convertLinearAction.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					selectedMarker.makeLinear();
				}
			});
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e)
		{
			if (!e.isPopupTrigger())
			{
				return;
			}
			
			selectedMarker = getKeyNodeMarker(e.getPoint());
			if (selectedMarker == null)
			{
				return;
			}
			
			updatePopupActions();
			
			showPopupMenu(e.getX(), e.getY());
		}

		private void updatePopupActions()
		{
			convertLockedBezierAction.setSelected(selectedMarker.curveNode.isBezier() && selectedMarker.curveNode.isLocked());
			convertLockedBezierAction.setEnabled(!convertLockedBezierAction.isSelected());
			
			convertUnlockedBezierAction.setSelected(selectedMarker.curveNode.isBezier() && !selectedMarker.curveNode.isLocked());
			convertUnlockedBezierAction.setEnabled(!convertUnlockedBezierAction.isSelected());
			
			convertLinearAction.setSelected(selectedMarker.curveNode.isLinear());
			convertLinearAction.setEnabled(!convertLinearAction.isSelected());
		}

		private void showPopupMenu(int x, int y)
		{
			popup.show(ParameterCurve.this, x, y);
		}
		
		public SelectableAction getConvertLockedBezierAction()
		{
			return convertLockedBezierAction;
		}
		
		public SelectableAction getConvertUnlockedBezierAction()
		{
			return convertUnlockedBezierAction;
		}
		
		public SelectableAction getConvertLinearAction()
		{
			return convertLinearAction;
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
			if (!isVisible())
			{
				return;
			}
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
			else
			{
				inHandle = null;
				inValueJoiner = null;
			}
			
			if (curveNode.isBezier() && curveNode.getOutPoint() != null)
			{
				outHandle = createNodeShape(curveNode.getOutPoint());
				valueOutJoiner = new Line2D.Double(getScreenPoint(curveNode.getOutPoint()), getScreenPoint(curveNode.getValuePoint()));
			}
			else
			{
				outHandle = null;
				valueOutJoiner = null;
			}
		}
		
		void paint(Graphics2D g2)
		{
			g2.setColor(getHandleColor(valueHandleSelected()));
			g2.fill(valueHandle);
			
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
		
		public void makeLinear()
		{
			curveNode.convertToLinear();
			updateMarker(curveNode);
		}
		
		public void makeLockedBezier()
		{
			curveNode.convertToLockedBezier();
			updateMarker(curveNode);
		}
		
		public void makeUnlockedBezier()
		{
			curveNode.convertToUnlockedBezier();
			updateMarker(curveNode);
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
