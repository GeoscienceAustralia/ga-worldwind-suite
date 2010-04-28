package gdal;

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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;

import util.MinMaxArray;
import util.NullableNumberArray;
import util.NumberArray;
import util.Sector;
import util.TilerException;

public class GDALTile
{
	// TODO test with various datatypes:
	// So far, only really tested with 3 and 4 band image datasets,
	// and 16-bit integer DEMs. Need to test replace and datatype
	// modification with other datatypes.

	// construtor globals
	private final Sector sector;
	private final int width;
	private final int height;
	private final boolean addAlpha;
	private final int selectedBand;

	// calculated globals
	private ByteBuffer buffer;
	private int bufferType;
	private int bufferTypeSize;
	private boolean floatingPoint;

	private Rectangle dataRectangle;
	private int bufferBandCount;
	private boolean indexed;
	private IndexColorModel indexColorModel;

	public GDALTile(Dataset dataset, int width, int height, Sector sector)
			throws GDALException, TilerException
	{
		this(dataset, width, height, sector, false, -1);
	}

	public GDALTile(Dataset dataset, int width, int height, Sector sector,
			boolean addAlpha, int selectedBand) throws GDALException,
			TilerException
	{
		if (sector.getMinLatitude() >= sector.getMaxLatitude()
				|| sector.getMinLongitude() >= sector.getMaxLongitude())
			throw new IllegalArgumentException();

		this.width = width;
		this.height = height;
		this.sector = sector;
		this.addAlpha = addAlpha;
		this.selectedBand = selectedBand;
		readDataset(dataset);
	}

	protected GDALTile(GDALTile tile, ByteBuffer buffer, int bufferType,
			int bufferTypeSize, boolean floatingPoint)
	{
		this.buffer = buffer;
		this.bufferType = bufferType;
		this.bufferTypeSize = bufferTypeSize;
		this.floatingPoint = floatingPoint;

		this.width = tile.width;
		this.height = tile.height;
		this.sector = tile.sector;
		this.addAlpha = tile.addAlpha;
		this.selectedBand = tile.selectedBand;

		this.dataRectangle = tile.dataRectangle;
		this.bufferBandCount = tile.bufferBandCount;
		this.indexed = tile.indexed;
		this.indexColorModel = tile.indexColorModel;
	}

