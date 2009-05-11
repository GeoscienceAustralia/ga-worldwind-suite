package tiler;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;

public class GDALTile
{
	static
	{
		gdal.AllRegister();
	}

	public static void init()
	{
	};

	public static Dataset open(File file)
	{
		try
		{
			Dataset dataset = (Dataset) gdal.Open(file.getAbsolutePath(),
					gdalconst.GA_ReadOnly);
			if (dataset == null)
			{
				printLastError();
			}
			return dataset;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	//construtor globals
	private final double minLatitude;
	private final double minLongitude;
	private final double maxLatitude;
	private final double maxLongitude;
	private final int width;
	private final int height;
	private final boolean addAlpha;

	//calculated globals
	private ByteBuffer buffer;
	private int bufferType;
	private int bufferTypeSize;

	private Rectangle dataRectangle;
	private int bandCount;
	private boolean indexed;
	private IndexColorModel indexColorModel;

	public GDALTile(Dataset dataset, int width, int height, double minLatitude,
			double minLongitude, double maxLatitude, double maxLongitude)
	{
		this(dataset, width, height, minLatitude, minLongitude, maxLatitude,
				maxLongitude, false);
	}

	public GDALTile(Dataset dataset, int width, int height, double minLatitude,
			double minLongitude, double maxLatitude, double maxLongitude,
			boolean addAlpha)
	{
		if (minLatitude >= maxLatitude || minLongitude >= maxLongitude)
			throw new IllegalArgumentException();

		this.width = width;
		this.height = height;
		this.minLatitude = minLatitude;
		this.minLongitude = minLongitude;
		this.maxLatitude = maxLatitude;
		this.maxLongitude = maxLongitude;
		this.addAlpha = addAlpha;
		readDataset(dataset);
	}

	private GDALTile(GDALTile tile, ByteBuffer buffer, int bufferType,
			int bufferTypeSize)
	{
		this.buffer = buffer;
		this.bufferType = bufferType;
		this.bufferTypeSize = bufferTypeSize;

		this.width = tile.width;
		this.height = tile.height;
		this.minLatitude = tile.minLatitude;
		this.minLongitude = tile.minLongitude;
		this.maxLatitude = tile.maxLatitude;
		this.maxLongitude = tile.maxLongitude;
		this.addAlpha = tile.addAlpha;

		this.dataRectangle = tile.dataRectangle;
		this.bandCount = tile.bandCount;
		this.indexed = tile.indexed;
		this.indexColorModel = tile.indexColorModel;
	}

	protected void readDataset(Dataset dataset)
	{
		//in order to use this, GDAL_DATA env variable must be set to the
		//location of gcs.csv (usually GDAL_DIR/data)
		SpatialReference dstSR = new SpatialReference();
		dstSR.ImportFromEPSG(4326); //WGS84

		String projection = dataset.GetProjection();
		SpatialReference srcSR = (projection == null || projection.length() == 0) ? null
				: new SpatialReference(projection);

		try
		{
			if (srcSR != null && dstSR != null && srcSR.IsSame(dstSR) != 1)
			{
				System.out.println("Reprojecting image to "
						+ dstSR.ExportToPrettyWkt(1));
				readDatasetReprojected(dataset, dstSR);
				//throw new IllegalArgumentException("Projection not supported: " + srcSR.ExportToPrettyWkt(1));
			}
			else
			{
				readDatasetNormal(dataset);
			}
		}
		finally
		{
			if (srcSR != null)
				srcSR.delete();
			if (dstSR != null)
				dstSR.delete();
		}
	}

	protected void readDatasetNormal(Dataset dataset)
	{
		double[] geoTransformArray = new double[6];
		dataset.GetGeoTransform(geoTransformArray);

		int srcX = (int) ((minLongitude - geoTransformArray[0])
				/ geoTransformArray[1] + 0.001);
		int srcY = (int) ((maxLatitude - geoTransformArray[3])
				/ geoTransformArray[5] + 0.001);
		int srcWidth = (int) ((maxLongitude - minLongitude)
				/ geoTransformArray[1] + 0.5);
		int srcHeight = (int) ((minLatitude - maxLatitude)
				/ geoTransformArray[5] + 0.5);

		Rectangle srcRect = new Rectangle(srcX, srcY, srcWidth, srcHeight);
		readRectangle(dataset, srcRect);
	}

	protected void readDatasetReprojected(Dataset dataset,
			SpatialReference dstSR)
	{
		Driver memDriver = gdal.GetDriverByName("MEM");
		Dataset dst = memDriver.Create("mem", width, height);

		while (dst.getRasterCount() < dataset.getRasterCount())
			dst.AddBand();

		double[] geoTransformArray = new double[6];
		geoTransformArray[0] = minLongitude;
		geoTransformArray[3] = maxLatitude;
		geoTransformArray[1] = (maxLongitude - minLongitude) / (double) width;
		geoTransformArray[5] = (minLatitude - maxLatitude) / (double) height;
		dst.SetGeoTransform(geoTransformArray);
		dst.SetProjection(dstSR.ExportToWkt());

		int returnVal = 0;
		try
		{
			returnVal = gdal.ReprojectImage(dataset, dst, null, null,
					gdalconst.GRA_NearestNeighbour);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (returnVal != gdalconstConstants.CE_None)
		{
			printLastError();
		}
		readRectangle(dataset, null);
	}

	protected void readRectangle(Dataset dataset, Rectangle srcRect)
	{
		//get and check raster band count
		bandCount = dataset.getRasterCount();
		if (bandCount <= 0)
		{
			throw new IllegalArgumentException(
					"No raster bands found in dataset");
		}

		//get the rasters and the raster data type
		bufferType = 0;
		Band[] bands = new Band[bandCount];
		Band lastBand = null;
		for (int i = 0; i < bandCount; i++)
		{
			//bands are 1-base indexed
			bands[i] = dataset.GetRasterBand(i + 1);
			lastBand = bands[i];
			//check raster data type
			if (i == 0)
			{
				bufferType = bands[i].getDataType();
			}
			else if (bands[i].getDataType() != bufferType)
			{
				throw new IllegalArgumentException(
						"Raster bands are of different types");
			}
		}
		int dataTypeSize = gdal.GetDataTypeSize(bufferType);
		bufferTypeSize = dataTypeSize / 8;

		//image parameters and data
		int pixels = width * height;
		int bandSize = pixels * bufferTypeSize;
		int bandCountAllocated = (addAlpha && bandCount == 3) ? 4 : bandCount;
		buffer = ByteBuffer.allocateDirect(bandSize * bandCountAllocated);
		buffer.order(ByteOrder.LITTLE_ENDIAN); //TODO check if always the case?

		//calculate src and dst image rectangles
		dataRectangle = new Rectangle(0, 0, width, height);
		if (srcRect == null)
		{
			srcRect = new Rectangle(0, 0, dataset.getRasterXSize(), dataset
					.getRasterYSize());
		}
		else if (srcRect.x < 0 || srcRect.y < 0
				|| srcRect.x + srcRect.width > dataset.getRasterXSize()
				|| srcRect.y + srcRect.height > dataset.getRasterYSize())
		{
			Rectangle newSrcRect = new Rectangle(0, 0,
					dataset.getRasterXSize(), dataset.getRasterYSize());
			newSrcRect = srcRect.intersection(newSrcRect);

			if (!newSrcRect.isEmpty())
			{
				//calculate dst rect for the new src rect
				int dstX = dataRectangle.width * (newSrcRect.x - srcRect.x)
						/ srcRect.width;
				int dstY = dataRectangle.height * (newSrcRect.y - srcRect.y)
						/ srcRect.height;
				int dstWidth = dataRectangle.width - dataRectangle.width
						* (srcRect.width - newSrcRect.width) / srcRect.width;
				int dstHeight = dataRectangle.height - dataRectangle.height
						* (srcRect.height - newSrcRect.height) / srcRect.height;
				dataRectangle = new Rectangle(dstX, dstY, dstWidth, dstHeight);
			}
			else
			{
				System.out.println("Warning: source window is empty");
			}
			srcRect = newSrcRect;
		}

		//read image data
		for (int b = 0; b < bandCount; b++)
		{
			if (!srcRect.isEmpty())
			{
				//slice buffer at the correct write position
				buffer.position(b * bandSize + dataRectangle.x
						+ dataRectangle.y * width);
				ByteBuffer sliced = buffer.slice();

				//read band into buffer
				int returnVal = 0;
				try
				{
					returnVal = bands[b].ReadRaster_Direct(srcRect.x,
							srcRect.y, srcRect.width, srcRect.height,
							dataRectangle.width, dataRectangle.height,
							bufferType, sliced, bufferTypeSize, bufferTypeSize
									* width);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				if (returnVal != gdalconstConstants.CE_None)
				{
					printLastError();
				}
			}
		}

		//if an alpha band has been added, fill with 1s
		if (bandCount != bandCountAllocated)
		{
			bandCount = bandCountAllocated;
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					int index = getBufferIndex(x, y, 3) * bufferTypeSize;
					for (int i = 0; i < bufferTypeSize; i++)
					{
						buffer.put(index + i, (byte) 0xff);
					}
				}
			}
		}

		//rewind the buffer
		buffer.rewind();

		//set variables for indexed image
		boolean indexed = lastBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex;
		indexColorModel = indexed ? lastBand.GetRasterColorTable()
				.getIndexColorModel(dataTypeSize) : null;
	}

	public static void printLastError()
	{
		System.err.println("Last error: " + gdal.GetLastErrorMsg());
		System.err.println("Last error no: " + gdal.GetLastErrorNo());
		System.err.println("Last error type: " + gdal.GetLastErrorType());
	}

	public BufferedImage getAsImage()
	{
		int pixels = width * height;
		int[] offsets = new int[bandCount];
		for (int b = 0; b < bandCount; b++)
		{
			offsets[b] = b * pixels;
		}

		//image data
		DataBuffer imgBuffer = null;
		int imageType = 0, dataType = 0;

		//create image data for buffer type
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			byte[] bytes = new byte[pixels * bandCount];
			buffer.get(bytes);
			imgBuffer = new DataBufferByte(bytes, bytes.length);
			dataType = DataBuffer.TYPE_BYTE;
			imageType = indexed ? BufferedImage.TYPE_BYTE_INDEXED
					: BufferedImage.TYPE_BYTE_GRAY;
		}
		else if (bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_UInt16)
		{
			short[] shorts = new short[pixels * bandCount];
			buffer.asShortBuffer().get(shorts);
			imgBuffer = new DataBufferShort(shorts, shorts.length);
			dataType = DataBuffer.TYPE_USHORT;
			imageType = BufferedImage.TYPE_USHORT_GRAY;
		}
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_UInt32)
		{
			int[] ints = new int[pixels * bandCount];
			buffer.asIntBuffer().get(ints);
			imgBuffer = new DataBufferInt(ints, ints.length);
			dataType = DataBuffer.TYPE_INT;
			imageType = BufferedImage.TYPE_CUSTOM;
		}
		else
		{
			throw new IllegalArgumentException("Unsupported image data type");
		}

		//create sample model and raster
		SampleModel sampleModel = new ComponentSampleModel(dataType, width,
				height, 1, width, offsets);
		WritableRaster raster = Raster.createWritableRaster(sampleModel,
				imgBuffer, null);
		BufferedImage img = null;

		//set image type
		if (bandCount == 4)
		{
			imageType = BufferedImage.TYPE_INT_ARGB;
		}
		else if (bandCount == 3)
		{
			imageType = BufferedImage.TYPE_INT_RGB;
		}
		else if (indexed && bandCount == 1)
		{
			//ok
		}
		else
		{
			System.out.println("Warning: unknown image type");
		}

		//create and return image
		img = (indexColorModel == null) ? new BufferedImage(width, height,
				imageType) : new BufferedImage(width, height, imageType,
				indexColorModel);
		img.setData(raster);
		return img;
	}

