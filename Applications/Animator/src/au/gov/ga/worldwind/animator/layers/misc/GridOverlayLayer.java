package au.gov.ga.worldwind.animator.layers.misc;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import au.gov.ga.worldwind.animator.util.Validate;

import com.sun.opengl.util.BufferUtil;

/**
 * A layer that renders a grid overlay on top of the viewport window
 * <p/>
 * The grid is centred in the x- and y-dimensions, and grid spacing can be configured.
 */
public class GridOverlayLayer extends AbstractLayer
{
	private static final float LINE_THICKNESS = 0.8f;
	private static final double DEFAULT_OPACITY = 0.4;
	
	// Grid colour
	private static final float[] DEFAULT_GRID_COLOR = Color.WHITE.getColorComponents(null);
	private float[] gridColor = DEFAULT_GRID_COLOR;
	
	// Grid spacing (in percent)
	private static final double DEFAULT_VERTICAL_SPACING = 0.2;
	private static final double DEFAULT_HORIZONTAL_SPACING = 0.2;
	private double verticalSpacing = DEFAULT_VERTICAL_SPACING;
	private double horizontalSpacing = DEFAULT_HORIZONTAL_SPACING;
	
	// Used to detect a change in the viewport
	private int viewportWidth = -1;
	private int viewportHeight = -1;
	
	// Buffer that holds the grid line end points, for drawing
	private DoubleBuffer gridBuffer;
	private int numberOfBufferEntries;
	
	// Grid overlay implemented as an ordered renderable with depth 0
	private OrderedRenderable gridOverlay = new OrderedRenderable()
	{
		@Override
		public void render(DrawContext dc)
		{
			drawGrid(dc);
		}
		
		@Override
		public void pick(DrawContext dc, Point pickPoint)
		{
			// Picking not implemented
		}
		
		@Override
		public double getDistanceFromEye()
		{
			return 0;
		}
	};
	
	private boolean centreGrid = true;
	
	/**
	 * Create a new grid overlay that is centred with a default grid spacing.
	 */
	public GridOverlayLayer()
	{
		setOpacity(DEFAULT_OPACITY);
	}
	
	/**
	 * Create a new grid overlay that is configured according to the provided parameters
	 * 
	 * @param centred Whether or not to centre the grid in the x- and y-axes.
	 * @param verticalSpacing The vertical spacing to apply to the grid (in percent, range <code>(0,1)</code>)
	 * @param horizontalSpacing The vertical spacing to apply to the grid (in percent, range <code>(0,1)</code>)
	 */
	public GridOverlayLayer(boolean centred, double verticalSpacing, double horizontalSpacing)
	{
		this();
		Validate.isTrue(verticalSpacing > 0 && verticalSpacing < 1, "Vertical spacing must be in the range (0,1)");
		Validate.isTrue(horizontalSpacing > 0 && horizontalSpacing < 1, "Horizontal spacing must be in the range (0,1)");
		
		centreGrid = centred;
		this.verticalSpacing = verticalSpacing;
		this.horizontalSpacing = horizontalSpacing;
	}
	
	@Override
	protected void doRender(DrawContext dc)
	{
		gridOverlay.render(dc);
	}
	
	/**
	 * Draw the grid overlay to the draw context
	 */
	private void drawGrid(DrawContext dc)
	{
		GL gl = dc.getGL();
		GLU glu = dc.getGLU();
		
		boolean clientAttribsPushed = false;
		boolean attribsPushed = false;
		boolean projectionPushed = false;
		boolean modelviewPushed = false;
		
		Rectangle viewport = dc.getView().getViewport();
		
		try
		{
			recalculateGrid(viewport);
			
			gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
			clientAttribsPushed = true;
			
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
			attribsPushed = true;
			
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			glu.gluOrtho2D(0, viewport.getWidth(), 0, viewport.getHeight());
			projectionPushed = true;
			
			gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
			modelviewPushed = true;
            gl.glLoadIdentity();
            
            gl.glColor4d(gridColor[0], gridColor[1], gridColor[2], getOpacity());
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
            
            gl.glLineWidth(LINE_THICKNESS);
            gl.glVertexPointer(2, GL.GL_DOUBLE, 0, gridBuffer.rewind());
            gl.glDrawArrays(GL.GL_LINES, 0, numberOfBufferEntries);
		}
		finally
		{
			if (modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
			if (projectionPushed)
			{
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glPopMatrix();
			}
			if (attribsPushed)
			{
				gl.glPopAttrib();
			}
			if (clientAttribsPushed)
			{
				gl.glPopClientAttrib();
			}
		}
	}

	/**
	 * Recalculate the grid lines if the viewport dimensions have changed since the last calculation
	 */
	private void recalculateGrid(Rectangle viewport)
	{
		if (!viewportHasChanged(viewport))
		{
			return;
		}
		viewportWidth = (int) viewport.getWidth();
		viewportHeight = (int) viewport.getHeight();

		List<Point> tempGridBuffer = new ArrayList<Point>();
		
		calculateVerticalGridLines(viewport, tempGridBuffer);
		calculateHorizontalGridLines(viewport, tempGridBuffer);
		
		numberOfBufferEntries = tempGridBuffer.size() * 2;
		gridBuffer = BufferUtil.newDoubleBuffer(numberOfBufferEntries);
		for (Point gridPoint : tempGridBuffer)
		{
			gridBuffer.put(gridPoint.x);
			gridBuffer.put(gridPoint.y);
		}
		gridBuffer.rewind();
	}

	/**
	 * Add the horizontal grid lines to the grid
	 */
	private void calculateHorizontalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		if (centreGrid)
		{
			calculateCentredHorizontalGridLines(viewport, gridBuffer);
		}
		else
		{
			calculateNonCentredHorizontalGridLines(viewport, gridBuffer);
		}
	}
	
