package util;

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

public class GDALTile
{
	//construtor globals
	private final double minLatitude;
	private final double minLongitude;
	private final double maxLatitude;
	private final double maxLongitude;
	private final int width;
	private final int height;
	private final boolean addAlpha;
	private final int selectedBand;

	//calculated globals
	private ByteBuffer buffer;
	private int bufferType;
	private int bufferTypeSize;

	private Rectangle dataRectangle;
	private int bufferBandCount;
	private boolean indexed;
	private IndexColorModel indexColorModel;

	public GDALTile(Dataset dataset, int width, int height, double minLatitude,
			double minLongitude, double maxLatitude, double maxLongitude)
			throws GDALException, TilerException
	{
		this(dataset, width, height, minLatitude, minLongitude, maxLatitude,
				maxLongitude, false, -1);
	}

	public GDALTile(Dataset dataset, int width, int height, double minLatitude,
			double minLongitude, double maxLatitude, double maxLongitude,
			boolean addAlpha, int selectedBand) throws GDALException,
			TilerException
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
		this.selectedBand = selectedBand;
		readDataset(dataset);
	}

	protected GDALTile(GDALTile tile, ByteBuffer buffer, int bufferType,
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
		this.selectedBand = tile.selectedBand;

		this.dataRectangle = tile.dataRectangle;
		this.bufferBandCount = tile.bufferBandCount;
		this.indexed = tile.indexed;
		this.indexColorModel = tile.indexColorModel;
	}

	protected void readDataset(Dataset dataset) throws GDALException,
			TilerException
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
				//throw new TilerException("Projection not supported: " + srcSR.ExportToPrettyWkt(1));
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

	protected void readDatasetNormal(Dataset dataset) throws GDALException,
			TilerException
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
			SpatialReference dstSR) throws GDALException, TilerException
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

		int returnVal = gdal.ReprojectImage(dataset, dst, null, null,
				gdalconst.GRA_NearestNeighbour);
		if (returnVal != gdalconstConstants.CE_None)
		{
			throw new GDALException();
		}
		readRectangle(dataset, null);
	}

	protected void readRectangle(Dataset dataset, Rectangle srcRect)
			throws GDALException, TilerException
	{
		//get and check raster band count
		int dataBandCount = dataset.getRasterCount();
		if (dataBandCount <= 0)
		{
			throw new TilerException("No raster bands found in dataset");
		}

		//check the selected band is valid
		if (selectedBand >= 0 && selectedBand >= dataBandCount)
		{
			throw new IllegalArgumentException("Selected band does not exist");
		}

		bufferBandCount = selectedBand >= 0 ? 1
				: (addAlpha && dataBandCount == 3) ? 4 : dataBandCount;

		//get the rasters and the raster data type		
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
				//bands are 1-base indexed
				bands[i] = dataset.GetRasterBand(i + 1);
				//check raster data type
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

		int dataTypeSize = gdal.GetDataTypeSize(bufferType); //in bits
		bufferTypeSize = dataTypeSize / 8; //in bytes

		//image parameters and data
		int pixels = width * height;
		int bandSize = pixels * bufferTypeSize;
		buffer = ByteBuffer.allocateDirect(bandSize * bufferBandCount);
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
				throw new TilerException("Source window is empty");
			}
			srcRect = newSrcRect;
		}

		//read image data
		for (int b = 0; b < bands.length; b++)
		{
			if (!srcRect.isEmpty())
			{
				//slice buffer at the correct write position
				buffer.position(b * bandSize + dataRectangle.x
						+ dataRectangle.y * width);
				ByteBuffer sliced = buffer.slice();

				//read band into buffer
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

		//if an alpha band has been added, fill alpha channel with 255
		//inside dataRectangle, 0 otherwise
		if (bufferBandCount == 4 && dataBandCount != 4)
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
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

		//rewind the buffer
		buffer.rewind();

		//set variables for indexed image
		Band lastBand = bands[bands.length - 1];
		boolean indexed = lastBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex;
		indexColorModel = indexed ? lastBand.GetRasterColorTable()
				.getIndexColorModel(dataTypeSize) : null;
	}

	public BufferedImage getAsImage() throws TilerException
	{
		int pixels = width * height;
		int[] offsets = new int[bufferBandCount];
		for (int b = 0; b < bufferBandCount; b++)
		{
			offsets[b] = b * pixels;
		}

		//image data
		DataBuffer imgBuffer = null;
		int imageType = 0, dataType = 0;

		//create image data for buffer type
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
				|| bufferType == gdalconstConstants.GDT_UInt16)
		{
			short[] shorts = new short[pixels * bufferBandCount];
			buffer.asShortBuffer().get(shorts);
			imgBuffer = new DataBufferShort(shorts, shorts.length);
			dataType = DataBuffer.TYPE_USHORT;
			imageType = BufferedImage.TYPE_USHORT_GRAY;
		}
		else if (bufferType == gdalconstConstants.GDT_Int32
				|| bufferType == gdalconstConstants.GDT_UInt32)
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

		//create sample model and raster
		SampleModel sampleModel = new ComponentSampleModel(dataType, width,
				height, 1, width, offsets);
		WritableRaster raster = Raster.createWritableRaster(sampleModel,
				imgBuffer, null);
		BufferedImage img = null;

		//set image type
		if (bufferBandCount == 4)
		{
			imageType = BufferedImage.TYPE_INT_ARGB;
		}
		else if (bufferBandCount == 3)
		{
			imageType = BufferedImage.TYPE_INT_RGB;
		}
		else if (indexed && bufferBandCount == 1)
		{
			//ok
		}
		else
		{
			//System.out.println("Warning: unknown image type");
		}

		//create and return image
		img = (indexColorModel == null) ? new BufferedImage(width, height,
				imageType) : new BufferedImage(width, height, imageType,
				indexColorModel);
		img.setData(raster);
		return img;
	}

	public void fillOutside(int[] values) throws TilerException
	{
		if (values.length != bufferBandCount)
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
						if (bufferType == gdalconstConstants.GDT_Byte)
						{
							buffer.put(index, (byte) values[b]);
						}
						else if (bufferType == gdalconstConstants.GDT_Int16
								|| bufferType == gdalconstConstants.GDT_UInt16)
						{
							buffer.putShort(index, (short) values[b]);
						}
						else if (bufferType == gdalconstConstants.GDT_Int32
								|| bufferType == gdalconstConstants.GDT_UInt32)
						{
							buffer.putInt(index, values[b]);
						}
						else
						{
							throw new TilerException("Unsupported buffer type");
						}
					}
				}
			}
		}
	}

	public void replaceValues(int[] min, int[] max, Integer[] replacement)
			throws TilerException
	{
		if (min.length != bufferBandCount || max.length != bufferBandCount
				|| replacement.length != bufferBandCount)
		{
			throw new IllegalArgumentException(
					"Array size does not equal band count");
		}

		boolean allNull = true;
		for (int b = 0; b < bufferBandCount; b++)
		{
			if (replacement[b] != null)
			{
				allNull = false;
				break;
			}
		}
		if (allNull)
		{
			//replacement values are all null, so nothing will happen
			return;
		}

		int[] values = new int[bufferBandCount];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				boolean between = true;

				for (int b = 0; b < bufferBandCount; b++)
				{
					int index = getBufferIndex(x, y, b) * bufferTypeSize;
					if (bufferType == gdalconstConstants.GDT_Byte)
					{
						values[b] = buffer.get(index);
					}
					else if (bufferType == gdalconstConstants.GDT_Int16
							|| bufferType == gdalconstConstants.GDT_UInt16)
					{
						values[b] = buffer.getShort(index);
					}
					else if (bufferType == gdalconstConstants.GDT_Int32
							|| bufferType == gdalconstConstants.GDT_UInt32)
					{
						values[b] = buffer.getInt(index);
					}
					else
					{
						throw new TilerException("Unsupported buffer type");
					}

					if (values[b] < min[b] || values[b] > max[b])
					{
						between = false;
						break;
					}
				}

				if (between)
				{
					for (int b = 0; b < bufferBandCount; b++)
					{
						if (replacement[b] != null)
						{
							int index = getBufferIndex(x, y, b)
									* bufferTypeSize;
							int rep = replacement[b];
							if (bufferType == gdalconstConstants.GDT_Byte)
							{
								buffer.put(index, (byte) rep);
							}
							else if (bufferType == gdalconstConstants.GDT_Int16
									|| bufferType == gdalconstConstants.GDT_UInt16)
							{
								buffer.putShort(index, (short) rep);
							}
							else if (bufferType == gdalconstConstants.GDT_Int32
									|| bufferType == gdalconstConstants.GDT_UInt32)
							{
								buffer.putInt(index, rep);
							}
							else
							{
								throw new TilerException(
										"Unsupported buffer type");
							}
						}
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
		return bufferBandCount;
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
			throw new IllegalStateException("Source buffer type not supported");

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

	protected void copy(ByteBuffer src, ByteBuffer dst, int srcBufferType,
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
			throw new IllegalStateException("Unknown source buffer type");
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