	public void fillNodata(long value)
	{
		long[] values = new long[bandCount];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = value;
		}
		fillNodata(values);
	}

	public void fillNodata(long[] values)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			byte[] bvalues = new byte[values.length];
			for (int i = 0; i < values.length; i++)
			{
				bvalues[i] = (byte) values[i];
			}
			fillNodataByte(bvalues);
		}
		else if (bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_UInt16)
		{
			short[] svalues = new short[values.length];
			for (int i = 0; i < values.length; i++)
			{
				svalues[i] = (short) values[i];
			}
			fillNodataShort(svalues);
		}
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_UInt32)
		{
			int[] ivalues = new int[values.length];
			for (int i = 0; i < values.length; i++)
			{
				ivalues[i] = (int) values[i];
			}
			fillNodataInt(ivalues);
		}
		/*else if (bufferTypeSize == 8)
		{
			fillNodataLong(values);
		}*/
		else
		{
			throw new IllegalArgumentException("Buffer type unknown");
		}
	}

	public void fillNodataByte(byte value)
	{
		byte[] values = new byte[bandCount];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = value;
		}
		fillNodataByte(values);
	}

	public void fillNodataByte(byte[] values)
	{
		if (values.length != bandCount)
			throw new IllegalArgumentException(
					"Array size does not equal band count");

		if (dataRectangle.x == 0 && dataRectangle.y == 0
				&& dataRectangle.width == width
				&& dataRectangle.height == height)
			return;

		ByteBuffer bb = buffer;
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!dataRectangle.contains(x, y))
				{
					for (int b = 0; b < bandCount; b++)
					{
						bb.put(getBufferIndex(x, y, b), values[b]);
					}
				}
			}
		}
	}

	public void fillNodataShort(short value)
	{
		short[] values = new short[bandCount];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = value;
		}
		fillNodataShort(values);
	}

	public void fillNodataShort(short[] values)
	{
		if (values.length != bandCount)
			throw new IllegalArgumentException(
					"Array size does not equal band count");

		if (dataRectangle.x == 0 && dataRectangle.y == 0
				&& dataRectangle.width == width
				&& dataRectangle.height == height)
			return;

		ShortBuffer sb = buffer.asShortBuffer();
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!dataRectangle.contains(x, y))
				{
					for (int b = 0; b < bandCount; b++)
					{
						sb.put(getBufferIndex(x, y, b), values[b]);
					}
				}
			}
		}
	}

	public void fillNodataInt(int value)
	{
		int[] values = new int[bandCount];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = value;
		}
		fillNodataInt(values);
	}

	public void fillNodataInt(int[] values)
	{
		if (values.length != bandCount)
			throw new IllegalArgumentException(
					"Array size does not equal band count");

		if (dataRectangle.x == 0 && dataRectangle.y == 0
				&& dataRectangle.width == width
				&& dataRectangle.height == height)
			return;

		IntBuffer ib = buffer.asIntBuffer();
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!dataRectangle.contains(x, y))
				{
					for (int b = 0; b < bandCount; b++)
					{
						ib.put(getBufferIndex(x, y, b), values[b]);
					}
				}
			}
		}
	}

	public void fillNodataLong(long value)
	{
		long[] values = new long[bandCount];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = value;
		}
		fillNodataLong(values);
	}

	public void fillNodataLong(long[] values)
	{
		if (values.length != bandCount)
			throw new IllegalArgumentException(
					"Array size does not equal band count");

		if (dataRectangle.x == 0 && dataRectangle.y == 0
				&& dataRectangle.width == width
				&& dataRectangle.height == height)
			return;

		LongBuffer lb = buffer.asLongBuffer();
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!dataRectangle.contains(x, y))
				{
					for (int b = 0; b < bandCount; b++)
					{
						lb.put(getBufferIndex(x, y, b), values[b]);
					}
				}
			}
		}
	}

	protected int getBufferIndex(int x, int y, int b)
	{
		return b * width * height + y * width + x;
	}

	public double getMinLatitude()
	{
		return minLatitude;
	}

	public double getMinLongitude()
	{
		return minLongitude;
	}

	public double getMaxLatitude()
	{
		return maxLatitude;
	}

	public double getMaxLongitude()
	{
		return maxLongitude;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public ByteBuffer getBuffer()
	{
		return buffer;
	}

	public Rectangle getDataRectangle()
	{
		return dataRectangle;
	}

	public int getBufferType()
	{
		return bufferType;
	}

	public int getBufferTypeSize()
	{
		return bufferTypeSize;
	}

	public int getBandCount()
	{
		return bandCount;
	}

	public GDALTile convertToType(int newBufferType)
	{
		if (newBufferType == bufferType)
			return this;

		int newBufferTypeSize = gdal.GetDataTypeSize(newBufferType) / 8;

		if (!(bufferType == gdalconstConstants.GDT_Byte
				|| bufferType == gdalconstConstants.GDT_Float32
				|| bufferType == gdalconstConstants.GDT_Float64
				|| bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_UInt16 || bufferType == gdalconstConstants.GDT_UInt32))
			throw new IllegalArgumentException(
					"Source buffer type not supported");

		if (!(newBufferType == gdalconstConstants.GDT_Byte
				|| newBufferType == gdalconstConstants.GDT_Float32
				|| newBufferType == gdalconstConstants.GDT_Float64
				|| newBufferType == gdalconstConstants.GDT_Int16
				|| newBufferType == gdalconstConstants.GDT_Int32
				|| newBufferType == gdalconstConstants.GDT_UInt16 || newBufferType == gdalconstConstants.GDT_UInt32))
			throw new IllegalArgumentException(
					"Destination buffer type not supported");

		int size = buffer.limit() / bufferTypeSize;
		ByteBuffer newBuffer = ByteBuffer.allocateDirect(size
				* newBufferTypeSize);
		newBuffer.order(buffer.order());

		buffer.rewind();
		while (buffer.hasRemaining())
		{
			copy(buffer, newBuffer, bufferType, newBufferType);
		}

		buffer.rewind();
		newBuffer.rewind();

		return new GDALTile(this, newBuffer, newBufferType, newBufferTypeSize);
	}

	private void copy(ByteBuffer src, ByteBuffer dst, int srcBufferType,
			int dstBufferType)
	{
		long lvalue = 0;
		double dvalue = 0;

		if (srcBufferType == gdalconstConstants.GDT_Byte)
		{
			lvalue = src.get();
			dvalue = lvalue;
		}
		else if (srcBufferType == gdalconstConstants.GDT_Int16)
		{
			lvalue = src.getShort();
			dvalue = lvalue;
		}
		else if (srcBufferType == gdalconstConstants.GDT_Int32)
		{
			lvalue = src.getInt();
			dvalue = lvalue;
		}
		else if (srcBufferType == gdalconstConstants.GDT_Float32)
		{
			dvalue = src.getFloat();
			lvalue = (long) dvalue;
		}
		else if (srcBufferType == gdalconstConstants.GDT_Float64)
		{
			dvalue = src.getDouble();
			lvalue = (long) dvalue;
		}
		else if (srcBufferType == gdalconstConstants.GDT_UInt16)
		{
			//TODO check byte order
			int first = 0xff & (int) src.get();
			int second = 0xff & (int) src.get();
			lvalue = (first << 8 | second);
			dvalue = lvalue;
		}
		else if (srcBufferType == gdalconstConstants.GDT_UInt32)
		{
			//TODO check byte order
			int first = 0xff & (int) src.get();
			int second = 0xff & (int) src.get();
			int third = 0xff & (int) src.get();
			int fourth = 0xff & (int) src.get();
			lvalue = (first << 24 | second << 16 | third << 8 | fourth);
			dvalue = lvalue;
		}
		else
		{
			throw new IllegalArgumentException("Unknown source buffer type");
		}

		if (dstBufferType == gdalconstConstants.GDT_Byte)
		{
			dst.put((byte) lvalue);
		}
		else if (dstBufferType == gdalconstConstants.GDT_Int16)
		{
			dst.putShort((short) lvalue);
		}
		else if (dstBufferType == gdalconstConstants.GDT_Int32)
		{
			dst.putInt((int) lvalue);
		}
		else if (dstBufferType == gdalconstConstants.GDT_Float32)
		{
			dst.putFloat((float) dvalue);
		}
		else if (dstBufferType == gdalconstConstants.GDT_Float64)
		{
			dst.putDouble(dvalue);
		}
		else if (dstBufferType == gdalconstConstants.GDT_UInt16)
		{
			//TODO check byte order
			byte first = (byte) ((lvalue >> 8) & 0xff);
			byte second = (byte) (lvalue & 0xff);
			dst.put((byte) first);
			dst.put((byte) second);
		}
		else if (dstBufferType == gdalconstConstants.GDT_UInt32)
		{
			//TODO check byte order
			byte first = (byte) ((lvalue >> 24) & 0xff);
			byte second = (byte) ((lvalue >> 16) & 0xff);
			byte third = (byte) ((lvalue >> 8) & 0xff);
			byte fourth = (byte) (lvalue & 0xff);
			dst.put((byte) first);
			dst.put((byte) second);
			dst.put((byte) third);
			dst.put((byte) fourth);
		}
		else
		{
			throw new IllegalArgumentException(
					"Unknown destination buffer type");
		}
	}
}