	private void calculateCentredHorizontalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		addHorizontalLineAt((int)viewport.getCenterY(), gridBuffer);
		
		int deltaY = (int)(viewportHeight * getVerticalSpacing());
		
		// Add lines below the centre
		int lineY = (int)viewport.getCenterY();
		while (lineY > 0)
		{
			lineY -= deltaY;
			if (lineY > 0)
			{
				addHorizontalLineAt(lineY, gridBuffer);
			}
		}
		
		// Add lines above the centre
		lineY = (int)viewport.getCenterY();
		while (lineY < viewportHeight)
		{
			lineY += deltaY;
			if (lineY < viewportHeight)
			{
				addHorizontalLineAt(lineY, gridBuffer);
			}
		}
	}
	
	private void calculateNonCentredHorizontalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		int deltaY = (int)(viewportHeight * getVerticalSpacing());
		
		int lineY = 0;
		while (lineY <= viewportHeight)
		{
			lineY += deltaY;
			addHorizontalLineAt(lineY, gridBuffer);
		}
	}
	
	private void addHorizontalLineAt(int y, List<Point> gridBuffer)
	{
		gridBuffer.add(new Point(0, y));
		gridBuffer.add(new Point(viewportWidth, y));
	}
	
	/**
	 * Add vertical grid lines to the grid
	 */
	private void calculateVerticalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		if (centreGrid)
		{
			calculateCentredVerticalGridLines(viewport, gridBuffer);
		}
		else
		{
			calculateNonCentredVerticalGridLines(viewport, gridBuffer);
		}
	}
	
	private void calculateCentredVerticalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		addVerticalLineAt((int)viewport.getCenterX(), gridBuffer);
		
		int deltaX = (int)(viewportWidth * getHorizontalSpacing());
		
		// Add lines to the left of centre
		int lineX = (int)viewport.getCenterX();
		while (lineX > 0)
		{
			lineX -= deltaX;
			if (lineX > 0)
			{
				addVerticalLineAt(lineX, gridBuffer);
			}
		}
		
		// Add lines to the right of centre
		lineX = (int)viewport.getCenterX();
		while (lineX < viewportWidth)
		{
			lineX += deltaX;
			if (lineX < viewportWidth)
			{
				addVerticalLineAt(lineX, gridBuffer);
			}
		}
	}
	
	private void calculateNonCentredVerticalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		int deltaX = (int)(viewportWidth * getHorizontalSpacing());
		
		int lineX = 0;
		while (lineX <= viewportWidth)
		{
			lineX += deltaX;
			addVerticalLineAt(lineX, gridBuffer);
		}
	}
	
	private void addVerticalLineAt(int x, List<Point> gridBuffer)
	{
		gridBuffer.add(new Point(x, 0));
		gridBuffer.add(new Point(x, viewportHeight));
	}

	public double getVerticalSpacing()
	{
		return verticalSpacing;
	}

	public void setVerticalSpacing(double verticalSpacing)
	{
		this.verticalSpacing = verticalSpacing;
		markChanged();
	}

	public double getHorizontalSpacing()
	{
		return horizontalSpacing;
	}

	public void setHorizontalSpacing(double horizontalSpacing)
	{
		this.horizontalSpacing = horizontalSpacing;
		markChanged();
	}
	
	public Color getGridColor()
	{
		return new Color(gridColor[0], gridColor[1], gridColor[2]);
	}
	
	public void setGridColor(Color color)
	{
		if (color != null)
		{
			this.gridColor = color.getColorComponents(null);
		}
	}
	
	private void markChanged()
	{
		// hijack the viewport check to mark the grid as changed
		viewportWidth = -1;
	}
	
	private boolean viewportHasChanged(Rectangle viewport)
	{
		return (int)viewport.getWidth() != viewportWidth || 
			   (int)viewport.getHeight() != viewportHeight;
	}
}