	protected void readDataset(Dataset dataset) throws GDALException,
			TilerException
	{
		if (GDALUtil.isProjectionsSupported())
		{
			SpatialReference dstSR = new SpatialReference();
			dstSR.ImportFromEPSG(4326); // WGS84

			String projection = dataset.GetProjection();
			SpatialReference srcSR = (projection == null || projection.length() == 0) ? null
					: new SpatialReference(projection);

			try
			{
				if (srcSR != null && dstSR != null && srcSR.IsSame(dstSR) != 1)
				{
					readDatasetReprojected(dataset, dstSR);
					// throw new TilerException("Projection not supported: " +
					// srcSR.ExportToPrettyWkt(1));
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
		else
		{
			readDatasetNormal(dataset);
		}
	}

	protected void readDatasetNormal(Dataset dataset) throws GDALException,
			TilerException
	{
		double[] geoTransformArray = new double[6];
		dataset.GetGeoTransform(geoTransformArray);

		int srcX = (int) ((sector.getMinLongitude() - geoTransformArray[0])
				/ geoTransformArray[1] + 0.001);
		int srcY = (int) ((sector.getMaxLatitude() - geoTransformArray[3])
				/ geoTransformArray[5] + 0.001);
		int srcWidth = (int) (sector.getDeltaLongitude() / geoTransformArray[1] + 0.5);
		int srcHeight = (int) (-sector.getDeltaLatitude()
				/ geoTransformArray[5] + 0.5);

		Rectangle srcRect = new Rectangle(srcX, srcY, srcWidth, srcHeight);
		readRectangle(dataset, srcRect);
	}

	protected void readDatasetReprojected(Dataset dataset,
			SpatialReference dstSR) throws GDALException, TilerException
	{
		Driver memDriver = gdal.GetDriverByName("MEM");
		//create a dataset with 1 band with the same data type as band 1 of the source
		Dataset dst = memDriver.Create("mem", width, height, 1, dataset
				.GetRasterBand(1).getDataType());
		//add the other bands with the same data type as the source bands
		for (int i = 1; i < dataset.getRasterCount(); i++)
		{
			dst.AddBand(dataset.GetRasterBand(i + 1).getDataType());
		}

		//currently the NODATA values are being ignored! this means datasets with NODATA in an incorrect projection will not work!
		/*for (int i = 0; i < dataset.getRasterCount(); i++)
		{
			Band srcBand = dataset.GetRasterBand(i + 1);
			Band dstBand = dst.GetRasterBand(i + 1);
			Double[] val = new Double[1];
			srcBand.GetNoDataValue(val);
			if (val[0] != null)
				dstBand.SetNoDataValue(val[0]);
		}*/

		double[] geoTransformArray = new double[6];
		geoTransformArray[0] = sector.getMinLongitude();
		geoTransformArray[3] = sector.getMaxLatitude();
		geoTransformArray[1] = sector.getDeltaLongitude() / (double) width;
		geoTransformArray[5] = -sector.getDeltaLatitude() / (double) height;
		dst.SetGeoTransform(geoTransformArray);
		dst.SetProjection(dstSR.ExportToWkt());

		int returnVal = gdal.ReprojectImage(dataset, dst, null, null,
				gdalconst.GRA_NearestNeighbour);
		if (returnVal != gdalconstConstants.CE_None)
		{
			throw new GDALException();
		}
		readRectangle(dst, null);
	}

	protected void readRectangle(Dataset dataset, Rectangle srcRect)
			throws GDALException, TilerException
	{
		// get and check raster band count
		int dataBandCount = dataset.getRasterCount();
		if (dataBandCount <= 0)
		{
			throw new TilerException("No raster bands found in dataset");
		}

		// check the selected band is valid
		if (selectedBand >= 0 && selectedBand >= dataBandCount)
		{
			throw new IllegalArgumentException("Selected band does not exist");
		}

		bufferBandCount = selectedBand >= 0 ? 1
				: (addAlpha && dataBandCount == 3) ? 4 : dataBandCount;

		// get the rasters and the raster data type
		bufferType = 0;
		Band[] bands = null;
		if (selectedBand >= 0)
		{
			bands = new Band[1];
			bands[0] = dataset.GetRasterBand(selectedBand + 1);
			bufferType = bands[0].getDataType();
		}
		else
		{
			bands = new Band[dataBandCount];
			for (int i = 0; i < dataBandCount; i++)
			{
				// bands are 1-base indexed
				bands[i] = dataset.GetRasterBand(i + 1);
				// check raster data type
				if (i == 0)
				{
					bufferType = bands[i].getDataType();
				}
				else if (bands[i].getDataType() != bufferType)
				{
					throw new TilerException(
							"Raster bands are of different types");
				}
			}
		}

		int dataTypeSize = gdal.GetDataTypeSize(bufferType); // in bits
		bufferTypeSize = dataTypeSize / 8; // in bytes
		floatingPoint = isTypeFloatingPoint(bufferType);

		// image parameters and data
		int pixels = width * height;
		int bandSize = pixels * bufferTypeSize;
		buffer = ByteBuffer.allocateDirect(bandSize * bufferBandCount);
		buffer.order(ByteOrder.LITTLE_ENDIAN); // TODO check if always the case?

		// calculate src and dst image rectangles
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
				// calculate dst rect for the new src rect
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
				throw new TilerException("Source window is empty");
			}
			srcRect = newSrcRect;
		}

		// read image data
		for (int b = 0; b < bands.length; b++)
		{
			if (!srcRect.isEmpty())
			{
				// slice buffer at the correct write position
				buffer.position(b * bandSize + dataRectangle.x
						+ dataRectangle.y * width);
				ByteBuffer sliced = buffer.slice();

				// read band into buffer
				int returnVal = bands[b].ReadRaster_Direct(srcRect.x,
						srcRect.y, srcRect.width, srcRect.height,
						dataRectangle.width, dataRectangle.height, bufferType,
						sliced, bufferTypeSize, bufferTypeSize * width);
				if (returnVal != gdalconstConstants.CE_None)
				{
					throw new GDALException();
				}
			}
		}

		// if an alpha band has been added, fill alpha channel with 255
		// inside dataRectangle, 0 otherwise
		if (bufferBandCount == 4 && dataBandCount != 4)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int index = getBufferIndex(x, y, 3) * bufferTypeSize;
					for (int i = 0; i < bufferTypeSize; i++)
					{
						if (dataRectangle.contains(x, y))
							buffer.put(index + i, (byte) 0xff);
						else
							buffer.put(index + i, (byte) 0x00);
					}
				}
			}
		}

		// rewind the buffer
		buffer.rewind();

		// set variables for indexed image
		Band lastBand = bands[bands.length - 1];
		boolean indexed = lastBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex;
		indexColorModel = indexed ? lastBand.GetRasterColorTable()
				.getIndexColorModel(dataTypeSize) : null;
	}

	public static boolean isTypeFloatingPoint(int bufferType)
	{
		return bufferType == gdalconstConstants.GDT_Float32
				|| bufferType == gdalconstConstants.GDT_CFloat32
				|| bufferType == gdalconstConstants.GDT_Float64
				|| bufferType == gdalconstConstants.GDT_CFloat64;
	}

	public BufferedImage getAsImage() throws TilerException
	{
		int pixels = width * height;
		int[] offsets = new int[bufferBandCount];
		for (int b = 0; b < bufferBandCount; b++)
		{
			offsets[b] = b * pixels;
		}

		// image data
		DataBuffer imgBuffer = null;
		int imageType = 0, dataType = 0;

		// create image data for buffer type
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			byte[] bytes = new byte[pixels * bufferBandCount];
			buffer.get(bytes);
			imgBuffer = new DataBufferByte(bytes, bytes.length);
			dataType = DataBuffer.TYPE_BYTE;
			imageType = indexed ? BufferedImage.TYPE_BYTE_INDEXED
					: BufferedImage.TYPE_BYTE_GRAY;
		}
		else if (bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_UInt16
				|| bufferType == gdalconstConstants.GDT_CInt16)
		{
			short[] shorts = new short[pixels * bufferBandCount];
			buffer.asShortBuffer().get(shorts);
			imgBuffer = new DataBufferShort(shorts, shorts.length);
			dataType = bufferType == gdalconstConstants.GDT_UInt16 ? DataBuffer.TYPE_USHORT
					: DataBuffer.TYPE_SHORT;
			imageType = BufferedImage.TYPE_USHORT_GRAY;
		}
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_UInt32
				|| bufferType == gdalconstConstants.GDT_CInt32)
		{
			int[] ints = new int[pixels * bufferBandCount];
			buffer.asIntBuffer().get(ints);
			imgBuffer = new DataBufferInt(ints, ints.length);
			dataType = DataBuffer.TYPE_INT;
			imageType = BufferedImage.TYPE_CUSTOM;
		}
		else
		{
			throw new TilerException("Unsupported image data type");
		}

		// create sample model and raster
		SampleModel sampleModel = new ComponentSampleModel(dataType, width,
				height, 1, width, offsets);
		WritableRaster raster = Raster.createWritableRaster(sampleModel,
				imgBuffer, null);
		BufferedImage img = null;

		// set image type
		if (bufferBandCount == 4)
		{
			imageType = BufferedImage.TYPE_INT_ARGB_PRE;
		}
		else if (bufferBandCount == 3)
		{
			imageType = BufferedImage.TYPE_INT_RGB;
		}
		else if (indexed && bufferBandCount == 1)
		{
			// ok
		}
		else
		{
			// System.out.println("Warning: unknown image type");
		}

		// create and return image
		img = (indexColorModel == null) ? new BufferedImage(width, height,
				imageType) : new BufferedImage(width, height, imageType,
				indexColorModel);
		img.setData(raster);
		return img;
	}

	public void fillOutside(NumberArray values) throws TilerException
	{
		if (values.length() != bufferBandCount)
			throw new IllegalArgumentException(
					"Array size does not equal band count");

		if (dataRectangle.x == 0 && dataRectangle.y == 0
				&& dataRectangle.width == width
				&& dataRectangle.height == height)
			return;

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!dataRectangle.contains(x, y))
				{
					for (int b = 0; b < bufferBandCount; b++)
					{
						int index = getBufferIndex(x, y, b) * bufferTypeSize;
						if (floatingPoint)
							putDoubleValue(index, buffer, bufferType, values
									.getDouble(b));
						else
							putLongValue(index, buffer, bufferType, values
									.getLong(b));
					}
				}
			}
		}
	}

	public void replaceValues(MinMaxArray[] minMaxs,
			NullableNumberArray replacement, NullableNumberArray otherwise)
			throws TilerException
	{
		if (minMaxs == null || minMaxs.length == 0)
			return;

		for (int i = 0; i < minMaxs.length; i++)
			if (minMaxs[i].length() != bufferBandCount)
				throw new IllegalArgumentException(
						"Array size must equal band count");

		if (replacement.length() != bufferBandCount
				|| otherwise.length() != bufferBandCount)
			throw new IllegalArgumentException(
					"Array size must equal band count");

		boolean allNull = true;
		for (int b = 0; b < bufferBandCount; b++)
		{
			if (replacement.getDouble(b) != null
					|| otherwise.getDouble(b) != null)
			{
				allNull = false;
				break;
			}
		}
		if (allNull)
		{
			// replacement values are all null, so nothing will happen
			return;
		}

		double[] doubleValues = new double[bufferBandCount];
		long[] longValues = new long[bufferBandCount];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				boolean between = false;

				for (int b = 0; b < bufferBandCount; b++)
				{
					int index = getBufferIndex(x, y, b) * bufferTypeSize;
					if (floatingPoint)
						doubleValues[b] = getDoubleValue(index, buffer,
								bufferType);
					else
						longValues[b] = getLongValue(index, buffer, bufferType);
				}

				for (int i = 0; i < minMaxs.length; i++)
				{
					if (minMaxs[i] != null)
					{
						if (floatingPoint)
							between = minMaxs[i].isBetweenDouble(doubleValues);
						else
							between = minMaxs[i].isBetweenLong(longValues);
						if (between)
							break;
					}
				}

				NullableNumberArray values = between ? replacement : otherwise;
				for (int b = 0; b < bufferBandCount; b++)
				{
					if (values.getDouble(b) != null)
					{
						int index = getBufferIndex(x, y, b) * bufferTypeSize;
						if (floatingPoint)
						{
							putDoubleValue(index, buffer, bufferType, values
									.getDouble(b));
						}
						else
						{
							putLongValue(index, buffer, bufferType, values
									.getLong(b));
						}
					}
				}
			}
		}
	}

	public void updateMinMax(NumberArray minmax, NumberArray outsideValues)
	{
		if (minmax == null)
			return;

		if (outsideValues != null && outsideValues.length() < bufferBandCount)
			outsideValues = null; // just in case

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				for (int b = 0; b < bufferBandCount; b++)
				{
					int index = getBufferIndex(x, y, b) * bufferTypeSize;
					if (floatingPoint)
					{
						Double value = getDoubleValue(index, buffer, bufferType);
						if (outsideValues != null
								&& value == outsideValues.getDouble(b))
							continue;

						if (value < minmax.getDouble(0))
							minmax.setDouble(0, value);
						if (value > minmax.getDouble(1))
							minmax.setDouble(1, value);
					}
					else
					{
						Long value = getLongValue(index, buffer, bufferType);
						if (outsideValues != null
								&& value == outsideValues.getLong(b))
							continue;

						if (value < minmax.getLong(0))
							minmax.setLong(0, value);
						if (value > minmax.getLong(1))
							minmax.setLong(1, value);
					}
				}
			}
		}
	}

	protected int getBufferIndex(int x, int y, int b)
	{
		return b * width * height + y * width + x;
	}

	public Sector getSector()
	{
		return sector;
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

	public boolean isFloatingPoint()
	{
		return floatingPoint;
	}

	public int getBandCount()
	{
		return bufferBandCount;
	}

	public GDALTile convertToType(int newBufferType)
	{
		if (newBufferType == bufferType)
			return this;

		int newBufferTypeSize = gdal.GetDataTypeSize(newBufferType) / 8;
		boolean newFloatingPoint = isTypeFloatingPoint(newBufferType);
		int size = buffer.limit() / bufferTypeSize;
		ByteBuffer newBuffer = ByteBuffer.allocateDirect(size
				* newBufferTypeSize);
		newBuffer.order(buffer.order());

		long lvalue = 0;
		double dvalue = 0;
		buffer.rewind();
		while (buffer.hasRemaining())
		{
			if (floatingPoint)
			{
				dvalue = getDoubleValue(buffer, bufferType);
				lvalue = (long) dvalue;
			}
			else
			{
				lvalue = getLongValue(buffer, bufferType);
				dvalue = lvalue;
			}

			if (newFloatingPoint)
				putDoubleValue(newBuffer, newBufferType, dvalue);
			else
				putLongValue(newBuffer, newBufferType, lvalue);
		}

		buffer.rewind();
		newBuffer.rewind();

		return new GDALTile(this, newBuffer, newBufferType, newBufferTypeSize,
				newFloatingPoint);
	}

	public static long getLongValue(ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			return buffer.get() & 0xff;
		else if (bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_CInt16)
			return buffer.getShort();
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_CInt32)
			return buffer.getInt();
		else if (bufferType == gdalconstConstants.GDT_UInt16)
			return getUInt16(buffer);
		else if (bufferType == gdalconstConstants.GDT_UInt32)
			return getUInt32(buffer);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static long getLongValue(int index, ByteBuffer buffer,
			int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			return buffer.get(index) & 0xff;
		else if (bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_CInt16)
			return buffer.getShort(index);
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_CInt32)
			return buffer.getInt(index);
		else if (bufferType == gdalconstConstants.GDT_UInt16)
			return getUInt16(index, buffer);
		else if (bufferType == gdalconstConstants.GDT_UInt32)
			return getUInt32(index, buffer);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	public static double getDoubleValue(ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Float32
				|| bufferType == gdalconstConstants.GDT_CFloat32)
			return buffer.getFloat();
		else if (bufferType == gdalconstConstants.GDT_Float64
				|| bufferType == gdalconstConstants.GDT_CFloat64)
			return buffer.getDouble();
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static double getDoubleValue(int index, ByteBuffer buffer,
			int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Float32
				|| bufferType == gdalconstConstants.GDT_CFloat32)
			return buffer.getFloat(index);
		else if (bufferType == gdalconstConstants.GDT_Float64
				|| bufferType == gdalconstConstants.GDT_CFloat64)
			return buffer.getDouble(index);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putLongValue(ByteBuffer buffer, int bufferType,
			long value)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			buffer.put((byte) value);
		else if (bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_CInt16)
			buffer.putShort((short) value);
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_CInt32)
			buffer.putInt((int) value);
		else if (bufferType == gdalconstConstants.GDT_UInt16)
			putUInt16(buffer, value);
		else if (bufferType == gdalconstConstants.GDT_UInt32)
			putUInt32(buffer, value);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putLongValue(int index, ByteBuffer buffer,
			int bufferType, long value)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			buffer.put(index, (byte) value);
		else if (bufferType == gdalconstConstants.GDT_Int16
				|| bufferType == gdalconstConstants.GDT_CInt16)
			buffer.putShort(index, (short) value);
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_CInt32)
			buffer.putInt(index, (int) value);
		else if (bufferType == gdalconstConstants.GDT_UInt16)
			putUInt16(index, buffer, value);
		else if (bufferType == gdalconstConstants.GDT_UInt32)
			putUInt32(index, buffer, value);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putDoubleValue(ByteBuffer buffer, int bufferType,
			double value)
	{
		if (bufferType == gdalconstConstants.GDT_Float32
				|| bufferType == gdalconstConstants.GDT_CFloat32)
			buffer.putFloat((float) value);
		else if (bufferType == gdalconstConstants.GDT_Float64
				|| bufferType == gdalconstConstants.GDT_CFloat64)
			buffer.putDouble(value);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putDoubleValue(int index, ByteBuffer buffer,
			int bufferType, double value)
	{
		if (bufferType == gdalconstConstants.GDT_Float32
				|| bufferType == gdalconstConstants.GDT_CFloat32)
			buffer.putFloat(index, (float) value);
		else if (bufferType == gdalconstConstants.GDT_Float64
				|| bufferType == gdalconstConstants.GDT_CFloat64)
			buffer.putDouble(index, value);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static int getUInt16(ByteBuffer buffer)
	{
		int first = 0xff & (int) buffer.get();
		int second = 0xff & (int) buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
			return (first << 8 | second);
		else
			return (first | second << 8);
	}

	private static int getUInt16(int index, ByteBuffer buffer)
	{
		int first = 0xff & (int) buffer.get(index);
		int second = 0xff & (int) buffer.get(index + 1);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
			return (first << 8 | second);
		else
			return (first | second << 8);
	}

	private static long getUInt32(ByteBuffer buffer)
	{
		long first = 0xff & (int) buffer.get();
		long second = 0xff & (int) buffer.get();
		long third = 0xff & (int) buffer.get();
		long fourth = 0xff & (int) buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
			return (first << 24l | second << 16l | third << 8l | fourth);
		else
			return (first | second << 8l | third << 16l | fourth << 24l);
	}

	private static long getUInt32(int index, ByteBuffer buffer)
	{
		long first = 0xff & (int) buffer.get(index);
		long second = 0xff & (int) buffer.get(index + 1);
		long third = 0xff & (int) buffer.get(index + 2);
		long fourth = 0xff & (int) buffer.get(index + 3);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
			return (first << 24l | second << 16l | third << 8l | fourth);
		else
			return (first | second << 8l | third << 16l | fourth << 24l);
	}

	private static void putUInt16(ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 8) & 0xff);
		byte second = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(first);
			buffer.put(second);
		}
		else
		{
			buffer.put(second);
			buffer.put(first);
		}
	}

	private static void putUInt16(int index, ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 8) & 0xff);
		byte second = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(index, first);
			buffer.put(index + 1, second);
		}
		else
		{
			buffer.put(index, second);
			buffer.put(index + 1, first);
		}
	}

	private static void putUInt32(ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 24) & 0xff);
		byte second = (byte) ((value >> 16) & 0xff);
		byte third = (byte) ((value >> 8) & 0xff);
		byte fourth = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(first);
			buffer.put(second);
			buffer.put(third);
			buffer.put(fourth);
		}
		else
		{
			buffer.put(fourth);
			buffer.put(third);
			buffer.put(second);
			buffer.put(first);
		}
	}

	private static void putUInt32(int index, ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 24) & 0xff);
		byte second = (byte) ((value >> 16) & 0xff);
		byte third = (byte) ((value >> 8) & 0xff);
		byte fourth = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(index, first);
			buffer.put(index + 1, second);
			buffer.put(index + 2, third);
			buffer.put(index + 3, fourth);
		}
		else
		{
			buffer.put(index, fourth);
			buffer.put(index + 1, third);
			buffer.put(index + 2, second);
			buffer.put(index + 3, first);
		}
	}
}
