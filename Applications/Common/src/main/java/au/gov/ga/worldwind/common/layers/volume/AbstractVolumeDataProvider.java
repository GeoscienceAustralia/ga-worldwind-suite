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
package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.worldwind.common.util.FastShape;

import com.sun.opengl.util.BufferUtil;

/**
 * Abstract implementation of the {@link VolumeDataProvider} interface. Provides
 * getter methods to the required fields, as well as curtain and surface shape
 * calculation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractVolumeDataProvider extends AbstractDataProvider<VolumeLayer> implements
		VolumeDataProvider
{
	protected int xSize;
	protected int ySize;
	protected int zSize;
	protected Sector sector = null;
	protected double depth;
	protected double top;
	protected float noDataValue;

	protected List<Position> positions;
	protected FloatBuffer data;

	@Override
	public int getXSize()
	{
		return xSize;
	}

	@Override
	public int getYSize()
	{
		return ySize;
	}

	@Override
	public int getZSize()
	{
		return zSize;
	}

	@Override
	public double getDepth()
	{
		return depth;
	}

	@Override
	public double getTop()
	{
		return top;
	}

	@Override
	public float getValue(int x, int y, int z)
	{
		return data.get(x + y * xSize + z * xSize * ySize);
	}

	@Override
	public float getNoDataValue()
	{
		return noDataValue;
	}

	@Override
	public Sector getSector()
	{
		return sector;
	}

	@Override
	public FastShape createHorizontalSurface(float maxVariance, Rectangle rectangle)
	{
		BinaryTriangleTree btt = new BinaryTriangleTree(positions, xSize, ySize);
		return btt.buildMeshFromCenter(maxVariance, rectangle);
	}

	@Override
	public TopBottomFastShape createLongitudeCurtain(int x)
	{
		List<Position> positions = new ArrayList<Position>();
		FloatBuffer textureCoordinateBuffer = BufferUtil.newFloatBuffer(ySize * 4);
		for (int y = 0; y < ySize; y++)
		{
			Position position = this.positions.get(x + y * xSize);
			TopBottomPosition top =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, false);
			TopBottomPosition bottom =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, true);
			positions.add(top);
			positions.add(bottom);
			float u = y / (float) Math.max(1, ySize - 1);
			textureCoordinateBuffer.put(u).put(0);
			textureCoordinateBuffer.put(u).put(1);
		}
		TopBottomFastShape shape = new TopBottomFastShape(positions, GL.GL_TRIANGLE_STRIP);
		shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		return shape;
	}

	@Override
	public TopBottomFastShape createLatitudeCurtain(int y)
	{
		List<Position> positions = new ArrayList<Position>();
		FloatBuffer textureCoordinateBuffer = BufferUtil.newFloatBuffer(xSize * 4);
		for (int x = 0; x < xSize; x++)
		{
			Position position = this.positions.get(x + y * xSize);
			TopBottomPosition top =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, false);
			TopBottomPosition bottom =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, true);
			positions.add(top);
			positions.add(bottom);
			float u = x / (float) Math.max(1, xSize - 1);
			textureCoordinateBuffer.put(u).put(0);
			textureCoordinateBuffer.put(u).put(1);
		}
		TopBottomFastShape shape = new TopBottomFastShape(positions, GL.GL_TRIANGLE_STRIP);
		shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		return shape;
	}
}
