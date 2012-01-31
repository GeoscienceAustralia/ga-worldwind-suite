package au.gov.ga.worldwind.common.layers.volume.btt;

import gov.nasa.worldwind.geom.Position;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.Validate;

import com.sun.opengl.util.BufferUtil;

public class BinaryTriangleTree
{
	private final List<Position> positions;
	private final int width;
	private final int height;

	public BinaryTriangleTree(List<Position> positions, int width, int height)
	{
		Validate.isTrue(positions.size() == width * height, "Positions list count doesn't match provided width/height");

		this.positions = positions;
		this.width = width;
		this.height = height;
	}

	public FastShape buildMesh(float maxVariance)
	{
		return buildMesh(maxVariance, new Rectangle(0, 0, width, height));
	}

	public FastShape buildMeshFromCenter(float maxVariance)
	{
		return buildMeshFromCenter(maxVariance, new Rectangle(0, 0, width, height));
	}

	public FastShape buildMesh(float maxVariance, Rectangle rectangle)
	{
		if (maxVariance <= 0)
		{
			return buildFullMesh(rectangle);
		}

		List<BTTTriangle> triangles = new ArrayList<BTTTriangle>();
		buildMesh(maxVariance, rectangle.x, rectangle.y, rectangle.width, rectangle.height, false, false, triangles);
		return buildFastShape(triangles);
	}

	public FastShape buildMeshFromCenter(float maxVariance, Rectangle rectangle)
	{
		if (maxVariance <= 0)
		{
			return buildFullMesh(rectangle);
		}

		List<BTTTriangle> triangles = new ArrayList<BTTTriangle>();

		int centerWidth = nextLowestPowerOf2Plus1(rectangle.width);
		int centerHeight = nextLowestPowerOf2Plus1(rectangle.height);
		int centerXOffset = (rectangle.width - centerWidth) / 2;
		int centerYOffset = (rectangle.height - centerHeight) / 2;
		int remainingWidth = rectangle.width - centerWidth - centerXOffset;
		int remainingHeight = rectangle.height - centerHeight - centerYOffset;

		buildMesh(maxVariance, rectangle.x + centerXOffset, rectangle.y + centerYOffset, centerWidth, centerHeight,
				false, false, triangles);
		buildMesh(maxVariance, rectangle.x, rectangle.y, centerWidth + centerXOffset, centerYOffset + 1, true, true,
				triangles);
		buildMesh(maxVariance, rectangle.x, rectangle.y + centerYOffset, centerXOffset + 1, rectangle.height
				- centerYOffset, true, false, triangles);
		buildMesh(maxVariance, rectangle.x + centerWidth + centerXOffset - 1, rectangle.y, remainingWidth + 1,
				centerHeight + centerYOffset, false, true, triangles);
		buildMesh(maxVariance, rectangle.x + centerXOffset, rectangle.y + centerHeight + centerYOffset - 1,
				rectangle.width - centerXOffset, remainingHeight + 1, false, false, triangles);

		return buildFastShape(triangles);
	}

	public FastShape buildFullMesh(Rectangle rect)
	{
		List<Position> positions;
		if (rect.x == 0 && rect.y == 0 && rect.width == width && rect.height == height)
		{
			//if using the entire area, then use the original positions list
			positions = this.positions;
		}
		else
		{
			//otherwise, create a new sub-list
			positions = new ArrayList<Position>(rect.width * rect.height);
			for (int y = rect.y; y < rect.height + rect.y; y++)
			{
				for (int x = rect.x; x < rect.width + rect.x; x++)
				{
					positions.add(this.positions.get(x + y * width));
				}
			}
		}

		int indexCount = 2 * rect.width * (rect.height - 1);
		IntBuffer indices = BufferUtil.newIntBuffer(indexCount);

		int k = 0;
		for (int y = 0; y < rect.height - 1; y++)
		{
			if (y % 2 == 0) //even
			{
				for (int x = 0; x < rect.width; x++, k++)
				{
					indices.put(k);
					indices.put(k + rect.width);
				}
			}
			else
			{
				k += rect.width - 1;
				for (int x = 0; x < rect.width; x++, k--)
				{
					indices.put(k + rect.width);
					indices.put(k);
				}
				k += rect.width + 1;
			}
		}

		FloatBuffer textureCoordinateBuffer = BufferUtil.newFloatBuffer(positions.size() * 2);
		for (int y = 0; y < rect.height; y++)
		{
			for (int x = 0; x < rect.width; x++)
			{
				textureCoordinateBuffer.put((x + rect.x) / (float) (width - 1))
						.put((y + rect.y) / (float) (height - 1));
			}
		}

		FastShape shape = new FastShape(positions, indices, GL.GL_TRIANGLE_STRIP);
		shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		return shape;
	}

