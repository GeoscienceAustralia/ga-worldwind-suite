package au.gov.ga.worldwind.tiler.gdal;

import java.awt.Dimension;
import java.awt.Point;
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
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;

import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.NumberArray;
import au.gov.ga.worldwind.tiler.util.TilerException;

public class GDALTile
{
	// TODO test with various datatypes:
	// So far, only really tested with 3 and 4 band image datasets and 1 band DEMs

	private final GDALTileParameters parameters;

	// calculated globals
	private ByteBuffer buffer;
	private int bufferType;
	private int bufferTypeSize;
	private boolean floatingPoint;
	private boolean isBlank;

	private Rectangle dataRectangle;
	private int bufferBandCount;
	private boolean indexed;
	private IndexColorModel indexColorModel;

	//ByteBuffer cache:
	private static NavigableMap<Integer, ByteBuffer> availableBuffers = new TreeMap<Integer, ByteBuffer>();

	public GDALTile(GDALTileParameters parameters) throws GDALException, TilerException
	{
		if (parameters.sector != null
				&& (parameters.sector.getMinLatitude() >= parameters.sector.getMaxLatitude() || parameters.sector
						.getMinLongitude() >= parameters.sector.getMaxLongitude()))
			throw new IllegalArgumentException();

		this.parameters = parameters;
		readDataset();
	}

	protected GDALTile(GDALTile tile, ByteBuffer buffer, int bufferType, int bufferTypeSize, boolean floatingPoint)
	{
		this.buffer = buffer;
		this.bufferType = bufferType;
		this.bufferTypeSize = bufferTypeSize;
		this.floatingPoint = floatingPoint;

		this.parameters = tile.parameters;

		this.dataRectangle = tile.dataRectangle;
		this.bufferBandCount = tile.bufferBandCount;
		this.indexed = tile.indexed;
		this.indexColorModel = tile.indexColorModel;
	}

