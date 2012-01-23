package au.gov.ga.worldwind.common.layers.volume.btt;

import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.util.Validate;

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

	public List<Triangle> buildMesh(float maxVariance)
	{
		List<Triangle> triangles = new ArrayList<Triangle>();
		buildMesh(maxVariance, 0, 0, width, height, false, false, triangles);
		return triangles;
	}

	public List<Triangle> buildMeshFromCenter(float maxVariance)
	{
		List<Triangle> triangles = new ArrayList<Triangle>();

		int centerWidth = nextLowestPowerOf2Plus1(width);
		int centerHeight = nextLowestPowerOf2Plus1(height);
		int centerXOffset = (width - centerWidth) / 2;
		int centerYOffset = (height - centerHeight) / 2;
		int remainingWidth = width - centerWidth - centerXOffset;
		int remainingHeight = height - centerHeight - centerYOffset;

		buildMesh(maxVariance, centerXOffset, centerYOffset, centerWidth, centerHeight, false, false, triangles);
		buildMesh(maxVariance, 0, 0, centerWidth + centerXOffset, centerYOffset + 1, true, true, triangles);
		buildMesh(maxVariance, 0, centerYOffset, centerXOffset + 1, height - centerYOffset, true, false, triangles);
		buildMesh(maxVariance, centerWidth + centerXOffset - 1, 0, remainingWidth + 1, centerHeight + centerYOffset,
				false, true, triangles);
		buildMesh(maxVariance, centerXOffset, centerHeight + centerYOffset - 1, width - centerXOffset,
				remainingHeight + 1, false, false, triangles);

		return triangles;
	}

	protected void buildMesh(float maxVariance, int x, int y, int width, int height, boolean reverseX,
			boolean reverseY, List<Triangle> triangles)
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

	protected void buildTree(float maxVariance, int x, int y, int size, List<Triangle> triangles)
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
		buildFace(maxVariance, t1);

		int apex2 = (x + size - 1) + (y + size - 1) * width, left2 = (x + size - 1) + y * width, right2 =
				x + (y + size - 1) * width;
		BTTTriangle t2 = new BTTTriangle(apex2, left2, right2);
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

	protected void addLeavesToTriangleList(BTTTriangle t, List<Triangle> triangles)
	{
		if (t.leftChild == null || t.rightChild == null)
		{
			triangles.add(new Triangle(positions.get(t.apexIndex), positions.get(t.leftIndex), positions
					.get(t.rightIndex)));
		}
		else
		{
			//recurse through children
			addLeavesToTriangleList(t.leftChild, triangles);
			addLeavesToTriangleList(t.rightChild, triangles);
		}
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

	public class Triangle
	{
		public final Position apexPosition;
		public final Position leftPosition;
		public final Position rightPosition;

		public Triangle(Position apexPosition, Position leftPosition, Position rightPosition)
		{
			this.apexPosition = apexPosition;
			this.leftPosition = leftPosition;
			this.rightPosition = rightPosition;
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
		List<Triangle> leaves = btt.buildMeshFromCenter(2);

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