	protected void buildMesh(float maxVariance, int x, int y, int width, int height, boolean reverseX,
			boolean reverseY, List<BTTTriangle> triangles)
	{
		int yStart = y;
		int remainingHeight = height;
		while (remainingHeight > 1)
		{
			int xStart = x;
			int remainingWidth = width;
			int currentHeight = nextLowestPowerOf2Plus1(Math.min(remainingWidth, remainingHeight));
			while (remainingWidth > 1)
			{
				int currentWidth = nextLowestPowerOf2Plus1(Math.min(remainingWidth, remainingHeight));
				for (int yOffset = 0; yOffset < currentHeight - 1; yOffset += currentWidth - 1)
				{
					int tx = reverseX ? width - xStart - currentWidth : xStart;
					int ty = reverseY ? height - yStart - yOffset - currentWidth : yStart + yOffset;
					buildTree(maxVariance, tx, ty, currentWidth, triangles);
				}
				remainingWidth -= currentWidth - 1;
				xStart += currentWidth - 1;
			}
			remainingHeight -= currentHeight - 1;
			yStart += currentHeight - 1;
		}
	}

	protected void buildTree(float maxVariance, int x, int y, int size, List<BTTTriangle> triangles)
	{
		/*
		 *  left
		 *     +---+
		 *     |\  |
		 *     | \ |
		 *     |  \|
		 *     +---+
		 *  apex   right
		 */

		int apex1 = x + y * width, left1 = x + (y + size - 1) * width, right1 = (x + size - 1) + y * width;
		BTTTriangle t1 = new BTTTriangle(apex1, left1, right1);

		int apex2 = (x + size - 1) + (y + size - 1) * width, left2 = (x + size - 1) + y * width, right2 =
				x + (y + size - 1) * width;
		BTTTriangle t2 = new BTTTriangle(apex2, left2, right2);

		t1.bottomNeighbour = t2;
		t2.bottomNeighbour = t1;

		buildFace(maxVariance, t1);
		buildFace(maxVariance, t2);

		addLeavesToTriangleList(t1, triangles);
		addLeavesToTriangleList(t2, triangles);
	}

	protected void buildFace(float maxVariance, BTTTriangle t)
	{
		if (t.leftChild != null)
		{
			buildFace(maxVariance, t.leftChild);
			buildFace(maxVariance, t.rightChild);
		}
		else
		{
			boolean atLowestLevel =
					Math.abs(t.apexIndex - t.leftIndex) == 1 || Math.abs(t.apexIndex - t.rightIndex) == 1;
			if (!atLowestLevel)
			{
				float variance = calculateVariance(t.apexIndex, t.leftIndex, t.rightIndex);
				if (variance >= maxVariance)
				{
					trySplitFace(t);
					buildFace(maxVariance, t.leftChild);
					buildFace(maxVariance, t.rightChild);
				}
			}
		}
	}

	protected void trySplitFace(BTTTriangle t)
	{
		if (t.bottomNeighbour != null)
		{
			if (t.bottomNeighbour.bottomNeighbour != t)
			{
				trySplitFace(t.bottomNeighbour);
			}
			splitFace(t);
			splitFace(t.bottomNeighbour);
			t.leftChild.rightNeighbour = t.bottomNeighbour.rightChild;
			t.rightChild.leftNeighbour = t.bottomNeighbour.leftChild;
			t.bottomNeighbour.leftChild.rightNeighbour = t.rightChild;
			t.bottomNeighbour.rightChild.leftNeighbour = t.leftChild;
		}
		else
		{
			splitFace(t);
		}
	}

