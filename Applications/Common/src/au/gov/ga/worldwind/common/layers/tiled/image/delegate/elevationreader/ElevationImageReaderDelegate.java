package au.gov.ga.worldwind.common.layers.tiled.image.delegate.elevationreader;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWBufferUtil;
import gov.nasa.worldwind.util.WWIO;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageReaderDelegate;

public abstract class ElevationImageReaderDelegate implements ImageReaderDelegate
{
	protected final static String doublePattern = "((?:-?\\d*\\.\\d*)|(?:-?\\d+))";

	protected final String pixelType;
	protected final String byteOrder;
	protected final double missingDataSignal;

	public ElevationImageReaderDelegate(String pixelType, String byteOrder, double missingDataSignal)
	{
		this.pixelType = pixelType;
		this.byteOrder = byteOrder;
		this.missingDataSignal = missingDataSignal;
	}

	@Override
	public BufferedImage readImage(TextureTile tile, URL url, Globe globe) throws IOException
	{
		ByteBuffer byteBuffer = WWIO.readURLContentToBuffer(url);

		int width = tile.getWidth();
		int height = tile.getHeight();

		// Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, pixelType);
		bufferParams.setValue(AVKey.BYTE_ORDER, byteOrder);
		BufferWrapper elevations = BufferWrapper.wrap(byteBuffer, bufferParams);

		try
		{
			BufferWrapperAndDimension filtered =
					removeNearestNeighborArtifacts(elevations, width, height, pixelType, byteOrder);
			if (filtered != null)
			{
				elevations = filtered.wrapper;
				width = filtered.dimension.width;
				height = filtered.dimension.height;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return generateImage(elevations, width, height, globe, tile.getSector());
	}

	protected static BufferWrapperAndDimension removeNearestNeighborArtifacts(BufferWrapper elevations, int width,
			int height, String dataType, String byteOrder)
	{
		Repeat xRepeat = calculateNearestNeighborRepeat(elevations, width, height, true);
		Repeat yRepeat = calculateNearestNeighborRepeat(elevations, width, height, false);

		if (xRepeat == null && yRepeat == null)
			return null;

		int removeX = xRepeat == null ? 0 : xRepeat.remove;
		int removeY = yRepeat == null ? 0 : yRepeat.remove;
		int newWidth = width - removeX;
		int newHeight = height - removeY;
		int newLength = newWidth * newHeight * bufferSize(dataType);
		ByteBuffer buffer = WWBufferUtil.newByteBuffer(newLength, false);

		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, dataType);
		bufferParams.setValue(AVKey.BYTE_ORDER, byteOrder);
		BufferWrapper wrapper = BufferWrapper.wrap(buffer, bufferParams);

		System.out.println("xRepeat = " + xRepeat + ", yRepeat = " + yRepeat);

		int j = 0;
		for (int y = 0, i = 0; y < height; y++)
		{
			if (yRepeat != null && (y - yRepeat.start) % yRepeat.length == 0)
			{
				//skip this row
				i += width;
				continue;
			}

			for (int x = 0; x < width; x++, i++)
			{
				if (xRepeat != null && (x - xRepeat.start) % xRepeat.length == 0)
				{
					//skip this column
					continue;
				}

				double elevation = elevations.getDouble(i);
				wrapper.putDouble(j++, elevation);
			}
		}

		return new BufferWrapperAndDimension(wrapper, new Dimension(newWidth, newHeight));
	}

	protected static int bufferSize(String dataType)
	{
		if (AVKey.INT8.equals(dataType))
			return 1;
		else if (AVKey.INT16.equals(dataType))
			return 2;
		else if (AVKey.INT32.equals(dataType))
			return 4;
		else if (AVKey.FLOAT32.equals(dataType))
			return 4;
		else if (AVKey.FLOAT64.equals(dataType))
			return 8;
		throw new IllegalArgumentException("Unknown data type: " + dataType);
	}

	protected static Repeat calculateNearestNeighborRepeat(BufferWrapper elevations, int width, int height,
			boolean xAxis)
	{
		int maxRepeatLength = -1;
		int maxRepeatStart = -1;

		int xMax = xAxis ? width : height;
		int yMax = xAxis ? height : width;

		//only allow repeat lengths within 10% of the total width or height
		int limitRepeatLength = xMax / 10;

		for (int y = 0; y < yMax; y++)
		{
			int lastX = -1;
			double lastElevation = -1;
			for (int x = 0; x < xMax; x++)
			{
				int i = xAxis ? y * height + x : x * height + y;
				double elevation = elevations.getDouble(i);

				if (x > 0 && elevation == lastElevation)
				{
					if (lastX > 0)
					{
						int repeatLength = x - lastX;
						if (repeatLength > maxRepeatLength && repeatLength <= limitRepeatLength)
						{
							maxRepeatLength = repeatLength;
							maxRepeatStart = x;
						}
					}
					lastX = x;
				}
				lastElevation = elevation;
			}
		}

		//no repeats found
		if (maxRepeatLength < 0)
			return null;

		//Removal calculation:
		//
		//150 pixels
		//repeat start = 6, length = 10
		//6, 16, 26, 36, ..., 146 (0 indexed)
		// = 15 removals
		//
		//151 pixels
		//repeat start = 0, length = 10
		//0, 10, 20, 30, ..., 150 (0 indexed)
		// = 16 removals
		//
		//151 pixels
		//repeat start = 1, length = 10
		//1, 11, 21, 31, ..., 141 (0 indexed, 151 is not valid)
		// = 15 removals

		int repeatStart = maxRepeatStart % maxRepeatLength;
		int remove = (xMax - repeatStart - 1) / maxRepeatLength + 1;

		System.out.println(maxRepeatStart + " % " + maxRepeatLength + " = " + repeatStart);

		Repeat repeat = new Repeat(maxRepeatLength, repeatStart, remove);
		if (repeatValid(elevations, width, height, repeat, xAxis))
		{
			return repeat;
		}
		return null;
	}

	protected static boolean repeatValid(BufferWrapper elevations, int width, int height, Repeat repeat, boolean xAxis)
	{
		int xMax = xAxis ? width : height;
		int yMax = xAxis ? height : width;

		int validRepeatCount = 0;
		for (int y = 0; y < yMax; y++)
		{
			for (int x = repeat.start; x < xMax; x += repeat.length)
			{
				int i0 = xAxis ? y * height + x : x * height + y;
				int i1 = xAxis ? y * height + (x - 1) : (x - 1) * height + y;

				if (i1 < 0)
				{
					System.out.println(repeat + " caused a negative index: " + i1 + " (xAxis = " + xAxis + ", x = " + x
							+ ", y = " + y + ", width = " + width + ", height = " + height);
					return false;
				}

				double elevation0 = elevations.getDouble(i0);
				double elevation1 = elevations.getDouble(i1);
				if (elevation0 == elevation1)
				{
					validRepeatCount++;
				}
			}
		}

		//repeat is valid if half of it is valid repeats to remove
		return validRepeatCount >= repeat.remove * yMax * 0.5;
	}

	protected abstract BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe,
			Sector sector);

	private static class Repeat
	{
		public final int length;
		public final int start;
		public final int remove;

		public Repeat(int length, int start, int remove)
		{
			this.length = length;
			this.start = start;
			this.remove = remove;
		}

		@Override
		public String toString()
		{
			return "(length = " + length + ", start = " + start + ", remove = " + remove + ")";
		}
	}

	private static class BufferWrapperAndDimension
	{
		public final BufferWrapper wrapper;
		public final Dimension dimension;

		public BufferWrapperAndDimension(BufferWrapper wrapper, Dimension dimension)
		{
			this.wrapper = wrapper;
			this.dimension = dimension;
		}
	}
}