	protected void readDataset() throws GDALException, TilerException
	{
		if (parameters.reprojectIfRequired)
		{
			SpatialReference dstSR = new SpatialReference();
			dstSR.ImportFromEPSG(4326); // WGS84
			/*SpatialReference dstSR2 = new SpatialReference();
			dstSR2.ImportFromEPSG(4283); // GDA94
			SpatialReference dstSR3 =
					new SpatialReference(
							"GEOGCS[\"GEOCENTRIC DATUM of AUSTRALIA\",DATUM[\"GDA94\",SPHEROID[\"GRS80\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433]]");*/

			String projection = parameters.dataset.GetProjection();
			SpatialReference srcSR =
					(projection == null || projection.length() == 0) ? null : new SpatialReference(projection);

			try
			{
				if (srcSR != null && srcSR.IsSame(dstSR) != 1)
				{
					readDatasetReprojected(dstSR);
					// throw new TilerException("Projection not supported: " +
					// srcSR.ExportToPrettyWkt(1));
				}
				else
				{
					readDatasetNormal();
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
			readDatasetNormal();
		}
	}

	protected void readDatasetNormal() throws GDALException, TilerException
	{
		double[] geoTransformArray = new double[6];
		parameters.dataset.GetGeoTransform(geoTransformArray);

		if (parameters.sourceRectangle != null)
		{
			readRectangle(parameters.dataset, parameters.sourceRectangle);
			return;
		}

		double small = 0.001;
		int srcX = (int) ((parameters.sector.getMinLongitude() - geoTransformArray[0]) / geoTransformArray[1] + small);
		int srcY = (int) ((parameters.sector.getMaxLatitude() - geoTransformArray[3]) / geoTransformArray[5] + small);
		int srcWidth = (int) (parameters.sector.getDeltaLongitude() / geoTransformArray[1] + 0.5);
		int srcHeight = (int) (-parameters.sector.getDeltaLatitude() / geoTransformArray[5] + 0.5);

		Rectangle srcRect = new Rectangle(srcX, srcY, srcWidth, srcHeight);
		readRectangle(parameters.dataset, srcRect);
	}

	protected void readDatasetReprojected(SpatialReference dstSR) throws GDALException, TilerException
	{
		int width = parameters.size.width;
		int height = parameters.size.height;

		Driver memDriver = gdal.GetDriverByName("MEM");
		//create a dataset with 1 band with the same data type as band 1 of the source
		Dataset dst = memDriver.Create("mem", width, height, 1, parameters.dataset.GetRasterBand(1).getDataType());
		//add the other bands with the same data type as the source bands
		for (int i = 1; i < parameters.dataset.getRasterCount(); i++)
		{
			dst.AddBand(parameters.dataset.GetRasterBand(i + 1).getDataType());
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
		geoTransformArray[0] = parameters.sector.getMinLongitude();
		geoTransformArray[3] = parameters.sector.getMaxLatitude();
		geoTransformArray[1] = parameters.sector.getDeltaLongitude() / (double) width;
		geoTransformArray[5] = -parameters.sector.getDeltaLatitude() / (double) height;
		dst.SetGeoTransform(geoTransformArray);
		dst.SetProjection(dstSR.ExportToWkt());

		int returnVal =
				gdal.ReprojectImage(parameters.dataset, dst, null, null, parameters.bilinearInterpolationIfRequired
						? gdalconst.GRA_Bilinear : gdalconst.GRA_NearestNeighbour);
		if (returnVal != gdalconstConstants.CE_None)
		{
			throw new GDALException();
		}
		readRectangle(dst, null);
	}

	protected void readRectangle(Dataset dataset, Rectangle srcRect) throws GDALException, TilerException
	{
		int width = parameters.size.width;
		int height = parameters.size.height;

		// get and check raster band count
		int dataBandCount = dataset.getRasterCount();
		if (dataBandCount <= 0)
		{
			throw new TilerException("No raster bands found in dataset");
		}

		// get the rasters and the raster data type
		bufferType = 0;
		Band[] bands = null;
		if (parameters.selectedBand >= 0)
		{
			// check the selected band is valid
			if (parameters.selectedBand >= dataBandCount)
				throw new IllegalArgumentException("Selected band does not exist");

			dataBandCount = 1;
			bands = new Band[dataBandCount];
			bands[0] = dataset.GetRasterBand(parameters.selectedBand + 1);
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
					throw new TilerException("Raster bands are of different types");
				}
			}
		}

		bufferBandCount = (parameters.addAlpha && dataBandCount == 3) ? 4 : dataBandCount;

		//data type
		int dataTypeSize = gdal.GetDataTypeSize(bufferType); // in bits
		bufferTypeSize = dataTypeSize / 8; // in bytes
		floatingPoint = isTypeFloatingPoint(bufferType);

		ByteBuffer directBuffer = null;
		try
		{
			// image parameters and data
			int bandSize = width * height * bufferTypeSize;
			//buffer = ByteBuffer.allocateDirect(bandSize * bufferBandCount);
			directBuffer = takeByteBuffer(bandSize * bufferBandCount);
			directBuffer.order(ByteOrder.LITTLE_ENDIAN); // TODO check if always the case?

			// calculate src and dst image rectangles
			dataRectangle = new Rectangle(0, 0, width, height);
			if (srcRect == null)
			{
				srcRect = new Rectangle(0, 0, dataset.getRasterXSize(), dataset.getRasterYSize());
			}
			else if (srcRect.x < 0 || srcRect.y < 0 || srcRect.x + srcRect.width > dataset.getRasterXSize()
					|| srcRect.y + srcRect.height > dataset.getRasterYSize())
			{
				//source rect is outside dataset extents, so must recalculate a subrectangle

				Rectangle newSrcRect = new Rectangle(0, 0, dataset.getRasterXSize(), dataset.getRasterYSize());
				newSrcRect = srcRect.intersection(newSrcRect);

				if (!newSrcRect.isEmpty())
				{
					// calculate dst rect for the new src rect
					int dstX = width * (newSrcRect.x - srcRect.x) / srcRect.width;
					int dstY = height * (newSrcRect.y - srcRect.y) / srcRect.height;
					int dstWidth = width - width * (srcRect.width - newSrcRect.width) / srcRect.width;
					int dstHeight = height - height * (srcRect.height - newSrcRect.height) / srcRect.height;
					dataRectangle = new Rectangle(dstX, dstY, dstWidth, dstHeight);
				}
				else
				{
					throw new TilerException("Source window is empty");
				}
				srcRect = newSrcRect;
			}

			//check if alpha needs to be filled
			boolean fillAlpha = bufferBandCount != dataBandCount && bufferBandCount == 4;

			// read image data
			if (!srcRect.isEmpty())
			{
				if (parameters.bilinearInterpolationIfRequired && srcRect.width < dataRectangle.width
						&& srcRect.height < dataRectangle.height)
				{
					//src rect is smaller: read into buffer, and then interpolate

					ByteBuffer small = null;
					try
					{
						int smallBandSize = srcRect.width * srcRect.height * bufferTypeSize;
						//ByteBuffer small = ByteBuffer.allocateDirect(smallBandSize * bufferBandCount);
						small = takeByteBuffer(smallBandSize * bufferBandCount);
						small.order(ByteOrder.LITTLE_ENDIAN);

						//fill the alpha channel if required
						Dimension srcSize = srcRect.getSize();
						Rectangle zeroSrcRect = new Rectangle(new Point(0, 0), srcSize);
						if (fillAlpha)
						{
							fillAlphaChannel(small, srcSize, zeroSrcRect, bufferTypeSize);
							fillAlpha = false;
						}

						//read the data
						for (int b = 0; b < dataBandCount; b++)
						{
							small.position(b * smallBandSize);
							ByteBuffer sliced = small.slice();

							int returnVal =
									bands[b].ReadRaster_Direct(srcRect.x, srcRect.y, srcRect.width, srcRect.height,
											srcRect.width, srcRect.height, bufferType, sliced, bufferTypeSize,
											bufferTypeSize * srcRect.width);
							if (returnVal != gdalconstConstants.CE_None)
							{
								throw new GDALException();
							}
						}

						//replace any values
						replaceValues(small, srcSize, zeroSrcRect, bufferBandCount, bufferType, bufferTypeSize,
								floatingPoint, parameters.minMaxs, parameters.replacement, parameters.otherwise);

						//interpolate
						enlarge(small, srcRect, directBuffer, dataRectangle, parameters.size, bufferType,
								bufferTypeSize, bufferBandCount, floatingPoint, parameters.noData);
					}
					finally
					{
						returnByteBuffer(small);
					}
				}
				else
				{
					//interpolation is not required, as dataset tile is higher res than tile

					//read directly into buffer
					for (int b = 0; b < dataBandCount; b++)
					{
						// slice buffer at the correct write position
						directBuffer.position(b * bandSize + (dataRectangle.x + dataRectangle.y * width)
								* bufferTypeSize);
						ByteBuffer sliced = directBuffer.slice();

						// read band into buffer
						int returnVal =
								bands[b].ReadRaster_Direct(srcRect.x, srcRect.y, srcRect.width, srcRect.height,
										dataRectangle.width, dataRectangle.height, bufferType, sliced, bufferTypeSize,
										bufferTypeSize * width);
						if (returnVal != gdalconstConstants.CE_None)
						{
							throw new GDALException();
						}
					}

					if (fillAlpha)
					{
						fillAlphaChannel(directBuffer, parameters.size, dataRectangle, bufferTypeSize);
						fillAlpha = false;
					}

					replaceValues(directBuffer, parameters.size, dataRectangle, bufferBandCount, bufferType,
							bufferTypeSize, floatingPoint, parameters.minMaxs, parameters.replacement,
							parameters.otherwise);
				}
			}

			//just in case it wasn't done above (if srcRect is empty)
			if (fillAlpha)
			{
				fillAlphaChannel(directBuffer, parameters.size, dataRectangle, bufferTypeSize);
			}

			isBlank = isEqual(directBuffer, parameters.noData);

			//fill the pixels outside the dataset extents
			fillOutside(directBuffer, parameters.noData);

			// rewind the buffer
			directBuffer.rewind();

			//copy the direct buffer into the standard buffer array
			this.buffer = ByteBuffer.allocate(directBuffer.limit());
			directBuffer.get(this.buffer.array());
		}
		finally
		{
			returnByteBuffer(directBuffer);
		}

		// set variables for indexed image
		Band lastBand = bands[bands.length - 1];
		indexed = lastBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex;
		indexColorModel = indexed ? lastBand.GetRasterColorTable().getIndexColorModel(dataTypeSize) : null;
	}

	private void fillAlphaChannel(ByteBuffer buffer, Dimension bufferSize, Rectangle fillRegion, int bufferTypeSize)
	{
		for (int y = 0; y < bufferSize.height; y++)
		{
			for (int x = 0; x < bufferSize.width; x++)
			{
				int index = getBufferIndex(x, y, 3, bufferSize.width, bufferSize.height);
				for (int i = 0; i < bufferTypeSize; i++)
				{
					if (fillRegion.contains(x, y))
						buffer.put(index + i, (byte) 0xff);
					else
						buffer.put(index + i, (byte) 0x00);
				}
			}
		}
	}

	private static void enlarge(ByteBuffer src, Rectangle srcRect, ByteBuffer dst, Rectangle dstRect,
			Dimension dstSize, int bufferType, int bufferTypeSize, int bands, boolean isFloat,
			NullableNumberArray noData)
	{
		if (noData != null && noData.length() != bands)
			throw new IllegalArgumentException("Array size does not equal band count");

		int srcStride = srcRect.width * bufferTypeSize;
		int dstStride = dstSize.width * bufferTypeSize;
		int srcBandSize = srcStride * srcRect.height;
		int dstBandSize = dstStride * dstSize.height;

		double[] doubleValues = new double[bands * 4];
		long[] longValues = new long[bands * 4];
		int[] indices = new int[bands * 4];
		boolean[] isNoData = new boolean[4];

		for (int y = 0; y < dstRect.height; y++)
		{
			double mixY = srcRect.getHeight() * y / dstRect.getHeight();
			int srcY = (int) Math.floor(mixY);
			int nextyOffset = srcY < srcRect.height - 1 ? srcStride : 0;
			mixY -= srcY;

			for (int x = 0; x < dstRect.width; x++)
			{
				double mixX = srcRect.getWidth() * x / dstRect.getWidth();
				int srcX = (int) Math.floor(mixX);
				int nextxOffset = srcX < srcRect.width - 1 ? bufferTypeSize : 0;
				mixX -= srcX;

				double mxX = mixX;
				double mxY = mixY;

				indices[0] = srcY * srcStride + srcX * bufferTypeSize;
				indices[bands * 1] = indices[0] + nextxOffset;
				indices[bands * 2] = indices[0] + nextyOffset;
				indices[bands * 3] = indices[bands * 2] + nextxOffset;

				for (int b = 1; b < bands; b++)
				{
					indices[b] = indices[0] + b * srcBandSize;
					indices[1 * bands + b] = indices[1 * bands] + b * srcBandSize;
					indices[2 * bands + b] = indices[2 * bands] + b * srcBandSize;
					indices[3 * bands + b] = indices[3 * bands] + b * srcBandSize;
				}

				//get all the values to interpolate from
				for (int i = 0; i < bands * 4; i++)
				{
					if (isFloat)
						doubleValues[i] = getDoubleValue(indices[i], src, bufferType);
					else
						longValues[i] = getLongValue(indices[i], src, bufferType);
				}

				//check if any of the pixels match the no data values
				if (noData != null)
				{
					for (int i = 0; i < 4; i++)
					{
						if (isFloat)
							isNoData[i] = noData.equalsDoubles(doubleValues, bands * i);
						else
							isNoData[i] = noData.equalsLongs(longValues, bands * i);
					}

					//If any pixels were no data values, then round the mixers. This will ensure that
					//the pixel is either full no data, or not interpolated with no data at all.
					if (isNoData[0] || isNoData[1] || isNoData[2] || isNoData[3])
					{
						mxX = Math.round(mixX);
						mxY = Math.round(mixY);

						//if rounding the Y mixer causes no no data pixels to be involved in the
						//interpolation, then reset the X mixer
						if ((mxY == 1d && !(isNoData[2] || isNoData[3]))
								|| (mxY == 0d && !(isNoData[0] || isNoData[1])))
							mxX = mixX;
						//do the same for the Y mixer
						if ((mxX == 1d && !(isNoData[1] || isNoData[3]))
								|| (mxX == 0d && !(isNoData[0] || isNoData[2])))
							mxY = mixY;
					}
				}

				//put the mixed values into the buffer
				for (int b = 0; b < bands; b++)
				{
					int index = b * dstBandSize + ((dstRect.x + x) + (dstRect.y + y) * dstSize.width) * bufferTypeSize;
					if (isFloat)
					{
						double mixed =
								doubleValues[b] * (1.0 - mxX) * (1.0 - mxY) + doubleValues[1 * bands + b] * mxX
										* (1.0 - mxY) + doubleValues[2 * bands + b] * (1.0 - mxX) * mxY
										+ doubleValues[3 * bands + b] * mxX * mxY;
						putDoubleValue(index, dst, bufferType, mixed);
					}
					else
					{
						long mixed =
								(long) (longValues[b] * (1.0 - mxX) * (1.0 - mxY) + longValues[1 * bands + b] * mxX
										* (1.0 - mxY) + longValues[2 * bands + b] * (1.0 - mxX) * mxY + longValues[3
										* bands + b]
										* mxX * mxY);
						putLongValue(index, dst, bufferType, mixed);
					}
				}
			}
		}

		/*for (int b = 0; b < bands; b++)
		{
			src.position(srcBandSize * b);
			dst
					.position(b * dstBandSize + (dstRect.x + dstRect.y * dstSize.width)
							* bufferTypeSize);
			ByteBuffer ss = src.slice();
			ByteBuffer ds = dst.slice();
			ss.order(src.order());
			ds.order(dst.order());

			for (int y = 0; y < dstRect.height; y++)
			{
				double mixY = srcRect.getHeight() * y / dstRect.getHeight();
				int srcY = (int) Math.floor(mixY);
				mixY -= srcY;

				ds.position(y * dstStride);

				for (int x = 0; x < dstRect.width; x++)
				{
					double mixX = srcRect.getWidth() * x / dstRect.getWidth();
					int srcX = (int) Math.floor(mixX);
					mixX -= srcX;

					int i0 = srcY * srcStride + srcX * bufferTypeSize;
					int i1 = srcX < srcRect.width - 1 ? i0 + bufferTypeSize : i0;
					int i2 = srcY < srcRect.height - 1 ? i0 + srcStride : i0;
					int i3 = srcX < srcRect.width - 1 ? i2 + bufferTypeSize : i2;

					if (isFloat)
					{
						double s0 = getDoubleValue(i0, ss, bufferType);
						double s1 = getDoubleValue(i1, ss, bufferType);
						double s2 = getDoubleValue(i2, ss, bufferType);
						double s3 = getDoubleValue(i3, ss, bufferType);
						double mixed =
								s0 * (1.0 - mixX) * (1.0 - mixY) + s1 * mixX * (1.0 - mixY) + s2
										* (1.0 - mixX) * mixY + s3 * mixX * mixY;
						putDoubleValue(ds, bufferType, mixed);
					}
					else
					{
						long s0 = getLongValue(i0, ss, bufferType);
						long s1 = getLongValue(i1, ss, bufferType);
						long s2 = getLongValue(i2, ss, bufferType);
						long s3 = getLongValue(i3, ss, bufferType);
						long mixed =
								(long) (s0 * (1.0 - mixX) * (1.0 - mixY) + s1 * mixX * (1.0 - mixY)
										+ s2 * (1.0 - mixX) * mixY + s3 * mixX * mixY);
						putLongValue(ds, bufferType, mixed);
					}
				}
			}
		}*/
	}

	public static boolean isTypeFloatingPoint(int bufferType)
	{
		return bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32
				|| bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64;
	}

	public BufferedImage getAsImage() throws TilerException
	{
		int width = parameters.size.width;
		int height = parameters.size.height;
		int pixels = width * height;
		int bandCount = bufferBandCount;

		// image data
		DataBuffer imgBuffer = null;
		int imageType = 0, dataType = 0;

		// create image data for buffer type
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			byte[] bytes = new byte[pixels * bandCount];
			buffer.get(bytes);
			imgBuffer = new DataBufferByte(bytes, bytes.length);
			dataType = DataBuffer.TYPE_BYTE;
			imageType = indexed ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
		}
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_UInt16
				|| bufferType == gdalconstConstants.GDT_CInt16)
		{
			short[] shorts = new short[pixels * bandCount];
			buffer.asShortBuffer().get(shorts);
			imgBuffer = new DataBufferShort(shorts, shorts.length);
			dataType = bufferType == gdalconstConstants.GDT_UInt16 ? DataBuffer.TYPE_USHORT : DataBuffer.TYPE_SHORT;
			imageType = BufferedImage.TYPE_USHORT_GRAY;
		}
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_UInt32
				|| bufferType == gdalconstConstants.GDT_CInt32)
		{
			int[] ints = new int[pixels * bandCount];
			buffer.asIntBuffer().get(ints);
			imgBuffer = new DataBufferInt(ints, ints.length);
			dataType = DataBuffer.TYPE_INT;
			imageType = BufferedImage.TYPE_CUSTOM;
		}
		//TEMP
		else if (bufferType == gdalconstConstants.GDT_Float32 && bandCount == 1)
		{
			GDALTile byteTile = this.convertToType(gdalconstConstants.GDT_Byte);
			bandCount = 4;
			byte[] bytes = new byte[pixels * bandCount];
			ByteBuffer buf = byteTile.getBuffer();
			for (int i = 0; i < bandCount; i++)
			{
				buf.rewind();
				buf.get(bytes, pixels * i, pixels);
			}
			imgBuffer = new DataBufferByte(bytes, bytes.length);
			dataType = DataBuffer.TYPE_BYTE;
		}
		//TEMP
		else
		{
			throw new TilerException("Unsupported image data type");
		}

		//create offsets array
		int[] offsets = new int[bandCount];
		for (int b = 0; b < bandCount; b++)
		{
			offsets[b] = b * pixels;
		}

		// create sample model and raster
		SampleModel sampleModel = new ComponentSampleModel(dataType, width, height, 1, width, offsets);
		WritableRaster raster = Raster.createWritableRaster(sampleModel, imgBuffer, null);
		BufferedImage img = null;

		// set image type
		if (bandCount == 4)
		{
			imageType = BufferedImage.TYPE_INT_ARGB_PRE;
		}
		else if (bandCount == 3)
		{
			imageType = BufferedImage.TYPE_INT_RGB;
		}
		else if (indexed && bandCount == 1)
		{
			// ok
		}
		else
		{
			// System.out.println("Warning: unknown image type");
		}

		// create and return image
		img =
				(indexColorModel == null) ? new BufferedImage(width, height, imageType) : new BufferedImage(width,
						height, imageType, indexColorModel);
		img.setData(raster);
		return img;
	}

	private boolean isEqual(ByteBuffer buffer, NullableNumberArray values)
	{
		if (values == null)
			return false;

		if (values.length() != bufferBandCount)
			throw new IllegalArgumentException("Array size does not equal band count");

		boolean allNull = true;
		for (int b = 0; b < bufferBandCount; b++)
		{
			if (floatingPoint)
			{
				if (values.getDouble(b) == null)
					continue;
			}
			else
			{
				if (values.getLong(b) == null)
					continue;
			}

			allNull = false;
			break;
		}

		if (allNull)
			return false;

		for (int b = 0; b < bufferBandCount; b++)
		{
			if (floatingPoint)
			{
				if (values.getDouble(b) == null)
					continue;
			}
			else
			{
				if (values.getLong(b) == null)
					continue;
			}

			for (int x = 0; x < dataRectangle.width; x++)
			{
				for (int y = 0; y < dataRectangle.height; y++)
				{
					int index = getBufferIndex(x, y, b, dataRectangle.width, dataRectangle.height) * bufferTypeSize;
					if (floatingPoint)
					{
						if (getDoubleValue(index, buffer, bufferType) != values.getDouble(b))
							return false;
					}
					else
					{
						if (getLongValue(index, buffer, bufferType) != values.getLong(b))
							return false;
					}
				}
			}
		}

		return true;
	}

	private void fillOutside(ByteBuffer buffer, NullableNumberArray values)
	{
		if (values == null)
			return;

		if (values.length() != bufferBandCount)
			throw new IllegalArgumentException("Array size does not equal band count");

		int width = parameters.size.width;
		int height = parameters.size.height;

		if (dataRectangle.x == 0 && dataRectangle.y == 0 && dataRectangle.width == width
				&& dataRectangle.height == height)
			return;

		for (int b = 0; b < bufferBandCount; b++)
		{
			if (floatingPoint)
			{
				if (values.getDouble(b) == null)
					continue;
			}
			else
			{
				if (values.getLong(b) == null)
					continue;
			}

			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					if (!dataRectangle.contains(x, y))
					{
						int index = getBufferIndex(x, y, b, width, height) * bufferTypeSize;
						if (floatingPoint)
							putDoubleValue(index, buffer, bufferType, values.getDouble(b));
						else
							putLongValue(index, buffer, bufferType, values.getLong(b));
					}
				}
			}
		}
	}

	private void replaceValues(ByteBuffer buffer, Dimension bufferSize, Rectangle replaceRegion, int bufferBandCount,
			int bufferType, int bufferTypeSize, boolean floatingPoint, MinMaxArray[] minMaxs,
			NullableNumberArray replacement, NullableNumberArray otherwise) throws TilerException
	{
		if (minMaxs == null || minMaxs.length == 0)
			return;
		if (replacement == null && otherwise == null)
			return;

		for (int i = 0; i < minMaxs.length; i++)
			if (minMaxs[i].length() != bufferBandCount)
				throw new IllegalArgumentException("Array size must equal band count");

		if ((replacement != null && replacement.length() != bufferBandCount)
				|| (otherwise != null && otherwise.length() != bufferBandCount))
			throw new IllegalArgumentException("Array size must equal band count");

		boolean allNull = true;
		for (int b = 0; b < bufferBandCount; b++)
		{
			if ((replacement != null && replacement.getDouble(b) != null)
					|| (otherwise != null && otherwise.getDouble(b) != null))
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
		int[] indices = new int[bufferBandCount];

		for (int y = replaceRegion.y; y < replaceRegion.y + replaceRegion.height; y++)
		{
			for (int x = replaceRegion.x; x < replaceRegion.x + replaceRegion.width; x++)
			{
				boolean between = false;

				for (int b = 0; b < bufferBandCount; b++)
				{
					indices[b] = getBufferIndex(x, y, b, bufferSize.width, bufferSize.height) * bufferTypeSize;
					if (floatingPoint)
						doubleValues[b] = getDoubleValue(indices[b], buffer, bufferType);
					else
						longValues[b] = getLongValue(indices[b], buffer, bufferType);
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
				if (values != null)
				{
					for (int b = 0; b < bufferBandCount; b++)
					{
						if (values.getDouble(b) != null)
						{
							if (floatingPoint)
								putDoubleValue(indices[b], buffer, bufferType, values.getDouble(b));
							else
								putLongValue(indices[b], buffer, bufferType, values.getLong(b));
						}
					}
				}
			}
		}
	}

	public void updateMinMax(NumberArray minmax, NullableNumberArray outsideValues)
	{
		if (minmax == null)
			return;

		if (outsideValues != null && outsideValues.length() < bufferBandCount)
			outsideValues = null; // just in case

		int width = parameters.size.width;
		int height = parameters.size.height;

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				for (int b = 0; b < bufferBandCount; b++)
				{
					int index = getBufferIndex(x, y, b, width, height) * bufferTypeSize;
					if (floatingPoint)
					{
						double value = getDoubleValue(index, buffer, bufferType);
						if (outsideValues != null && outsideValues.getDouble(b) != null
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
						if (outsideValues != null && outsideValues.getLong(b) != null
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

	protected static int getBufferIndex(int x, int y, int b, int width, int height)
	{
		return b * width * height + y * width + x;
	}

	public boolean isBlank()
	{
		return isBlank;
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
		//don't need a direct buffer, as buffer is not passed to GDAL
		ByteBuffer newBuffer = ByteBuffer.allocate(size * newBufferTypeSize);
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

		return new GDALTile(this, newBuffer, newBufferType, newBufferTypeSize, newFloatingPoint);
	}

	public static long getLongValue(ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			return buffer.get() & 0xff;
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
			return buffer.getShort();
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
			return buffer.getInt();
		else if (bufferType == gdalconstConstants.GDT_UInt16)
			return getUInt16(buffer);
		else if (bufferType == gdalconstConstants.GDT_UInt32)
			return getUInt32(buffer);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static long getLongValue(int index, ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			return buffer.get(index) & 0xff;
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
			return buffer.getShort(index);
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
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
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
			return buffer.getFloat();
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
			return buffer.getDouble();
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static double getDoubleValue(int index, ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
			return buffer.getFloat(index);
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
			return buffer.getDouble(index);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putLongValue(ByteBuffer buffer, int bufferType, long value)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			buffer.put((byte) value);
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
			buffer.putShort((short) value);
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
			buffer.putInt((int) value);
		else if (bufferType == gdalconstConstants.GDT_UInt16)
			putUInt16(buffer, value);
		else if (bufferType == gdalconstConstants.GDT_UInt32)
			putUInt32(buffer, value);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putLongValue(int index, ByteBuffer buffer, int bufferType, long value)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
			buffer.put(index, (byte) value);
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
			buffer.putShort(index, (short) value);
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
			buffer.putInt(index, (int) value);
		else if (bufferType == gdalconstConstants.GDT_UInt16)
			putUInt16(index, buffer, value);
		else if (bufferType == gdalconstConstants.GDT_UInt32)
			putUInt32(index, buffer, value);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putDoubleValue(ByteBuffer buffer, int bufferType, double value)
	{
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
			buffer.putFloat((float) value);
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
			buffer.putDouble(value);
		else
			throw new IllegalStateException("Unknown buffer type");
	}

	private static void putDoubleValue(int index, ByteBuffer buffer, int bufferType, double value)
	{
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
			buffer.putFloat(index, (float) value);
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
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

	private synchronized static ByteBuffer takeByteBuffer(int size)
	{
		Entry<Integer, ByteBuffer> entry = availableBuffers.higherEntry(size);
		if (entry != null)
		{
			ByteBuffer buffer = entry.getValue();
			availableBuffers.remove(entry.getKey());
			buffer.rewind();
			buffer.limit(size);
			return buffer;
		}
		return ByteBuffer.allocateDirect(size);
	}

	private synchronized static void returnByteBuffer(ByteBuffer buffer)
	{
		if (buffer != null)
		{
			availableBuffers.put(buffer.capacity(), buffer);
		}
	}
}