	protected void splitFace(BTTTriangle t)
	{
		int midpointIndex = hypotenuseMidpointIndex(t.leftIndex, t.rightIndex);
		t.rightChild = new BTTTriangle(midpointIndex, t.rightIndex, t.apexIndex);
		t.leftChild = new BTTTriangle(midpointIndex, t.apexIndex, t.leftIndex);
		t.leftChild.leftNeighbour = t.rightChild;
		t.rightChild.rightNeighbour = t.leftChild;

		t.leftChild.bottomNeighbour = t.leftNeighbour;
		if (t.leftNeighbour != null)
		{
			if (t.leftNeighbour.bottomNeighbour == t)
			{
				t.leftNeighbour.bottomNeighbour = t.leftChild;
			}
			else if (t.leftNeighbour.leftNeighbour == t)
			{
				t.leftNeighbour.leftNeighbour = t.leftChild;
			}
			else
			{
				t.leftNeighbour.rightNeighbour = t.leftChild;
			}
		}

		t.rightChild.bottomNeighbour = t.rightNeighbour;
		if (t.rightNeighbour != null)
		{
			if (t.rightNeighbour.bottomNeighbour == t)
			{
				t.rightNeighbour.bottomNeighbour = t.rightChild;
			}
			else if (t.rightNeighbour.rightNeighbour == t)
			{
				t.rightNeighbour.rightNeighbour = t.rightChild;
			}
			else
			{
				t.rightNeighbour.leftNeighbour = t.rightChild;
			}
		}
	}

	protected float calculateVariance(int apexIndex, int leftIndex, int rightIndex)
	{
		if (Math.abs(apexIndex - leftIndex) == 1 || Math.abs(apexIndex - rightIndex) == 1)
			return 0;

		int midpointIndex = hypotenuseMidpointIndex(leftIndex, rightIndex);
		double midpointElevation = positions.get(midpointIndex).elevation;
		double interpolatedElevation = (positions.get(leftIndex).elevation + positions.get(rightIndex).elevation) / 2;
		float delta = (float) Math.abs(midpointElevation - interpolatedElevation);
		delta = Math.max(delta, calculateVariance(midpointIndex, rightIndex, apexIndex));
		delta = Math.max(delta, calculateVariance(midpointIndex, apexIndex, leftIndex));
		return delta;
	}

	protected int hypotenuseMidpointIndex(int leftIndex, int rightIndex)
	{
		int leftX = leftIndex % width;
		int leftY = leftIndex / width;
		int rightX = rightIndex % width;
		int rightY = rightIndex / width;
		return (leftX + rightX) / 2 + ((leftY + rightY) / 2) * width;
	}

	public static int nextLowestPowerOf2Plus1(int v)
	{
		//based on http://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		v >>= 1;
		return ++v;
	}

	protected void addLeavesToTriangleList(BTTTriangle t, List<BTTTriangle> triangles)
	{
		if (t.leftChild == null || t.rightChild == null)
		{
			triangles.add(t);
		}
		else
		{
			//recurse through children
			addLeavesToTriangleList(t.leftChild, triangles);
			addLeavesToTriangleList(t.rightChild, triangles);
		}
	}

	protected FastShape buildFastShape(List<BTTTriangle> triangles)
	{
		List<Position> positions = new ArrayList<Position>();
		List<Integer> originalIndices = new ArrayList<Integer>();
		Map<Position, Integer> positionIndexMap = new HashMap<Position, Integer>();
		IntBuffer indices = BufferUtil.newIntBuffer(triangles.size() * 3);

		for (BTTTriangle triangle : triangles)
		{
			Position apexPosition = this.positions.get(triangle.apexIndex);
			Position leftPosition = this.positions.get(triangle.leftIndex);
			Position rightPosition = this.positions.get(triangle.rightIndex);
			Integer apexIndex = positionIndexMap.get(apexPosition);
			Integer leftIndex = positionIndexMap.get(leftPosition);
			Integer rightIndex = positionIndexMap.get(rightPosition);
			if (apexIndex == null)
			{
				apexIndex = positions.size();
				positionIndexMap.put(apexPosition, apexIndex);
				positions.add(apexPosition);
				originalIndices.add(triangle.apexIndex);
			}
			if (leftIndex == null)
			{
				leftIndex = positions.size();
				positionIndexMap.put(leftPosition, leftIndex);
				positions.add(leftPosition);
				originalIndices.add(triangle.leftIndex);
			}
			if (rightIndex == null)
			{
				rightIndex = positions.size();
				positionIndexMap.put(rightPosition, rightIndex);
				positions.add(rightPosition);
				originalIndices.add(triangle.rightIndex);
			}
			indices.put(leftIndex).put(apexIndex).put(rightIndex);
		}

		FloatBuffer textureCoordinateBuffer = BufferUtil.newFloatBuffer(positions.size() * 2);
		for (Integer index : originalIndices)
		{
			int x = index % width;
			int y = index / width;
			textureCoordinateBuffer.put(x / (float) (width - 1)).put(y / (float) (height - 1));
		}

		FastShape shape = new FastShape(positions, indices, GL.GL_TRIANGLES);
		shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		return shape;
	}

