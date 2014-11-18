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
package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Color;
import java.nio.DoubleBuffer;

import javax.media.opengl.GL2;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.common.util.HSLColor;

import com.jogamp.common.nio.Buffers;

/**
 * Base class for camera position paths
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractCameraPositionPath implements Renderable
{
	private int frameCount;

	// Buffers used to draw the eye path
	private DoubleBuffer pathVertexFrontBuffer;
	private DoubleBuffer pathVertexBackBuffer;
	private DoubleBuffer pathColourFrontBuffer;
	private DoubleBuffer pathColourBackBuffer;
	private Object pathBufferLock = new Object();
	private Vec4 pathReferenceCenterFront;
	private Vec4 pathReferenceCenterBack;

	/** The animation whose camera path is to be displayed on this layer */
	private Animation animation;

	boolean enableDepthTesting = true;

	public AbstractCameraPositionPath(Animation animation)
	{
		setAnimation(animation);
	}

	public void setAnimation(Animation animation)
	{
		this.animation = animation;
	}

	public void recalulatePath()
	{
		populatePathBuffers();
		swapBuffers();
	}

	public void resetPath()
	{
		frameCount = animation.getFrameCount();
		this.pathVertexFrontBuffer = Buffers.newDirectDoubleBuffer(frameCount * 3);
		this.pathVertexBackBuffer = Buffers.newDirectDoubleBuffer(frameCount * 3);
		this.pathColourFrontBuffer = Buffers.newDirectDoubleBuffer(frameCount * 3);
		this.pathColourBackBuffer = Buffers.newDirectDoubleBuffer(frameCount * 3);
	}

	@Override
	public final void render(DrawContext dc)
	{
		drawPath(dc);
	}

	/**
	 * Draw the camera path
	 */
	private void drawPath(DrawContext dc)
	{
		if (frameCount <= 0 || dc.isPickingMode())
		{
			return;
		}

		GL2 gl = dc.getGL().getGL2();
		OGLStackHandler stack = new OGLStackHandler();
		stack.pushAttrib(gl, GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT | GL2.GL_LINE_BIT | GL2.GL_HINT_BIT | GL2.GL_LIGHTING_BIT
				| GL2.GL_DEPTH_BUFFER_BIT);
		stack.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
		boolean popRefCenter = false;
		try
		{
			synchronized (pathBufferLock)
			{
				if (pathReferenceCenterFront != null)
				{
					dc.getView().pushReferenceCenter(dc, pathReferenceCenterFront);
					popRefCenter = true;
				}

				// Points are drawn over the line to prevent gaps forming when 
				// antialiasing and smoothing is applied to the line
				gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
				gl.glShadeModel(GL2.GL_SMOOTH);
				gl.glEnable(GL2.GL_LINE_SMOOTH);
				gl.glEnable(GL2.GL_POINT_SMOOTH);
				gl.glEnable(GL2.GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
				gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
				gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
				gl.glLineWidth(2.0f);
				gl.glPointSize(2.0f);
				int numberOfPointsInPath = animation.getFrameOfLastKeyFrame() - animation.getFrameOfFirstKeyFrame() + 1;

				//don't draw lines with less than 2 points
				if (numberOfPointsInPath <= 1)
				{
					return;
				}


				if (enableDepthTesting)
				{
					gl.glEnable(GL2.GL_DEPTH_TEST);
				}
				else
				{
					gl.glDisable(GL2.GL_DEPTH_TEST);
				}

				//use flat shading, so that each segment is colored with the previous first vertex's color
				//instead of interpolating between the two vertices colors
				gl.glShadeModel(GL2.GL_FLAT);

				gl.glColorPointer(3, GL2.GL_DOUBLE, 0, pathColourFrontBuffer);
				gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, pathVertexFrontBuffer);

				// Draw a smooth line without modifying the depth buffer, filling gaps with points
				gl.glDepthMask(false);
				gl.glDrawArrays(GL2.GL_LINE_STRIP, 0, numberOfPointsInPath);
				gl.glDrawArrays(GL2.GL_POINTS, 0, numberOfPointsInPath);
				gl.glDepthMask(true);

				// Now redraw the line, writing to the depth buffer, to ensure line looks correct with markers
				gl.glDrawArrays(GL2.GL_LINE_STRIP, 0, numberOfPointsInPath);
			}
		}
		finally
		{
			if (popRefCenter)
			{
				dc.getView().popReferenceCenter(dc);
			}
			stack.pop(gl);
		}
	}

	/**
	 * Populate the update vertex buffers with the coordinates of the eye
	 * location at each frame in the animation.
	 * <p/>
	 * Vertex buffer has size <code>3*animation.getlastFrame()</code>, with
	 * <code>[x,y,z]</code> stored at
	 * <code>[3*frame, 3*frame+1, 3*frame+2]</code>
	 * <p/>
	 * Colour buffer has size <code>3*animation.getLastFrame()</code> with
	 * colours ramping through the HSV colour space based on the rate of change
	 * of the camera eye position. Colours <code>[r,g,b]</code> are stored at
	 * <code>[3*frame, 3*frame+1, 3*frame+2]</code>.
	 */
	private void populatePathBuffers()
	{
		pathReferenceCenterBack = null;

		int firstFrame = animation.getFrameOfFirstKeyFrame();
		int lastFrame = animation.getFrameOfLastKeyFrame();

		double[] deltas = new double[lastFrame - firstFrame + 1];
		double minDelta = Double.MAX_VALUE;
		double maxDelta = 0d;

		pathVertexBackBuffer.rewind();

		Position[] pathPositions = getPathPositions(firstFrame, lastFrame);

		Position previousPathPosition = null;
		for (int i = 0; i < pathPositions.length; i++)
		{
			Position currentPathPosition = pathPositions[i];

			// Populate the vertex buffer
			Vec4 eyeVector = animation.getView().getGlobe().computePointFromPosition(currentPathPosition);
			if (pathReferenceCenterBack == null)
			{
				pathReferenceCenterBack = eyeVector; // Choose the first point in the path to be the reference point
			}
			pathVertexBackBuffer.put(eyeVector.x - pathReferenceCenterBack.x);
			pathVertexBackBuffer.put(eyeVector.y - pathReferenceCenterBack.y);
			pathVertexBackBuffer.put(eyeVector.z - pathReferenceCenterBack.z);

			// Populate the delta array
			if (previousPathPosition != null)
			{
				double positionDelta = calculateDelta(currentPathPosition, previousPathPosition);
				deltas[i] = positionDelta;
				maxDelta = Math.max(maxDelta, positionDelta);
				minDelta = Math.min(minDelta, positionDelta);
			}
			previousPathPosition = currentPathPosition;
		}

		//first wasn't set in loop above, so just copy from second:
		if (deltas.length > 1)
		{
			deltas[0] = deltas[1];
		}

		populatePathColourBufferFromDeltas(deltas, minDelta, maxDelta);

		pathVertexBackBuffer.rewind();
	}

	/**
	 * @return the path positions for this path between the start and end frames
	 */
	protected abstract Position[] getPathPositions(int startFrame, int endFrame);

	/**
	 * @return The delta of the current and previous position
	 */
	private double calculateDelta(Position currentPosition, Position previousPosition)
	{
		Vec4 current = animation.getView().getGlobe().computePointFromPosition(currentPosition);
		Vec4 previous = animation.getView().getGlobe().computePointFromPosition(previousPosition);
		return Math.abs(current.distanceTo3(previous));
	}

	private void populatePathColourBufferFromDeltas(double[] deltas, double minDelta, double maxDelta)
	{
		double deltaWindow = maxDelta - minDelta;
		if (deltaWindow < 1)
		{
			deltaWindow = 1;
		}

		// Use the HSL colour ramp to indicate magnitude of deltas
		HSLColor hslColor = new HSLColor(0, 80, 50);
		Color pathColor = null;

		pathColourBackBuffer.rewind();
		for (double delta : deltas)
		{
			float normalizedDelta = (float) ((delta - minDelta) / deltaWindow);
			pathColor = hslColor.adjustHue((1f - normalizedDelta) * 240f);
			pathColourBackBuffer.put((double) pathColor.getRed() / 255d);
			pathColourBackBuffer.put((double) pathColor.getGreen() / 255d);
			pathColourBackBuffer.put((double) pathColor.getBlue() / 255d);
		}
		pathColourBackBuffer.rewind();
	}

	/**
	 * @return Whether the provided key frame is one that should be included in
	 *         this camera path
	 */
	protected abstract boolean isPathFrame(KeyFrame keyFrame);

	/**
	 * Swap the front and back buffers, giving access to the newly updated data
	 * for drawing
	 */
	private void swapBuffers()
	{
		synchronized (pathBufferLock)
		{
			DoubleBuffer tmp = pathVertexFrontBuffer;
			pathVertexFrontBuffer = pathVertexBackBuffer;
			pathVertexBackBuffer = tmp;
			pathVertexFrontBuffer.rewind();

			tmp = pathColourFrontBuffer;
			pathColourFrontBuffer = pathColourBackBuffer;
			pathColourBackBuffer = tmp;
			pathColourFrontBuffer.rewind();
			
			pathReferenceCenterFront = pathReferenceCenterBack;
		}
	}

	public Animation getAnimation()
	{
		return animation;
	}

	public void setEnableDepthTesting(boolean enableDepthTesting)
	{
		this.enableDepthTesting = enableDepthTesting;
	}
}
