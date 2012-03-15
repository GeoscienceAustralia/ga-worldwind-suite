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
package au.gov.ga.worldwind.animator.layers.misc;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import au.gov.ga.worldwind.common.util.Validate;

import com.sun.opengl.util.BufferUtil;

/**
 * A layer that renders a grid overlay on top of the viewport window
 * <p/>
 * The grid is centred in the x- and y-dimensions, and grid spacing can be
 * configured.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
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

	// Used to detect changes in the viewport
	private Rectangle lastViewport = null;

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
	 * Create a new grid overlay that is configured according to the provided
	 * parameters
	 * 
	 * @param centred
	 *            Whether or not to centre the grid in the x- and y-axes.
	 * @param verticalSpacing
	 *            The vertical spacing to apply to the grid (in percent, range
	 *            <code>(0,1)</code>)
	 * @param horizontalSpacing
	 *            The vertical spacing to apply to the grid (in percent, range
	 *            <code>(0,1)</code>)
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

		OGLStackHandler stack = new OGLStackHandler();
		Rectangle viewport = dc.getView().getViewport();

		try
		{
			recalculateGrid(viewport);

			stack.pushAttrib(gl, GL.GL_CURRENT_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_LINE_BIT);
			stack.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);

			stack.pushProjection(gl);
			gl.glLoadIdentity();
			glu.gluOrtho2D(0, viewport.getWidth(), 0, viewport.getHeight());

			stack.pushModelview(gl);
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
			stack.pop(gl);
		}
	}

	/**
	 * Recalculate the grid lines if the viewport dimensions have changed since
	 * the last calculation
	 */
	private void recalculateGrid(Rectangle viewport)
	{
		if (this.lastViewport != null && this.lastViewport.equals(viewport))
		{
			//viewport hasn't changed since last time, so don't need to recalculate
			return;
		}
		this.lastViewport = viewport;

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
		int centerY = (int) viewport.getCenterY();
		addHorizontalLineAt(centerY, viewport, gridBuffer);

		int deltaY = (int) (viewport.height * getVerticalSpacing());

		// Add lines below the centre
		for (int lineY = centerY - deltaY; lineY > viewport.y; lineY -= deltaY)
		{
			addHorizontalLineAt(lineY, viewport, gridBuffer);
		}

		// Add lines above the centre
		for (int lineY = centerY + deltaY; lineY < viewport.y + viewport.height; lineY += deltaY)
		{
			addHorizontalLineAt(lineY, viewport, gridBuffer);
		}
	}

	private void calculateNonCentredHorizontalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		int deltaY = (int) (viewport.height * getVerticalSpacing());

		for (int lineY = viewport.y; lineY <= viewport.y + viewport.height; lineY += deltaY)
		{
			addHorizontalLineAt(lineY, viewport, gridBuffer);
		}
	}

	private void addHorizontalLineAt(int y, Rectangle viewport, List<Point> gridBuffer)
	{
		gridBuffer.add(new Point(viewport.x, y));
		gridBuffer.add(new Point(viewport.x + viewport.width, y));
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
		int centerX = (int) viewport.getCenterX() - 1; //move to the left 1 pixel so it lines up with the crosshair
		addVerticalLineAt(centerX, viewport, gridBuffer);

		int deltaX = (int) (viewport.width * getHorizontalSpacing());

		// Add lines to the left of centre
		for (int lineX = centerX - deltaX; lineX > viewport.x; lineX -= deltaX)
		{
			addVerticalLineAt(lineX, viewport, gridBuffer);
		}

		// Add lines to the right of centre
		for (int lineX = centerX + deltaX; lineX < viewport.x + viewport.width; lineX += deltaX)
		{
			addVerticalLineAt(lineX, viewport, gridBuffer);
		}
	}

	private void calculateNonCentredVerticalGridLines(Rectangle viewport, List<Point> gridBuffer)
	{
		int deltaX = (int) (viewport.width * getHorizontalSpacing());
		for (int lineX = viewport.x; lineX <= viewport.x + viewport.width; lineX += deltaX)
		{
			addVerticalLineAt(lineX, viewport, gridBuffer);
		}
	}

	private void addVerticalLineAt(int x, Rectangle viewport, List<Point> gridBuffer)
	{
		gridBuffer.add(new Point(x, viewport.y));
		gridBuffer.add(new Point(x, viewport.y + viewport.height));
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
		this.lastViewport = null;
	}
}
