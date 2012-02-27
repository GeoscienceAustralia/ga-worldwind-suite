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
package au.gov.ga.worldwind.animator.layers.sky;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * Layer that renders a skybox in the background.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Skybox extends RenderableLayer
{
	private boolean inited = false;
	private Texture[] skybox = new Texture[6];
	private static final String[] keys = new String[6];
	private static final String[] id = { "front", "left", "back", "right", "top", "bottom" };
	private String skyboxDir = "/images/skybox/";
	private String extension = ".png";

	static
	{
		for (int i = 0; i < keys.length; i++)
		{
			keys[i] = Skybox.class.getName() + "." + i;
		}
	}

	private void initializeTextures(DrawContext dc)
	{
		for (int i = 0; i < keys.length; i++)
		{
			//skybox[i] = dc.getTextureCache().get(keys[i]);
			if (skybox[i] == null)
			{
				try
				{
					String filename = skyboxDir + id[i] + extension;
					InputStream stream = this.getClass().getResourceAsStream(filename);
					if (stream == null)
					{
						File file = new File(filename);
						if (file.exists())
						{
							stream = new FileInputStream(file);
						}
					}

					skybox[i] = TextureIO.newTexture(stream, true, null);
					skybox[i].bind();
					//dc.getTextureCache().put(keys[i], skybox[i]);
				}
				catch (IOException e)
				{
					String msg = Logging.getMessage("layers.IOExceptionDuringInitialization");
					Logging.logger().severe(msg);
					throw new WWRuntimeException(msg, e);
				}

				GL gl = dc.getGL();
				gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
				// Enable texture anisotropy, improves "tilted" world map quality.
				/*int[] maxAnisotropy = new int[1];
				gl
						.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
								maxAnisotropy, 0);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
						maxAnisotropy[0]);*/
			}
		}
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		GL gl = dc.getGL();

		if (!inited)
		{
			initializeTextures(dc);
			inited = true;
		}

		//set up projection matrix
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		dc.getGLU().gluPerspective(dc.getView().getFieldOfView().degrees,
				dc.getView().getViewport().getWidth() / dc.getView().getViewport().getHeight(), 0.1, 5.0);

		//set up modelview matrix
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		Angle heading = Angle.ZERO;
		Angle pitch = Angle.ZERO;
		Angle roll = Angle.ZERO;
		if (dc.getView() instanceof OrbitView)
		{
			heading = ((OrbitView) dc.getView()).getHeading();
			pitch = ((OrbitView) dc.getView()).getPitch();
		}
		if (dc.getView() instanceof BasicView)
		{
			roll = ((BasicView) dc.getView()).getRoll();
		}

		Matrix transform = Matrix.IDENTITY;
		transform = transform.multiply(Matrix.fromRotationY(heading.multiply(-1.0)));
		transform = transform.multiply(Matrix.fromRotationX(pitch.addDegrees(90)));
		transform = transform.multiply(Matrix.fromRotationZ(roll)); //TODO check

		Vec4 up = Vec4.UNIT_Y; //TODO recalculate if forward is parallel to up
		Vec4 forward = Vec4.UNIT_Z.transformBy4(transform);
		dc.getGLU().gluLookAt(0, 0, 0, forward.x, forward.y, forward.z, up.x, up.y, up.z);

		// Enable/Disable features
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_BLEND);

		// Just in case we set all vertices to white.
		gl.glColor4f(1, 1, 1, 1);
		// Draw
		drawSkybox(gl);

		// Restore enable bits and matrix
		gl.glPopAttrib();
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
	}

	private void drawSkybox(GL gl)
	{
		// Render the front quad
		skybox[0].bind();
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(1, 1);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
			gl.glTexCoord2f(0, 1);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);
			gl.glTexCoord2f(1, 0);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
		}
		gl.glEnd();

		// Render the left quad
		skybox[1].bind();
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(1, 1);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glTexCoord2f(0, 1);
			gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glTexCoord2f(1, 0);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);
		}
		gl.glEnd();

		// Render the back quad
		skybox[2].bind();
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(1, 1);
			gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(0, 1);
			gl.glVertex3f(0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(0.5f, 0.5f, 0.5f);
			gl.glTexCoord2f(1, 0);
			gl.glVertex3f(-0.5f, 0.5f, 0.5f);
		}
		gl.glEnd();

		// Render the right quad
		skybox[3].bind();
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(1, 1);
			gl.glVertex3f(0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(0, 1);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
			gl.glTexCoord2f(1, 0);
			gl.glVertex3f(0.5f, 0.5f, 0.5f);
		}
		gl.glEnd();

		// Render the top quad
		skybox[4].bind();
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(0, 1);
			gl.glVertex3f(-0.5f, 0.5f, -0.5f);
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(-0.5f, 0.5f, 0.5f);
			gl.glTexCoord2f(1, 0);
			gl.glVertex3f(0.5f, 0.5f, 0.5f);
			gl.glTexCoord2f(1, 1);
			gl.glVertex3f(0.5f, 0.5f, -0.5f);
		}
		gl.glEnd();

		// Render the bottom quad
		skybox[5].bind();
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(-0.5f, -0.5f, -0.5f);
			gl.glTexCoord2f(0, 1);
			gl.glVertex3f(-0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(1, 1);
			gl.glVertex3f(0.5f, -0.5f, 0.5f);
			gl.glTexCoord2f(1, 0);
			gl.glVertex3f(0.5f, -0.5f, -0.5f);
		}
		gl.glEnd();
	}
}
