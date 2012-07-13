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
package au.gov.ga.worldwind.common.render;

import java.awt.Dimension;

import javax.media.opengl.GL;

/**
 * Class used by the {@link FrameBuffer} to generate/store the texture.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FrameBufferTexture
{
	private int id = 0;

	private int target = GL.GL_TEXTURE_2D;
	private int minificationFilter = GL.GL_LINEAR;
	private int magnificationFilter = GL.GL_LINEAR;
	private int internalFormat = GL.GL_RGBA8;
	private int format = GL.GL_RGBA;
	private int type = GL.GL_UNSIGNED_BYTE;

	protected void create(GL gl, Dimension dimensions)
	{
		delete(gl);

		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		if (textures[0] <= 0)
		{
			throw new IllegalStateException("Error generating texture for frame buffer");
		}
		id = textures[0];

		gl.glBindTexture(getTarget(), id);

		gl.glTexParameteri(getTarget(), GL.GL_TEXTURE_MAG_FILTER, getMagnificationFilter());
		gl.glTexParameteri(getTarget(), GL.GL_TEXTURE_MIN_FILTER, getMinificationFilter());
		gl.glTexParameteri(getTarget(), GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
		gl.glTexParameteri(getTarget(), GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

		gl.glTexImage2D(getTarget(), 0, getInternalFormat(), dimensions.width, dimensions.height, 0, getFormat(),
				getType(), null);
		gl.glBindTexture(getTarget(), 0);
	}

	protected void delete(GL gl)
	{
		if (isCreated())
		{
			gl.glDeleteTextures(1, new int[] { id }, 0);
			id = 0;
		}
	}

	public boolean isCreated()
	{
		return id > 0;
	}

	public int getId()
	{
		return id;
	}

	public int getTarget()
	{
		return target;
	}

	public void setTarget(int target)
	{
		this.target = target;
	}

	public int getMinificationFilter()
	{
		return minificationFilter;
	}

	public void setMinificationFilter(int minificationFilter)
	{
		this.minificationFilter = minificationFilter;
	}

	public int getMagnificationFilter()
	{
		return magnificationFilter;
	}

	public void setMagnificationFilter(int magnificationFilter)
	{
		this.magnificationFilter = magnificationFilter;
	}

	public int getInternalFormat()
	{
		return internalFormat;
	}

	public void setInternalFormat(int internalFormat)
	{
		this.internalFormat = internalFormat;
	}

	public int getFormat()
	{
		return format;
	}

	public void setFormat(int format)
	{
		this.format = format;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}
}