	protected class BTTTriangle
	{
		public final int apexIndex;
		public final int leftIndex;
		public final int rightIndex;
		public BTTTriangle leftChild;
		public BTTTriangle rightChild;
		public BTTTriangle leftNeighbour;
		public BTTTriangle rightNeighbour;
		public BTTTriangle bottomNeighbour;

		public BTTTriangle(int apexIndex, int leftIndex, int rightIndex)
		{
			this.apexIndex = apexIndex;
			this.leftIndex = leftIndex;
			this.rightIndex = rightIndex;
		}

		@Override
		public String toString()
		{
			return "Left: " + (leftIndex % BinaryTriangleTree.this.width) + ","
					+ (leftIndex / BinaryTriangleTree.this.width) + ", Apex: "
					+ (apexIndex % BinaryTriangleTree.this.width) + "," + (apexIndex / BinaryTriangleTree.this.width)
					+ ", Right: " + (rightIndex % BinaryTriangleTree.this.width) + ","
					+ (rightIndex / BinaryTriangleTree.this.width);
		}
	}

	/*public static void main(String[] args) throws IOException
	{
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				if (e instanceof StackOverflowError)
				{
					System.err.println(e);
				}
				else
				{
					e.printStackTrace();
				}
			}
		});

		ByteBuffer byteBuffer = WWIO.readURLContentToBuffer(BinaryTriangleTree.class.getResource("366_1884.bil"));

		// Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, AVKey.INT16);
		bufferParams.setValue(AVKey.BYTE_ORDER, AVKey.LITTLE_ENDIAN);
		BufferWrapper wrapper = BufferWrapper.wrap(byteBuffer, bufferParams);

		int width = 150;
		int height = 150;
		List<Position> positions = new ArrayList<Position>(width * height);

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				positions.add(Position.fromDegrees(y, x, wrapper.getDouble(y * width + x)));
			}
		}

		BinaryTriangleTree btt = new BinaryTriangleTree(positions, width, height);
		List<Triangle> leaves = btt.buildMeshFromCenter(3);

		int s = (width - 1) * 8 + 1;
		BufferedImage image = new BufferedImage(s, s, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, s, s);
		g.setColor(Color.black);
		for (Triangle t : leaves)
		{
			Position left = t.leftPosition;
			Position apex = t.apexPosition;
			Position right = t.rightPosition;
			g.drawLine((int) (left.longitude.degrees * (s - 1) / (width - 1)),
					(int) (left.latitude.degrees * (s - 1) / (height - 1)),
					(int) (apex.longitude.degrees * (s - 1) / (width - 1)),
					(int) (apex.latitude.degrees * (s - 1) / (height - 1)));
			g.drawLine((int) (left.longitude.degrees * (s - 1) / (width - 1)),
					(int) (left.latitude.degrees * (s - 1) / (height - 1)),
					(int) (right.longitude.degrees * (s - 1) / (width - 1)),
					(int) (right.latitude.degrees * (s - 1) / (height - 1)));
			g.drawLine((int) (right.longitude.degrees * (s - 1) / (width - 1)),
					(int) (right.latitude.degrees * (s - 1) / (height - 1)),
					(int) (apex.longitude.degrees * (s - 1) / (width - 1)),
					(int) (apex.latitude.degrees * (s - 1) / (height - 1)));
		}
		g.dispose();

		ImageIO.write(image, "JPG", new File("output.jpg"));
	}*/
}
