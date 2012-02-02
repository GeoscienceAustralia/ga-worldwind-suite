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

public abstract class AbstractVolumeDataProvider extends AbstractDataProvider<VolumeLayer> implements VolumeDataProvider
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
	public TopBottomFastShape createLatitudeCurtain(int x)
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
	public TopBottomFastShape createLongitudeCurtain(int y)
	{
		List<Position> positions = new ArrayList<Position>();
		FloatBuffer textureCoordinateBuffer = BufferUtil.newFloatBuffer(ySize * 4);
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
